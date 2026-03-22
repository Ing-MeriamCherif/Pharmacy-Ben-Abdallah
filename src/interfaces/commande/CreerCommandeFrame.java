package interfaces.commande;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import entite.*;
import entitebd.*;
import gestion.GestionCommande;

public class CreerCommandeFrame extends PharmBaseFrame {
    private JComboBox<MedItem> cmbMed;
    private JSpinner spnQte;
    private JTextField txtPrix, txtRemise, txtImpots, txtDateAchat, txtDateLimite, txtNumEmp;
    private JComboBox<Integer> cmbFournisseur;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private final ArrayList<VoieCommande> lignes = new ArrayList<>();
    private final GestionCommande gestionCommande = new GestionCommande();
    private final MedicamentBD medicamentBD = new MedicamentBD();
    private final FournisseurBD fournisseurBD = new FournisseurBD();
    private final StockBD stockBD = new StockBD();

    public CreerCommandeFrame() {
        super("Créer une commande", "Nouvelle commande fournisseur", 1050, 680);
        buildUI(); chargerDonnees();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton cancel = PharmTheme.ghostButton("Annuler"); cancel.addActionListener(e -> dispose());
        JButton create = PharmTheme.primaryButton("Créer commande →"); create.addActionListener(e -> creer());
        p.add(cancel); p.add(create);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 12));

        // Header info
        JPanel info = PharmTheme.card();
        info.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        info.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0));
        info.setPreferredSize(new Dimension(0, 54));
        addInf(info, "Fournisseur :"); cmbFournisseur = new JComboBox<>(); cmbFournisseur.setFont(PharmTheme.FONT_BODY); cmbFournisseur.setPreferredSize(new Dimension(140, 32)); info.add(cmbFournisseur);
        addInf(info, "Date achat :"); txtDateAchat = PharmTheme.textField(LocalDate.now().toString()); txtDateAchat.setPreferredSize(new Dimension(120, 32)); info.add(txtDateAchat);
        addInf(info, "Date limite :"); txtDateLimite = PharmTheme.textField(LocalDate.now().plusDays(30).toString()); txtDateLimite.setPreferredSize(new Dimension(120, 32)); info.add(txtDateLimite);
        addInf(info, "Employé :"); txtNumEmp = PharmTheme.textField("1"); txtNumEmp.setPreferredSize(new Dimension(60, 32)); info.add(txtNumEmp);
        contentArea.add(info, BorderLayout.NORTH);

        // Center: add line + table
        JPanel center = new JPanel(new BorderLayout(0, 10)); center.setBackground(PharmTheme.BG);

        JPanel addRow = PharmTheme.card();
        addRow.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 10));
        addRow.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0));
        addRow.setPreferredSize(new Dimension(0, 54));
        addInf(addRow, "Médicament :"); cmbMed = new JComboBox<>(); cmbMed.setFont(PharmTheme.FONT_BODY); cmbMed.setPreferredSize(new Dimension(220, 32)); cmbMed.addActionListener(e -> onMedSelected()); addRow.add(cmbMed);
        addInf(addRow, "Qté :"); spnQte = PharmTheme.spinner(1,1,10000,1); spnQte.setPreferredSize(new Dimension(70, 32)); addRow.add(spnQte);
        addInf(addRow, "Prix U. :"); txtPrix = PharmTheme.textField("0.00"); txtPrix.setPreferredSize(new Dimension(80, 32)); addRow.add(txtPrix);
        addInf(addRow, "Remise % :"); txtRemise = PharmTheme.textField("0"); txtRemise.setPreferredSize(new Dimension(55, 32)); addRow.add(txtRemise);
        addInf(addRow, "Impôts % :"); txtImpots = PharmTheme.textField("19"); txtImpots.setPreferredSize(new Dimension(55, 32)); addRow.add(txtImpots);
        JButton btnAdd = PharmTheme.accentButton("+ Ajouter"); btnAdd.addActionListener(e -> ajouterLigne()); addRow.add(btnAdd);
        center.add(addRow, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Médicament", "Qté", "Prix U.", "Remise%", "Impôts%", "Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        center.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); foot.setBackground(PharmTheme.BG);
        lblTotal = new JLabel("Total : 0.00 DT"); lblTotal.setFont(new Font("SansSerif", Font.BOLD, 16)); lblTotal.setForeground(PharmTheme.PM_500);
        foot.add(lblTotal); center.add(foot, BorderLayout.SOUTH);
        contentArea.add(center, BorderLayout.CENTER);
    }

    private void addInf(JPanel p, String t) { JLabel l = PharmTheme.formLabel(t); p.add(l); }

    private void chargerDonnees() {
        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception {
                for (entite.Fournisseur f : fournisseurBD.listerTous()) cmbFournisseur.addItem(f.getNumFournisseur());
                for (Medicament m : medicamentBD.listerTous()) cmbMed.addItem(new MedItem(m));
                return null;
            }
        }.execute();
    }

    private void onMedSelected() {
        MedItem sel = (MedItem) cmbMed.getSelectedItem(); if (sel == null) return;
        try { StockMedicament s = stockBD.rechercherParRef(sel.med.getRefMedicament()); if (s != null) txtPrix.setText(String.valueOf(s.getPrixAchat())); } catch (Exception ignored) {}
    }

    private void ajouterLigne() {
        MedItem sel = (MedItem) cmbMed.getSelectedItem(); if (sel == null) return;
        try {
            int qte = (Integer) spnQte.getValue();
            double prix = Double.parseDouble(txtPrix.getText()), remise = Double.parseDouble(txtRemise.getText()), impots = Double.parseDouble(txtImpots.getText());
            VoieCommande lc = new VoieCommande(0, sel.med.getRefMedicament(), qte, prix, remise, impots);
            lignes.add(lc);
            tableModel.addRow(new Object[]{ sel.med.getNom(), qte, prix, remise, impots, String.format("%.2f DT", lc.calculerTotal()) });
            refreshTotal();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Saisie invalide", "Vérifiez les champs numériques."); }
    }

    private void refreshTotal() {
        double t = lignes.stream().mapToDouble(VoieCommande::calculerTotal).sum();
        lblTotal.setText(String.format("Total : %.2f DT", t));
    }

    private void creer() {
        if (lignes.isEmpty()) { PharmTheme.showWarning(this, "Panier vide", "Ajoutez au moins une ligne."); return; }
        try {
            Commande c = new Commande();
            c.setDateAchat(txtDateAchat.getText()); c.setDateLimRendreProduit(txtDateLimite.getText());
            c.setNumFournisseur((Integer) cmbFournisseur.getSelectedItem()); c.setNumCarteEmp(Integer.parseInt(txtNumEmp.getText()));
            int num = gestionCommande.creerCommande(c, lignes);
            PharmTheme.showSuccess(this, "Commande créée", "Commande #" + num + " créée avec succès !");
            dispose();
        } catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }

    private record MedItem(Medicament med) {
        @Override public String toString() { return med.getRefMedicament() + " — " + med.getNom(); }
    }
}
