package interfaces.employe;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.*;
import java.util.Date;
import entite.*;
import gestion.GestionEmploye;

public class AjouterEmployeFrame extends PharmBaseFrame {
    private JTextField txtNumCarte, txtNom, txtPrenom, txtAge, txtAdresse, txtEmail, txtTelephone;
    private JTextField txtCNSS, txtSalaire, txtMotDePasse, txtDateRecrutement, txtHeureDebut, txtHeureFin;
    private JSpinner spnJours;
    private JComboBox<String> cmbPoste;
    private JCheckBox chkCV;
    private JTextField txtDiplome, txtExperience;
    private JTextArea txtFormation, txtStage;
    private final GestionEmploye gestionEmploye = new GestionEmploye();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public AjouterEmployeFrame() {
        super("Ajouter un employé", "Nouveau dossier employé", 900, 700);
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
        form.setBorder(BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0), BorderFactory.createEmptyBorder(20, 22, 20, 22)));

        addFormSection(form, "Informations personnelles");
        txtNumCarte  = addF(form, "N° Carte Identité *", "");
        txtNom       = addF(form, "Nom *", "");
        txtPrenom    = addF(form, "Prénom *", "");
        txtAge       = addF(form, "Âge *", "");
        txtAdresse   = addF(form, "Adresse", "");
        txtEmail     = addF(form, "Email", "");
        txtTelephone = addF(form, "Téléphone", "");

        addFormSection(form, "Informations professionnelles");
        txtCNSS   = addF(form, "N° CNSS *", "");
        cmbPoste  = new JComboBox<>(new String[]{"Pharmacien", "Assistant", "Administrateur", "Caissier", "Préparateur"}); styleCombo(form, cmbPoste, "Poste *");
        txtSalaire       = addF(form, "Salaire (DT) *", "");
        txtMotDePasse    = addF(form, "Mot de passe * (min 4 car.)", "");
        txtDateRecrutement = addF(form, "Date recrutement (jj/mm/aaaa) *", df.format(new Date()));
        txtHeureDebut    = addF(form, "Heure début (HH:MM)", "08:00");
        txtHeureFin      = addF(form, "Heure fin (HH:MM)", "17:00");
        JLabel jl = PharmTheme.formLabel("Jours / semaine *"); jl.setAlignmentX(LEFT_ALIGNMENT); form.add(jl); form.add(Box.createVerticalStrut(4));
        spnJours = PharmTheme.spinner(5, 1, 7, 1); spnJours.setAlignmentX(LEFT_ALIGNMENT); spnJours.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); form.add(spnJours); form.add(Box.createVerticalStrut(10));

        addFormSection(form, "CV (optionnel)");
        chkCV = new JCheckBox("Ajouter un CV"); chkCV.setBackground(PharmTheme.CARD); chkCV.setFont(PharmTheme.FONT_BODY); chkCV.setAlignmentX(LEFT_ALIGNMENT);
        chkCV.addActionListener(e -> { txtDiplome.setEnabled(chkCV.isSelected()); txtExperience.setEnabled(chkCV.isSelected()); txtFormation.setEnabled(chkCV.isSelected()); txtStage.setEnabled(chkCV.isSelected()); });
        form.add(chkCV); form.add(Box.createVerticalStrut(8));
        txtDiplome    = addF(form, "Diplôme", ""); txtDiplome.setEnabled(false);
        txtExperience = addF(form, "Années d'expérience", "0"); txtExperience.setEnabled(false);
        JLabel fl = PharmTheme.formLabel("Formation"); fl.setAlignmentX(LEFT_ALIGNMENT); form.add(fl); form.add(Box.createVerticalStrut(4));
        txtFormation = PharmTheme.textArea(2, 0); txtFormation.setEnabled(false); JScrollPane fsp = PharmTheme.scrollTextArea(txtFormation); fsp.setAlignmentX(LEFT_ALIGNMENT); fsp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); form.add(fsp); form.add(Box.createVerticalStrut(10));
        JLabel sl = PharmTheme.formLabel("Stage"); sl.setAlignmentX(LEFT_ALIGNMENT); form.add(sl); form.add(Box.createVerticalStrut(4));
        txtStage = PharmTheme.textArea(2, 0); txtStage.setEnabled(false); JScrollPane ssp = PharmTheme.scrollTextArea(txtStage); ssp.setAlignmentX(LEFT_ALIGNMENT); ssp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); form.add(ssp);

        JScrollPane sp = new JScrollPane(form); sp.setBorder(BorderFactory.createEmptyBorder()); sp.setBackground(PharmTheme.BG); PharmTheme.styleScrollBar(sp.getVerticalScrollBar());
        contentArea.add(sp, BorderLayout.CENTER);
    }

    private JTextField addF(JPanel p, String label, String def) {
        JLabel l = PharmTheme.formLabel(label); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(4));
        JTextField f = PharmTheme.textField(def); f.setText(def); f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); f.setAlignmentX(LEFT_ALIGNMENT); p.add(f); p.add(Box.createVerticalStrut(10));
        return f;
    }

    private void styleCombo(JPanel p, JComboBox<?> cb, String label) {
        JLabel l = PharmTheme.formLabel(label); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(4));
        cb.setFont(PharmTheme.FONT_BODY); cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); cb.setAlignmentX(LEFT_ALIGNMENT); p.add(cb); p.add(Box.createVerticalStrut(10));
    }

    private void save() {
        if (txtNumCarte.getText().isBlank() || txtNom.getText().isBlank() || txtPrenom.getText().isBlank() || txtCNSS.getText().isBlank() || txtSalaire.getText().isBlank() || txtMotDePasse.getText().isBlank()) {
            PharmTheme.showWarning(this, "Champs manquants", "Remplissez tous les champs obligatoires (*)."); return;
        }
        try {
            Employe emp = new Employe();
            emp.setNumCarteIdentite(Integer.parseInt(txtNumCarte.getText().trim()));
            emp.setNom(txtNom.getText().trim()); emp.setPrenom(txtPrenom.getText().trim());
            emp.setAge(Integer.parseInt(txtAge.getText().trim())); emp.setAdresse(txtAdresse.getText().trim());
            emp.setAdresseMail(txtEmail.getText().trim()); emp.setTelephone(txtTelephone.getText().trim());
            emp.setNumCNSS(Integer.parseInt(txtCNSS.getText().trim())); emp.setPoste((String) cmbPoste.getSelectedItem());
            emp.setSalaire(Double.parseDouble(txtSalaire.getText().trim())); emp.setMotDePasse(txtMotDePasse.getText().trim());
            emp.setDateRejoindTravail(df.parse(txtDateRecrutement.getText().trim()));
            emp.setHeureDebutTravail(txtHeureDebut.getText().trim()); emp.setHeureSortie(txtHeureFin.getText().trim());
            emp.setNbJoursParSemaine((Integer) spnJours.getValue());
            CvEmployee cv = null;
            if (chkCV.isSelected()) {
                cv = new CvEmployee(); cv.setDiplome(txtDiplome.getText().trim()); cv.setNbAnneeExperience(Integer.parseInt(txtExperience.getText().trim())); cv.setFormation(txtFormation.getText().trim()); cv.setStage(txtStage.getText().trim());
            }
            int id = gestionEmploye.ajouterEmployeComplet(emp, cv);
            PharmTheme.showSuccess(this, "Employé ajouté", "Carte employé : #" + id + "\n" + emp.getPrenom() + " " + emp.getNom() + " — " + emp.getPoste());
            dispose();
        } catch (ParseException ex) { PharmTheme.showError(this, "Date invalide", "Format : jj/mm/aaaa"); }
        catch (NumberFormatException ex) { PharmTheme.showError(this, "Saisie invalide", "Vérifiez les champs numériques."); }
        catch (IllegalArgumentException ex) { PharmTheme.showError(this, "Validation", ex.getMessage()); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}
