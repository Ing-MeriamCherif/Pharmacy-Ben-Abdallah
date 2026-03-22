package interfaces.commande;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import entite.*;
import entitebd.*;
import gestion.GestionCommande;

public class ListerCommandesFrame extends PharmBaseFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFilter;
    private final GestionCommande gestionCommande = new GestionCommande();

    public ListerCommandesFrame() {
        super("Liste des commandes", "Historique de toutes les commandes", 1050, 600);
        buildUI(); load("Toutes");
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> load((String) cmbFilter.getSelectedItem()));
        JButton details = PharmTheme.primaryButton("Voir détails →"); details.addActionListener(e -> voirDetails());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(refresh); p.add(details); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 8));
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); filterBar.setBackground(PharmTheme.BG);
        filterBar.add(PharmTheme.formLabel("Filtrer :"));
        cmbFilter = PharmTheme.comboBox(new String[]{"Toutes", "En attente", "Reçue", "Annulée"});
        cmbFilter.addActionListener(e -> load((String) cmbFilter.getSelectedItem())); filterBar.add(cmbFilter);
        contentArea.add(filterBar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"N° Cmd", "Date achat", "Date limite", "Statut", "Fournisseur", "Employé", "Total (DT)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer((t, v, s, f, r, c) -> {
            String sv = String.valueOf(v);
            PharmTheme.BadgeType bt = sv.contains("Reçue") ? PharmTheme.BadgeType.SUCCESS : sv.contains("Annulée") ? PharmTheme.BadgeType.DANGER : PharmTheme.BadgeType.WARN;
            return PharmTheme.badgeComponent(sv, bt);
        });
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);
    }

    private void load(String filtre) {
        tableModel.setRowCount(0);
        new SwingWorker<ArrayList<Commande>,Void>() {
            @Override protected ArrayList<Commande> doInBackground() throws Exception { return gestionCommande.listerToutesCommandes(); }
            @Override protected void done() {
                try { for (Commande c : get()) { if (!"Toutes".equals(filtre) && !filtre.equals(c.getStatut())) continue; tableModel.addRow(new Object[]{ c.getNumCommande(), c.getDateAchat(), c.getDateLimRendreProduit(), c.getStatut(), c.getNumFournisseur(), c.getNumCarteEmp(), String.format("%.2f", c.getMontantTotalCommande()) }); }}
                catch (Exception ex) { PharmTheme.showError(ListerCommandesFrame.this, "Erreur", ex.getMessage()); }
            }
        }.execute();
    }

    private void voirDetails() {
        int row = table.getSelectedRow(); if (row < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez une commande."); return; }
        int num = (Integer) tableModel.getValueAt(row, 0);
        try {
            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(num);
            Commande c = bilan.getCommande();
            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n  Commande #").append(num).append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            sb.append("Date     : ").append(c.getDateAchat()).append("\nStatut   : ").append(c.getStatut()).append("\nTotal    : ").append(String.format("%.2f DT", c.getMontantTotalCommande())).append("\nLignes   : ").append(bilan.getNombreLignes()).append("\n\nDÉTAIL LIGNES :\n");
            for (VoieCommande lc : bilan.getLignes()) { sb.append("  • Méd #").append(lc.getRefMedicament()).append(" — Qté: ").append(lc.getQuantite()).append(" — PU: ").append(lc.getPrixUnitaire()).append(" — Total: ").append(String.format("%.2f DT", lc.calculerTotal())).append("\n"); }
            JOptionPane.showMessageDialog(this, sb.toString(), "Détails commande #" + num, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }
}
