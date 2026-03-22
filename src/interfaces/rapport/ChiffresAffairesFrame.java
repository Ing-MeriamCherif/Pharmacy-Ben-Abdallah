package interfaces.rapport;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import entite.Vente;
import entitebd.VenteBD;
import gestion.GestionVente;

public class ChiffresAffairesFrame extends PharmBaseFrame {

    private JLabel  lblNbVentes, lblTotal, lblMoyenne, lblMeilleur;
    private JTable  table;
    private DefaultTableModel tableModel;
    private ChartPanel chartPanel;

    private final GestionVente gestionVente = new GestionVente();
    private final VenteBD      venteBD      = new VenteBD();

    public ChiffresAffairesFrame() {
        super("Chiffre d'affaires", "Analyse des ventes", 1200, 740);
        buildUI();
        generer();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton refresh = PharmTheme.ghostButton("↻ Actualiser"); refresh.addActionListener(e -> generer());
        JButton export  = PharmTheme.primaryButton("Exporter →");  export.addActionListener(e -> exporter());
        JButton close   = PharmTheme.ghostButton("Fermer");         close.addActionListener(e -> dispose());
        p.add(refresh); p.add(export); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 14));

        // ── Top: 4 stat chips ──────────────────────────────────────────
        JPanel chips = new JPanel(new GridLayout(1, 4, 12, 0));
        chips.setBackground(PharmTheme.BG);
        chips.setPreferredSize(new Dimension(0, 80));
        lblNbVentes  = chip(chips, "Nombre de ventes",      "—", PharmTheme.PM_500);
        lblTotal     = chip(chips, "Chiffre d'affaires",    "—", PharmTheme.SUCCESS);
        lblMoyenne   = chip(chips, "Panier moyen",          "—", PharmTheme.INFO);
        lblMeilleur  = chip(chips, "Meilleure vente",       "—", PharmTheme.WARN);
        contentArea.add(chips, BorderLayout.NORTH);

        // ── Center: chart LEFT + table RIGHT ──────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(PharmTheme.BG);
        split.setDividerSize(8);
        split.setDividerLocation(560);

        // Chart card
        JPanel chartCard = PharmTheme.card();
        chartCard.setLayout(new BorderLayout(0, 0));
        chartCard.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel chartTitle = new JLabel("Ventes par mois");
        chartTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        chartTitle.setForeground(PharmTheme.TXT);
        chartTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        chartCard.add(chartTitle, BorderLayout.NORTH);

        chartPanel = new ChartPanel();
        chartCard.add(chartPanel, BorderLayout.CENTER);
        split.setLeftComponent(chartCard);

        // Table card
        JPanel tableCard = PharmTheme.card();
        tableCard.setLayout(new BorderLayout(0, 0));
        tableCard.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0));

        JPanel tableHead = new JPanel(new BorderLayout());
        tableHead.setBackground(PharmTheme.CARD);
        tableHead.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, PharmTheme.BORDER),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel tableTitle = new JLabel("Détail des ventes");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        tableTitle.setForeground(PharmTheme.TXT);
        tableHead.add(tableTitle, BorderLayout.WEST);
        tableCard.add(tableHead, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"N°", "Date", "Montant (DT)", "Client", "Employé"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        PharmTheme.styleTable(table);
        table.setIntercellSpacing(new Dimension(10, 2));
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        int[] w = {50, 110, 110, 130, 110};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        // Right-align montant column
        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightR);

        tableCard.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);
        split.setRightComponent(tableCard);

        contentArea.add(split, BorderLayout.CENTER);
    }

    private JLabel chip(JPanel parent, String label, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accent);
                g.fillRect(0, 0, 4, getHeight());
            }
        };
        c.setBackground(PharmTheme.CARD);
        c.setBorder(BorderFactory.createCompoundBorder(
            new PharmTheme.RoundedBorder(PharmTheme.R8, PharmTheme.BORDER, 0),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(PharmTheme.CARD);
        JLabel lbl = PharmTheme.helperLabel(label); lbl.setAlignmentX(LEFT_ALIGNMENT);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 22));
        val.setForeground(PharmTheme.TXT);
        val.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(lbl); inner.add(Box.createVerticalStrut(2)); inner.add(val);
        c.add(inner, BorderLayout.CENTER);
        parent.add(c);
        return val;
    }

    // ── Data ──────────────────────────────────────────────────────────────
    private void generer() {
        new SwingWorker<List<Vente>, Void>() {
            @Override protected List<Vente> doInBackground() throws Exception {
                return venteBD.getAllVentes();
            }
            @Override protected void done() {
                try {
                    List<Vente> ventes = get();
                    double ca = ventes.stream().mapToDouble(Vente::getMontantTotalVente).sum();
                    double max = ventes.stream().mapToDouble(Vente::getMontantTotalVente).max().orElse(0);
                    double avg = ventes.isEmpty() ? 0 : ca / ventes.size();

                    // Update chips
                    lblNbVentes.setText(String.valueOf(ventes.size()));
                    lblTotal.setText(String.format("%.2f DT", ca));
                    lblMoyenne.setText(String.format("%.2f DT", avg));
                    lblMeilleur.setText(String.format("%.2f DT", max));

                    // Update table
                    tableModel.setRowCount(0);
                    SimpleDateFormat dfIn = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat dfOut = new SimpleDateFormat("dd/MM/yyyy");
                    for (Vente v : ventes) {
                        String dateStr = v.getDateVente();
                        try {
                            Date d = dfIn.parse(dateStr);
                            dateStr = dfOut.format(d);
                        } catch (Exception ignored) {}
                        tableModel.addRow(new Object[]{
                            v.getNumVente(), dateStr,
                            String.format("%.2f", v.getMontantTotalVente()),
                            "Client #" + v.getNumClient(),
                            "Emp. #" + v.getNumCarteEmp()
                        });
                    }

                    // Build monthly chart data
                    LinkedHashMap<String, Double> monthly = new LinkedHashMap<>();
                    SimpleDateFormat dfMonth = new SimpleDateFormat("MM/yyyy");
                    for (Vente v : ventes) {
                        String key = "?";
                        try {
                            Date d = new SimpleDateFormat("yyyy-MM-dd").parse(v.getDateVente());
                            key = dfMonth.format(d);
                        } catch (Exception ignored) { key = v.getDateVente().substring(0, 7); }
                        monthly.merge(key, v.getMontantTotalVente(), Double::sum);
                    }
                    // Sort by date key
                    List<Map.Entry<String, Double>> entries = new ArrayList<>(monthly.entrySet());
                    entries.sort(Comparator.comparing(e -> {
                        try { return new SimpleDateFormat("MM/yyyy").parse(e.getKey()); }
                        catch (Exception ex) { return new Date(0); }
                    }));
                    LinkedHashMap<String, Double> sorted = new LinkedHashMap<>();
                    for (Map.Entry<String, Double> e : entries) sorted.put(e.getKey(), e.getValue());

                    chartPanel.setData(sorted, ca);
                    chartPanel.repaint();

                } catch (Exception ex) {
                    PharmTheme.showError(ChiffresAffairesFrame.this, "Erreur", ex.getMessage());
                }
            }
        }.execute();
    }

    private void exporter() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("rapport_ca_" +
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) {
            pw.println("RAPPORT CHIFFRE D'AFFAIRES");
            pw.println("Généré le : " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            pw.println("─────────────────────────────────────");
            pw.println("Nombre de ventes    : " + lblNbVentes.getText());
            pw.println("Chiffre d'affaires  : " + lblTotal.getText());
            pw.println("Panier moyen        : " + lblMoyenne.getText());
            pw.println("Meilleure vente     : " + lblMeilleur.getText());
            pw.println("─────────────────────────────────────");
            pw.println("N°\tDate\tMontant\tClient\tEmployé");
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                pw.printf("%s\t%s\t%s\t%s\t%s%n",
                    tableModel.getValueAt(r,0), tableModel.getValueAt(r,1),
                    tableModel.getValueAt(r,2), tableModel.getValueAt(r,3),
                    tableModel.getValueAt(r,4));
            }
            PharmTheme.showSuccess(this, "Exporté", fc.getSelectedFile().getName());
        } catch (Exception ex) { PharmTheme.showError(this, "Erreur export", ex.getMessage()); }
    }

    // ── Custom bar chart ──────────────────────────────────────────────────
    static class ChartPanel extends JPanel {

        private LinkedHashMap<String, Double> data = new LinkedHashMap<>();
        private double totalCA = 0;
        private Integer hoveredBar = null;

        ChartPanel() {
            setBackground(PharmTheme.CARD);
            setOpaque(true);
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                    // Detect hovered bar for tooltip effect
                    if (data.isEmpty()) return;
                    int pad = 40, barArea = getWidth() - pad * 2;
                    int n = data.size();
                    int barW = Math.max(20, barArea / (n * 2));
                    int gap  = barArea / n - barW;
                    int x = pad;
                    int idx = 0;
                    Integer newHov = null;
                    for (Map.Entry<String, Double> entry : data.entrySet()) {
                        if (e.getX() >= x && e.getX() <= x + barW + gap) { newHov = idx; break; }
                        x += barW + gap; idx++;
                    }
                    if (!Objects.equals(newHov, hoveredBar)) { hoveredBar = newHov; repaint(); }
                }
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseExited(java.awt.event.MouseEvent e) { hoveredBar = null; repaint(); }
            });
        }

        void setData(LinkedHashMap<String, Double> data, double totalCA) {
            this.data = data; this.totalCA = totalCA;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int padL = 68, padR = 20, padT = 20, padB = 56;
            int chartW = W - padL - padR;
            int chartH = H - padT - padB;

            if (data.isEmpty()) {
                g2.setColor(PharmTheme.TXT3);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
                String msg = "Aucune donnée disponible";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2);
                g2.dispose(); return;
            }

            double maxVal = data.values().stream().mapToDouble(d -> d).max().orElse(1);

            // ── Grid lines ──
            int gridLines = 5;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int i = 0; i <= gridLines; i++) {
                int y = padT + chartH - (int)((double) i / gridLines * chartH);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1, new float[]{4, 4}, 0));
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setStroke(new BasicStroke(1));
                // Y-axis label
                double val = maxVal * i / gridLines;
                String label = val >= 1000 ? String.format("%.0fk", val/1000) : String.format("%.0f", val);
                g2.setColor(PharmTheme.TXT3);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, padL - fm.stringWidth(label) - 6, y + fm.getAscent()/2 - 1);
            }

            // ── Baseline ──
            g2.setColor(PharmTheme.BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);
            g2.drawLine(padL, padT, padL, padT + chartH);
            g2.setStroke(new BasicStroke(1));

            // ── Bars ──
            int n = data.size();
            int barW = Math.max(18, Math.min(60, chartW / (n * 2)));
            int totalGroupW = n * barW + (n - 1) * (barW / 2);
            int startX = padL + (chartW - totalGroupW) / 2;

            int idx = 0;
            List<String> keys = new ArrayList<>(data.keySet());
            List<Double> vals = new ArrayList<>(data.values());

            for (int i = 0; i < n; i++) {
                double val = vals.get(i);
                int barH = (int)((val / maxVal) * chartH);
                int x = startX + i * (barW + barW / 2);
                int y = padT + chartH - barH;

                boolean hov = (hoveredBar != null && hoveredBar == i);

                // Bar gradient
                Color base = hov ? PharmTheme.PM_500 : PharmTheme.PM_700;
                Color top  = hov ? PharmTheme.PM_400 : PharmTheme.PM_500;
                GradientPaint gp = new GradientPaint(x, y, top, x, y + barH, base);
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                // Value label on top
                g2.setColor(hov ? PharmTheme.PM_800 : PharmTheme.TXT2);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String valStr = val >= 1000 ? String.format("%.1fk", val/1000) : String.format("%.0f", val);
                int lx = x + (barW - fm.stringWidth(valStr)) / 2;
                g2.drawString(valStr, lx, y - 4);

                // X-axis label
                g2.setColor(PharmTheme.TXT2);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                fm = g2.getFontMetrics();
                String xLabel = keys.get(i);
                // Show only MM/YY if it's MM/yyyy
                if (xLabel.length() == 7) xLabel = xLabel.substring(0, 2) + "/" + xLabel.substring(5);
                int xlx = x + (barW - fm.stringWidth(xLabel)) / 2;
                g2.drawString(xLabel, xlx, padT + chartH + 16);

                // Hover tooltip box
                if (hov) {
                    String tip = keys.get(i) + " : " + String.format("%.2f DT", val);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                    fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(tip) + 16, th = 24;
                    int tx = Math.min(x - 4, W - tw - 4);
                    int ty = Math.max(padT, y - th - 6);
                    g2.setColor(PharmTheme.PM_800);
                    g2.fillRoundRect(tx, ty, tw, th, 6, 6);
                    g2.setColor(PharmTheme.ACC);
                    g2.drawString(tip, tx + 8, ty + th - 7);
                }

                idx++;
            }

            // ── Trend line ──
            if (n >= 2) {
                g2.setColor(new Color(PharmTheme.ACC.getRed(), PharmTheme.ACC.getGreen(), PharmTheme.ACC.getBlue(), 200));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xs = new int[n], ys = new int[n];
                for (int i = 0; i < n; i++) {
                    int barH = (int)((vals.get(i) / maxVal) * chartH);
                    xs[i] = startX + i * (barW + barW / 2) + barW / 2;
                    ys[i] = padT + chartH - barH;
                }
                for (int i = 0; i < n - 1; i++) {
                    g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
                }
                // Dots
                g2.setColor(PharmTheme.ACC);
                for (int i = 0; i < n; i++) {
                    g2.fillOval(xs[i]-4, ys[i]-4, 8, 8);
                }
                g2.setStroke(new BasicStroke(1));
            }

            g2.dispose();
        }
    }
}