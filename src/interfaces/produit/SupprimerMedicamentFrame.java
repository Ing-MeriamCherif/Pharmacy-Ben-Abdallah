package interfaces.produit;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.*;
import entitebd.*;
import gestion.GestionProduit;
import exception.ProduitNonTrouveException;

public class SupprimerMedicamentFrame extends PharmBaseFrame {
    private JTextField        txtSearch;
    private JTable            table;
    private DefaultTableModel tableModel;

    private final MedicamentBD  medicamentBD  = new MedicamentBD();
    private final StockBD       stockBD       = new StockBD();
    private final GestionProduit gestionProduit = new GestionProduit();

    public SupprimerMedicamentFrame() {
        super("Supprimer un médicament", "Sélectionnez le médicament à supprimer", 900, 560);
        buildUI(); loadMedicaments();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton close = PharmTheme.ghostButton("Fermer");    close.addActionListener(e -> dispose());
        JButton del   = PharmTheme.dangerButton("Supprimer"); del.addActionListener(e -> supprimer());
        p.add(close); p.add(del);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 10));

        JPanel searchRow = new JPanel(new BorderLayout(6, 0)); searchRow.setBackground(PharmTheme.BG);
        txtSearch = PharmTheme.textField("Rechercher par nom…");
        JButton btn = PharmTheme.ghostButton("Chercher");
        btn.addActionListener(e -> loadMedicaments()); txtSearch.addActionListener(e -> loadMedicaments());
        searchRow.add(txtSearch, BorderLayout.CENTER); searchRow.add(btn, BorderLayout.EAST);
        contentArea.add(searchRow, BorderLayout.NORTH);

        // Columns: Réf, Nom, Description, Nb Lots, Stock Total, Statut
        tableModel = new DefaultTableModel(
            new String[]{"Réf.", "Nom", "Description", "Nb Lots", "Stock Total", "Statut"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        int[] w = {50, 200, 220, 70, 90, 90};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        table.getColumnModel().getColumn(5).setCellRenderer((t, v, s, f, r, c) -> {
            String val = String.valueOf(v);
            PharmTheme.BadgeType bt = val.contains("Périmé") ? PharmTheme.BadgeType.DANGER
                : val.contains("Alerte") ? PharmTheme.BadgeType.WARN : PharmTheme.BadgeType.SUCCESS;
            return PharmTheme.badgeComponent(val, bt);
        });
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel warn = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        warn.setBackground(PharmTheme.WARN_BG);
        warn.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 4, 12));
        JLabel wl = new JLabel("⚠  Attention : la suppression supprime aussi tous les lots de stock. Action irréversible.");
        wl.setFont(PharmTheme.FONT_SM); wl.setForeground(PharmTheme.WARN);
        warn.add(wl);
        contentArea.add(warn, BorderLayout.SOUTH);
    }

    private void loadMedicaments() {
        tableModel.setRowCount(0);
        String q = txtSearch.getText().trim();
        new SwingWorker<java.util.List<Medicament>, Void>() {
            @Override protected java.util.List<Medicament> doInBackground() throws Exception {
                return q.isEmpty() ? medicamentBD.listerTous() : medicamentBD.rechercherParNom(q);
            }
            @Override protected void done() {
                try {
                    for (Medicament m : get()) {
                        List<StockMedicament> lots = new java.util.ArrayList<>();
                        try { lots = stockBD.getStocksParExpiration(m.getRefMedicament()); } catch (Exception ignored) {}
                        int total = lots.stream().mapToInt(StockMedicament::getQuantiteProduit).sum();
                        boolean hasPerime = lots.stream().anyMatch(StockMedicament::estPerime);
                        boolean hasAlerte = lots.stream().anyMatch(StockMedicament::Alerte);
                        String statut = total == 0 ? "Rupture" : hasPerime ? "Périmé" : hasAlerte ? "Alerte" : "Normal";
                        tableModel.addRow(new Object[]{
                            m.getRefMedicament(), m.getNom(),
                            m.getDescriptio() != null ? m.getDescriptio() : "—",
                            lots.size() + " lot(s)", total + " unités", statut
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void supprimer() {
        int row = table.getSelectedRow();
        if (row < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez un médicament."); return; }
        int ref    = (Integer) tableModel.getValueAt(row, 0);
        String nom = (String)  tableModel.getValueAt(row, 1);
        String stockInfo = (String) tableModel.getValueAt(row, 4);
        if (!PharmTheme.showConfirm(this, "Confirmer suppression",
            "Supprimer «" + nom + "» (réf. #" + ref + ") ?\n" +
            "Stock associé : " + stockInfo + "\nCette action est irréversible.")) return;
        try {
            gestionProduit.supprimerMedicament(ref);
            PharmTheme.showSuccess(this, "Supprimé", "Médicament «" + nom + "» et ses lots supprimés.");
            loadMedicaments();
        } catch (IllegalStateException ex) {
            PharmTheme.showError(this, "Suppression impossible", ex.getMessage());
        } catch (ProduitNonTrouveException ex) {
            PharmTheme.showError(this, "Introuvable", "Médicament réf. #" + ref + " introuvable.");
        } catch (SQLException ex) {
            PharmTheme.showError(this, "Erreur BD", ex.getMessage());
        }
    }
}