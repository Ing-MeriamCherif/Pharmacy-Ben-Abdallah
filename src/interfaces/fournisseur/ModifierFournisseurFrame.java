package interfaces.fournisseur;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class ModifierFournisseurFrame extends PharmBaseFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtNom, txtAdresse, txtTelephone, txtEmail;
    private JSpinner spnRate;
    private JLabel lblNum;
    private int currentId = -1;
    private final FournisseurBD fournisseurBD = new FournisseurBD();

    public ModifierFournisseurFrame() {
        super("Modifier un fournisseur", "Mettre à jour les informations", 1000, 580);
        buildUI(); load();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton cancel = PharmTheme.ghostButton("Annuler"); cancel.addActionListener(e -> dispose());
        JButton save = PharmTheme.primaryButton("Enregistrer →"); save.addActionListener(e -> save());
        p.add(cancel); p.add(save);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(12, 0));

        tableModel = new DefaultTableModel(new String[]{"N°", "Nom", "Téléphone", "Email", "Évaluation"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadSelected(); });
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel right = PharmTheme.card(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(320, 0));
        right.setBorder(BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0), BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        lblNum = PharmTheme.helperLabel("Sélectionnez un fournisseur"); lblNum.setAlignmentX(LEFT_ALIGNMENT); right.add(lblNum); right.add(Box.createVerticalStrut(12));
        addFormSection(right, "Informations");
        txtNom       = addF(right, "Nom *", "");
        txtAdresse   = addF(right, "Adresse *", "");
        txtTelephone = addF(right, "Téléphone *", "");
        txtEmail     = addF(right, "Email *", "");
        JLabel rl = PharmTheme.formLabel("Évaluation (0 – 5)"); rl.setAlignmentX(LEFT_ALIGNMENT); right.add(rl); right.add(Box.createVerticalStrut(4));
        spnRate = PharmTheme.spinner(3, 0, 5, 1); spnRate.setAlignmentX(LEFT_ALIGNMENT); spnRate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); right.add(spnRate);
        contentArea.add(right, BorderLayout.EAST);
    }

    private JTextField addF(JPanel p, String label, String def) {
        JLabel l = PharmTheme.formLabel(label); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(4));
        JTextField f = PharmTheme.textField(def); f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); f.setAlignmentX(LEFT_ALIGNMENT); p.add(f); p.add(Box.createVerticalStrut(10));
        return f;
    }

    private void load() {
        tableModel.setRowCount(0);
        try { for (Fournisseur f : fournisseurBD.listerTous()) tableModel.addRow(new Object[]{ f.getNumFournisseur(), f.getNomFournisseur(), f.getTelephone(), f.getAdresseEmail(), String.format("%.1f/5", f.getRate()) }); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void loadSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        currentId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Fournisseur f = fournisseurBD.rechercherParId(currentId);
            if (f == null) return;
            lblNum.setText("N° fournisseur : " + f.getNumFournisseur());
            txtNom.setText(f.getNomFournisseur()); txtAdresse.setText(f.getAdresse() != null ? f.getAdresse() : "");
            txtTelephone.setText(f.getTelephone()); txtEmail.setText(f.getAdresseEmail());
            spnRate.setValue((int) f.getRate());
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void save() {
        if (currentId < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez un fournisseur."); return; }
        if (txtNom.getText().isBlank() || txtTelephone.getText().isBlank() || txtEmail.getText().isBlank()) { PharmTheme.showWarning(this, "Champs manquants", "Remplissez les champs obligatoires."); return; }
        try {
            Fournisseur f = new Fournisseur();
            f.setNumFournisseur(currentId); f.setNomFournisseur(txtNom.getText().trim()); f.setAdresse(txtAdresse.getText().trim());
            f.setTelephone(txtTelephone.getText().trim()); f.setAdresseEmail(txtEmail.getText().trim()); f.setRate((double)(int)spnRate.getValue());
            fournisseurBD.modifier(f);
            PharmTheme.showSuccess(this, "Modifié", f.getNomFournisseur() + " mis à jour."); load();
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}
