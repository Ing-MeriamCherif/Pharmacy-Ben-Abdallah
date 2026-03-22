package interfaces.commande;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import entite.*;
import gestion.GestionCommande;

public class ModifierCommandeFrame extends PharmBaseFrame {
    private JTextField txtNum;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblInfo;
    private ArrayList<VoieCommande> lignes = new ArrayList<>();
    private Commande commandeActuelle;
    private final GestionCommande gestionCommande = new GestionCommande();

    public ModifierCommandeFrame() {
        super("Modifier une commande", "Modifier les lignes d'une commande existante", 1000, 620);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton cancel = PharmTheme.ghostButton("Annuler"); cancel.addActionListener(e -> dispose());
        JButton save = PharmTheme.primaryButton("Enregistrer →"); save.addActionListener(e -> enregistrer());
        p.add(cancel); p.add(save);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); top.setBackground(PharmTheme.BG);
        top.add(PharmTheme.formLabel("N° Commande :"));
        txtNum = PharmTheme.textField("Numéro…"); txtNum.setPreferredSize(new Dimension(150, 34)); txtNum.addActionListener(e -> charger());
        JButton btnLoad = PharmTheme.ghostButton("Charger"); btnLoad.addActionListener(e -> charger());
        lblInfo = PharmTheme.helperLabel("Recherchez une commande");
        top.add(txtNum); top.add(btnLoad); top.add(lblInfo);
        contentArea.add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID Ligne", "Médicament", "Qté", "Prix U.", "Remise %", "Impôts %", "Total"}, 0);
        table = new JTable(tableModel); PharmTheme.styleTable(table);
        contentArea.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);

        JPanel editRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); editRow.setBackground(PharmTheme.BG2); editRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, PharmTheme.BORDER));
        editRow.add(PharmTheme.formLabel("Double-cliquez sur une ligne pour la modifier directement dans le tableau."));
        contentArea.add(editRow, BorderLayout.SOUTH);
    }

    private void charger() {
        try {
            int num = Integer.parseInt(txtNum.getText().trim());
            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(num);
            commandeActuelle = bilan.getCommande(); lignes = bilan.getLignes();
            if ("Reçue".equals(commandeActuelle.getStatut()) || "Annulée".equals(commandeActuelle.getStatut())) {
                PharmTheme.showError(this, "Non modifiable", "Impossible de modifier une commande " + commandeActuelle.getStatut()); return;
            }
            lblInfo.setText("Commande #" + num + " — " + commandeActuelle.getStatut() + " — " + String.format("%.2f DT", bilan.getTotal()));
            tableModel.setRowCount(0);
            for (VoieCommande lc : lignes) {
                tableModel.addRow(new Object[]{ lc.getIdLigneCommande(), "Méd. #" + lc.getRefMedicament(), lc.getQuantite(), lc.getPrixUnitaire(), lc.getRemise(), lc.getImpotSurCommande(), String.format("%.2f DT", lc.calculerTotal()) });
            }
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Invalide", "Entrez un numéro valide."); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }

    private void enregistrer() {
        if (commandeActuelle == null) { PharmTheme.showWarning(this, "Aucune commande", "Chargez d'abord une commande."); return; }
        try {
            gestionCommande.modifierCommande(commandeActuelle.getNumCommande(), lignes);
            PharmTheme.showSuccess(this, "Modifiée", "Commande #" + commandeActuelle.getNumCommande() + " mise à jour.");
            dispose();
        } catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }
}
