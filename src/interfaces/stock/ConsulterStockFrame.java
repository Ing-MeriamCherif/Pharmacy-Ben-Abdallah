package interfaces.stock;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import entitebd.ConnectionBD;

/**
 * ConsulterStockFrame — Vue complète stock × médicament
 * Colonnes: N° Lot, Réf, Nom, Description, Qté, P.Achat, P.Vente, Seuil, Date Fab, Date Exp, Statut
 * Toutes les dates viennent de stock_medicament
 */
public class ConsulterStockFrame extends PharmBaseFrame {
    private JTable              table;
    private DefaultTableModel   model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField          txtSearch;
    private JComboBox<String>   cmbFilter;
    private JLabel              statTotal, statValeur, statAlertes, statRuptures;
    private JPanel              detailPanel;
    private JLabel              detailNom, detailRef, detailStatut, detailQte, detailPrixA, detailPrixV, detailMarge, detailSeuil, detailFab, detailExp;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public ConsulterStockFrame() {
        super("Consultation du stock", "Vue complète · Lot × Médicament", 1440, 760);
        buildUI(); loadData();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.primaryButton("↻ Actualiser"); refresh.addActionListener(e -> loadData());
        JButton export  = PharmTheme.ghostButton("Exporter CSV");   export.addActionListener(e -> exportCSV());
        p.add(export); p.add(refresh);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0,12));
        contentArea.add(buildStatStrip(), BorderLayout.NORTH);
        JPanel main = new JPanel(new BorderLayout(12,0)); main.setBackground(PharmTheme.BG);
        main.add(buildTablePanel(), BorderLayout.CENTER);
        main.add(buildDetailPanel(), BorderLayout.EAST);
        contentArea.add(main, BorderLayout.CENTER);
    }

    private JPanel buildStatStrip() {
        JPanel strip = new JPanel(new GridLayout(1,4,10,0)); strip.setBackground(PharmTheme.BG); strip.setPreferredSize(new Dimension(0,72));
        statTotal    = chip(strip,"Total lots","—",PharmTheme.PM_500);
        statValeur   = chip(strip,"Valeur stock","—",PharmTheme.INFO);
        statAlertes  = chip(strip,"Alertes","—",PharmTheme.WARN);
        statRuptures = chip(strip,"Ruptures","—",PharmTheme.DANGER);
        return strip;
    }

    private JLabel chip(JPanel parent, String label, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);g.setColor(accent);g.fillRect(0,0,4,getHeight());}};
        c.setBackground(PharmTheme.CARD); c.setBorder(javax.swing.BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R8,PharmTheme.BORDER,0),javax.swing.BorderFactory.createEmptyBorder(10,16,10,16)));
        JPanel inner=new JPanel();inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS));inner.setBackground(PharmTheme.CARD);
        JLabel lbl=PharmTheme.helperLabel(label);lbl.setAlignmentX(LEFT_ALIGNMENT);
        JLabel val=new JLabel(value);val.setFont(new Font("SansSerif",Font.BOLD,20));val.setForeground(PharmTheme.TXT);val.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(lbl);inner.add(val);c.add(inner,BorderLayout.CENTER);parent.add(c);return val;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0,8)); p.setBackground(PharmTheme.BG);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); bar.setBackground(PharmTheme.BG);
        txtSearch = PharmTheme.textField("Rechercher nom, réf…"); txtSearch.setPreferredSize(new Dimension(260,34)); txtSearch.addCaretListener(e->applyFilter());
        cmbFilter = PharmTheme.comboBox(new String[]{"Tous","En stock","Alerte stock","Rupture","Périmés","Exp. proche"});
        cmbFilter.setPreferredSize(new Dimension(150,34)); cmbFilter.addActionListener(e->applyFilter());
        bar.add(txtSearch); bar.add(cmbFilter); p.add(bar,BorderLayout.NORTH);

        // Columns: N°Lot, Réf, Nom, Desc, Qté, P.Achat, P.Vente, Seuil, Date Fab, Date Exp, Statut
        String[] cols={"N° Lot","Réf.","Médicament","Description","Qté","Prix achat","Prix vente","Seuil","Date fab.","Date exp.","Statut"};
        model = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){return(c==0||c==1||c==4||c==7)?Integer.class:String.class;}
        };
        table = new JTable(model);
        PharmTheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setIntercellSpacing(new Dimension(12, 2));
        table.setShowVerticalLines(false);
        sorter = new TableRowSorter<>(model); table.setRowSorter(sorter);
        // [N°Lot, Réf, Médicament, Description, Qté, P.Achat, P.Vente, Seuil, Date fab, Date exp, Statut]
        int[] pref = {70, 55, 185, 150, 70, 95, 95, 65, 96, 96, 115};
        int[] min  = {60, 50, 150, 120, 60, 85, 85, 55, 88, 88, 100};
        for (int i = 0; i < pref.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(pref[i]);
            table.getColumnModel().getColumn(i).setMinWidth(min[i]);
        }
        table.getColumnModel().getColumn(10).setCellRenderer((t,v,sel,foc,r,c)->{
            String s=v!=null?v.toString():"";
            PharmTheme.BadgeType bt=s.equals("Normal")?PharmTheme.BadgeType.SUCCESS:s.equals("Rupture")||s.contains("Périmé")?PharmTheme.BadgeType.DANGER:PharmTheme.BadgeType.WARN;
            return PharmTheme.badgeComponent(s,bt);
        });
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())showDetail();});
        JScrollPane tsp = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        tsp.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        tsp.setBackground(PharmTheme.CARD);
        tsp.getViewport().setBackground(PharmTheme.CARD);
        PharmTheme.styleScrollBar(tsp.getVerticalScrollBar());
        PharmTheme.styleScrollBar(tsp.getHorizontalScrollBar());
        p.add(tsp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildDetailPanel() {
        detailPanel=PharmTheme.card(); detailPanel.setLayout(new BoxLayout(detailPanel,BoxLayout.Y_AXIS)); detailPanel.setPreferredSize(new Dimension(240,0));
        detailPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R12,PharmTheme.BORDER,0),javax.swing.BorderFactory.createEmptyBorder(16,16,16,16)));
        JLabel title=PharmTheme.sectionLabel("Détails du lot");title.setAlignmentX(LEFT_ALIGNMENT);detailPanel.add(title);detailPanel.add(Box.createVerticalStrut(10));
        detailNom=mkV("—");detailRef=mkM("—");detailStatut=mkM("");
        detailPanel.add(detailNom);detailPanel.add(detailRef);detailPanel.add(detailStatut);
        detailPanel.add(Box.createVerticalStrut(10));detailPanel.add(PharmTheme.separator());detailPanel.add(Box.createVerticalStrut(8));
        detailQte=dRow("Quantité");detailPrixA=dRow("Prix achat");detailPrixV=dRow("Prix vente");
        detailMarge=dRow("Marge");detailSeuil=dRow("Seuil min");detailFab=dRow("Fabriqué");detailExp=dRow("Expire le");
        detailPanel.add(Box.createVerticalGlue());
        return detailPanel;
    }
    private JLabel mkV(String t){JLabel l=new JLabel(t);l.setFont(PharmTheme.FONT_H3);l.setForeground(PharmTheme.TXT);l.setAlignmentX(LEFT_ALIGNMENT);return l;}
    private JLabel mkM(String t){JLabel l=new JLabel(t);l.setFont(PharmTheme.FONT_SM);l.setForeground(PharmTheme.TXT3);l.setAlignmentX(LEFT_ALIGNMENT);return l;}
    private JLabel dRow(String label){
        JPanel row=new JPanel(new BorderLayout());row.setBackground(PharmTheme.CARD);row.setAlignmentX(LEFT_ALIGNMENT);row.setMaximumSize(new Dimension(Integer.MAX_VALUE,24));
        JLabel lbl=new JLabel(label);lbl.setFont(PharmTheme.FONT_SM);lbl.setForeground(PharmTheme.TXT3);
        JLabel val=new JLabel("—");val.setFont(new Font("SansSerif",Font.BOLD,12));val.setForeground(PharmTheme.TXT);
        row.add(lbl,BorderLayout.WEST);row.add(val,BorderLayout.EAST);detailPanel.add(row);detailPanel.add(Box.createVerticalStrut(4));return val;
    }

    private void showDetail() {
        int sel=table.getSelectedRow();if(sel<0)return;
        int row=table.convertRowIndexToModel(sel);
        detailNom.setText((String)model.getValueAt(row,2));
        detailRef.setText("Réf. #"+model.getValueAt(row,1)+" · Lot #"+model.getValueAt(row,0));
        detailStatut.setText((String)model.getValueAt(row,10));
        int qte=(Integer)model.getValueAt(row,4),seuil=(Integer)model.getValueAt(row,7);
        detailQte.setText(qte+" unités");detailQte.setForeground(qte==0?PharmTheme.DANGER:qte<=seuil?PharmTheme.WARN:PharmTheme.SUCCESS);
        String pa=(String)model.getValueAt(row,5),pv=(String)model.getValueAt(row,6);
        detailPrixA.setText(pa);detailPrixV.setText(pv);
        try{double a=Double.parseDouble(pa.replace(" DT","").replace(",","."));double v=Double.parseDouble(pv.replace(" DT","").replace(",","."));double m=a>0?((v-a)/a)*100:0;detailMarge.setText(String.format("%.1f%%",m));detailMarge.setForeground(m<0?PharmTheme.DANGER:PharmTheme.SUCCESS);}catch(NumberFormatException e){detailMarge.setText("—");}
        detailSeuil.setText(String.valueOf(seuil));
        detailFab.setText((String)model.getValueAt(row,8));
        detailExp.setText((String)model.getValueAt(row,9));
    }

    private void loadData() {
        model.setRowCount(0);
        // All dates come from stock_medicament; medicament only has nom and descriptio
        String sql = "SELECT s.num_stock, m.ref_medicament, m.nom, m.descriptio, " +
            "s.quantite_produit, s.prix_achat, s.prix_vente, s.seuil_min, " +
            "s.date_fabrication, s.date_expiration " +
            "FROM stock_medicament s JOIN medicament m ON s.ref_medicament=m.ref_medicament ORDER BY s.num_stock";

        new SwingWorker<Void,Object[]>(){
            int tot=0,al=0,ru=0;double val=0;
            @Override protected Void doInBackground() throws Exception {
                try(Connection con=ConnectionBD.getConnection();Statement st=con.createStatement();ResultSet rs=st.executeQuery(sql)){
                    while(rs.next()){
                        int ns=rs.getInt("num_stock"),ref=rs.getInt("ref_medicament");
                        int qte=rs.getInt("quantite_produit"),seuil=rs.getInt("seuil_min");
                        String nom=rs.getString("nom"),desc=rs.getString("descriptio");
                        double pa=rs.getDouble("prix_achat"),pv=rs.getDouble("prix_vente");
                        java.sql.Date fab=rs.getDate("date_fabrication"),exp=rs.getDate("date_expiration");
                        val+=qte*pa;tot++;
                        String statut;
                        if(qte==0){statut="Rupture";ru++;}
                        else if(exp!=null&&new java.util.Date().after(exp)){statut="Périmé";al++;}
                        else if(qte<=seuil){statut="Alerte stock";al++;}
                        else if(exp!=null&&(exp.getTime()-System.currentTimeMillis())<30L*24*3600*1000){statut="Exp. proche";}
                        else{statut="Normal";}
                        publish(new Object[]{ns,ref,nom!=null?nom:"",desc!=null?desc:"",qte,
                            String.format("%.2f DT",pa),String.format("%.2f DT",pv),seuil,
                            fab!=null?df.format(fab):"—",exp!=null?df.format(exp):"—",statut});
                    }
                }
                return null;
            }
            @Override protected void process(List<Object[]> chunks){for(Object[] r:chunks)model.addRow(r);}
            @Override protected void done(){
                statTotal.setText(String.valueOf(tot));statValeur.setText(String.format("%.0f DT",val));
                statAlertes.setText(String.valueOf(al));statRuptures.setText(String.valueOf(ru));
                statAlertes.setForeground(al>0?PharmTheme.WARN:PharmTheme.SUCCESS);
                statRuptures.setForeground(ru>0?PharmTheme.DANGER:PharmTheme.SUCCESS);
            }
        }.execute();
    }

    private void applyFilter(){
        String text=txtSearch.getText().trim();
        String filter=(String)cmbFilter.getSelectedItem();
        List<RowFilter<DefaultTableModel,Object>> filters=new ArrayList<>();
        if(!text.isEmpty()) filters.add(RowFilter.regexFilter("(?i)"+text,2,1));
        if(!"Tous".equals(filter)){
            final String f=filter;
            filters.add(new RowFilter<DefaultTableModel,Object>(){
                @Override public boolean include(Entry<? extends DefaultTableModel,? extends Object> e){
                    String stat=e.getStringValue(10);int qte;
                    try{qte=Integer.parseInt(e.getStringValue(4));}catch(NumberFormatException ex){qte=0;}
                    switch(f){
                        case "En stock":    return qte>0&&!stat.contains("Périmé");
                        case "Alerte stock":return stat.equals("Alerte stock");
                        case "Rupture":     return qte==0;
                        case "Périmés":     return stat.contains("Périmé");
                        case "Exp. proche": return stat.equals("Exp. proche");
                        default:            return true;
                    }
                }
            });
        }
        if(filters.isEmpty()) sorter.setRowFilter(null);
        else if(filters.size()==1) sorter.setRowFilter(filters.get(0));
        else sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private void exportCSV(){
        JFileChooser fc=new JFileChooser();fc.setSelectedFile(new java.io.File("stock_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date())+".csv"));
        if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        try(java.io.PrintWriter pw=new java.io.PrintWriter(fc.getSelectedFile())){
            for(int c=0;c<model.getColumnCount();c++){if(c>0)pw.print(";");pw.print("\""+model.getColumnName(c)+"\"");}pw.println();
            for(int r=0;r<model.getRowCount();r++){for(int c=0;c<model.getColumnCount();c++){if(c>0)pw.print(";");Object v=model.getValueAt(r,c);pw.print("\""+(v!=null?v:"")+"\"");}pw.println();}
            PharmTheme.showSuccess(this,"Export réussi",fc.getSelectedFile().getName());
        }catch(java.io.IOException ex){PharmTheme.showError(this,"Erreur",ex.getMessage());}
    }
}