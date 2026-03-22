package interfaces.fournisseur;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Fournisseur;
import entitebd.FournisseurBD;

public class SupprimerFournisseurFrame extends PharmBaseFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private final FournisseurBD fournisseurBD = new FournisseurBD();

    public SupprimerFournisseurFrame() {
        super("Supprimer un fournisseur", "Retirer un partenaire commercial", 880, 520);
        buildUI(); load();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> load());
        JButton del = PharmTheme.dangerButton("Supprimer"); del.addActionListener(e -> supprimer());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(refresh); p.add(del); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 10));
        tableModel = new DefaultTableModel(new String[]{"N°", "Nom", "Adresse", "Téléphone", "Email", "Évaluation"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel warn = new JPanel(new FlowLayout(FlowLayout.LEFT)); warn.setBackground(PharmTheme.WARN_BG); warn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        JLabel wl = new JLabel("⚠  La suppression d'un fournisseur peut affecter les médicaments associés."); wl.setFont(PharmTheme.FONT_SM); wl.setForeground(PharmTheme.WARN); warn.add(wl);
        contentArea.add(warn, BorderLayout.SOUTH);
    }

    private void load() {
        tableModel.setRowCount(0);
        try { for (Fournisseur f : fournisseurBD.listerTous()) tableModel.addRow(new Object[]{ f.getNumFournisseur(), f.getNomFournisseur(), f.getAdresse(), f.getTelephone(), f.getAdresseEmail(), String.format("%.1f/5", f.getRate()) }); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void supprimer() {
        int row = table.getSelectedRow(); if (row < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez un fournisseur."); return; }
        int id = (Integer) tableModel.getValueAt(row, 0); String nom = (String) tableModel.getValueAt(row, 1);
        if (!PharmTheme.showConfirm(this, "Confirmer suppression", "Supprimer le fournisseur «" + nom + "» (#" + id + ") ?\nCette action est irréversible.")) return;
        try { fournisseurBD.supprimer(id); PharmTheme.showSuccess(this, "Supprimé", nom + " supprimé."); load(); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }
}
