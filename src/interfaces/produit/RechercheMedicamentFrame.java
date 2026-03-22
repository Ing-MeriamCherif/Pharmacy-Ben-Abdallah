package interfaces.produit;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import entite.*;
import entitebd.*;
import gestion.GestionProduit;

public class RechercheMedicamentFrame extends PharmBaseFrame {
    private JTextField        txtNom, txtRef;
    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextArea         txtDetails;

    private final MedicamentBD   medicamentBD  = new MedicamentBD();
    private final StockBD        stockBD       = new StockBD();
    private final GestionProduit gestionProduit = new GestionProduit();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public RechercheMedicamentFrame() {
        super("Rechercher un médicament", "Recherche par nom ou référence", 1080, 660);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton close  = PharmTheme.ghostButton("Fermer");        close.addActionListener(e -> dispose());
        JButton search = PharmTheme.primaryButton("Rechercher →"); search.addActionListener(e -> search());
        p.add(close); p.add(search);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 12));

        // Search bar
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); filters.setBackground(PharmTheme.BG);
        filters.add(PharmTheme.formLabel("Nom :"));
        txtNom = PharmTheme.textField("Rechercher par nom…"); txtNom.setPreferredSize(new Dimension(220, 34)); txtNom.addActionListener(e -> search());
        filters.add(txtNom);
        filters.add(PharmTheme.formLabel("Réf. :"));
        txtRef = PharmTheme.textField("Ex: 1"); txtRef.setPreferredSize(new Dimension(100, 34)); txtRef.addActionListener(e -> search());
        filters.add(txtRef);
        contentArea.add(filters, BorderLayout.NORTH);

        // Split: table top, detail bottom
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(300); split.setBorder(javax.swing.BorderFactory.createEmptyBorder()); split.setBackground(PharmTheme.BG);

        // Columns: Réf, Nom, Description, Nb Lots, Stock Total, P.Vente (premier lot), Statut
        tableModel = new DefaultTableModel(
            new String[]{"Réf.", "Nom", "Description", "Nb Lots", "Stock Total", "Prix vente", "Statut"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        int[] w = {50, 180, 200, 70, 90, 80, 90};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        table.getColumnModel().getColumn(6).setCellRenderer((t, v, s, f, r, c) -> {
            String val = String.valueOf(v);
            PharmTheme.BadgeType bt = val.equals("Normal") ? PharmTheme.BadgeType.SUCCESS
                : val.equals("Rupture") || val.contains("Périmé") ? PharmTheme.BadgeType.DANGER
                : PharmTheme.BadgeType.WARN;
            return PharmTheme.badgeComponent(val, bt);
        });
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showDetails(); });
        split.setTopComponent(PharmTheme.tableScrollPane(table));

        txtDetails = PharmTheme.textArea(6, 0); txtDetails.setEditable(false);
        txtDetails.setFont(PharmTheme.FONT_MONO); txtDetails.setText("Sélectionnez un médicament pour voir les détails…");
        JScrollPane dsp = PharmTheme.scrollTextArea(txtDetails);
        dsp.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0));
        split.setBottomComponent(dsp);
        contentArea.add(split, BorderLayout.CENTER);
    }

    private void search() {
        tableModel.setRowCount(0);
        String nom = txtNom.getText().trim(), ref = txtRef.getText().trim();
        if (nom.isEmpty() && ref.isEmpty()) { PharmTheme.showWarning(this, "Critère manquant", "Saisissez un nom ou une référence."); return; }
        new SwingWorker<List<Medicament>, Void>() {
            @Override protected List<Medicament> doInBackground() throws Exception {
                if (!ref.isEmpty()) {
                    Medicament m = medicamentBD.rechercherParRef(Integer.parseInt(ref));
                    return m != null ? java.util.List.of(m) : java.util.List.of();
                }
                return medicamentBD.rechercherParNom(nom);
            }
            @Override protected void done() {
                try {
                    for (Medicament m : get()) {
                        List<StockMedicament> lots = new java.util.ArrayList<>();
                        try { lots = stockBD.getStocksParExpiration(m.getRefMedicament()); } catch (Exception ignored) {}
                        int total = lots.stream().mapToInt(StockMedicament::getQuantiteProduit).sum();
                        double pv = lots.isEmpty() ? 0 : lots.get(0).getPrixVente();
                        boolean hasPerime = lots.stream().anyMatch(StockMedicament::estPerime);
                        boolean hasAlerte = lots.stream().anyMatch(StockMedicament::Alerte);
                        String statut = total == 0 ? "Rupture" : hasPerime ? "Périmé" : hasAlerte ? "Alerte stock" : "Normal";
                        tableModel.addRow(new Object[]{
                            m.getRefMedicament(), m.getNom(),
                            m.getDescriptio() != null ? m.getDescriptio() : "—",
                            lots.size() + " lot(s)", total + " unités",
                            pv > 0 ? String.format("%.2f DT", pv) : "—",
                            statut
                        });
                    }
                } catch (Exception e) { PharmTheme.showError(RechercheMedicamentFrame.this, "Erreur", e.getMessage()); }
            }
        }.execute();
    }

    private void showDetails() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int ref = (Integer) tableModel.getValueAt(row, 0);
        try {
            Medicament m = medicamentBD.rechercherParRef(ref);
            List<StockMedicament> lots = stockBD.getStocksParExpiration(ref);
            if (m == null) return;

            int totalQte = lots.stream().mapToInt(StockMedicament::getQuantiteProduit).sum();

            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("  ").append(m.getNom()).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            sb.append("Référence         : #").append(m.getRefMedicament()).append("\n");
            sb.append("Description       : ").append(m.getDescriptio() != null ? m.getDescriptio() : "—").append("\n");
            sb.append("Nombre de lots    : ").append(lots.size()).append("\n");
            sb.append("Stock total       : ").append(totalQte).append(" unités\n\n");

            if (!lots.isEmpty()) {
                sb.append("--- DÉTAIL DES LOTS ---\n");
                for (StockMedicament s : lots) {
                    sb.append(String.format("  Lot #%-4d  Qté: %-5d  P.Achat: %-8s  P.Vente: %-8s  Seuil: %-4d",
                        s.getNumStock(), s.getQuantiteProduit(),
                        String.format("%.2f DT", s.getPrixAchat()),
                        String.format("%.2f DT", s.getPrixVente()),
                        s.getSeuilMin()));
                    if (s.getDateFabrication() != null) sb.append("  Fab: ").append(df.format(s.getDateFabrication()));
                    if (s.getDateExpiration()  != null) sb.append("  Exp: ").append(df.format(s.getDateExpiration()));
                    if (s.estPerime())  sb.append("  ⛔ PÉRIMÉ");
                    else if (s.Alerte()) sb.append("  ⚠ ALERTE");
                    sb.append("\n");
                }
                double marge = lots.get(0).getPrixAchat() > 0
                    ? ((lots.get(0).getPrixVente() - lots.get(0).getPrixAchat()) / lots.get(0).getPrixAchat()) * 100
                    : 0;
                sb.append(String.format("\nMarge (lot 1)     : %.1f%%\n", marge));
            } else {
                sb.append("Aucun stock enregistré.\n");
            }

            txtDetails.setText(sb.toString()); txtDetails.setCaretPosition(0);
        } catch (SQLException ex) { txtDetails.setText("Erreur : " + ex.getMessage()); }
    }
}