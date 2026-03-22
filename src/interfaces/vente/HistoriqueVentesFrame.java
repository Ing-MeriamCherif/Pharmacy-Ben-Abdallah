package interfaces.vente;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import entite.*;
import entitebd.*;
import gestion.GestionVente;

public class HistoriqueVentesFrame extends PharmBaseFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtDetails;
    private final VenteBD venteBD = new VenteBD();
    private final VoieVenteBD voieVenteBD = new VoieVenteBD();
    private final ClientBD clientBD = new ClientBD();
    private final MedicamentBD medicamentBD = new MedicamentBD();

    public HistoriqueVentesFrame() {
        super("Historique des ventes", "Toutes les transactions enregistrées", 1150, 660);
        buildUI(); load();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> load());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(refresh); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(12, 0));

        tableModel = new DefaultTableModel(new String[]{"N° Vente", "Date", "Client", "Employé", "Montant (DT)", "Date retour"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showDetails(); });
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel right = PharmTheme.card(); right.setLayout(new BorderLayout()); right.setPreferredSize(new Dimension(290, 0));
        right.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0));
        txtDetails = PharmTheme.textArea(20, 0); txtDetails.setEditable(false); txtDetails.setFont(PharmTheme.FONT_MONO); txtDetails.setText("Sélectionnez une vente…");
        right.add(PharmTheme.scrollTextArea(txtDetails), BorderLayout.CENTER);
        contentArea.add(right, BorderLayout.EAST);
    }

    private void load() {
        tableModel.setRowCount(0);
        new SwingWorker<ArrayList<Vente>,Void>() {
            @Override protected ArrayList<Vente> doInBackground() throws Exception { return venteBD.getAllVentes(); }
            @Override protected void done() {
                try { for (Vente v : get()) {
                    String nomClient = "Vente directe";
                    try { if (v.getNumClient() > 0) { Client c = clientBD.rechercherParId(v.getNumClient()); if (c != null) nomClient = c.getNom() + " " + c.getPrenom(); } } catch (Exception ignored) {}
                    tableModel.addRow(new Object[]{ v.getNumVente(), v.getDateVente(), nomClient, "Emp. #" + v.getNumCarteEmp(), String.format("%.2f", v.getMontantTotalVente()), v.getDateLimRendreProduit() });
                }} catch (Exception ex) { PharmTheme.showError(HistoriqueVentesFrame.this, "Erreur", ex.getMessage()); }
            }
        }.execute();
    }

    private void showDetails() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int numVente = (Integer) tableModel.getValueAt(row, 0);
        try {
            Vente v = venteBD.getVenteById(numVente);
            ArrayList<VoieVente> lignes = voieVenteBD.getLignesParVente(numVente);
            if (v == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━\n  Vente #").append(numVente).append("\n━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            sb.append("Date    : ").append(v.getDateVente()).append("\nRetour  : ").append(v.getDateLimRendreProduit()).append("\n\nPRODUITS :\n");
            for (VoieVente lv : lignes) {
                try { Medicament m = medicamentBD.rechercherParRef(lv.getRefMedicament()); sb.append("  • ").append(m != null ? m.getNom() : "Réf. #" + lv.getRefMedicament()); } catch (Exception ignored) { sb.append("  • Réf. #").append(lv.getRefMedicament()); }
                sb.append("\n    x").append(lv.getQuantite()).append(" × ").append(String.format("%.2f DT", lv.getPrixUnitaire())).append(" = ").append(String.format("%.2f DT", lv.getPrixTotalVoieVente())).append("\n");
            }
            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━\nTOTAL : ").append(String.format("%.2f DT", v.getMontantTotalVente())).append("\n");
            txtDetails.setText(sb.toString()); txtDetails.setCaretPosition(0);
        } catch (SQLException ex) { txtDetails.setText("Erreur : " + ex.getMessage()); }
    }
}
