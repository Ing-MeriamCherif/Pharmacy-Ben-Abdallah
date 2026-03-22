package interfaces.employe;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.*;
import entitebd.EmployeBD;
import gestion.GestionEmploye;

public class GererCVFrame extends PharmBaseFrame {
    private final int numCarteEmp;
    private CvEmployee cv;
    private JTextField txtDiplome, txtExperience;
    private JTextArea txtFormation, txtStage;
    private final GestionEmploye gestionEmploye = new GestionEmploye();
    private final EmployeBD employeBD = new EmployeBD();

    public GererCVFrame(int numCarteEmp) {
        super("Gérer le CV", "Curriculum vitae de l'employé", 640, 500);
        this.numCarteEmp = numCarteEmp;
        buildUI();
        loadCV();
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

        addFormSection(form, "Curriculum vitae");

        JLabel dl = PharmTheme.formLabel("Diplôme"); dl.setAlignmentX(LEFT_ALIGNMENT); form.add(dl); form.add(Box.createVerticalStrut(4));
        txtDiplome = PharmTheme.textField(""); txtDiplome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); txtDiplome.setAlignmentX(LEFT_ALIGNMENT); form.add(txtDiplome); form.add(Box.createVerticalStrut(10));

        JLabel el = PharmTheme.formLabel("Années d'expérience"); el.setAlignmentX(LEFT_ALIGNMENT); form.add(el); form.add(Box.createVerticalStrut(4));
        txtExperience = PharmTheme.textField("0"); txtExperience.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); txtExperience.setAlignmentX(LEFT_ALIGNMENT); form.add(txtExperience); form.add(Box.createVerticalStrut(10));

        JLabel fl = PharmTheme.formLabel("Formation"); fl.setAlignmentX(LEFT_ALIGNMENT); form.add(fl); form.add(Box.createVerticalStrut(4));
        txtFormation = PharmTheme.textArea(3, 0);
        JScrollPane fsp = PharmTheme.scrollTextArea(txtFormation); fsp.setAlignmentX(LEFT_ALIGNMENT); fsp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); form.add(fsp); form.add(Box.createVerticalStrut(10));

        JLabel sl = PharmTheme.formLabel("Stage(s)"); sl.setAlignmentX(LEFT_ALIGNMENT); form.add(sl); form.add(Box.createVerticalStrut(4));
        txtStage = PharmTheme.textArea(3, 0);
        JScrollPane ssp = PharmTheme.scrollTextArea(txtStage); ssp.setAlignmentX(LEFT_ALIGNMENT); ssp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); form.add(ssp);

        JScrollPane sp = new JScrollPane(form); sp.setBorder(BorderFactory.createEmptyBorder()); sp.setBackground(PharmTheme.BG); PharmTheme.styleScrollBar(sp.getVerticalScrollBar());
        contentArea.add(sp, BorderLayout.CENTER);
    }

    private void loadCV() {
        try {
            Employe emp = employeBD.rechercherParId(numCarteEmp);
            if (emp != null) setFrameTitle("Gérer le CV", emp.getPrenom() + " " + emp.getNom() + " — Carte #" + numCarteEmp);
            cv = gestionEmploye.obtenirCV(numCarteEmp);
            if (cv != null) {
                txtDiplome.setText(cv.getDiplome() != null ? cv.getDiplome() : "");
                txtExperience.setText(String.valueOf(cv.getNbAnneeExperience()));
                txtFormation.setText(cv.getFormation() != null ? cv.getFormation() : "");
                txtStage.setText(cv.getStage() != null ? cv.getStage() : "");
            } else {
                cv = new CvEmployee(); cv.setNumEmployee(numCarteEmp);
            }
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void save() {
        try {
            int exp = Integer.parseInt(txtExperience.getText().trim());
            cv.setDiplome(txtDiplome.getText().trim());
            cv.setNbAnneeExperience(exp);
            cv.setFormation(txtFormation.getText().trim());
            cv.setStage(txtStage.getText().trim());
            gestionEmploye.ajouterOuModifierCV(numCarteEmp, cv);
            PharmTheme.showSuccess(this, "CV enregistré", "CV mis à jour avec succès.");
            dispose();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Invalide", "L'expérience doit être un entier."); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}
