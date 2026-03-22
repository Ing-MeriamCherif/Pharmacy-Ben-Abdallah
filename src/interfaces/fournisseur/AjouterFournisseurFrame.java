package interfaces.fournisseur;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class AjouterFournisseurFrame extends PharmBaseFrame {
    private JTextField txtNum, txtNom, txtAdresse, txtTelephone, txtEmail;
    private JSpinner spnRate;
    private final FournisseurBD fournisseurBD = new FournisseurBD();

    public AjouterFournisseurFrame() {
        super("Ajouter un fournisseur", "Nouveau partenaire commercial", 680, 500);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton cancel = PharmTheme.ghostButton("Annuler"); cancel.addActionListener(e -> dispose());
        JButton save = PharmTheme.primaryButton("Enregistrer →"); save.addActionListener(e -> save());
        p.add(cancel); p.add(save);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout());
        JPanel form = new JPanel(); form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(PharmTheme.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(20, 22, 20, 22)));

        addFormSection(form, "Informations fournisseur");
        txtNum     = addF(form, "Numéro (vide = auto)", "");
        txtNom     = addF(form, "Nom *", "");
        txtAdresse = addF(form, "Adresse *", "");
        txtTelephone = addF(form, "Téléphone *", "");
        txtEmail   = addF(form, "Email *", "");

        JLabel rl = PharmTheme.formLabel("Évaluation (0 – 5)"); rl.setAlignmentX(LEFT_ALIGNMENT); form.add(rl); form.add(Box.createVerticalStrut(4));
        spnRate = PharmTheme.spinner(3, 0, 5, 1); spnRate.setAlignmentX(LEFT_ALIGNMENT); spnRate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); form.add(spnRate); form.add(Box.createVerticalStrut(10));

        JLabel note = PharmTheme.helperLabel("* Champs obligatoires"); note.setAlignmentX(LEFT_ALIGNMENT); form.add(note);
        contentArea.add(form, BorderLayout.CENTER);
    }

    private JTextField addF(JPanel p, String label, String def) {
        JLabel l = PharmTheme.formLabel(label); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(4));
        JTextField f = PharmTheme.textField(def); f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); f.setAlignmentX(LEFT_ALIGNMENT); p.add(f); p.add(Box.createVerticalStrut(10));
        return f;
    }

    private void save() {
        if (txtNom.getText().isBlank() || txtAdresse.getText().isBlank() || txtTelephone.getText().isBlank() || txtEmail.getText().isBlank()) {
            PharmTheme.showWarning(this, "Champs manquants", "Remplissez tous les champs obligatoires (*)."); return;
        }
        if (!txtEmail.getText().contains("@")) { PharmTheme.showError(this, "Email invalide", "L'email doit contenir @."); return; }
        try {
            int num = 0;
            if (!txtNum.getText().isBlank()) num = Integer.parseInt(txtNum.getText().trim());
            Fournisseur f = new Fournisseur();
            f.setNumFournisseur(num); f.setNomFournisseur(txtNom.getText().trim()); f.setAdresse(txtAdresse.getText().trim());
            f.setTelephone(txtTelephone.getText().trim()); f.setAdresseEmail(txtEmail.getText().trim()); f.setRate((double)(int)spnRate.getValue());
            int result = fournisseurBD.ajouter(f);
            PharmTheme.showSuccess(this, "Fournisseur ajouté", "N° fournisseur : " + result + "\n" + f.getNomFournisseur());
            dispose();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Invalide", "Le numéro doit être un entier."); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}
