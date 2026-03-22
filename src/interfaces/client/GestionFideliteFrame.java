package interfaces.client;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.Client;
import gestion.GestionClient;

public class GestionFideliteFrame extends PharmBaseFrame {
    private JTextField txtNumClient, txtNom, txtPoints, txtQte;
    private JRadioButton rbAjouter, rbUtiliser;
    private JLabel lblReduction;
    private final GestionClient gestionClient = new GestionClient();
    private int currentId = -1;

    public GestionFideliteFrame() {
        super("Fidélité client", "Gérer les points de fidélité", 640, 500);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        JButton apply = PharmTheme.primaryButton("Valider →"); apply.addActionListener(e -> ajuster());
        p.add(close); p.add(apply);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout());
        JPanel card = PharmTheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0), BorderFactory.createEmptyBorder(20, 22, 20, 22)));

        addS(card, "Rechercher le client");
        JPanel row = new JPanel(new BorderLayout(6, 0)); row.setBackground(PharmTheme.CARD); row.setAlignmentX(LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtNumClient = PharmTheme.textField("N° client"); txtNumClient.addActionListener(e -> rechercher());
        JButton btn = PharmTheme.ghostButton("Chercher"); btn.addActionListener(e -> rechercher());
        row.add(txtNumClient, BorderLayout.CENTER); row.add(btn, BorderLayout.EAST); card.add(row); card.add(Box.createVerticalStrut(10));
        addL(card, "Nom complet"); txtNom = ro(); card.add(txtNom); card.add(Box.createVerticalStrut(8));
        addL(card, "Points actuels"); txtPoints = ro(); txtPoints.setFont(new Font("SansSerif", Font.BOLD, 14)); card.add(txtPoints); card.add(Box.createVerticalStrut(16));

        addS(card, "Opération");
        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)); typeRow.setBackground(PharmTheme.CARD); typeRow.setAlignmentX(LEFT_ALIGNMENT);
        rbAjouter = new JRadioButton("Ajouter des points", true); rbUtiliser = new JRadioButton("Utiliser des points");
        rbAjouter.setBackground(PharmTheme.CARD); rbUtiliser.setBackground(PharmTheme.CARD);
        rbAjouter.setFont(PharmTheme.FONT_BODY); rbUtiliser.setFont(PharmTheme.FONT_BODY);
        rbAjouter.addActionListener(e -> updateReduction()); rbUtiliser.addActionListener(e -> updateReduction());
        ButtonGroup bg = new ButtonGroup(); bg.add(rbAjouter); bg.add(rbUtiliser);
        typeRow.add(rbAjouter); typeRow.add(rbUtiliser); card.add(typeRow); card.add(Box.createVerticalStrut(8));
        addL(card, "Nombre de points");
        txtQte = PharmTheme.textField("0"); txtQte.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); txtQte.setAlignmentX(LEFT_ALIGNMENT);
        txtQte.addCaretListener(e -> updateReduction()); card.add(txtQte); card.add(Box.createVerticalStrut(8));
        lblReduction = PharmTheme.helperLabel(""); lblReduction.setAlignmentX(LEFT_ALIGNMENT); lblReduction.setForeground(PharmTheme.SUCCESS); card.add(lblReduction);
        contentArea.add(card, BorderLayout.CENTER);
    }

    private void addS(JPanel p, String t) { JLabel l = PharmTheme.sectionLabel(t); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(8)); }
    private void addL(JPanel p, String t) { JLabel l = PharmTheme.formLabel(t); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(4)); }
    private JTextField ro() { JTextField f = new JTextField(); f.setEditable(false); f.setBackground(PharmTheme.BG3); f.setFont(PharmTheme.FONT_BODY); f.setBorder(new PharmTheme.RoundedFieldBorder(PharmTheme.R8)); f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); f.setAlignmentX(LEFT_ALIGNMENT); return f; }

    private void rechercher() {
        String s = txtNumClient.getText().trim(); if (s.isEmpty()) return;
        try {
            Client c = gestionClient.rechercherParId(Integer.parseInt(s));
            if (c == null) { PharmTheme.showWarning(this, "Introuvable", "Aucun client #" + s); return; }
            currentId = c.getNumClient(); txtNom.setText(c.getPrenom() + " " + c.getNom()); txtPoints.setText(String.valueOf(c.getPointFidelite()));
        } catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }

    private void updateReduction() {
        try {
            int pts = Integer.parseInt(txtQte.getText().trim());
            if (rbUtiliser.isSelected()) lblReduction.setText("Réduction équivalente : " + String.format("%.2f DT", gestionClient.calculerReduction(pts, 0.1)));
            else lblReduction.setText("");
        } catch (NumberFormatException ignored) { lblReduction.setText(""); }
    }

    private void ajuster() {
        if (currentId < 0) { PharmTheme.showWarning(this, "Aucun client", "Recherchez d'abord un client."); return; }
        try {
            int pts = Integer.parseInt(txtQte.getText().trim());
            if (pts <= 0) { PharmTheme.showWarning(this, "Quantité invalide", "Le nombre de points doit être positif."); return; }
            if (rbAjouter.isSelected()) gestionClient.ajouterPoints(currentId, pts);
            else gestionClient.utiliserPoints(currentId, pts);
            PharmTheme.showSuccess(this, "Opération réussie", "Points mis à jour.");
            rechercher(); txtQte.setText("");
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Saisie invalide", "Entrez un nombre entier."); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }
}
