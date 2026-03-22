package interfaces.stock;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import entite.*;
import entitebd.*;
import gestion.GestionStock;

public class RapportStockFrame extends PharmBaseFrame {

    // Stat chips
    private JLabel lblLots, lblAlertes, lblValeur, lblPerimes;
    // Chart
    private DonutChart donutChart;
    // Table
    private JTable            table;
    private DefaultTableModel tableModel;

    private final GestionStock gestionStock = new GestionStock();
    private final MedicamentBD medicamentBD = new MedicamentBD();
    private final StockBD      stockBD      = new StockBD();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public RapportStockFrame() {
        super("Rapport de stock", "État et statistiques complets du stock", 1260, 760);
        buildUI();
        genererRapport();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> genererRapport());
        JButton export  = PharmTheme.primaryButton("Exporter →");  export.addActionListener(e -> exporter());
        JButton close   = PharmTheme.ghostButton("Fermer");         close.addActionListener(e -> dispose());
        p.add(refresh); p.add(export); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 14));

        // ── Top: 4 stat chips ──
        JPanel chips = new JPanel(new GridLayout(1, 4, 12, 0));
        chips.setBackground(PharmTheme.BG);
        chips.setPreferredSize(new Dimension(0, 82));
        lblLots    = chip(chips, "Total lots",     "—", PharmTheme.PM_500);
        lblAlertes = chip(chips, "Lots en alerte", "—", PharmTheme.WARN);
        lblPerimes = chip(chips, "Lots périmés",   "—", PharmTheme.DANGER);
        lblValeur  = chip(chips, "Valeur totale",  "—", PharmTheme.INFO);
        contentArea.add(chips, BorderLayout.NORTH);

        // ── Center: donut chart LEFT + table RIGHT ──
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(PharmTheme.BG);
        split.setDividerSize(10);
        split.setDividerLocation(380);

        // Chart card
        JPanel chartCard = PharmTheme.card();
        chartCard.setLayout(new BorderLayout(0, 12));
        chartCard.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel chartTitle = new JLabel("Répartition du stock");
        chartTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        chartTitle.setForeground(PharmTheme.TXT);
        chartCard.add(chartTitle, BorderLayout.NORTH);

        donutChart = new DonutChart();
        chartCard.add(donutChart, BorderLayout.CENTER);
        split.setLeftComponent(chartCard);

        // Table card
        JPanel tableCard = new JPanel(new BorderLayout(0, 0));
        tableCard.setBackground(PharmTheme.BG);

        JPanel tableHead = new JPanel(new BorderLayout());
        tableHead.setBackground(PharmTheme.CARD);
        tableHead.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, PharmTheme.BORDER),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel tableTitle = new JLabel("Détail de tous les lots");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        tableTitle.setForeground(PharmTheme.TXT);
        tableHead.add(tableTitle, BorderLayout.WEST);
        tableCard.add(tableHead, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"N° Lot","Réf","Médicament","Qté","P.Achat","P.Vente","Seuil","Date fab.","Date exp.","Statut"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (c==0||c==1||c==3||c==6) ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        PharmTheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setIntercellSpacing(new Dimension(10, 2));
        int[] w = {65,50,175,55,80,80,55,88,88,105};
        for (int i=0;i<w.length;i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            table.getColumnModel().getColumn(i).setMinWidth(w[i]-10);
        }
        // Status badge renderer
        table.getColumnModel().getColumn(9).setCellRenderer((t,v,sel,foc,r,c) -> {
            String s = String.valueOf(v);
            PharmTheme.BadgeType bt = s.equals("Normal") ? PharmTheme.BadgeType.SUCCESS
                : s.contains("Périmé")||s.equals("Rupture") ? PharmTheme.BadgeType.DANGER
                : PharmTheme.BadgeType.WARN;
            return PharmTheme.badgeComponent(s, bt);
        });
        // Highlight alert rows
        TableCellRenderer rowR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if (!sel) {
                    String stat = String.valueOf(tableModel.getValueAt(
                        t.convertRowIndexToModel(r), 9));
                    comp.setBackground(stat.contains("Périmé") ? new Color(0xFFF0F0)
                        : stat.contains("Alerte") ? new Color(0xFFF8E1)
                        : PharmTheme.CARD);
                    comp.setForeground(PharmTheme.TXT);
                }
                return comp;
            }
        };
        for (int i=0;i<9;i++) table.getColumnModel().getColumn(i).setCellRenderer(rowR);

        JScrollPane tsp = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        tsp.setBorder(BorderFactory.createEmptyBorder());
        tsp.setBackground(PharmTheme.CARD); tsp.getViewport().setBackground(PharmTheme.CARD);
        PharmTheme.styleScrollBar(tsp.getVerticalScrollBar());
        PharmTheme.styleScrollBar(tsp.getHorizontalScrollBar());
        tableCard.add(tsp, BorderLayout.CENTER);
        split.setRightComponent(tableCard);

        contentArea.add(split, BorderLayout.CENTER);
    }

    private JLabel chip(JPanel parent, String label, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); g.setColor(accent); g.fillRect(0,0,4,getHeight());
            }
        };
        c.setBackground(PharmTheme.CARD);
        c.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(12,18,12,18)));
        JPanel inner = new JPanel(); inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS)); inner.setBackground(PharmTheme.CARD);
        JLabel lbl = PharmTheme.helperLabel(label); lbl.setAlignmentX(LEFT_ALIGNMENT);
        JLabel val = new JLabel(value); val.setFont(new Font("SansSerif",Font.BOLD,24)); val.setForeground(PharmTheme.TXT); val.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(lbl); inner.add(Box.createVerticalStrut(2)); inner.add(val);
        c.add(inner,BorderLayout.CENTER); parent.add(c); return val;
    }

    // ── Data ──────────────────────────────────────────────────────────────
    private void genererRapport() {
        new SwingWorker<Void, Void>() {
            List<StockMedicament> tous; List<StockMedicament> alertes;
            double valeur; int perimes, ruptures, normal;

            @Override protected Void doInBackground() throws Exception {
                tous    = stockBD.listerTous();
                alertes = gestionStock.obtenirAlertes();
                valeur  = gestionStock.calculerValeurTotaleStock();
                perimes = (int) tous.stream().filter(StockMedicament::estPerime).count();
                ruptures= (int) tous.stream().filter(s->s.getQuantiteProduit()==0).count();
                normal  = (int) tous.stream().filter(s->!s.estPerime()&&!s.Alerte()&&s.getQuantiteProduit()>0).count();
                return null;
            }

            @Override protected void done() {
                try {
                    // Chips
                    lblLots.setText(String.valueOf(tous.size()));
                    lblAlertes.setText(String.valueOf(alertes.size()));
                    lblAlertes.setForeground(alertes.isEmpty()?PharmTheme.SUCCESS:PharmTheme.WARN);
                    lblPerimes.setText(String.valueOf(perimes));
                    lblPerimes.setForeground(perimes>0?PharmTheme.DANGER:PharmTheme.SUCCESS);
                    lblValeur.setText(String.format("%.0f DT",valeur));

                    // Donut chart data
                    donutChart.setData(normal, alertes.size(), perimes, ruptures, valeur);
                    donutChart.repaint();

                    // Table
                    tableModel.setRowCount(0);
                    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                    table.setRowSorter(sorter);

                    for (StockMedicament s : tous) {
                        String nom="—";
                        try { Medicament m=medicamentBD.rechercherParRef(s.getRefMedicament()); if(m!=null) nom=m.getNom(); } catch(Exception ignored){}
                        String statut = s.estPerime()?"Périmé":s.getQuantiteProduit()==0?"Rupture":s.Alerte()?"Alerte stock":"Normal";
                        tableModel.addRow(new Object[]{
                            s.getNumStock(), s.getRefMedicament(), nom, s.getQuantiteProduit(),
                            String.format("%.2f DT",s.getPrixAchat()),
                            String.format("%.2f DT",s.getPrixVente()),
                            s.getSeuilMin(),
                            s.getDateFabrication()!=null?df.format(s.getDateFabrication()):"—",
                            s.getDateExpiration() !=null?df.format(s.getDateExpiration()) :"—",
                            statut
                        });
                    }
                } catch (Exception ex) {
                    PharmTheme.showError(RapportStockFrame.this,"Erreur",ex.getMessage());
                }
            }
        }.execute();
    }

    private void exporter() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("rapport_stock_"+
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".txt"));
        if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) {
            pw.println("RAPPORT DE STOCK - PharmaSys");
            pw.println("Généré le : "+new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            pw.println("─".repeat(60));
            pw.println("Total lots    : "+lblLots.getText());
            pw.println("Alertes       : "+lblAlertes.getText());
            pw.println("Périmés       : "+lblPerimes.getText());
            pw.println("Valeur totale : "+lblValeur.getText());
            pw.println("─".repeat(60));
            pw.printf("%-6s %-6s %-22s %-6s %-10s %-10s %-6s %-12s %-12s %s%n",
                "Lot","Réf","Médicament","Qté","P.Achat","P.Vente","Seuil","Date fab.","Date exp.","Statut");
            for (int r=0;r<tableModel.getRowCount();r++) {
                for (int c=0;c<tableModel.getColumnCount();c++) {
                    if(c>0) pw.print("\t");
                    pw.print(tableModel.getValueAt(r,c));
                }
                pw.println();
            }
            PharmTheme.showSuccess(this,"Exporté",fc.getSelectedFile().getName());
        } catch (Exception ex) { PharmTheme.showError(this,"Erreur export",ex.getMessage()); }
    }

    // ── Donut Chart ───────────────────────────────────────────────────────
    static class DonutChart extends JPanel {
        private int normal=0, alerte=0, perime=0, rupture=0;
        private double valeur=0;
        private Integer hovered = null; // segment index

        // Colors matching app palette
        private static final Color[] COLORS = {
            new Color(0x1A6B45),  // normal  = deep green
            new Color(0xD97706),  // alerte  = amber
            new Color(0xC62828),  // perimé  = red
            new Color(0x6B7280),  // rupture = gray
        };
        private static final Color[] COLORS_HOV = {
            new Color(0x12543F),
            new Color(0xB45309),
            new Color(0x9B1B1B),
            new Color(0x4B5563),
        };
        private static final String[] LABELS = {"Normal","Alerte","Périmé","Rupture"};

        DonutChart() {
            setBackground(PharmTheme.CARD);
            setOpaque(false);
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                    Integer newHov = getSegmentAt(e.getX(), e.getY());
                    if (!Objects.equals(newHov, hovered)) { hovered=newHov; repaint(); }
                }
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseExited(java.awt.event.MouseEvent e) { hovered=null; repaint(); }
            });
        }

        void setData(int normal, int alerte, int perime, int rupture, double valeur) {
            this.normal=normal; this.alerte=alerte; this.perime=perime;
            this.rupture=rupture; this.valeur=valeur;
        }

        private int[] getValues() { return new int[]{normal,alerte,perime,rupture}; }
        private int total() { return normal+alerte+perime+rupture; }

        private Integer getSegmentAt(int mx, int my) {
            int W=getWidth(), H=getHeight();
            int size=Math.min(W,H)-40;
            int cx=W/2, cy=H/2-30;
            double dx=mx-cx, dy=my-cy;
            double dist=Math.sqrt(dx*dx+dy*dy);
            double inner=size*0.28, outer=size/2.0;
            if (dist<inner||dist>outer) return null;
            double angle=Math.toDegrees(Math.atan2(dy,dx));
            if(angle<0) angle+=360;
            int tot=total(); if(tot==0) return null;
            int[] vals=getValues();
            double start=-90;
            for(int i=0;i<vals.length;i++){
                if(vals[i]==0) continue;
                double sweep=360.0*vals[i]/tot;
                double end=start+sweep;
                double a=angle; if(a<start%360&&start<0) a+=360;
                // normalize
                double s2=(start+360)%360, e2=(end+360)%360, a2=(angle+360)%360;
                boolean inArc;
                if(s2<=e2) inArc = a2>=s2&&a2<=e2;
                else       inArc = a2>=s2||a2<=e2;
                if(inArc) return i;
                start=end;
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W=getWidth(), H=getHeight();
            int size=Math.min(W, H-80)-20;
            if(size<60){g2.dispose();return;}
            int cx=W/2, cy=H/2-20;
            int r=(int)(size/2.0);
            int inner=(int)(r*0.55);

            int tot=total();
            int[] vals=getValues();

            if(tot==0){
                g2.setColor(PharmTheme.BORDER);
                g2.setStroke(new BasicStroke(14,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
                g2.drawOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(PharmTheme.TXT3);
                g2.setFont(new Font("SansSerif",Font.PLAIN,13));
                String msg="Aucune donnée";
                g2.drawString(msg,cx-g2.getFontMetrics().stringWidth(msg)/2,cy+5);
                g2.dispose(); return;
            }

            // Draw segments
            double startAngle=-90;
            for(int i=0;i<vals.length;i++){
                if(vals[i]==0) continue;
                double sweep=360.0*vals[i]/tot;
                boolean hov=(hovered!=null&&hovered==i);
                Color col=hov?COLORS_HOV[i]:COLORS[i];

                // Explode hovered segment
                int ox=0,oy=0;
                if(hov){
                    double mid=Math.toRadians(startAngle+sweep/2);
                    ox=(int)(Math.cos(mid)*8); oy=(int)(Math.sin(mid)*8);
                }

                Arc2D arc=new Arc2D.Double(cx-r+ox,cy-r+oy,r*2,r*2,startAngle,-sweep,Arc2D.PIE);
                g2.setColor(col); g2.fill(arc);
                // White gap between segments
                g2.setColor(PharmTheme.CARD);
                g2.setStroke(new BasicStroke(2));
                g2.draw(arc);

                startAngle+=sweep;
            }

            // Inner white circle (donut hole)
            g2.setColor(PharmTheme.CARD);
            g2.fillOval(cx-inner,cy-inner,inner*2,inner*2);
            g2.setColor(PharmTheme.BORDER);
            g2.setStroke(new BasicStroke(1));
            g2.drawOval(cx-inner,cy-inner,inner*2,inner*2);

            // Center text
            if(hovered!=null&&hovered<vals.length&&vals[hovered]>0){
                double pct=100.0*vals[hovered]/tot;
                g2.setFont(new Font("SansSerif",Font.BOLD,22));
                g2.setColor(COLORS[hovered]);
                String pctStr=String.format("%.0f%%",pct);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(pctStr,cx-fm.stringWidth(pctStr)/2,cy+fm.getAscent()/2-2);
                g2.setFont(new Font("SansSerif",Font.PLAIN,12));
                g2.setColor(PharmTheme.TXT2);
                String lbl=LABELS[hovered];
                fm=g2.getFontMetrics();
                g2.drawString(lbl,cx-fm.stringWidth(lbl)/2,cy+fm.getAscent()/2+16);
            } else {
                g2.setFont(new Font("SansSerif",Font.BOLD,20));
                g2.setColor(PharmTheme.TXT);
                String totStr=String.valueOf(tot);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(totStr,cx-fm.stringWidth(totStr)/2,cy+fm.getAscent()/2-4);
                g2.setFont(new Font("SansSerif",Font.PLAIN,12));
                g2.setColor(PharmTheme.TXT3);
                String sub="lots";
                fm=g2.getFontMetrics();
                g2.drawString(sub,cx-fm.stringWidth(sub)/2,cy+fm.getAscent()/2+14);
            }

            // Legend
            int legendY=cy+r+22;
            int legendX=cx-(LABELS.length*90)/2;
            for(int i=0;i<LABELS.length;i++){
                if(vals[i]==0) continue;
                boolean hov=(hovered!=null&&hovered==i);
                int lx=legendX+i*90;
                // Colored square
                g2.setColor(hov?COLORS_HOV[i]:COLORS[i]);
                g2.fillRoundRect(lx,legendY,12,12,4,4);
                // Label + count
                g2.setColor(hov?PharmTheme.TXT:PharmTheme.TXT2);
                g2.setFont(new Font("SansSerif",hov?Font.BOLD:Font.PLAIN,12));
                g2.drawString(LABELS[i]+" ("+vals[i]+")",lx+16,legendY+11);
            }

            // Valeur strip
            g2.setFont(new Font("SansSerif",Font.PLAIN,13));
            g2.setColor(PharmTheme.TXT3);
            String vStr="Valeur : "+String.format("%.2f DT",valeur);
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(vStr,cx-fm.stringWidth(vStr)/2,legendY+32);

            g2.dispose();
        }
    }
}