package interfaces.vente;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import entite.*;
import entitebd.*;
import gestion.*;
import exception.*;

/**
 * NouvelleVenteFrame — POS-style sales entry
 * Fixed: Medicament no longer has estPerime()/getDateExpiration() — use StockMedicament instead
 */
public class NouvelleVenteFrame extends PharmBaseFrame {

    private final GestionVente  gestionVente  = new GestionVente();
    private final GestionStock  gestionStock  = new GestionStock();
    private final ClientBD      clientBD      = new ClientBD();
    private final MedicamentBD  medicamentBD  = new MedicamentBD();
    private final StockBD       stockBD       = new StockBD();

    // Cart state
    private final ArrayList<VoieVente>  lignes   = new ArrayList<>();
    private final Map<Integer, Integer> reserves = new HashMap<>();
    private Client clientActuel = null;

    // Left widgets
    private JComboBox<MedicamentItem> cmbMedicament;
    private JSpinner                  spnQuantite;
    private JTextField                txtPrix;
    private JLabel                    lblFEFO;
    private JTable                    cartTable;
    private DefaultTableModel         cartModel;

    // Right widgets
    private JTextField txtCodeCnam;
    private JLabel     lblClientName, lblClientPoints;
    private JLabel     lblTotal, lblItemCount;
    private JTextField txtDateVente, txtDateLimite, txtNumEmp;

    public NouvelleVenteFrame() {
        super("Nouvelle vente", "FEFO · First Expired, First Out", 1100, 680);
        buildUI();
        loadMedicaments();
    }

