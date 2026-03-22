package interfaces.stock;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import entite.*;
import entitebd.*;
import gestion.GestionStock;

public class AlertesStockFrame extends PharmBaseFrame {
    private JTable            table;
    private DefaultTableModel tableModel;
    private JLabel            lblCount, lblValeur;

    private final GestionStock gestionStock  = new GestionStock();
    private final MedicamentBD medicamentBD  = new MedicamentBD();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public AlertesStockFrame() {
        super("Alertes stock", "Produits sous seuil minimal ou périmés", 1050, 580);
        buildUI(); loadAlertes();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.primaryButton("↻ Actualiser"); refresh.addActionListener(e -> loadAlertes());
        JButton close   = PharmTheme.ghostButton("Fermer");          close.addActionListener(e -> dispose());
        p.add(refresh); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 10));

        JPanel statsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        statsRow.setBackground(PharmTheme.BG); statsRow.setPreferredSize(new Dimension(0, 66));
        lblCount  = chip(statsRow, "Lots en alerte",    "—", PharmTheme.DANGER);
        lblValeur = chip(statsRow, "Valeur totale stock","—", PharmTheme.PM_500);
        contentArea.add(statsRow, BorderLayout.NORTH);

        // Columns: Réf, Médicament, N°Lot, Quantité, Seuil, À commander, P.Achat, Coût estimé, Date exp, Statut
        tableModel = new DefaultTableModel(
            new String[]{"Réf.", "Médicament", "N° Lot", "Quantité", "Seuil", "À commander", "Prix achat", "Coût estimé", "Date exp.", "Statut"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        int[] w = {45, 180, 60, 70, 55, 80, 75, 85, 85, 90};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        table.getColumnModel().getColumn(9).setCellRenderer((t, v, s, f, r, c) -> {
            String val = String.valueOf(v);
            PharmTheme.BadgeType bt = val.contains("Périmé") || val.equals("Rupture") ? PharmTheme.BadgeType.DANGER : PharmTheme.BadgeType.WARN;
            return PharmTheme.badgeComponent(val, bt);
        });
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel warn = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        warn.setBackground(PharmTheme.DANGER_BG);
        warn.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 4, 12));
        JLabel wl = new JLabel("⚠  Ces lots nécessitent une attention immédiate (réapprovisionnement ou retrait).");
        wl.setFont(PharmTheme.FONT_SM); wl.setForeground(PharmTheme.DANGER);
        warn.add(wl);
        contentArea.add(warn, BorderLayout.SOUTH);
    }

    private JLabel chip(JPanel parent, String label, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g); g.setColor(accent); g.fillRect(0,0,3,getHeight()); }
        };
        c.setBackground(PharmTheme.CARD);
        c.setBorder(javax.swing.BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0), javax.swing.BorderFactory.createEmptyBorder(10,16,10,16)));
        JPanel inner = new JPanel(); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS)); inner.setBackground(PharmTheme.CARD);
        JLabel lbl = PharmTheme.helperLabel(label); lbl.setAlignmentX(LEFT_ALIGNMENT);
        JLabel val = new JLabel(value); val.setFont(new Font("SansSerif", Font.BOLD, 20)); val.setForeground(PharmTheme.TXT); val.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(lbl); inner.add(val); c.add(inner, BorderLayout.CENTER); parent.add(c); return val;
    }

    private void loadAlertes() {
        tableModel.setRowCount(0);
        new SwingWorker<List<StockMedicament>, Void>() {
            @Override protected List<StockMedicament> doInBackground() throws Exception {
                // Get both: lots in alert AND lots that are expired
                List<StockMedicament> alertes = gestionStock.obtenirAlertes();
                // Also add expired lots not already in alertes
                List<StockMedicament> tous = new StockBD().listerTous();
                java.util.Set<Integer> inAlert = new java.util.HashSet<>();
                for (StockMedicament s : alertes) inAlert.add(s.getNumStock());
                for (StockMedicament s : tous) {
                    if (s.estPerime() && !inAlert.contains(s.getNumStock())) { alertes.add(s); inAlert.add(s.getNumStock()); }
                }
                return alertes;
            }
            @Override protected void done() {
                try {
                    List<StockMedicament> alertes = get();
                    double valeurTotale = 0;
                    try { valeurTotale = gestionStock.calculerValeurTotaleStock(); } catch (Exception ignored) {}
                    for (StockMedicament s : alertes) {
                        String nom = "Médicament #" + s.getRefMedicament();
                        try { Medicament m = medicamentBD.rechercherParRef(s.getRefMedicament()); if (m != null) nom = m.getNom(); } catch (Exception ignored) {}
                        int aCommander = Math.max(0, (s.getSeuilMin() * 2) - s.getQuantiteProduit());
                        String statut = s.estPerime() ? "Périmé" : s.getQuantiteProduit() == 0 ? "Rupture" : "Stock faible";
                        String dateExp = s.getDateExpiration() != null ? df.format(s.getDateExpiration()) : "—";
                        tableModel.addRow(new Object[]{
                            s.getRefMedicament(), nom, s.getNumStock(),
                            s.getQuantiteProduit(), s.getSeuilMin(), aCommander,
                            String.format("%.2f DT", s.getPrixAchat()),
                            String.format("%.2f DT", aCommander * s.getPrixAchat()),
                            dateExp, statut
                        });
                    }
                    lblCount.setText(String.valueOf(alertes.size()));
                    lblCount.setForeground(alertes.isEmpty() ? PharmTheme.SUCCESS : PharmTheme.DANGER);
                    lblValeur.setText(String.format("%.0f DT", valeurTotale));
                } catch (Exception ex) { PharmTheme.showError(AlertesStockFrame.this, "Erreur", ex.getMessage()); }
            }
        }.execute();
    }
}