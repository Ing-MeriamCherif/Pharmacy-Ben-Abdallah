package interfaces.rapport;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class PerformanceFournisseursFrame extends PharmBaseFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtAnalyse;
    private final FournisseurBD fournisseurBD = new FournisseurBD();

    public PerformanceFournisseursFrame() {
        super("Performance fournisseurs", "Classement et analyse des partenaires", 1000, 660);
        buildUI(); load();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> load());
        JButton export = PharmTheme.primaryButton("Exporter →"); export.addActionListener(e -> exporter());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(refresh); p.add(export); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(12, 0));

        tableModel = new DefaultTableModel(new String[]{"Rang", "N°", "Nom", "Évaluation", "Performance", "Statut"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        table.getColumnModel().getColumn(5).setCellRenderer((t, v, s, f, r, c) -> {
            String sv = String.valueOf(v);
            PharmTheme.BadgeType bt = sv.contains("Excellent") ? PharmTheme.BadgeType.SUCCESS : sv.contains("Bon") ? PharmTheme.BadgeType.INFO : sv.contains("Faible") ? PharmTheme.BadgeType.DANGER : PharmTheme.BadgeType.WARN;
            return PharmTheme.badgeComponent(sv, bt);
        });
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel right = PharmTheme.card(); right.setLayout(new BorderLayout()); right.setPreferredSize(new Dimension(300, 0));
        right.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0));
        txtAnalyse = PharmTheme.textArea(20, 0); txtAnalyse.setEditable(false); txtAnalyse.setFont(PharmTheme.FONT_MONO); txtAnalyse.setText("Chargement…");
        right.add(PharmTheme.scrollTextArea(txtAnalyse), BorderLayout.CENTER);
        contentArea.add(right, BorderLayout.EAST);
    }

    private void load() {
        tableModel.setRowCount(0);
        new SwingWorker<List<Fournisseur>,Void>() {
            @Override protected List<Fournisseur> doInBackground() throws Exception { return fournisseurBD.listerTous(); }
            @Override protected void done() {
                try {
                    List<Fournisseur> list = get();
                    list.sort((a, b) -> Double.compare(b.getRate(), a.getRate()));
                    int rang = 1; int nbExc = 0, nbBon = 0, nbMoy = 0, nbFaible = 0; double somme = 0;
                    for (Fournisseur f : list) {
                        double perf = 0; try { perf = fournisseurBD.calculerPerformance(f.getNumFournisseur()); } catch (Exception ignored) {}
                        String statut; if (f.getRate() >= 4.0) { statut = "Excellent"; nbExc++; } else if (f.getRate() >= 3.0) { statut = "Bon"; nbBon++; } else if (f.getRate() >= 2.0) { statut = "Moyen"; nbMoy++; } else { statut = "Faible"; nbFaible++; }
                        somme += f.getRate();
                        tableModel.addRow(new Object[]{ rang++, f.getNumFournisseur(), f.getNomFournisseur(), String.format("%.1f/5", f.getRate()), String.format("%.2f%%", perf), statut });
                    }
                    int total = list.size();
                    double moy = total > 0 ? somme / total : 0;
                    StringBuilder sb = new StringBuilder();
                    sb.append("════════════════════════\n  ANALYSE FOURNISSEURS\n════════════════════════\n\n");
                    sb.append("Date : ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append("\n\n");
                    sb.append("STATISTIQUES\n"); sb.append("Total     : ").append(total).append("\n"); sb.append("Moy. note : ").append(String.format("%.2f/5", moy)).append("\n\n");
                    sb.append("PAR CATÉGORIE\n"); sb.append("Excellent (≥4.0) : ").append(nbExc).append("\n"); sb.append("Bon       (≥3.0) : ").append(nbBon).append("\n"); sb.append("Moyen     (≥2.0) : ").append(nbMoy).append("\n"); sb.append("Faible    (<2.0) : ").append(nbFaible).append("\n\n");
                    sb.append("RECOMMANDATIONS\n");
                    if (nbFaible > 0) sb.append("⚠  ").append(nbFaible).append(" fournisseur(s)\n   à réévaluer ou\n   remplacer.\n\n");
                    if (nbExc > 0) sb.append("✅ ").append(nbExc).append(" excellent(s) —\n   renforcer la\n   collaboration.\n");
                    sb.append("\n════════════════════════\n");
                    txtAnalyse.setText(sb.toString()); txtAnalyse.setCaretPosition(0);
                } catch (Exception ex) { PharmTheme.showError(PerformanceFournisseursFrame.this, "Erreur", ex.getMessage()); }
            }
        }.execute();
    }

    private void exporter() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("perf_fournisseurs_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) {
            pw.println(txtAnalyse.getText()); pw.println("\nDÉTAIL DES FOURNISSEURS\n════════════════════════");
            for (int i = 0; i < tableModel.getRowCount(); i++) pw.printf("Rang %d : %s — Note : %s — Performance : %s%n", tableModel.getValueAt(i,0), tableModel.getValueAt(i,2), tableModel.getValueAt(i,3), tableModel.getValueAt(i,4));
            PharmTheme.showSuccess(this, "Exporté", fc.getSelectedFile().getName());
        } catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }
}