    @Override
    protected void populateHeaderActions(JPanel actionsPanel) {
        JButton cancel = PharmTheme.ghostButton("Annuler"); cancel.addActionListener(e -> dispose());
        actionsPanel.add(cancel);
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    private void buildUI() {
        contentArea.setLayout(new GridLayout(1, 2, 16, 0));
        contentArea.add(buildLeftPanel());
        contentArea.add(buildRightPanel());
    }

    // ── Left: product selector + cart ──────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12)); p.setBackground(PharmTheme.BG);

        // Selector card
        JPanel selector = PharmTheme.card();
        selector.setLayout(new GridBagLayout());
        selector.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(16, 16, 14, 16)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(4, 0, 4, 8);

        gc.gridx=0; gc.gridy=0; gc.gridwidth=3; gc.weightx=1;
        selector.add(PharmTheme.requiredLabel("Médicament"), gc);

        gc.gridy=1; gc.insets=new Insets(0,0,4,6);
        cmbMedicament = new JComboBox<>();
        cmbMedicament.setFont(PharmTheme.FONT_BODY); cmbMedicament.setBackground(PharmTheme.CARD);
        cmbMedicament.setPreferredSize(new Dimension(0, 36));
        cmbMedicament.addActionListener(e -> onMedSelected());
        selector.add(cmbMedicament, gc);

        gc.gridy=2; gc.gridwidth=3; gc.insets=new Insets(0,0,4,8);
        lblFEFO = new JLabel(" "); lblFEFO.setFont(PharmTheme.FONT_LABEL); lblFEFO.setForeground(PharmTheme.INFO);
        selector.add(lblFEFO, gc);

        gc.gridy=3; gc.gridwidth=1; gc.weightx=0; gc.insets=new Insets(6,0,0,8);
        gc.gridx=0; selector.add(PharmTheme.formLabel("Quantité"), gc);
        gc.gridx=1; selector.add(PharmTheme.formLabel("Prix unitaire (DT)"), gc);
        gc.gridx=2; selector.add(new JLabel(""), gc);

        gc.gridy=4; gc.gridx=0;
        spnQuantite = PharmTheme.spinner(1, 1, 9999, 1);
        spnQuantite.addChangeListener(e -> onMedSelected());
        selector.add(spnQuantite, gc);

        gc.gridx=1;
        txtPrix = PharmTheme.textField("0.00"); txtPrix.setPreferredSize(new Dimension(110, 36));
        selector.add(txtPrix, gc);

        gc.gridx=2; gc.weightx=0;
        JButton addBtn = PharmTheme.accentButton("+ Ajouter"); addBtn.addActionListener(e -> addToCart());
        selector.add(addBtn, gc);

        p.add(selector, BorderLayout.NORTH);

        // Cart card
        JPanel cartCard = PharmTheme.card(); cartCard.setLayout(new BorderLayout());
        JPanel cartHead = new JPanel(new BorderLayout()); cartHead.setBackground(PharmTheme.CARD);
        cartHead.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,PharmTheme.BORDER),
            BorderFactory.createEmptyBorder(10,16,10,16)));
        JPanel cartTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); cartTitleRow.setBackground(PharmTheme.CARD);
        JLabel cartTitle = PharmTheme.titleLabel("Panier"); cartTitle.setFont(PharmTheme.FONT_H3);
        lblItemCount = PharmTheme.helperLabel("0 article");
        cartTitleRow.add(cartTitle); cartTitleRow.add(lblItemCount);
        cartHead.add(cartTitleRow, BorderLayout.WEST);
        JButton clearBtn = PharmTheme.ghostButton("Vider"); clearBtn.addActionListener(e -> clearCart());
        cartHead.add(clearBtn, BorderLayout.EAST);
        cartCard.add(cartHead, BorderLayout.NORTH);

        cartModel = new DefaultTableModel(
            new String[]{"Médicament","Qté","Prix U.","Total","Lots FEFO",""}, 0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        cartTable = new JTable(cartModel); PharmTheme.styleTable(cartTable);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(36);
        cartTable.getColumnModel().getColumn(5).setCellRenderer((t,v,sel,foc,row,col)->{
            JLabel l=new JLabel("✕"); l.setFont(new Font("SansSerif",Font.BOLD,11));
            l.setForeground(PharmTheme.DANGER); l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return l;
        });
        cartTable.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){
                int col=cartTable.columnAtPoint(e.getPoint()), row=cartTable.rowAtPoint(e.getPoint());
                if(col==5&&row>=0) removeFromCart(row);
            }
        });
        cartCard.add(PharmTheme.tableScrollPane(cartTable), BorderLayout.CENTER);
        p.add(cartCard, BorderLayout.CENTER);
        return p;
    }

    // ── Right: client + summary ─────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12)); p.setBackground(PharmTheme.BG);

        // Client card
        JPanel clientCard = PharmTheme.card();
        clientCard.setLayout(new BoxLayout(clientCard, BoxLayout.Y_AXIS));
        clientCard.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(16,16,16,16)));
        JLabel clientLbl = PharmTheme.sectionLabel("Client (optionnel)"); clientLbl.setAlignmentX(LEFT_ALIGNMENT);
        clientCard.add(clientLbl); clientCard.add(Box.createVerticalStrut(10));
        JPanel codeRow = new JPanel(new BorderLayout(6,0)); codeRow.setBackground(PharmTheme.CARD);
        codeRow.setAlignmentX(LEFT_ALIGNMENT); codeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        txtCodeCnam = PharmTheme.textField("Code CNAM");
        JButton searchBtn = PharmTheme.ghostButton("Chercher"); searchBtn.addActionListener(e->searchClient());
        codeRow.add(txtCodeCnam, BorderLayout.CENTER); codeRow.add(searchBtn, BorderLayout.EAST);
        clientCard.add(codeRow); clientCard.add(Box.createVerticalStrut(8));
        lblClientName = PharmTheme.helperLabel("—"); lblClientName.setAlignmentX(LEFT_ALIGNMENT);
        lblClientPoints = new JLabel(""); lblClientPoints.setFont(PharmTheme.FONT_SM); lblClientPoints.setForeground(new Color(0xE65100)); lblClientPoints.setAlignmentX(LEFT_ALIGNMENT);
        clientCard.add(lblClientName); clientCard.add(lblClientPoints);
        p.add(clientCard, BorderLayout.NORTH);

        // Meta card
        JPanel metaCard = PharmTheme.card();
        metaCard.setLayout(new BoxLayout(metaCard, BoxLayout.Y_AXIS));
        metaCard.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(14,16,14,16)));
        JLabel metaLbl = PharmTheme.sectionLabel("Transaction"); metaLbl.setAlignmentX(LEFT_ALIGNMENT);
        metaCard.add(metaLbl); metaCard.add(Box.createVerticalStrut(10));
        txtDateVente  = PharmTheme.textField(LocalDate.now().toString()); txtDateVente.setText(LocalDate.now().toString());
        txtDateLimite = PharmTheme.textField(LocalDate.now().plusDays(7).toString()); txtDateLimite.setText(LocalDate.now().plusDays(7).toString());
        txtNumEmp     = PharmTheme.textField("N° carte employé"); txtNumEmp.setText("1");
        addMeta(metaCard, "Date de vente",      txtDateVente);  metaCard.add(Box.createVerticalStrut(8));
        addMeta(metaCard, "Date limite retour", txtDateLimite); metaCard.add(Box.createVerticalStrut(8));
        addMeta(metaCard, "Employé (carte)",    txtNumEmp);
        p.add(metaCard, BorderLayout.CENTER);

        // Summary + validate
        JPanel sumCard = PharmTheme.card(); sumCard.setLayout(new BorderLayout());
        sumCard.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0));
        JPanel sumInner = new JPanel(); sumInner.setLayout(new BoxLayout(sumInner, BoxLayout.Y_AXIS));
        sumInner.setBackground(PharmTheme.CARD); sumInner.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        JLabel sumTitle = PharmTheme.sectionLabel("Récapitulatif"); sumTitle.setAlignmentX(LEFT_ALIGNMENT); sumInner.add(sumTitle);
        sumInner.add(Box.createVerticalStrut(10));
        lblTotal = new JLabel("0,00 DT"); lblTotal.setFont(new Font("SansSerif",Font.BOLD,30)); lblTotal.setForeground(PharmTheme.TXT); lblTotal.setAlignmentX(LEFT_ALIGNMENT);
        sumInner.add(lblTotal);
        JLabel totalLegend = PharmTheme.helperLabel("Montant total TTC"); totalLegend.setAlignmentX(LEFT_ALIGNMENT); sumInner.add(totalLegend);
        sumInner.add(Box.createVerticalStrut(16));
        sumCard.add(sumInner, BorderLayout.CENTER);

        JPanel validatePanel = new JPanel(new BorderLayout()); validatePanel.setBackground(PharmTheme.PM_800);
        validatePanel.setBorder(BorderFactory.createEmptyBorder(12,16,12,16));
        JButton validateBtn = new JButton("Valider la vente →");
        validateBtn.setFont(new Font("SansSerif",Font.BOLD,14)); validateBtn.setBackground(PharmTheme.PM_800);
        validateBtn.setForeground(PharmTheme.ACC); validateBtn.setBorderPainted(false); validateBtn.setFocusPainted(false);
        validateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        validateBtn.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){validateBtn.setForeground(PharmTheme.ACC_HOV);}
            @Override public void mouseExited(MouseEvent e) {validateBtn.setForeground(PharmTheme.ACC);}
        });
        validateBtn.addActionListener(e -> validerVente());
        validatePanel.add(validateBtn, BorderLayout.CENTER);
        sumCard.add(validatePanel, BorderLayout.SOUTH);
        p.add(sumCard, BorderLayout.SOUTH);
        return p;
    }

    private void addMeta(JPanel parent, String label, JTextField field) {
        JLabel lbl = PharmTheme.formLabel(label); lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT); field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        parent.add(lbl); parent.add(Box.createVerticalStrut(3)); parent.add(field);
    }

    // ── Data loading ────────────────────────────────────────────────────────
    private void loadMedicaments() {
        new SwingWorker<List<MedicamentItem>, Void>() {
            @Override protected List<MedicamentItem> doInBackground() throws Exception {
                List<MedicamentItem> items = new ArrayList<>();
                for (Medicament med : medicamentBD.listerTous()) {
                    List<StockMedicament> lots = stockBD.getStocksParExpiration(med.getRefMedicament());
                    // Filter out lots that are expired; keep only valid lots with stock
                    List<StockMedicament> lotsValides = new ArrayList<>();
                    for (StockMedicament lot : lots) {
                        if (!lot.estPerime() && lot.getQuantiteProduit() > 0) lotsValides.add(lot);
                    }
                    int total = lotsValides.stream().mapToInt(StockMedicament::getQuantiteProduit).sum();
                    if (total > 0) items.add(new MedicamentItem(med, lotsValides));
                }
                return items;
            }
            @Override protected void done() {
                try {
                    cmbMedicament.removeAllItems();
                    for (MedicamentItem item : get()) cmbMedicament.addItem(item);
                    if (cmbMedicament.getItemCount() == 0)
                        lblFEFO.setText("Aucun médicament disponible en stock");
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ── Interactions ────────────────────────────────────────────────────────
    private void onMedSelected() {
        MedicamentItem sel = (MedicamentItem) cmbMedicament.getSelectedItem();
        if (sel == null) { lblFEFO.setText(" "); return; }
        // Use price from first valid lot
        if (!sel.lots.isEmpty()) txtPrix.setText(String.format("%.2f", sel.lots.get(0).getPrixVente()));
        int qte = (int) spnQuantite.getValue();
        int dejaRes = reserves.getOrDefault(sel.med.getRefMedicament(), 0);
        int avail = sel.lots.stream().mapToInt(StockMedicament::getQuantiteProduit).sum() - dejaRes;
        ((SpinnerNumberModel) spnQuantite.getModel()).setMaximum(Math.max(avail, 0));
        if (avail > 0) {
            lblFEFO.setText("FEFO : " + buildFEFOPreview(sel.lots, Math.min(qte, avail)));
            lblFEFO.setForeground(PharmTheme.INFO);
        } else {
            lblFEFO.setText("⚠ Stock épuisé pour ce produit");
            lblFEFO.setForeground(PharmTheme.DANGER);
        }
    }

    private String buildFEFOPreview(List<StockMedicament> lots, int qte) {
        StringBuilder sb = new StringBuilder();
        int rem = qte;
        for (StockMedicament s : lots) {
            if (rem <= 0) break;
            int take = Math.min(s.getQuantiteProduit(), rem);
            if (sb.length() > 0) sb.append(" + ");
            sb.append(take).append(" du lot #").append(s.getNumStock());
            if (s.getDateExpiration() != null) {
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yy");
                sb.append(" (exp ").append(df.format(s.getDateExpiration())).append(")");
            }
            rem -= take;
        }
        return sb.toString();
    }

    private void searchClient() {
        String code = txtCodeCnam.getText().trim(); if (code.isEmpty()) return;
        try {
            clientActuel = clientBD.rechercherParCodeCnam(code);
            if (clientActuel != null) {
                lblClientName.setText(clientActuel.getPrenom() + " " + clientActuel.getNom());
                lblClientPoints.setText("⭐ " + clientActuel.getPointFidelite() + " pts fidélité");
                lblClientName.setForeground(PharmTheme.TXT);
            } else {
                lblClientName.setText("Client introuvable"); lblClientName.setForeground(PharmTheme.DANGER);
                lblClientPoints.setText(""); clientActuel = null;
            }
        } catch (SQLException ex) { PharmTheme.showError(this, "Recherche client", ex.getMessage()); }
    }

    private void addToCart() {
        MedicamentItem sel = (MedicamentItem) cmbMedicament.getSelectedItem(); if (sel == null) return;
        int qte = (int) spnQuantite.getValue();
        double prix;
        try { prix = Double.parseDouble(txtPrix.getText().trim().replace(",",".")); }
        catch (NumberFormatException ex) { PharmTheme.showError(this,"Prix invalide","Saisissez un prix valide."); return; }
        int ref = sel.med.getRefMedicament();
        int avail = sel.lots.stream().mapToInt(StockMedicament::getQuantiteProduit).sum() - reserves.getOrDefault(ref, 0);
        if (qte > avail) { PharmTheme.showError(this,"Stock insuffisant","Disponible : "+avail+" unités. Demandé : "+qte); return; }

        reserves.merge(ref, qte, Integer::sum);
        VoieVente lv = new VoieVente();
        lv.setRefMedicament(ref); lv.setQuantite(qte); lv.setPrixUnitaire(prix); lv.setPrixTotalVoieVente();
        lignes.add(lv);
        cartModel.addRow(new Object[]{
            sel.med.getNom(), qte,
            String.format("%.2f DT", prix),
            String.format("%.2f DT", lv.getPrixTotalVoieVente()),
            buildFEFOPreview(sel.lots, qte), "✕"
        });
        refreshTotal(); spnQuantite.setValue(1); onMedSelected();
    }

    private void removeFromCart(int row) {
        VoieVente lv = lignes.get(row);
        reserves.merge(lv.getRefMedicament(), -lv.getQuantite(), Integer::sum);
        lignes.remove(row); cartModel.removeRow(row);
        refreshTotal(); onMedSelected();
    }

    private void clearCart() {
        lignes.clear(); reserves.clear(); cartModel.setRowCount(0);
        refreshTotal(); onMedSelected();
    }

    private void refreshTotal() {
        double total = lignes.stream().mapToDouble(VoieVente::getPrixTotalVoieVente).sum();
        lblTotal.setText(String.format("%.2f DT", total));
        lblItemCount.setText(lignes.size() + " article" + (lignes.size() > 1 ? "s" : ""));
    }

    private void validerVente() {
        if (lignes.isEmpty()) { PharmTheme.showWarning(this,"Panier vide","Ajoutez au moins un article."); return; }
        try {
            Vente v = new Vente();
            v.setDateVente(txtDateVente.getText().trim());
            v.setDateLimRendreProduit(txtDateLimite.getText().trim());
            v.setMontantTotalVente(lignes.stream().mapToDouble(VoieVente::getPrixTotalVoieVente).sum());
            v.setNumClient(clientActuel != null ? clientActuel.getNumClient() : 0);
            v.setNumEmp(Integer.parseInt(txtNumEmp.getText().trim()));
            gestionVente.enregistrerVente(v, lignes);
            String pts = clientActuel != null ? "\nPoints gagnés : +" + (int)(v.getMontantTotalVente()/10) : "";
            PharmTheme.showSuccess(this,"Vente enregistrée",
                "Vente #"+v.getNumVente()+"\nMontant : "+String.format("%.2f DT",v.getMontantTotalVente())+pts);
            dispose();
        } catch (StockInsuffisantException ex) { PharmTheme.showError(this,"Stock insuffisant",ex.getMessage());
        } catch (ProduitNonTrouveException ex)  { PharmTheme.showError(this,"Produit introuvable",ex.getMessage());
        } catch (SQLException|NumberFormatException ex) { PharmTheme.showError(this,"Erreur",ex.getMessage()); }
    }

    // ── MedicamentItem ──────────────────────────────────────────────────────
    private static class MedicamentItem {
        final Medicament med;
        final List<StockMedicament> lots;
        MedicamentItem(Medicament med, List<StockMedicament> lots) { this.med=med; this.lots=lots; }
        @Override public String toString() {
            int total = lots.stream().mapToInt(StockMedicament::getQuantiteProduit).sum();
            // Show expiry of first lot if available
            String exp = "";
            if (!lots.isEmpty() && lots.get(0).getDateExpiration() != null) {
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yy");
                exp = "  exp." + df.format(lots.get(0).getDateExpiration());
            }
            return med.getNom() + "  ·  " + total + " en stock" + exp;
        }
    }
}