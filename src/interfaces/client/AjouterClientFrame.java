package interfaces.client;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Client;
import gestion.GestionClient;

public class AjouterClientFrame extends PharmBaseFrame {
    private JTextField txtNumCarte, txtNom, txtPrenom, txtAge, txtAdresse, txtEmail, txtTelephone, txtCodeCnam;
    private final GestionClient gestionClient = new GestionClient();

    public AjouterClientFrame() {
        super("Ajouter un client", "Nouveau dossier client", 760, 580);
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
        JScrollPane sp = new JScrollPane(buildForm());
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(PharmTheme.BG);
        PharmTheme.styleScrollBar(sp.getVerticalScrollBar());
        contentArea.add(sp, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel p = PharmTheme.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0), BorderFactory.createEmptyBorder(20, 22, 20, 22)));

        addFormSection(p, "Informations personnelles");
        txtNumCarte  = addF(p, "N° Carte Identité *", "");
        txtNom       = addF(p, "Nom *", "");
        txtPrenom    = addF(p, "Prénom *", "");
        txtAge       = addF(p, "Âge *", "");
        txtTelephone = addF(p, "Téléphone *", "");
        txtEmail     = addF(p, "Email", "");
        txtAdresse   = addF(p, "Adresse", "");
        addFormSection(p, "Fidélité CNAM");
        txtCodeCnam  = addF(p, "Code CNAM", "");
        p.add(Box.createVerticalStrut(8));
        JLabel note = PharmTheme.helperLabel("* Champs obligatoires — Points fidélité initiaux : 0"); note.setAlignmentX(LEFT_ALIGNMENT); p.add(note);
        return p;
    }

    private JTextField addF(JPanel p, String lbl, String def) {
        JLabel l = PharmTheme.requiredLabel(lbl); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(4));
        JTextField f = PharmTheme.textField(def); f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); f.setAlignmentX(LEFT_ALIGNMENT); p.add(f); p.add(Box.createVerticalStrut(10));
        return f;
    }

    private void save() {
        if (txtNumCarte.getText().isBlank() || txtNom.getText().isBlank() || txtPrenom.getText().isBlank() || txtAge.getText().isBlank() || txtTelephone.getText().isBlank()) {
            PharmTheme.showWarning(this, "Champs manquants", "Remplissez tous les champs obligatoires (*)."); return;
        }
        try {
            Client c = new Client();
            c.setNumCarteIdentite(Integer.parseInt(txtNumCarte.getText().trim()));
            c.setNom(txtNom.getText().trim()); c.setPrenom(txtPrenom.getText().trim());
            c.setAge(Integer.parseInt(txtAge.getText().trim())); c.setTelephone(txtTelephone.getText().trim());
            c.setAdresseMail(txtEmail.getText().trim()); c.setAdresse(txtAdresse.getText().trim());
            c.setCodeCnam(txtCodeCnam.getText().trim()); c.setPointFidelite(0);
            int id = gestionClient.ajouterClient(c);
            PharmTheme.showSuccess(this, "Client ajouté", "N° client : " + id + "\n" + c.getPrenom() + " " + c.getNom());
            dispose();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Saisie invalide", "N° carte et âge doivent être des nombres."); }
        catch (IllegalArgumentException ex) { PharmTheme.showError(this, "Validation", ex.getMessage()); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}
