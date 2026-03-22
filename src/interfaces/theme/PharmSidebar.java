package interfaces.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PharmSidebar — persistent left navigation panel
 *
 * Renders a dark forest-green sidebar with:
 *   - Logo / app identity at top
 *   - Grouped nav items with hover / active states
 *   - User identity card at bottom
 *   - Logout action
 */
public class PharmSidebar extends JPanel {

    public interface NavListener {
        void onNavSelected(String id);
    }

    private final List<NavListener> listeners = new ArrayList<>();
    private String activeId = "";

    // Keep references to items to toggle active state
    private final List<NavItemPanel> items = new ArrayList<>();

    public PharmSidebar(String userName, String userRole, boolean isAdmin) {
        setLayout(new BorderLayout());
        setBackground(PharmTheme.PM_800);
        setPreferredSize(new Dimension(PharmTheme.SIDEBAR_W, 0));

        // ── Logo ──────────────────────────────────────────────────────────
        add(buildLogo(), BorderLayout.NORTH);

        // ── Navigation ────────────────────────────────────────────────────
        JPanel navWrapper = new JPanel();
        navWrapper.setLayout(new BoxLayout(navWrapper, BoxLayout.Y_AXIS));
        navWrapper.setBackground(PharmTheme.PM_800);
        navWrapper.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        addSection(navWrapper, "Principal");
        addNavItem(navWrapper, "accueil",     "⌂",  "Accueil");
        addNavItem(navWrapper, "produits",    "\u25A3",  "Produits");
        addNavItem(navWrapper, "ventes",      "\u2318",  "Ventes");
        addNavItem(navWrapper, "clients",     "\u265C",  "Clients");
        addNavItem(navWrapper, "commandes",   "\u2637",  "Commandes");

        if (isAdmin) {
            addSection(navWrapper, "Administration");
            addNavItem(navWrapper, "stock",       "\u25CE",  "Stock");
            addNavItem(navWrapper, "employes",    "\u265A",  "Employ\u00e9s");
            addNavItem(navWrapper, "fournisseurs","\u2606",  "Fournisseurs");
            addNavItem(navWrapper, "rapports",    "\u2630",  "Rapports");
        }

        navWrapper.add(Box.createVerticalGlue());

        JScrollPane navScroll = new JScrollPane(navWrapper);
        navScroll.setBorder(BorderFactory.createEmptyBorder());
        navScroll.setBackground(PharmTheme.PM_800);
        navScroll.getViewport().setBackground(PharmTheme.PM_800);
        navScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        PharmTheme.styleScrollBar(navScroll.getVerticalScrollBar());
        add(navScroll, BorderLayout.CENTER);

        // ── User footer ───────────────────────────────────────────────────
        add(buildFooter(userName, userRole), BorderLayout.SOUTH);
    }

    // ── Logo panel ────────────────────────────────────────────────────────
    private JPanel buildLogo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PharmTheme.PM_900);
        p.setPreferredSize(new Dimension(PharmTheme.SIDEBAR_W, 62));
        p.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setBackground(PharmTheme.PM_900);
        logoRow.setPreferredSize(new Dimension(PharmTheme.SIDEBAR_W - 32, 62));

        // Icon mark
        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PharmTheme.ACC);
                g2.fillRoundRect(0, 0, 34, 34, 8, 8);
                g2.setColor(PharmTheme.PM_900);
                g2.setStroke(new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(17, 8,  17, 26);
                g2.drawLine(8,  17, 26, 17);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(34, 34));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setBackground(PharmTheme.PM_900);

        JLabel nameL = new JLabel("PharmaSys");
        nameL.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameL.setForeground(Color.WHITE);

        JLabel verL = new JLabel("Gestion de pharmacie");
        verL.setFont(PharmTheme.FONT_LABEL);
        verL.setForeground(new Color(255, 255, 255, 80));

        textCol.add(nameL);
        textCol.add(verL);

        logoRow.add(icon);
        logoRow.add(textCol);
        p.add(logoRow, BorderLayout.CENTER);
        return p;
    }

    // ── Section header ────────────────────────────────────────────────────
    private void addSection(JPanel parent, String label) {
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(255, 255, 255, 70));
        l.setBorder(BorderFactory.createEmptyBorder(14, 8, 4, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(l);
    }

    // ── Nav item ─────────────────────────────────────────────────────────
    private void addNavItem(JPanel parent, String id, String symbol, String label) {
        NavItemPanel item = new NavItemPanel(id, symbol, label);
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { setActive(id); }
        });
        items.add(item);
        parent.add(item);
        parent.add(Box.createVerticalStrut(2));
    }

    // ── Footer ────────────────────────────────────────────────────────────
    private JPanel buildFooter(String name, String role) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PharmTheme.PM_900);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255,255,255,30)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userRow.setBackground(PharmTheme.PM_900);

        // Avatar circle
        String initials = name.length() >= 2
                ? String.valueOf(name.charAt(0)) + name.substring(name.lastIndexOf(' ') + 1, name.lastIndexOf(' ') + 2)
                : name.substring(0, 1);
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PharmTheme.PM_600);
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(PharmTheme.ACC);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String ini = initials.toUpperCase();
                int x = (32 - fm.stringWidth(ini)) / 2;
                int y = (32 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(ini, x, y);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(32, 32));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setBackground(PharmTheme.PM_900);

        JLabel nameL = new JLabel(name);
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameL.setForeground(Color.WHITE);

        JLabel roleL = new JLabel(role);
        roleL.setFont(PharmTheme.FONT_LABEL);
        roleL.setForeground(new Color(255, 255, 255, 100));

        textCol.add(nameL);
        textCol.add(roleL);

        userRow.add(avatar);
        userRow.add(textCol);
        p.add(userRow, BorderLayout.CENTER);
        return p;
    }

    // ── Public API ────────────────────────────────────────────────────────
    public void setActive(String id) {
        activeId = id;
        for (NavItemPanel item : items) item.setActive(item.id.equals(id));
        for (NavListener l : listeners) l.onNavSelected(id);
    }

    public void addNavListener(NavListener l) { listeners.add(l); }

    // ── NavItemPanel ──────────────────────────────────────────────────────
    private static class NavItemPanel extends JPanel {
        final String id;
        private boolean active = false;
        private boolean hovered = false;
        private final JLabel iconL;
        private final JLabel textL;

        NavItemPanel(String id, String symbol, String label) {
            this.id = id;
            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setPreferredSize(new Dimension(200, 36));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            iconL = new JLabel(symbol);
            iconL.setFont(new Font("SansSerif", Font.PLAIN, 16));
            iconL.setForeground(new Color(255, 255, 255, 120));
            iconL.setPreferredSize(new Dimension(18, 18));

            textL = new JLabel(label);
            textL.setFont(new Font("SansSerif", Font.PLAIN, 14));
            textL.setForeground(new Color(255, 255, 255, 150));

            add(Box.createHorizontalStrut(4));
            add(iconL);
            add(textL);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            if (active) {
                iconL.setForeground(PharmTheme.ACC);
                textL.setForeground(PharmTheme.ACC);
                textL.setFont(new Font("SansSerif", Font.BOLD, 14));
            } else {
                iconL.setForeground(new Color(255,255,255,120));
                textL.setForeground(new Color(255,255,255,150));
                textL.setFont(new Font("SansSerif", Font.PLAIN, 14));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(new Color(PharmTheme.ACC.getRed(), PharmTheme.ACC.getGreen(), PharmTheme.ACC.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}