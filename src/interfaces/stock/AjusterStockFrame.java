package interfaces.stock;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import entite.*;
import entitebd.*;

public class AjusterStockFrame extends PharmBaseFrame {

    private JTextField        txtRef;
    private JLabel            lblNom, lblDesc;
    private JTable            tableLots;
    private DefaultTableModel modelLots;
    // Edit fields
    private JTextField txtQte, txtPrixA, txtPrixV, txtSeuil, txtDateFab, txtDateExp;
    // New lot fields
    private JTextField txtNewQte, txtNewPrixA, txtNewPrixV, txtNewSeuil, txtNewDateFab, txtNewDateExp;

    private final MedicamentBD  medicamentBD = new MedicamentBD();
    private final StockBD       stockBD      = new StockBD();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private int currentRef     = -1;
    private int currentNumStock = -1;

    public AjusterStockFrame() {
        super("Ajuster le stock", "Modifier un lot existant ou en ajouter un nouveau", 1150, 700);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(12, 0));
        contentArea.add(buildLeft(),  BorderLayout.CENTER);
        contentArea.add(buildRight(), BorderLayout.EAST);
    }

    // ── Left: search bar + med info + lots table ─────────────────────────
    private JPanel buildLeft() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(PharmTheme.BG);

        // ── Top: search row ──
        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setBackground(PharmTheme.BG);
        txtRef = PharmTheme.textField("Référence médicament…");
        JButton btn = PharmTheme.ghostButton("Chercher");
        btn.addActionListener(e -> rechercher());
        txtRef.addActionListener(e -> rechercher());
        searchRow.add(txtRef, BorderLayout.CENTER);
        searchRow.add(btn, BorderLayout.EAST);

        // ── Med info strip (hidden until search) ──
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        info.setBackground(PharmTheme.BG2);
        info.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0));
        lblNom  = new JLabel("—");
        lblNom.setFont(PharmTheme.FONT_H3);
        lblNom.setForeground(PharmTheme.TXT);
        lblDesc = new JLabel("");
        lblDesc.setFont(PharmTheme.FONT_SM);
        lblDesc.setForeground(PharmTheme.TXT3);
        info.add(lblNom);
        info.add(lblDesc);

        // ── Top section = search + info stacked ──
        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setBackground(PharmTheme.BG);
        topSection.add(searchRow, BorderLayout.NORTH);
        topSection.add(info, BorderLayout.CENTER);
        p.add(topSection, BorderLayout.NORTH);

        // ── Lots table ──
        modelLots = new DefaultTableModel(
            new String[]{"N° Lot", "Qté", "P.Achat", "P.Vente", "Seuil", "Date fab.", "Date exp.", "Statut"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableLots = new JTable(modelLots);
        PharmTheme.styleTable(tableLots);
        tableLots.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableLots.setIntercellSpacing(new Dimension(10, 2));
        int[] w = {65, 55, 80, 80, 58, 88, 88, 95};
        for (int i = 0; i < w.length; i++) {
            tableLots.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            tableLots.getColumnModel().getColumn(i).setMinWidth(w[i] - 10);
        }
        tableLots.getColumnModel().getColumn(7).setCellRenderer((t, v, s, f, r, c) -> {
            String val = String.valueOf(v);
            PharmTheme.BadgeType bt = val.equals("Normal") ? PharmTheme.BadgeType.SUCCESS
                : val.contains("Périmé") || val.equals("Rupture") ? PharmTheme.BadgeType.DANGER
                : PharmTheme.BadgeType.WARN;
            return PharmTheme.badgeComponent(val, bt);
        });
        tableLots.getSelectionModel().addListSelectionListener(e -> {
            loadLotToForm();
        });
        // Also trigger on mouse click (even same row re-click)
        tableLots.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                loadLotToForm();
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout(0, 6));
        tablePanel.setBackground(PharmTheme.BG);
        JLabel lotsTitle = PharmTheme.sectionLabel("LOTS EN STOCK");
        lotsTitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        tablePanel.add(lotsTitle, BorderLayout.NORTH);

        JScrollPane tsp = new JScrollPane(tableLots,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tsp.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0));
        tsp.setBackground(PharmTheme.CARD);
        tsp.getViewport().setBackground(PharmTheme.CARD);
        PharmTheme.styleScrollBar(tsp.getVerticalScrollBar());
        PharmTheme.styleScrollBar(tsp.getHorizontalScrollBar());
        tablePanel.add(tsp, BorderLayout.CENTER);

        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    // ── Right: edit form + add lot form ──────────────────────────────────
    private JScrollPane buildRight() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(PharmTheme.CARD);
        right.setPreferredSize(new Dimension(350, 0));
        right.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Edit selected lot section
        addFormSection(right, "Modifier le lot sélectionné");
        txtQte     = fld(right, "Quantité");
        txtPrixA   = fld(right, "Prix achat (DT)");
        txtPrixV   = fld(right, "Prix vente (DT)");
        txtSeuil   = fld(right, "Seuil alerte");
        txtDateFab = fld(right, "Date fabrication (jj/mm/aaaa)");
        txtDateExp = fld(right, "Date expiration (jj/mm/aaaa)");

        JButton btnSave = PharmTheme.primaryButton("Enregistrer modification");
        btnSave.setAlignmentX(LEFT_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSave.addActionListener(e -> saveLot());
        right.add(btnSave);
        right.add(Box.createVerticalStrut(8));

        JButton btnDel = PharmTheme.dangerButton("Supprimer ce lot");
        btnDel.setAlignmentX(LEFT_ALIGNMENT);
        btnDel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnDel.addActionListener(e -> deleteLot());
        right.add(btnDel);
        right.add(Box.createVerticalStrut(12));
        right.add(PharmTheme.separator());
        right.add(Box.createVerticalStrut(12));

        // Add new lot section
        addFormSection(right, "Ajouter un nouveau lot");
        txtNewQte     = fld(right, "Quantité *");
        txtNewPrixA   = fld(right, "Prix achat (DT) *");
        txtNewPrixV   = fld(right, "Prix vente (DT) *");
        txtNewSeuil   = fld(right, "Seuil alerte *");
        txtNewDateFab = fld(right, "Date fabrication (jj/mm/aaaa)");
        txtNewDateExp = fld(right, "Date expiration (jj/mm/aaaa)");

        JButton btnAdd = PharmTheme.accentButton("+ Ajouter ce lot");
        btnAdd.setAlignmentX(LEFT_ALIGNMENT);
        btnAdd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnAdd.addActionListener(e -> addLot());
        right.add(btnAdd);
        right.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(right);
        sp.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0));
        sp.setBackground(PharmTheme.CARD);
        sp.getViewport().setBackground(PharmTheme.CARD);
        PharmTheme.styleScrollBar(sp.getVerticalScrollBar());
        sp.setPreferredSize(new Dimension(350, 0));
        return sp;
    }

    private JTextField fld(JPanel p, String label) {
        JLabel l = PharmTheme.formLabel(label);
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        JTextField f = PharmTheme.textField("");
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(LEFT_ALIGNMENT);
        p.add(f);
        p.add(Box.createVerticalStrut(10));
        return f;
    }

    // ── Logic ─────────────────────────────────────────────────────────────
    private void rechercher() {
        String r = txtRef.getText().trim();
        if (r.isEmpty()) return;
        try {
            int ref = Integer.parseInt(r);
            Medicament m = medicamentBD.rechercherParRef(ref);
            if (m == null) {
                PharmTheme.showWarning(this, "Introuvable", "Aucun médicament réf. #" + ref);
                lblNom.setText("—"); lblDesc.setText(""); return;
            }
            currentRef = ref;
            lblNom.setText(m.getNom());
            lblDesc.setText(m.getDescriptio() != null && !m.getDescriptio().isEmpty()
                ? m.getDescriptio() : "");
            refreshLots();
        } catch (NumberFormatException ex) {
            PharmTheme.showError(this, "Référence invalide", "La référence doit être un nombre entier.");
        } catch (SQLException ex) {
            PharmTheme.showError(this, "Erreur BD", ex.getMessage());
        }
    }

    private void refreshLots() throws SQLException {
        modelLots.setRowCount(0);
        currentNumStock = -1;
        clearEditForm();
        List<StockMedicament> lots = stockBD.getStocksParExpiration(currentRef);
        for (StockMedicament s : lots) {
            String statut;
            if (s.getQuantiteProduit() == 0)  statut = "Rupture";
            else if (s.estPerime())            statut = "Périmé";
            else if (s.Alerte())               statut = "Alerte";
            else                               statut = "Normal";
            modelLots.addRow(new Object[]{
                s.getNumStock(),
                s.getQuantiteProduit(),
                String.format("%.2f", s.getPrixAchat()),
                String.format("%.2f", s.getPrixVente()),
                s.getSeuilMin(),
                s.getDateFabrication() != null ? df.format(s.getDateFabrication()) : "—",
                s.getDateExpiration()  != null ? df.format(s.getDateExpiration())  : "—",
                statut
            });
        }
    }


    /** Parse a number that may use comma OR dot as decimal separator */
    private static double parseNum(String s) {
        return Double.parseDouble(s.replace(',', '.').trim());
    }

    private void loadLotToForm() {
        int row = tableLots.getSelectedRow();
        if (row < 0 || row >= modelLots.getRowCount()) return;
        // Convert view row to model row (in case of sorting)
        currentNumStock = (Integer) modelLots.getValueAt(row, 0);
        // Set all edit fields from the selected row
        txtQte.setText(String.valueOf(modelLots.getValueAt(row, 1)));
        txtPrixA.setText(String.valueOf(modelLots.getValueAt(row, 2)));
        txtPrixV.setText(String.valueOf(modelLots.getValueAt(row, 3)));
        txtSeuil.setText(String.valueOf(modelLots.getValueAt(row, 4)));
        String fab = String.valueOf(modelLots.getValueAt(row, 5));
        String exp = String.valueOf(modelLots.getValueAt(row, 6));
        txtDateFab.setText("—".equals(fab) ? "" : fab);
        txtDateExp.setText("—".equals(exp) ? "" : exp);
        // Move focus to first edit field so user can immediately type
        txtQte.selectAll();
        txtQte.requestFocusInWindow();
    }

    private void clearEditForm() {
        txtQte.setText(""); txtPrixA.setText(""); txtPrixV.setText("");
        txtSeuil.setText(""); txtDateFab.setText(""); txtDateExp.setText("");
    }

    private void saveLot() {
        if (currentNumStock < 0) {
            PharmTheme.showWarning(this, "Aucun lot", "Sélectionnez un lot dans le tableau.");
            return;
        }
        try {
            StockMedicament s = stockBD.rechercherParNumStock(currentNumStock);
            if (s == null) { PharmTheme.showError(this, "Introuvable", "Lot #" + currentNumStock + " introuvable."); return; }
            s.setQuantiteProduit(Integer.parseInt(txtQte.getText().trim()));
            s.setPrixAchat(parseNum(txtPrixA.getText()));
            s.setPrixVente(parseNum(txtPrixV.getText()));
            s.setSeuilMin(Integer.parseInt(txtSeuil.getText().trim()));
            String fab = txtDateFab.getText().trim(), exp = txtDateExp.getText().trim();
            s.setDateFabrication(!fab.isEmpty() ? df.parse(fab) : null);
            s.setDateExpiration(!exp.isEmpty() ? df.parse(exp) : null);
            stockBD.modifier(s);
            PharmTheme.showSuccess(this, "Lot modifié", "Lot #" + currentNumStock + " mis à jour.");
            refreshLots();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Saisie invalide", "Vérifiez les champs numériques.");
        } catch (ParseException ex)        { PharmTheme.showError(this, "Date invalide", "Format attendu : jj/mm/aaaa");
        } catch (SQLException ex)          { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void deleteLot() {
        if (currentNumStock < 0) {
            PharmTheme.showWarning(this, "Aucun lot", "Sélectionnez un lot à supprimer.");
            return;
        }
        if (!PharmTheme.showConfirm(this, "Supprimer lot", "Supprimer le lot #" + currentNumStock + " ?")) return;
        try {
            stockBD.supprimerParNumStock(currentNumStock);
            PharmTheme.showSuccess(this, "Lot supprimé", "Lot #" + currentNumStock + " supprimé.");
            refreshLots();
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void addLot() {
        if (currentRef < 0) {
            PharmTheme.showWarning(this, "Aucun médicament", "Recherchez d'abord un médicament par référence.");
            return;
        }
        try {
            StockMedicament s = new StockMedicament();
            s.setRefMedicament(currentRef);
            s.setQuantiteProduit(Integer.parseInt(txtNewQte.getText().trim()));
            s.setPrixAchat(parseNum(txtNewPrixA.getText()));
            s.setPrixVente(parseNum(txtNewPrixV.getText()));
            s.setSeuilMin(Integer.parseInt(txtNewSeuil.getText().trim()));
            String fab = txtNewDateFab.getText().trim(), exp = txtNewDateExp.getText().trim();
            s.setDateFabrication(!fab.isEmpty() ? df.parse(fab) : null);
            s.setDateExpiration(!exp.isEmpty() ? df.parse(exp) : null);
            int num = stockBD.ajouter(s);
            PharmTheme.showSuccess(this, "Lot ajouté", "Nouveau lot #" + num + " créé pour «" + lblNom.getText() + "».");
            // Clear new lot fields
            txtNewQte.setText(""); txtNewPrixA.setText(""); txtNewPrixV.setText("");
            txtNewSeuil.setText(""); txtNewDateFab.setText(""); txtNewDateExp.setText("");
            refreshLots();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Saisie invalide", "Vérifiez les champs numériques.");
        } catch (ParseException ex)        { PharmTheme.showError(this, "Date invalide", "Format attendu : jj/mm/aaaa");
        } catch (SQLException ex)          { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}