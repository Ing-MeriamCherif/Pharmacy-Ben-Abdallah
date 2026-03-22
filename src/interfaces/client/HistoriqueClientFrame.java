package interfaces.client;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import entite.*;
import entitebd.*;
import gestion.GestionVente;

public class HistoriqueClientFrame extends PharmBaseFrame {
    private JTextField txtCodeCnam;
    private JLabel lblInfo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtDetails;
    private JLabel lblStats;
    private Client clientActuel;
    private final ClientBD clientBD = new ClientBD();
    private final VenteBD venteBD = new VenteBD();
    private final VoieVenteBD voieVenteBD = new VoieVenteBD();
    private final MedicamentBD medicamentBD = new MedicamentBD();
    private final GestionVente gestionVente = new GestionVente();

    public HistoriqueClientFrame() {
        super("Historique d'achats", "Consulter les achats d'un client", 1100, 680);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); searchBar.setBackground(PharmTheme.BG);
        searchBar.add(PharmTheme.formLabel("Code CNAM :"));
        txtCodeCnam = PharmTheme.textField("Ex: CN-001"); txtCodeCnam.setPreferredSize(new Dimension(180, 34)); txtCodeCnam.addActionListener(e -> rechercher());
        JButton btn = PharmTheme.primaryButton("Chercher →"); btn.addActionListener(e -> rechercher());
        lblInfo = PharmTheme.helperLabel("Saisissez le code CNAM du client");
        searchBar.add(txtCodeCnam); searchBar.add(btn); searchBar.add(lblInfo);
        contentArea.add(searchBar, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT); split.setDividerLocation(340); split.setBorder(BorderFactory.createEmptyBorder());

        JPanel topPanel = new JPanel(new BorderLayout(0, 4)); topPanel.setBackground(PharmTheme.BG);
        tableModel = new DefaultTableModel(new String[]{"N° Vente", "Date", "Montant (DT)", "Nb produits", "Points gagnés"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showDetails(); });
        topPanel.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);
        lblStats = PharmTheme.helperLabel("—"); topPanel.add(lblStats, BorderLayout.SOUTH);
        split.setTopComponent(topPanel);

        JPanel botPanel = PharmTheme.card(); botPanel.setLayout(new BorderLayout());
        botPanel.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0));
        txtDetails = PharmTheme.textArea(8, 0); txtDetails.setEditable(false); txtDetails.setFont(PharmTheme.FONT_MONO); txtDetails.setText("Sélectionnez un achat pour voir les détails…");
        botPanel.add(PharmTheme.scrollTextArea(txtDetails), BorderLayout.CENTER);
        split.setBottomComponent(botPanel);

        contentArea.add(split, BorderLayout.CENTER);
    }

    private void rechercher() {
        String code = txtCodeCnam.getText().trim(); if (code.isEmpty()) return;
        try {
            clientActuel = clientBD.rechercherParCodeCnam(code);
            if (clientActuel == null) { PharmTheme.showWarning(this, "Introuvable", "Aucun client avec ce code CNAM."); lblInfo.setText("Client introuvable"); return; }
            lblInfo.setText(clientActuel.getPrenom() + " " + clientActuel.getNom() + "  |  ⭐ " + clientActuel.getPointFidelite() + " points");
            tableModel.setRowCount(0);
            ArrayList<Vente> achats = gestionVente.obtenirHistoriqueClient(clientActuel.getNumClient());
            double totalDepense = 0;
            for (Vente v : achats) {
                ArrayList<VoieVente> lignes = voieVenteBD.getLignesParVente(v.getNumVente());
                int nbProd = lignes.stream().mapToInt(VoieVente::getQuantite).sum();
                int pts = (int)(v.getMontantTotalVente() / 10);
                totalDepense += v.getMontantTotalVente();
                tableModel.addRow(new Object[]{ v.getNumVente(), v.getDateVente(), String.format("%.2f", v.getMontantTotalVente()), nbProd, pts });
            }
            lblStats.setText(String.format("Total : %d achats  |  Dépensé : %.2f DT  |  Moyenne : %.2f DT", achats.size(), totalDepense, achats.size() > 0 ? totalDepense/achats.size() : 0));
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void showDetails() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int numVente = (Integer) tableModel.getValueAt(row, 0);
        try {
            Vente v = venteBD.getVenteById(numVente);
            ArrayList<VoieVente> lignes = voieVenteBD.getLignesParVente(numVente);
            if (v == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("  DÉTAILS — Vente #").append(numVente).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            sb.append("Date         : ").append(v.getDateVente()).append("\n");
            sb.append("Retour avant : ").append(v.getDateLimRendreProduit()).append("\n\n");
            sb.append("PRODUITS :\n");
            for (VoieVente lv : lignes) {
                try { Medicament m = medicamentBD.rechercherParRef(lv.getRefMedicament()); sb.append("  • ").append(m != null ? m.getNom() : "Réf. #" + lv.getRefMedicament()); }
                catch (Exception ignored) { sb.append("  • Médicament #").append(lv.getRefMedicament()); }
                sb.append("\n    Qté : ").append(lv.getQuantite()).append("  |  PU : ").append(String.format("%.2f DT", lv.getPrixUnitaire())).append("  |  Total : ").append(String.format("%.2f DT", lv.getPrixTotalVoieVente())).append("\n");
            }
            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("TOTAL : ").append(String.format("%.2f DT", v.getMontantTotalVente())).append("\n");
            sb.append("Points gagnés : ").append((int)(v.getMontantTotalVente()/10)).append("\n");
            txtDetails.setText(sb.toString()); txtDetails.setCaretPosition(0);
        } catch (SQLException ex) { txtDetails.setText("Erreur : " + ex.getMessage()); }
    }
}
