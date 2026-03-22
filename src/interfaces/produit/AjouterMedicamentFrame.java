package interfaces.produit;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Medicament;
import gestion.GestionProduit;

/**
 * AjouterMedicamentFrame — Crée un médicament (nom + description).
 * Le stock sera ajouté séparément lors de la réception des commandes.
 */
public class AjouterMedicamentFrame extends PharmBaseFrame {

    private JTextField txtNom;
    private JTextArea  txtDesc;
    // Summary
    private JLabel summNom, summNote;

    private final GestionProduit gestionProduit = new GestionProduit();

    public AjouterMedicamentFrame() {
        super("Ajouter un médicament", "Nouveau produit au catalogue", 840, 520);
        buildUI();
        wireListeners();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton cancel = PharmTheme.ghostButton("Annuler"); cancel.addActionListener(e -> dispose());
        JButton save   = PharmTheme.primaryButton("Enregistrer →"); save.addActionListener(e -> save());
        p.add(cancel); p.add(save);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(16, 0));

        // Left card
        JPanel leftCard = PharmTheme.card(); leftCard.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(buildForm());
        sp.setBorder(null); sp.setBackground(PharmTheme.CARD); sp.getViewport().setBackground(PharmTheme.CARD);
        PharmTheme.styleScrollBar(sp.getVerticalScrollBar());
        leftCard.add(sp, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCard, buildSummary());
        split.setBorder(null); split.setDividerSize(8); split.setResizeWeight(0.62);
        split.setDividerLocation(500); split.setBackground(PharmTheme.BG);
        contentArea.add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PharmTheme.CARD); p.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,22,20,22));

        addFormSection(p, "Informations médicament");

        txtNom = PharmTheme.textField("Ex: Amoxicilline 500mg");
        row(p, "Nom du médicament", txtNom, true);

        p.add(Box.createVerticalStrut(12));
        JLabel dl = PharmTheme.formLabel("Description (optionnel)"); dl.setAlignmentX(LEFT_ALIGNMENT); p.add(dl);
        p.add(Box.createVerticalStrut(5));
        txtDesc = PharmTheme.textArea(4, 0);
        JScrollPane ds = PharmTheme.scrollTextArea(txtDesc); ds.setAlignmentX(LEFT_ALIGNMENT);
        ds.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); p.add(ds);

        p.add(Box.createVerticalStrut(16));
        JLabel note = PharmTheme.helperLabel("* Champs obligatoires · Le stock sera ajouté lors des commandes");
        note.setAlignmentX(LEFT_ALIGNMENT); p.add(note);
        return p;
    }

    private void row(JPanel form, String label, JComponent field, boolean required) {
        form.add(Box.createVerticalStrut(10));
        JLabel lbl = required ? PharmTheme.requiredLabel(label) : PharmTheme.formLabel(label);
        lbl.setAlignmentX(LEFT_ALIGNMENT); form.add(lbl);
        form.add(Box.createVerticalStrut(4)); field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); form.add(field);
    }

    private JPanel buildSummary() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(PharmTheme.BG2);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,16,20,20));
        JPanel inner = new JPanel(); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS)); inner.setBackground(PharmTheme.BG2);

        JLabel title = PharmTheme.sectionLabel("Aperçu"); title.setAlignmentX(LEFT_ALIGNMENT); inner.add(title);
        inner.add(Box.createVerticalStrut(14));

        summNom = new JLabel("—"); summNom.setFont(PharmTheme.FONT_H2); summNom.setForeground(PharmTheme.TXT); summNom.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(summNom); inner.add(Box.createVerticalStrut(10));

        summNote = new JLabel("<html><i>Stock vide — sera rempli lors des réceptions</i></html>");
        summNote.setFont(PharmTheme.FONT_SM); summNote.setForeground(PharmTheme.TXT3); summNote.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(summNote); inner.add(Box.createVerticalStrut(20));

        tip(inner, "Catalogue médicaments", "Créez d'abord le médicament ici.");
        tip(inner, "Commandes & réceptions", "Ajoutez les lots lors de la réception des commandes fournisseur.");
        tip(inner, "FEFO automatique", "Le système consommera les lots les plus proches de l'expiration.");

        p.add(inner, BorderLayout.NORTH); return p;
    }

    private void tip(JPanel parent, String t1, String t2) {
        JPanel tip = new JPanel(new BorderLayout(8,0)); tip.setBackground(PharmTheme.BG2);
        tip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52)); tip.setAlignmentX(LEFT_ALIGNMENT);
        tip.setBorder(javax.swing.BorderFactory.createEmptyBorder(8,0,0,0));
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) { g.setColor(PharmTheme.PM_400); ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g.fillOval(0,4,8,8); }
        }; dot.setPreferredSize(new Dimension(10,16)); dot.setOpaque(false);
        JPanel txt = new JPanel(); txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS)); txt.setBackground(PharmTheme.BG2);
        JLabel l1 = new JLabel(t1); l1.setFont(new Font("SansSerif",Font.BOLD,12)); l1.setForeground(PharmTheme.TXT2);
        JLabel l2 = new JLabel("<html>"+t2+"</html>"); l2.setFont(PharmTheme.FONT_LABEL); l2.setForeground(PharmTheme.TXT3);
        txt.add(l1); txt.add(l2); tip.add(dot,BorderLayout.WEST); tip.add(txt,BorderLayout.CENTER); parent.add(tip);
    }

    private void wireListeners() {
        txtNom.addCaretListener(e -> summNom.setText(txtNom.getText().trim().isEmpty() ? "—" : txtNom.getText().trim()));
    }

    private void save() {
        String nom = txtNom.getText().trim();
        if (nom.isEmpty()) { PharmTheme.showError(this,"Validation","Le nom est obligatoire."); txtNom.requestFocus(); return; }
        try {
            Medicament med = new Medicament();
            med.setNom(nom);
            med.setDescriptio(txtDesc.getText().trim());
            int ref = gestionProduit.ajouterMedicament(med);
            PharmTheme.showSuccess(this, "Médicament ajouté", "Réf #" + ref + " · " + nom + "\nAjoutez le stock lors des réceptions.");
            dispose();
        } catch (IllegalArgumentException ex) { PharmTheme.showError(this, "Validation", ex.getMessage());
        } catch (SQLException ex)              { PharmTheme.showError(this, "Erreur base de données", ex.getMessage()); }
    }
}