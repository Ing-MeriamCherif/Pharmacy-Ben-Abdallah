package interfaces.employe;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import entite.*;
import entitebd.EmployeBD;
import gestion.GestionEmploye;

public class GererSalaireFrame extends PharmBaseFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSalaire, txtPct;
    private JLabel lblStats;
    private final GestionEmploye gestionEmploye = new GestionEmploye();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public GererSalaireFrame() {
        super("Gérer les salaires", "Modifier et analyser les rémunérations", 1050, 620);
        buildUI(); load();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> load());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(refresh); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 10));

        tableModel = new DefaultTableModel(new String[]{"N° Carte", "Nom", "Prénom", "Poste", "Salaire actuel", "Date recruit.", "Ancienneté"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0)); bottom.setBackground(PharmTheme.BG);

        JPanel p1 = PharmTheme.card(); p1.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 10));
        p1.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0));
        p1.add(PharmTheme.formLabel("Nouveau salaire (DT) :"));
        txtSalaire = PharmTheme.textField("Ex: 2500.00"); txtSalaire.setPreferredSize(new Dimension(140, 32)); p1.add(txtSalaire);
        JButton btnMod = PharmTheme.primaryButton("Modifier"); btnMod.addActionListener(e -> modifierSalaire()); p1.add(btnMod);

        JPanel p2 = PharmTheme.card(); p2.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 10));
        p2.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0));
        p2.add(PharmTheme.formLabel("Augmentation (%) :"));
        txtPct = PharmTheme.textField("Ex: 5"); txtPct.setPreferredSize(new Dimension(100, 32)); p2.add(txtPct);
        JButton btnAug = PharmTheme.accentButton("Appliquer"); btnAug.addActionListener(e -> appliquer()); p2.add(btnAug);

        bottom.add(p1); bottom.add(p2);
        contentArea.add(bottom, BorderLayout.SOUTH);
    }

    private void load() {
        tableModel.setRowCount(0);
        new SwingWorker<List<Employe>,Void>() {
            @Override protected List<Employe> doInBackground() throws Exception { return gestionEmploye.listerTousEmployes(); }
            @Override protected void done() {
                try {
                    GestionEmploye.StatistiquesEmployes stats = gestionEmploye.obtenirStatistiques();
                    for (Employe e : get()) {
                        long anc = (System.currentTimeMillis() - e.getDateRejoindTravail().getTime()) / (1000L * 60 * 60 * 24 * 365);
                        tableModel.addRow(new Object[]{ e.getNumCarteEmp(), e.getNom(), e.getPrenom(), e.getPoste(), String.format("%.2f DT", e.getSalaire()), df.format(e.getDateRejoindTravail()), anc + " an(s)" });
                    }
                } catch (Exception ex) { PharmTheme.showError(GererSalaireFrame.this, "Erreur", ex.getMessage()); }
            }
        }.execute();
    }

    private void modifierSalaire() {
        int row = table.getSelectedRow(); if (row < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez un employé."); return; }
        try {
            int id = (Integer) tableModel.getValueAt(row, 0); String nom = tableModel.getValueAt(row, 1) + " " + tableModel.getValueAt(row, 2);
            double sal = Double.parseDouble(txtSalaire.getText().trim());
            if (!PharmTheme.showConfirm(this, "Confirmer", "Modifier le salaire de " + nom + " à " + String.format("%.2f DT", sal) + " ?")) return;
            gestionEmploye.modifierSalaire(id, sal); PharmTheme.showSuccess(this, "Modifié", "Salaire mis à jour."); load();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Invalide", "Entrez un salaire valide."); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }

    private void appliquer() {
        int row = table.getSelectedRow(); if (row < 0) { PharmTheme.showWarning(this, "Aucune sélection", "Sélectionnez un employé."); return; }
        try {
            int id = (Integer) tableModel.getValueAt(row, 0); String nom = tableModel.getValueAt(row, 1) + " " + tableModel.getValueAt(row, 2);
            double pct = Double.parseDouble(txtPct.getText().trim());
            String salStr = ((String)tableModel.getValueAt(row, 4)).replace(" DT", "").replace(",", ".");
            double actuel = Double.parseDouble(salStr); double nouveau = actuel * (1 + pct/100);
            if (!PharmTheme.showConfirm(this, "Confirmer", "Augmenter " + nom + " de " + pct + "% ?\n" + String.format("%.2f DT → %.2f DT", actuel, nouveau))) return;
            gestionEmploye.modifierSalaire(id, nouveau); PharmTheme.showSuccess(this, "Appliqué", "Augmentation appliquée."); load();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Invalide", "Entrez un pourcentage valide."); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }
}
