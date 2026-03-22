package interfaces.produit;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.*;
import java.util.*;
import entite.*;
import entitebd.*;
import gestion.GestionProduit;

/**
 * ModifierMedicamentFrame
 * Left: searchable table of médicaments with stock summary per lot
 * Right: edit form (nom, description) + stock lots panel (prix achat/vente, seuil, dates)
 */
public class ModifierMedicamentFrame extends PharmBaseFrame {

    private JTextField          txtSearch;
    private JTable              tableMed;
    private DefaultTableModel   modelMed;
    // Medicament fields
    private JLabel              lblRef;
    private JTextField          txtNom;
    private JTextArea           txtDesc;
    // Stock lots table
    private JTable              tableLots;
    private DefaultTableModel   modelLots;
    // Stock add fields
    private JTextField          txtQte, txtPrixA, txtPrixV, txtSeuil, txtDateFab, txtDateExp;

    private final MedicamentBD   medicamentBD  = new MedicamentBD();
    private final StockBD        stockBD       = new StockBD();
    private final GestionProduit gestionProduit = new GestionProduit();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private int currentRef = -1;

    public ModifierMedicamentFrame() {
        super("Modifier un médicament", "Sélectionnez puis modifiez", 1200, 700);
        buildUI(); loadMedicaments();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        JButton save  = PharmTheme.primaryButton("Enregistrer médicament"); save.addActionListener(e -> saveMedicament());
        p.add(close); p.add(save);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(12, 0));
        contentArea.add(buildLeft(), BorderLayout.CENTER);
        contentArea.add(buildRight(), BorderLayout.EAST);
    }

    // ── Left: search + med table ────────────────────────────────────────
    private JPanel buildLeft() {
        JPanel p = new JPanel(new BorderLayout(0, 8)); p.setBackground(PharmTheme.BG);

        JPanel searchRow = new JPanel(new BorderLayout(6, 0)); searchRow.setBackground(PharmTheme.BG);
        txtSearch = PharmTheme.textField("Rechercher par nom…");
        JButton btnS = PharmTheme.ghostButton("Chercher");
        btnS.addActionListener(e -> loadMedicaments()); txtSearch.addActionListener(e -> loadMedicaments());
        searchRow.add(txtSearch, BorderLayout.CENTER); searchRow.add(btnS, BorderLayout.EAST);
        p.add(searchRow, BorderLayout.NORTH);

        modelMed = new DefaultTableModel(new String[]{"Réf.","Nom","Description","Nb Lots","Stock Total"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        tableMed = new JTable(modelMed); PharmTheme.styleTable(tableMed);
        int[] w={50,180,200,70,90};
        for(int i=0;i<w.length;i++) tableMed.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        tableMed.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())loadSelected();});
        p.add(PharmTheme.tableScrollPane(tableMed), BorderLayout.CENTER);
        return p;
    }

    // ── Right: edit form + lots ──────────────────────────────────────────
    private JScrollPane buildRight() {
        JPanel right = new JPanel(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(PharmTheme.CARD); right.setPreferredSize(new Dimension(380, 0));
        right.setBorder(javax.swing.BorderFactory.createEmptyBorder(18,18,18,18));

        lblRef = PharmTheme.helperLabel("Sélectionnez un médicament"); lblRef.setAlignmentX(LEFT_ALIGNMENT);
        right.add(lblRef); right.add(Box.createVerticalStrut(12));

        // Medicament info
        addFormSection(right, "Informations médicament");
        txtNom  = fld(right, "Nom *");
        right.add(PharmTheme.formLabel("Description")); right.add(Box.createVerticalStrut(4));
        txtDesc = PharmTheme.textArea(2,0);
        JScrollPane ds = PharmTheme.scrollTextArea(txtDesc); ds.setAlignmentX(LEFT_ALIGNMENT); ds.setMaximumSize(new Dimension(Integer.MAX_VALUE,60)); right.add(ds);

        // Lots panel
        right.add(Box.createVerticalStrut(16));
        addFormSection(right, "Lots en stock");

        modelLots = new DefaultTableModel(new String[]{"N° Lot","Qté","P.Achat","P.Vente","Seuil","Fab.","Exp."},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        tableLots = new JTable(modelLots); PharmTheme.styleTable(tableLots);
        tableLots.setRowHeight(24);
        int[] lw={55,40,60,60,45,72,72};
        for(int i=0;i<lw.length;i++) tableLots.getColumnModel().getColumn(i).setPreferredWidth(lw[i]);
        JScrollPane lotsScroll = PharmTheme.tableScrollPane(tableLots);
        lotsScroll.setAlignmentX(LEFT_ALIGNMENT); lotsScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 110));
        lotsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110)); right.add(lotsScroll);

        // Add lot
        right.add(Box.createVerticalStrut(10));
        JLabel addLotLbl = PharmTheme.sectionLabel("Ajouter un lot"); addLotLbl.setAlignmentX(LEFT_ALIGNMENT); right.add(addLotLbl);
        right.add(Box.createVerticalStrut(6));
        JPanel g = new JPanel(new GridLayout(3,4,6,6)); g.setBackground(PharmTheme.CARD); g.setAlignmentX(LEFT_ALIGNMENT); g.setMaximumSize(new Dimension(Integer.MAX_VALUE,100));
        txtQte    = mini("0");    txtPrixA  = mini("P.Achat"); txtPrixV  = mini("P.Vente"); txtSeuil  = mini("Seuil");
        txtDateFab= mini("Fab jj/mm/aaaa"); txtDateExp= mini("Exp jj/mm/aaaa");
        g.add(lbl("Quantité")); g.add(txtQte); g.add(lbl("Prix achat")); g.add(txtPrixA);
        g.add(lbl("Prix vente")); g.add(txtPrixV); g.add(lbl("Seuil min")); g.add(txtSeuil);
        g.add(lbl("Date fab.")); g.add(txtDateFab); g.add(lbl("Date exp.")); g.add(txtDateExp);
        right.add(g); right.add(Box.createVerticalStrut(8));
        JButton btnAddLot = PharmTheme.accentButton("+ Ajouter ce lot"); btnAddLot.setAlignmentX(LEFT_ALIGNMENT); btnAddLot.addActionListener(e -> addLot());
        right.add(btnAddLot); right.add(Box.createVerticalStrut(8));
        JButton btnDelLot = PharmTheme.dangerButton("Supprimer lot sélectionné"); btnDelLot.setAlignmentX(LEFT_ALIGNMENT); btnDelLot.addActionListener(e -> deleteLot());
        right.add(btnDelLot);

        JScrollPane rsp = new JScrollPane(right); rsp.setBorder(null); rsp.setBackground(PharmTheme.CARD); rsp.getViewport().setBackground(PharmTheme.CARD);
        PharmTheme.styleScrollBar(rsp.getVerticalScrollBar()); rsp.setPreferredSize(new Dimension(380,0));
        return rsp;
    }

    private JTextField fld(JPanel p, String label){
        JLabel l=PharmTheme.formLabel(label); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(4));
        JTextField f=PharmTheme.textField(""); f.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); f.setAlignmentX(LEFT_ALIGNMENT); p.add(f); p.add(Box.createVerticalStrut(8));
        return f;
    }
    private JTextField mini(String ph){ JTextField f=PharmTheme.textField(ph); return f; }
    private JLabel lbl(String t){ JLabel l=PharmTheme.formLabel(t); return l; }

    // ── Data loading ─────────────────────────────────────────────────────
    private void loadMedicaments() {
        modelMed.setRowCount(0);
        String q = txtSearch!=null ? txtSearch.getText().trim() : "";
        new SwingWorker<java.util.List<Medicament>,Void>(){
            @Override protected java.util.List<Medicament> doInBackground() throws Exception {
                return q.isEmpty() ? medicamentBD.listerTous() : medicamentBD.rechercherParNom(q);
            }
            @Override protected void done(){
                try { for(Medicament m : get()){
                    java.util.List<StockMedicament> lots = new ArrayList<>();
                    try{ lots = stockBD.getStocksParExpiration(m.getRefMedicament()); }catch(Exception ignored){}
                    int total = lots.stream().mapToInt(StockMedicament::getQuantiteProduit).sum();
                    modelMed.addRow(new Object[]{m.getRefMedicament(),m.getNom(),m.getDescriptio()!=null?m.getDescriptio():"",lots.size()+" lot(s)",total+" unités"});
                }} catch(Exception ignored){}
            }
        }.execute();
    }

    private void loadSelected() {
        int row = tableMed.getSelectedRow(); if(row<0) return;
        currentRef = (Integer) modelMed.getValueAt(row,0);
        try {
            Medicament m = medicamentBD.rechercherParRef(currentRef);
            if(m!=null){ lblRef.setText("Réf. #"+m.getRefMedicament()+" — "+m.getNom()); txtNom.setText(m.getNom()); txtDesc.setText(m.getDescriptio()!=null?m.getDescriptio():""); }
            refreshLots();
        } catch(SQLException ex){ PharmTheme.showError(this,"Erreur",ex.getMessage()); }
    }

    private void refreshLots() throws SQLException {
        modelLots.setRowCount(0);
        for(StockMedicament s : stockBD.getStocksParExpiration(currentRef)){
            modelLots.addRow(new Object[]{
                s.getNumStock(), s.getQuantiteProduit(),
                String.format("%.2f",s.getPrixAchat()), String.format("%.2f",s.getPrixVente()),
                s.getSeuilMin(),
                s.getDateFabrication()!=null?df.format(s.getDateFabrication()):"—",
                s.getDateExpiration()!=null?df.format(s.getDateExpiration()):"—"
            });
        }
    }

    // ── Save medicament info ──────────────────────────────────────────────
    private void saveMedicament() {
        if(currentRef<0){ PharmTheme.showWarning(this,"Aucune sélection","Sélectionnez un médicament."); return; }
        if(txtNom.getText().trim().isEmpty()){ PharmTheme.showError(this,"Validation","Le nom est obligatoire."); return; }
        try {
            Medicament m = new Medicament(); m.setRefMedicament(currentRef); m.setNom(txtNom.getText().trim()); m.setDescriptio(txtDesc.getText().trim());
            medicamentBD.modifier(m);
            PharmTheme.showSuccess(this,"Médicament modifié","Réf. #"+currentRef+" mis à jour.");
            loadMedicaments();
        } catch(SQLException ex){ PharmTheme.showError(this,"Erreur BD",ex.getMessage()); }
    }

    // ── Add stock lot ─────────────────────────────────────────────────────
    private void addLot() {
        if(currentRef<0){ PharmTheme.showWarning(this,"Aucune sélection","Sélectionnez un médicament."); return; }
        try {
            int    qte   = Integer.parseInt(txtQte.getText().trim());
            double pa    = Double.parseDouble(txtPrixA.getText().trim().replace(",","."));
            double pv    = Double.parseDouble(txtPrixV.getText().trim().replace(",","."));
            int    seuil = Integer.parseInt(txtSeuil.getText().trim());
            String fabStr = txtDateFab.getText().trim();
            String expStr = txtDateExp.getText().trim();

            StockMedicament s = new StockMedicament();
            s.setRefMedicament(currentRef); s.setQuantiteProduit(qte); s.setPrixAchat(pa); s.setPrixVente(pv); s.setSeuilMin(seuil);
            if(!fabStr.isEmpty() && !fabStr.equals("Fab jj/mm/aaaa")) s.setDateFabrication(df.parse(fabStr));
            if(!expStr.isEmpty() && !expStr.equals("Exp jj/mm/aaaa")) s.setDateExpiration(df.parse(expStr));

            int num = stockBD.ajouter(s);
            if(num>0){
                PharmTheme.showSuccess(this,"Lot ajouté","Lot #"+num+" créé pour réf. #"+currentRef);
                txtQte.setText(""); txtPrixA.setText(""); txtPrixV.setText(""); txtSeuil.setText(""); txtDateFab.setText(""); txtDateExp.setText("");
                refreshLots(); loadMedicaments();
            }
        } catch(NumberFormatException ex){ PharmTheme.showError(this,"Saisie invalide","Vérifiez les champs numériques.");
        } catch(ParseException ex)       { PharmTheme.showError(this,"Date invalide","Format : jj/mm/aaaa");
        } catch(SQLException ex)         { PharmTheme.showError(this,"Erreur BD",ex.getMessage()); }
    }

    // ── Delete selected lot ───────────────────────────────────────────────
    private void deleteLot() {
        int row = tableLots.getSelectedRow(); if(row<0){ PharmTheme.showWarning(this,"Aucune sélection","Sélectionnez un lot."); return; }
        int numStock = (Integer) modelLots.getValueAt(row,0);
        if(!PharmTheme.showConfirm(this,"Supprimer lot","Supprimer le lot #"+numStock+" ?")) return;
        try {
            stockBD.supprimerParNumStock(numStock);
            refreshLots(); loadMedicaments();
        } catch(SQLException ex){ PharmTheme.showError(this,"Erreur BD",ex.getMessage()); }
    }
}