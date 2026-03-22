package interfaces.employe;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.*;
import entite.*;
import entitebd.EmployeBD;
import gestion.GestionEmploye;

public class ModifierEmployeFrame extends PharmBaseFrame {
    private final int numCarteEmp;
    private Employe employe;
    private JTextField txtNom, txtPrenom, txtAge, txtAdresse, txtEmail, txtTelephone;
    private JTextField txtSalaire, txtHeureDebut, txtHeureFin;
    private JPasswordField txtMotDePasse;
    private JSpinner spnJours;
    private JComboBox<String> cmbPoste;
    private JLabel lblInfo;
    private final GestionEmploye gestionEmploye = new GestionEmploye();
    private final EmployeBD employeBD = new EmployeBD();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public ModifierEmployeFrame(int numCarteEmp) {
        super("Modifier un employé", "Mettre à jour les informations", 780, 640);
        this.numCarteEmp = numCarteEmp;
        buildUI();
        loadEmploye();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton cv = PharmTheme.ghostButton("📄 Gérer CV"); cv.addActionListener(e -> new GererCVFrame(numCarteEmp).setVisible(true));
        JButton cancel = PharmTheme.ghostButton("Annuler"); cancel.addActionListener(e -> dispose());
        JButton save = PharmTheme.primaryButton("Enregistrer →"); save.addActionListener(e -> save());
        p.add(cv); p.add(cancel); p.add(save);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout());
        JPanel form = new JPanel(); form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(PharmTheme.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(20, 22, 20, 22)));

        lblInfo = PharmTheme.helperLabel("Chargement…"); lblInfo.setAlignmentX(LEFT_ALIGNMENT); form.add(lblInfo); form.add(Box.createVerticalStrut(12));

        addFormSection(form, "Informations personnelles");
        txtNom       = addF(form, "Nom");
        txtPrenom    = addF(form, "Prénom");
        txtAge       = addF(form, "Âge");
        txtAdresse   = addF(form, "Adresse");
        txtEmail     = addF(form, "Email");
        txtTelephone = addF(form, "Téléphone");

        addFormSection(form, "Informations professionnelles");
        cmbPoste = new JComboBox<>(new String[]{"Pharmacien","Assistant","Administrateur","Caissier","Préparateur"});
        cmbPoste.setFont(PharmTheme.FONT_BODY); cmbPoste.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); cmbPoste.setAlignmentX(LEFT_ALIGNMENT);
        JLabel pl = PharmTheme.formLabel("Poste"); pl.setAlignmentX(LEFT_ALIGNMENT); form.add(pl); form.add(Box.createVerticalStrut(4)); form.add(cmbPoste); form.add(Box.createVerticalStrut(10));
        txtSalaire    = addF(form, "Salaire (DT)");
        txtMotDePasse = new JPasswordField(); txtMotDePasse.setFont(PharmTheme.FONT_BODY); txtMotDePasse.setBackground(PharmTheme.CARD); txtMotDePasse.setBorder(new PharmTheme.RoundedFieldBorder(PharmTheme.R8)); txtMotDePasse.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); txtMotDePasse.setAlignmentX(LEFT_ALIGNMENT);
        JLabel mpl = PharmTheme.formLabel("Nouveau mot de passe (laisser vide = inchangé)"); mpl.setAlignmentX(LEFT_ALIGNMENT); form.add(mpl); form.add(Box.createVerticalStrut(4)); form.add(txtMotDePasse); form.add(Box.createVerticalStrut(10));
        txtHeureDebut = addF(form, "Heure début (HH:MM)");
        txtHeureFin   = addF(form, "Heure fin (HH:MM)");
        JLabel jl = PharmTheme.formLabel("Jours / semaine"); jl.setAlignmentX(LEFT_ALIGNMENT); form.add(jl); form.add(Box.createVerticalStrut(4));
        spnJours = PharmTheme.spinner(5,1,7,1); spnJours.setAlignmentX(LEFT_ALIGNMENT); spnJours.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); form.add(spnJours);

        JScrollPane sp = new JScrollPane(form); sp.setBorder(BorderFactory.createEmptyBorder()); sp.setBackground(PharmTheme.BG); PharmTheme.styleScrollBar(sp.getVerticalScrollBar());
        contentArea.add(sp, BorderLayout.CENTER);
    }

    private JTextField addF(JPanel p, String label) {
        JLabel l = PharmTheme.formLabel(label); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(4));
        JTextField f = PharmTheme.textField(""); f.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); f.setAlignmentX(LEFT_ALIGNMENT); p.add(f); p.add(Box.createVerticalStrut(10));
        return f;
    }

    private void loadEmploye() {
        try {
            employe = employeBD.rechercherParId(numCarteEmp);
            if (employe == null) { PharmTheme.showError(this, "Introuvable", "Employé #" + numCarteEmp + " introuvable."); dispose(); return; }
            lblInfo.setText("Carte #" + employe.getNumCarteEmp() + "  ·  CNSS " + employe.getNumCNSS() + "  ·  Recruté le " + df.format(employe.getDateRejoindTravail()));
            txtNom.setText(employe.getNom()); txtPrenom.setText(employe.getPrenom()); txtAge.setText(String.valueOf(employe.getAge()));
            txtAdresse.setText(employe.getAdresse() != null ? employe.getAdresse() : ""); txtEmail.setText(employe.getAdresseMail() != null ? employe.getAdresseMail() : ""); txtTelephone.setText(employe.getTelephone() != null ? employe.getTelephone() : "");
            cmbPoste.setSelectedItem(employe.getPoste()); txtSalaire.setText(String.valueOf(employe.getSalaire()));
            txtHeureDebut.setText(employe.getHeureDebutTravail() != null ? employe.getHeureDebutTravail() : ""); txtHeureFin.setText(employe.getHeureSortie() != null ? employe.getHeureSortie() : "");
            spnJours.setValue(employe.getNbJoursParSemaine());
        } catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); dispose(); }
    }

    private void save() {
        try {
            employe.setNom(txtNom.getText().trim()); employe.setPrenom(txtPrenom.getText().trim());
            employe.setAge(Integer.parseInt(txtAge.getText().trim())); employe.setAdresse(txtAdresse.getText().trim());
            employe.setAdresseMail(txtEmail.getText().trim()); employe.setTelephone(txtTelephone.getText().trim());
            employe.setPoste((String) cmbPoste.getSelectedItem()); employe.setSalaire(Double.parseDouble(txtSalaire.getText().trim()));
            employe.setHeureDebutTravail(txtHeureDebut.getText().trim()); employe.setHeureSortie(txtHeureFin.getText().trim());
            employe.setNbJoursParSemaine((Integer) spnJours.getValue());
            String newPwd = new String(txtMotDePasse.getPassword()).trim();
            if (!newPwd.isEmpty()) employe.setMotDePasse(newPwd);
            gestionEmploye.modifierEmploye(employe);
            PharmTheme.showSuccess(this, "Modifié", employe.getPrenom() + " " + employe.getNom() + " mis à jour.");
            dispose();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Saisie invalide", "Vérifiez les champs numériques."); }
        catch (IllegalArgumentException ex) { PharmTheme.showError(this, "Validation", ex.getMessage()); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}
