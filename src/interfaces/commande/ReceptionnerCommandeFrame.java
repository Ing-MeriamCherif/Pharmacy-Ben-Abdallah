package interfaces.commande;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import entite.*;
import entitebd.*;
import gestion.GestionCommande;

public class ReceptionnerCommandeFrame extends PharmBaseFrame {

    private JTextField        txtNum, txtNumEmp;
    private JTextArea         txtInfo;
    private JTable            table;
    private DefaultTableModel tableModel;
    private JButton           btnReceptionner;

    private Commande                commandeActuelle;
    private ArrayList<VoieCommande> lignes = new ArrayList<>();

    private final GestionCommande gestionCommande = new GestionCommande();
    private final MedicamentBD    medicamentBD    = new MedicamentBD();
    private final StockBD         stockBD         = new StockBD();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    // Column indices
    private static final int COL_NOM     = 0;
    private static final int COL_QTE     = 1;
    private static final int COL_PA      = 2;  // read-only
    private static final int COL_PV      = 3;  // editable
    private static final int COL_SEUIL   = 4;  // editable
    private static final int COL_DATE_FAB= 5;  // editable
    private static final int COL_DATE_EXP= 6;  // editable

    public ReceptionnerCommandeFrame() {
        super("Réceptionner une commande", "Valider réception et créer les stocks", 1200, 700);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton close = PharmTheme.ghostButton("Fermer");
        close.addActionListener(e -> dispose());
        btnReceptionner = PharmTheme.primaryButton("📦 Réceptionner →");
        btnReceptionner.setEnabled(false);
        btnReceptionner.addActionListener(e -> receptionner());
        p.add(close); p.add(btnReceptionner);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 10));

        // ── Search row ──
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setBackground(PharmTheme.BG);
        searchRow.add(PharmTheme.formLabel("N° Commande :"));
        txtNum = PharmTheme.textField("Numéro…");
        txtNum.setPreferredSize(new Dimension(140, 34));
        txtNum.addActionListener(e -> charger());
        searchRow.add(txtNum);
        JButton btn = PharmTheme.ghostButton("Charger");
        btn.addActionListener(e -> charger());
        searchRow.add(btn);
        searchRow.add(PharmTheme.formLabel("Employé :"));
        txtNumEmp = PharmTheme.textField("1");
        txtNumEmp.setText("1");
        txtNumEmp.setPreferredSize(new Dimension(60, 34));
        searchRow.add(txtNumEmp);
        contentArea.add(searchRow, BorderLayout.NORTH);

        // ── Split: info top, table bottom ──
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(160);
        split.setBorder(BorderFactory.createEmptyBorder());

        txtInfo = PharmTheme.textArea(5, 0);
        txtInfo.setEditable(false);
        txtInfo.setFont(PharmTheme.FONT_MONO);
        txtInfo.setText("Chargez une commande par son numéro…");
        split.setTopComponent(PharmTheme.scrollTextArea(txtInfo));

        // ── Table: 7 columns, last 4 editable ──
        tableModel = new DefaultTableModel(
            new String[]{
                "Médicament", "Qté", "Prix achat",
                "Prix vente ✎", "Seuil min ✎",
                "Date fab. ✎ (jj/mm/aaaa)", "Date exp. ✎ (jj/mm/aaaa)"
            }, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == COL_PV || c == COL_SEUIL || c == COL_DATE_FAB || c == COL_DATE_EXP;
            }
            @Override public Class<?> getColumnClass(int c) {
                return (c == COL_QTE) ? Integer.class : String.class;
            }
        };

        table = new JTable(tableModel);
        PharmTheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setIntercellSpacing(new Dimension(10, 2));
        table.setRowHeight(36);

        int[] w = {185, 55, 105, 110, 90, 170, 170};
        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            table.getColumnModel().getColumn(i).setMinWidth(w[i] - 20);
        }

        // Renderer: green tint for editable columns
        DefaultTableCellRenderer editR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if (!sel) {
                    boolean editable = (c==COL_PV||c==COL_SEUIL||c==COL_DATE_FAB||c==COL_DATE_EXP);
                    comp.setBackground(editable ? new Color(0xEEF7F1) : PharmTheme.CARD);
                    comp.setForeground(PharmTheme.TXT);
                }
                return comp;
            }
        };
        for (int i = 0; i < 7; i++) table.getColumnModel().getColumn(i).setCellRenderer(editR);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 6));
        tablePanel.setBackground(PharmTheme.BG);

        // Hint strip
        JPanel hintStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        hintStrip.setBackground(new Color(0xEEF7F1));
        hintStrip.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, new Color(0x1A6B45, true), 0));
        JLabel hintIcon = new JLabel("✎");
        hintIcon.setFont(new Font("SansSerif", Font.BOLD, 14));
        hintIcon.setForeground(PharmTheme.PM_500);
        JLabel hintText = new JLabel(
            "Colonnes en vert = éditables. Renseignez Prix vente, Seuil, Date fabrication et Date expiration de chaque lot.");
        hintText.setFont(PharmTheme.FONT_SM);
        hintText.setForeground(new Color(0x1A6B45));
        hintStrip.add(hintIcon); hintStrip.add(hintText);
        tablePanel.add(hintStrip, BorderLayout.NORTH);

        JScrollPane tsp = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        tsp.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0));
        tsp.setBackground(PharmTheme.CARD); tsp.getViewport().setBackground(PharmTheme.CARD);
        PharmTheme.styleScrollBar(tsp.getVerticalScrollBar());
        PharmTheme.styleScrollBar(tsp.getHorizontalScrollBar());
        tablePanel.add(tsp, BorderLayout.CENTER);

        split.setBottomComponent(tablePanel);
        contentArea.add(split, BorderLayout.CENTER);
    }

    // ── Load command ──────────────────────────────────────────────────────
    private void charger() {
        String numStr = txtNum.getText().trim();
        if (numStr.isEmpty()) {
            PharmTheme.showWarning(this, "Champ vide", "Entrez un numéro de commande."); return;
        }
        try {
            int num = Integer.parseInt(numStr);
            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(num);
            commandeActuelle = bilan.getCommande();
            lignes = bilan.getLignes();

            StringBuilder sb = new StringBuilder();
            sb.append("Commande #").append(commandeActuelle.getNumCommande())
              .append("  —  Statut : ").append(commandeActuelle.getStatut()).append("\n");
            sb.append("Date : ").append(commandeActuelle.getDateAchat())
              .append("  |  Total : ").append(String.format("%.2f DT", bilan.getTotal()))
              .append("  |  Lignes : ").append(bilan.getNombreLignes()).append("\n\n");

            boolean canReceive = !"Reçue".equals(commandeActuelle.getStatut())
                              && !"Annulée".equals(commandeActuelle.getStatut());
            sb.append(canReceive
                ? "✅ Prête à être réceptionnée.\n"
                  + "Renseignez Prix vente, Seuil min, Date fabrication et Date expiration pour chaque lot."
                : "⛔ Cette commande est " + commandeActuelle.getStatut().toLowerCase()
                  + " — impossible de la réceptionner.");
            txtInfo.setText(sb.toString());
            btnReceptionner.setEnabled(canReceive);

            tableModel.setRowCount(0);
            for (VoieCommande lc : lignes) {
                String nomMed = "Méd. #" + lc.getRefMedicament();
                try {
                    Medicament m = medicamentBD.rechercherParRef(lc.getRefMedicament());
                    if (m != null) nomMed = m.getNom();
                } catch (Exception ignored) {}

                tableModel.addRow(new Object[]{
                    nomMed,
                    lc.getQuantite(),
                    String.format("%.2f DT", lc.getPrixUnitaire()),   // read-only
                    String.format("%.2f", lc.getPrixUnitaire() * 1.3), // prix vente
                    "10",                                               // seuil min
                    "",                                                 // date fab (blank)
                    ""                                                  // date exp (blank)
                });
            }

            // Focus first editable cell
            if (tableModel.getRowCount() > 0) {
                table.changeSelection(0, COL_PV, false, false);
                table.requestFocusInWindow();
            }

        } catch (NumberFormatException ex) {
            PharmTheme.showError(this, "Invalide", "Entrez un numéro de commande valide.");
        } catch (Exception ex) {
            PharmTheme.showError(this, "Erreur", ex.getMessage());
        }
    }

    // ── Validate & receive ────────────────────────────────────────────────
    private void receptionner() {
        if (commandeActuelle == null) return;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        // Validate employee
        String empStr = txtNumEmp.getText().trim();
        if (empStr.isEmpty()) empStr = "1";
        int numEmp;
        try { numEmp = Integer.parseInt(empStr); }
        catch (NumberFormatException ex) {
            PharmTheme.showError(this, "Employé invalide", "Entrez un numéro d'employé valide.");
            txtNumEmp.requestFocus(); return;
        }

        int n = tableModel.getRowCount();
        double[] prixVentes  = new double[n];
        int[]    seuilMins   = new int[n];
        Date[]   datesFab    = new Date[n];
        Date[]   datesExp    = new Date[n];

        for (int r = 0; r < n; r++) {
            String nomMed = String.valueOf(tableModel.getValueAt(r, COL_NOM));

            // Prix vente
            String pvStr = String.valueOf(tableModel.getValueAt(r, COL_PV)).replace(",", ".").trim();
            if (pvStr.isEmpty()) pvStr = "0";
            try {
                prixVentes[r] = Double.parseDouble(pvStr);
                if (prixVentes[r] <= 0) {
                    PharmTheme.showError(this, "Prix invalide",
                        "Ligne " + (r+1) + " (" + nomMed + ") : Prix vente doit être positif.");
                    table.changeSelection(r, COL_PV, false, false); return;
                }
            } catch (NumberFormatException ex) {
                PharmTheme.showError(this, "Prix invalide",
                    "Ligne " + (r+1) + " (" + nomMed + ") : Prix vente non numérique.");
                table.changeSelection(r, COL_PV, false, false); return;
            }

            // Seuil min
            String smStr = String.valueOf(tableModel.getValueAt(r, COL_SEUIL)).replace(",", ".").trim();
            if (smStr.isEmpty()) smStr = "10";
            try { seuilMins[r] = (int) Double.parseDouble(smStr); }
            catch (NumberFormatException ex) {
                PharmTheme.showError(this, "Seuil invalide",
                    "Ligne " + (r+1) + " (" + nomMed + ") : Seuil min non numérique.");
                table.changeSelection(r, COL_SEUIL, false, false); return;
            }

            // Date fabrication (optional)
            String fabStr = String.valueOf(tableModel.getValueAt(r, COL_DATE_FAB)).trim();
            if (!fabStr.isEmpty()) {
                try { datesFab[r] = df.parse(fabStr); }
                catch (ParseException ex) {
                    PharmTheme.showError(this, "Date invalide",
                        "Ligne " + (r+1) + " (" + nomMed + ") : Date fabrication — format attendu jj/mm/aaaa");
                    table.changeSelection(r, COL_DATE_FAB, false, false); return;
                }
            }

            // Date expiration (optional but recommended)
            String expStr = String.valueOf(tableModel.getValueAt(r, COL_DATE_EXP)).trim();
            if (!expStr.isEmpty()) {
                try {
                    datesExp[r] = df.parse(expStr);
                    if (datesFab[r] != null && datesExp[r].before(datesFab[r])) {
                        PharmTheme.showError(this, "Date invalide",
                            "Ligne " + (r+1) + " (" + nomMed + ") : Date expiration doit être après Date fabrication.");
                        table.changeSelection(r, COL_DATE_EXP, false, false); return;
                    }
                } catch (ParseException ex) {
                    PharmTheme.showError(this, "Date invalide",
                        "Ligne " + (r+1) + " (" + nomMed + ") : Date expiration — format attendu jj/mm/aaaa");
                    table.changeSelection(r, COL_DATE_EXP, false, false); return;
                }
            }
        }

        // Confirm
        if (!PharmTheme.showConfirm(this, "Confirmer réception",
            "Réceptionner la commande #" + commandeActuelle.getNumCommande() + " ?\n"
            + "Cela créera " + lignes.size() + " lot(s) de stock.")) return;

        try {
            // Create stock entries with all validated values
            for (int r = 0; r < lignes.size(); r++) {
                VoieCommande lc = lignes.get(r);
                StockMedicament stock = new StockMedicament();
                stock.setRefMedicament(lc.getRefMedicament());
                stock.setQuantiteProduit(lc.getQuantite());
                stock.setPrixAchat(lc.getPrixUnitaire());
                stock.setPrixVente(prixVentes[r]);
                stock.setSeuilMin(seuilMins[r]);
                stock.setDateFabrication(datesFab[r]);   // null if not entered
                stock.setDateExpiration(datesExp[r]);    // null if not entered
                stockBD.ajouter(stock);
            }

            // Mark command as received
            commandeActuelle.setStatut("Reçue");
            new CommandeBD().modifierCommande(commandeActuelle);

            PharmTheme.showSuccess(this, "Réceptionnée",
                "Commande #" + commandeActuelle.getNumCommande() + " réceptionnée.\n"
                + lignes.size() + " lot(s) ajouté(s) au stock.");
            btnReceptionner.setEnabled(false);
            charger();

        } catch (Exception ex) {
            PharmTheme.showError(this, "Erreur", ex.getMessage());
        }
    }
}