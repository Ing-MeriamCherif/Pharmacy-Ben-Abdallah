package interfaces.employe;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import entite.*;
import gestion.GestionEmploye;

public class ListerEmployesFrame extends PharmBaseFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblStats;
    private final GestionEmploye gestionEmploye = new GestionEmploye();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public ListerEmployesFrame() {
        super("Liste des employés", "Consulter et gérer l'équipe", 1100, 620);
        buildUI(); load();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> load());
        JButton modifier = PharmTheme.ghostButton("✎ Modifier"); modifier.addActionListener(e -> modifier());
        JButton supprimer = PharmTheme.dangerButton("Supprimer"); supprimer.addActionListener(e -> supprimer());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(refresh); p.add(modifier); p.add(supprimer); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 8));
        tableModel = new DefaultTableModel(new String[]{"N° Carte", "CNSS", "Nom", "Prénom", "Poste", "Salaire", "Téléphone", "Date recruit.", "Jours/sem."}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);
        lblStats = PharmTheme.helperLabel(""); contentArea.add(lblStats, BorderLayout.SOUTH);
    }

    private void load() {
        tableModel.setRowCount(0);
        new SwingWorker<List<Employe>,Void>() {
            @Override protected List<Employe> doInBackground() throws Exception { return gestionEmploye.listerTousEmployes(); }
            @Override protected void done() {
                try {
                    List<Employe> list = get();
                    GestionEmploye.StatistiquesEmployes stats = gestionEmploye.obtenirStatistiques();
                    for (Employe e : list) tableModel.addRow(new Object[]{ e.getNumCarteEmp(), e.getNumCNSS(), e.getNom(), e.getPrenom(), e.getPoste(), String.format("%.2f DT", e.getSalaire()), e.getTelephone(), df.format(e.getDateRejoindTravail()), e.getNbJoursParSemaine() });
                    lblStats.setText(String.format("Total : %d employé(s)  |  Admins : %d  |  Masse salariale : %.2f DT  |  Salaire moyen : %.2f DT", stats.getNbTotal(), stats.getNbAdmins(), stats.getMasseSalariale(), stats.getSalaireMoyen()));
                } catch (Exception ex) { PharmTheme.showError(ListerEmployesFrame.this, "Erreur", ex.getMessage()); }
            }
        }.execute();
    }

    private void modifier() {
        int row = table.getSelectedRow(); if (row < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez un employé."); return; }
        int id = (Integer) tableModel.getValueAt(row, 0);
        new ModifierEmployeFrame(id).setVisible(true);
    }

    private void supprimer() {
        int row = table.getSelectedRow(); if (row < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez un employé."); return; }
        int id = (Integer) tableModel.getValueAt(row, 0);
        String nom = tableModel.getValueAt(row, 2) + " " + tableModel.getValueAt(row, 3);
        if (!PharmTheme.showConfirm(this, "Confirmer suppression", "Supprimer l'employé " + nom + " (#" + id + ") ?\nSon CV sera également supprimé.")) return;
        try { gestionEmploye.supprimerEmploye(id); PharmTheme.showSuccess(this, "Supprimé", nom + " supprimé."); load(); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }
}
