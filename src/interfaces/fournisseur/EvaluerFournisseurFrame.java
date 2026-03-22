package interfaces.fournisseur;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class EvaluerFournisseurFrame extends PharmBaseFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JSlider sliderRate;
    private JLabel lblRateValue, lblPerf, lblNom;
    private JTextArea txtCommentaire;
    private int currentId = -1;
    private final FournisseurBD fournisseurBD = new FournisseurBD();

    public EvaluerFournisseurFrame() {
        super("Évaluer un fournisseur", "Attribuer une note de performance", 1000, 600);
        buildUI(); load();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> load());
        JButton save = PharmTheme.primaryButton("Enregistrer →"); save.addActionListener(e -> sauvegarder());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(refresh); p.add(save); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(12, 0));

        // Table
        tableModel = new DefaultTableModel(new String[]{"N°", "Nom", "Téléphone", "Évaluation", "Performance"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadSelected(); });
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        // Right panel
        JPanel right = PharmTheme.card(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(310, 0));
        right.setBorder(BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0), BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        addFormSection(right, "Évaluation");
        lblNom = PharmTheme.helperLabel("Sélectionnez un fournisseur"); lblNom.setAlignmentX(LEFT_ALIGNMENT); right.add(lblNom); right.add(Box.createVerticalStrut(12));

        sliderRate = new JSlider(0, 50, 25); sliderRate.setBackground(PharmTheme.CARD); sliderRate.setAlignmentX(LEFT_ALIGNMENT); sliderRate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        sliderRate.addChangeListener(e -> updateRateDisplay()); right.add(sliderRate); right.add(Box.createVerticalStrut(8));

        lblRateValue = new JLabel("⭐ 2.5 / 5.0"); lblRateValue.setFont(new Font("SansSerif", Font.BOLD, 22)); lblRateValue.setForeground(PharmTheme.WARN); lblRateValue.setAlignmentX(CENTER_ALIGNMENT); right.add(lblRateValue); right.add(Box.createVerticalStrut(6));
        lblPerf = PharmTheme.helperLabel("Performance : —"); lblPerf.setAlignmentX(CENTER_ALIGNMENT); right.add(lblPerf); right.add(Box.createVerticalStrut(14));

        JLabel cl = PharmTheme.formLabel("Commentaire (optionnel)"); cl.setAlignmentX(LEFT_ALIGNMENT); right.add(cl); right.add(Box.createVerticalStrut(4));
        txtCommentaire = PharmTheme.textArea(4, 0);
        JScrollPane csp = PharmTheme.scrollTextArea(txtCommentaire); csp.setAlignmentX(LEFT_ALIGNMENT); csp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); right.add(csp);

        contentArea.add(right, BorderLayout.EAST);
    }

    private void load() {
        tableModel.setRowCount(0);
        try {
            for (Fournisseur f : fournisseurBD.listerTous()) {
                double perf = fournisseurBD.calculerPerformance(f.getNumFournisseur());
                tableModel.addRow(new Object[]{ f.getNumFournisseur(), f.getNomFournisseur(), f.getTelephone(), String.format("%.1f/5", f.getRate()), String.format("%.2f%%", perf) });
            }
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void loadSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        currentId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Fournisseur f = fournisseurBD.rechercherParId(currentId);
            if (f == null) return;
            lblNom.setText(f.getNomFournisseur() + " (#" + f.getNumFournisseur() + ")");
            sliderRate.setValue((int)(f.getRate() * 10));
            double perf = fournisseurBD.calculerPerformance(currentId);
            lblPerf.setText(String.format("Performance : %.2f%%", perf));
            txtCommentaire.setText("");
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }

    private void updateRateDisplay() {
        double rate = sliderRate.getValue() / 10.0;
        String stars = "⭐".repeat(Math.max(1, (int) Math.round(rate)));
        lblRateValue.setText(String.format("%s  %.1f / 5.0", stars, rate));
        lblRateValue.setForeground(rate >= 4.0 ? PharmTheme.SUCCESS : rate >= 2.5 ? PharmTheme.WARN : PharmTheme.DANGER);
    }

    private void sauvegarder() {
        if (currentId < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez d'abord un fournisseur."); return; }
        try {
            Fournisseur f = fournisseurBD.rechercherParId(currentId);
            if (f == null) return;
            double newRate = sliderRate.getValue() / 10.0;
            f.setRate(newRate);
            fournisseurBD.modifier(f);
            PharmTheme.showSuccess(this, "Évaluation enregistrée", f.getNomFournisseur() + "\nNote : " + String.format("%.1f/5", newRate));
            load();
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}
