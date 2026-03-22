package interfaces.theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * PharmTheme — Central design system for PharmaSys
 *
 * Aesthetic direction: Organic-minimal / apothecary-modern
 * - Deep forest green primary palette
 * - Warm parchment backgrounds (off-white, not pure white)
 * - Acid lime accent for interactive affordances
 * - DM Sans / system-sans typography hierarchy
 * - Thin 1px borders, generous whitespace, understated depth
 *
 * Usage: Call PharmTheme.install() before creating any frames.
 * Use PharmTheme static factories for all UI components.
 */
public final class PharmTheme {

    // ─── Primary palette ──────────────────────────────────────────────────
    public static final Color PM_900   = new Color(0x051F17);  // darkest forest
    public static final Color PM_800   = new Color(0x0A3D2E);  // primary dark
    public static final Color PM_700   = new Color(0x12543F);  // sidebar bg
    public static final Color PM_600   = new Color(0x1A5C44);  // hover state
    public static final Color PM_500   = new Color(0x2E7D5A);  // mid green
    public static final Color PM_400   = new Color(0x3FA37A);  // accent light
    public static final Color PM_200   = new Color(0xA8D5BF);  // muted green
    public static final Color PM_100   = new Color(0xD4EDE1);  // very light

    // ─── Accent (lime) ────────────────────────────────────────────────────
    public static final Color ACC      = new Color(0xC8F56A);  // primary accent
    public static final Color ACC_HOV  = new Color(0xB4E050);  // hover

    // ─── Neutrals / backgrounds ───────────────────────────────────────────
    public static final Color BG       = new Color(0xF0EDE8);  // page background (warm)
    public static final Color BG2      = new Color(0xFAFAF7);  // card / topbar
    public static final Color BG3      = new Color(0xE8E4DC);  // hover / subtle
    public static final Color CARD     = new Color(0xFFFFFF);  // card surface
    public static final Color BORDER   = new Color(0xD4D0C8);  // default border
    public static final Color BORDER2  = new Color(0xBBB7AE);  // emphasis border

    // ─── Text ─────────────────────────────────────────────────────────────
    public static final Color TXT      = new Color(0x1A1A16);  // primary text
    public static final Color TXT2     = new Color(0x4A4A42);  // secondary text
    public static final Color TXT3     = new Color(0x8A8A7E);  // muted / labels
    public static final Color TXT_SIDE = new Color(0xFFFFFF);  // sidebar text

    // ─── Semantic ─────────────────────────────────────────────────────────
    public static final Color SUCCESS  = new Color(0x2E7D32);
    public static final Color SUCCESS_BG = new Color(0xE8F5E9);
    public static final Color WARN    = new Color(0xE65100);
    public static final Color WARN_BG = new Color(0xFFF3E0);
    public static final Color DANGER  = new Color(0xC62828);
    public static final Color DANGER_BG = new Color(0xFFEBEE);
    public static final Color INFO    = new Color(0x1565C0);
    public static final Color INFO_BG = new Color(0xE3F2FD);

    // ─── Geometry ─────────────────────────────────────────────────────────
    public static final int R4  = 4;
    public static final int R8  = 8;
    public static final int R12 = 12;
    public static final int R16 = 16;

    // ─── Typography ───────────────────────────────────────────────────────
    public static final Font FONT_H1   = new Font("SansSerif", Font.BOLD,  26);
    public static final Font FONT_H2   = new Font("SansSerif", Font.BOLD,  20);
    public static final Font FONT_H3   = new Font("SansSerif", Font.BOLD,  16);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_SM   = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_LABEL= new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN,13);
    public static final Font FONT_BTN  = new Font("SansSerif", Font.BOLD,  14);

    // ─── Sidebar dimensions ───────────────────────────────────────────────
    public static final int SIDEBAR_W = 250;
    public static final int TOPBAR_H  = 58;
    public static final int ROW_H     = 46;

    private PharmTheme() {}

    // ─────────────────────────────────────────────────────────────────────
    // Global LAF install
    // ─────────────────────────────────────────────────────────────────────
    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIDefaults ui = UIManager.getDefaults();

        // Panels / frames
        ui.put("Panel.background", BG);
        ui.put("OptionPane.background", CARD);
        ui.put("OptionPane.messageForeground", TXT);

        // Buttons — disable Metal's default gradient/shadow so our colors show
        ui.put("Button.background", CARD);
        ui.put("Button.foreground", TXT);
        ui.put("Button.font", FONT_BTN);
        ui.put("Button.select", PM_600);           // pressed state bg
        ui.put("Button.focus", new Color(0,0,0,0));
        ui.put("Button.rollover", Boolean.TRUE);
        ui.put("Button.gradient", null);           // kill Metal gradient
        ui.put("Button.shadow", new Color(0,0,0,0));
        ui.put("Button.darkShadow", new Color(0,0,0,0));
        ui.put("Button.light", new Color(0,0,0,0));
        ui.put("Button.highlight", new Color(0,0,0,0));
        ui.put("Button.disabledText", TXT3);
        ui.put("Button.disabledBackground", BG3);
        ui.put("Button.border", BorderFactory.createLineBorder(BORDER));
        ui.put("Button.focus", new Color(0,0,0,0));

        // Text fields
        ui.put("TextField.background", CARD);
        ui.put("TextField.foreground", TXT);
        ui.put("TextField.font", FONT_BODY);
        ui.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        ui.put("TextField.caretForeground", PM_500);
        ui.put("TextField.selectionBackground", PM_100);
        ui.put("TextField.selectionForeground", PM_800);

        // Passwords
        ui.put("PasswordField.background", CARD);
        ui.put("PasswordField.foreground", TXT);
        ui.put("PasswordField.font", FONT_BODY);
        ui.put("PasswordField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        ui.put("PasswordField.caretForeground", PM_500);

        // Text area
        ui.put("TextArea.background", CARD);
        ui.put("TextArea.foreground", TXT);
        ui.put("TextArea.font", FONT_BODY);
        ui.put("TextArea.caretForeground", PM_500);
        ui.put("TextArea.selectionBackground", PM_100);

        // Labels
        ui.put("Label.foreground", TXT);
        ui.put("Label.font", FONT_BODY);

        // Combo box
        ui.put("ComboBox.background", CARD);
        ui.put("ComboBox.foreground", TXT);
        ui.put("ComboBox.font", FONT_BODY);
        ui.put("ComboBox.border", BorderFactory.createLineBorder(BORDER));
        ui.put("ComboBox.selectionBackground", PM_100);
        ui.put("ComboBox.selectionForeground", PM_800);
        ui.put("ComboBox.listBackground", CARD);
        ui.put("ComboBox.listForeground", TXT);

        // Spinner
        ui.put("Spinner.background", CARD);
        ui.put("Spinner.foreground", TXT);
        ui.put("Spinner.font", FONT_BODY);
        ui.put("Spinner.border", BorderFactory.createLineBorder(BORDER));

        // Table
        ui.put("Table.background", CARD);
        ui.put("Table.foreground", TXT);
        ui.put("Table.font", FONT_BODY);
        ui.put("Table.gridColor", BORDER);
        ui.put("Table.selectionBackground", PM_100);
        ui.put("Table.selectionForeground", PM_800);
        ui.put("Table.focusCellHighlightBorder", BorderFactory.createLineBorder(PM_400));
        ui.put("TableHeader.background", BG2);
        ui.put("TableHeader.foreground", TXT2);
        ui.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 13));
        ui.put("TableHeader.cellBorder", BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // Scroll pane
        ui.put("ScrollPane.background", CARD);
        ui.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        ui.put("ScrollBar.background", BG);
        ui.put("ScrollBar.thumb", BORDER);
        ui.put("ScrollBar.width", 8);
        ui.put("ScrollBar.thumbDarkShadow", BORDER);
        ui.put("ScrollBar.thumbHighlight", BORDER2);
        ui.put("ScrollBar.thumbShadow", BORDER);
        ui.put("ScrollBar.track", BG);
        ui.put("ScrollBar.trackHighlight", BG3);

        // Tabbed pane (rarely used now, but keep sane)
        ui.put("TabbedPane.background", BG);
        ui.put("TabbedPane.foreground", TXT2);
        ui.put("TabbedPane.selected", CARD);
        ui.put("TabbedPane.selectedForeground", TXT);
        ui.put("TabbedPane.font", FONT_BODY);

        // Checkboxes / radios
        ui.put("CheckBox.background", new Color(0,0,0,0));
        ui.put("CheckBox.foreground", TXT);
        ui.put("CheckBox.font", FONT_BODY);
        ui.put("RadioButton.background", new Color(0,0,0,0));
        ui.put("RadioButton.foreground", TXT);
        ui.put("RadioButton.font", FONT_BODY);

        // Lists
        ui.put("List.background", CARD);
        ui.put("List.foreground", TXT);
        ui.put("List.font", FONT_BODY);
        ui.put("List.selectionBackground", PM_100);
        ui.put("List.selectionForeground", PM_800);

        // Menu
        ui.put("Menu.background", CARD);
        ui.put("Menu.foreground", TXT);
        ui.put("MenuBar.background", CARD);
        ui.put("MenuItem.background", CARD);
        ui.put("MenuItem.foreground", TXT);
        ui.put("MenuItem.font", FONT_BODY);
        ui.put("MenuItem.selectionBackground", PM_100);
        ui.put("MenuItem.selectionForeground", PM_800);
        ui.put("PopupMenu.background", CARD);
        ui.put("PopupMenu.border", BorderFactory.createLineBorder(BORDER));

        // Split pane
        ui.put("SplitPane.background", BG);
        ui.put("SplitPane.dividerSize", 1);
        ui.put("SplitPaneDivider.border", BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        // Tool tips
        ui.put("ToolTip.background", PM_900);
        ui.put("ToolTip.foreground", new Color(0xDDDDD0));
        ui.put("ToolTip.font", FONT_SM);
        ui.put("ToolTip.border", BorderFactory.createEmptyBorder(5, 8, 5, 8));

        // Slider
        ui.put("Slider.background", BG);
        ui.put("Slider.foreground", PM_500);
        ui.put("Slider.thumbSize", new Dimension(14, 14));
        ui.put("Slider.border", BorderFactory.createEmptyBorder());
    }

    // ─────────────────────────────────────────────────────────────────────
    // Component factories
    // ─────────────────────────────────────────────────────────────────────

    /** Primary action button — dark green with lime text */
    public static JButton primaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? PM_600
                         : getModel().isRollover() ? PM_600 : PM_800;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), R8*2, R8*2);
                g2.setColor(ACC);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(FONT_BTN);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 24, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Accent button — lime background with dark text */
    public static JButton accentButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? ACC_HOV
                         : getModel().isRollover() ? ACC_HOV : ACC;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), R8*2, R8*2);
                g2.setColor(PM_900);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(FONT_BTN);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 24, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Ghost button — transparent with border */
    public static JButton ghostButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? BG3
                         : getModel().isRollover() ? BG3 : CARD;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), R8*2, R8*2);
                // Border
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, R8*2, R8*2);
                // Text
                g2.setColor(getModel().isRollover() ? TXT : TXT2);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(FONT_BTN);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 24, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Danger button — red outline */
    public static JButton dangerButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? DANGER_BG
                         : getModel().isRollover() ? DANGER_BG : CARD;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), R8*2, R8*2);
                // Red border
                g2.setColor(new Color(DANGER.getRed(), DANGER.getGreen(), DANGER.getBlue(),
                    getModel().isRollover() ? 200 : 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, R8*2, R8*2);
                // Text
                g2.setColor(DANGER);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(FONT_BTN);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 24, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Styled text field */
    public static JTextField textField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(TXT3);
                    g.setFont(FONT_SM);
                    Insets ins = getInsets();
                    g.drawString(placeholder, ins.left, getHeight()/2 + g.getFontMetrics().getAscent()/2 - 1);
                }
            }
        };
        styleTextField(f);
        return f;
    }

    public static void styleTextField(JTextField f) {
        f.setFont(FONT_BODY);
        f.setOpaque(true);
        f.setBackground(CARD);
        f.setForeground(TXT);
        f.setCaretColor(PM_500);
        f.setSelectionColor(PM_100);
        f.setSelectedTextColor(PM_800);
        f.setBorder(new RoundedFieldBorder(R8));
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 36));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
    }

    /** Styled password field */
    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setBackground(CARD);
        f.setForeground(TXT);
        f.setCaretColor(PM_500);
        f.setBorder(new RoundedFieldBorder(R8));
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 36));
        return f;
    }

    /** Styled text area */
    public static JTextArea textArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(FONT_BODY);
        ta.setBackground(CARD);
        ta.setForeground(TXT);
        ta.setCaretColor(PM_500);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return ta;
    }

    /** Scroll pane wrapper for text areas */
    public static JScrollPane scrollTextArea(JTextArea ta) {
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(new RoundedBorder(R8, BORDER, 0));
        sp.setBackground(CARD);
        styleScrollBar(sp.getVerticalScrollBar());
        return sp;
    }

    /** Styled combo box */
    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setOpaque(true);
        cb.setBackground(CARD);
        cb.setForeground(TXT);
        cb.setBorder(new RoundedBorder(R8, BORDER, 0));
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 36));
        return cb;
    }

    /** Card panel with rounded border */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(new RoundedBorder(R12, BORDER, 0));
        return p;
    }

    /** Section panel (off-white, no visible border) */
    public static JPanel section() {
        JPanel p = new JPanel();
        p.setBackground(BG2);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        return p;
    }

    /** Sidebar panel */
    public static JPanel sidebarPanel() {
        JPanel p = new JPanel();
        p.setBackground(PM_800);
        p.setPreferredSize(new Dimension(SIDEBAR_W, Integer.MAX_VALUE));
        return p;
    }

    /** Topbar panel */
    public static JPanel topbarPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG2);
        p.setPreferredSize(new Dimension(Integer.MAX_VALUE, TOPBAR_H));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        return p;
    }

    /** Form label */
    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SM);
        l.setForeground(TXT2);
        return l;
    }

    /** Required field label (adds asterisk) */
    public static JLabel requiredLabel(String text) {
        JLabel l = new JLabel("<html>" + text + " <span style='color:#E53E3E'>*</span></html>");
        l.setFont(FONT_SM);
        l.setForeground(TXT2);
        return l;
    }

    /** Muted helper text */
    public static JLabel helperLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TXT3);
        return l;
    }

    /** Section header label */
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TXT3);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }

    /** Page title label */
    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_H2);
        l.setForeground(TXT);
        return l;
    }

    /** Subtitle / page sub */
    public static JLabel subtitleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SM);
        l.setForeground(TXT3);
        return l;
    }

    /** Styled table */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TXT);
        table.setBackground(CARD);
        table.setRowHeight(ROW_H);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(PM_100);
        table.setSelectionForeground(PM_800);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(TXT2);
        header.setBackground(BG2);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        // Uppercase header renderer
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setText(value != null ? value.toString().toUpperCase() : "");
                setFont(new Font("SansSerif", Font.BOLD, 10));
                setForeground(TXT3);
                setBackground(BG2);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                        BorderFactory.createEmptyBorder(0, 14, 0, 14)));
                setHorizontalAlignment(LEFT);
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Row hover
        table.addMouseMotionListener(new MouseAdapter() {
            int lastRow = -1;
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != lastRow) { lastRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { table.repaint(); }
        });
    }

    /** Styled scroll pane for tables */
    public static JScrollPane tableScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(CARD);
        sp.getViewport().setBackground(CARD);
        styleScrollBar(sp.getVerticalScrollBar());
        styleScrollBar(sp.getHorizontalScrollBar());
        return sp;
    }

    /** Styled scroll bar */
    public static void styleScrollBar(JScrollBar sb) {
        sb.setBackground(BG);
        sb.setPreferredSize(new Dimension(6, 6));
        sb.setBorder(BorderFactory.createEmptyBorder());
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BORDER;
                this.trackColor = BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0,0));
                b.setMinimumSize(new Dimension(0,0));
                b.setMaximumSize(new Dimension(0,0));
                return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER2);
                g2.fillRoundRect(r.x+1, r.y+1, r.width-2, r.height-2, 4, 4);
                g2.dispose();
            }
        });
    }

    /** Spinner (numeric) */
    public static JSpinner spinner(int value, int min, int max, int step) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        sp.setFont(FONT_BODY);
        sp.setBorder(new RoundedBorder(R8, BORDER, 0));
        sp.setPreferredSize(new Dimension(90, 36));
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setFont(FONT_BODY);
            de.getTextField().setForeground(TXT);
            de.getTextField().setBackground(CARD);
            de.getTextField().setCaretColor(PM_500);
            de.getTextField().setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        }
        return sp;
    }

    /** Badge label */
    public static JLabel badge(String text, BadgeType type) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setOpaque(true);
        l.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        switch (type) {
            case SUCCESS -> { l.setBackground(SUCCESS_BG); l.setForeground(SUCCESS); }
            case WARN    -> { l.setBackground(WARN_BG);    l.setForeground(WARN);    }
            case DANGER  -> { l.setBackground(DANGER_BG);  l.setForeground(DANGER);  }
            case INFO    -> { l.setBackground(INFO_BG);    l.setForeground(INFO);    }
            case NEUTRAL -> { l.setBackground(BG3); l.setForeground(TXT2); }
        }
        // Round via a wrapper panel
        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);
        wrap.setBackground(l.getBackground());
        l.setOpaque(false);
        wrap.add(l);
        // Return as JLabel for simplicity — embed the wrap in calling code
        // or use BadgePanel instead
        return l;
    }

    public enum BadgeType { SUCCESS, WARN, DANGER, INFO, NEUTRAL }

    /** Render a badge inline in a table cell */
    public static Component badgeComponent(String text, BadgeType type) {
        Color bg, fg;
        switch (type) {
            case SUCCESS -> { bg = SUCCESS_BG; fg = SUCCESS; }
            case WARN    -> { bg = WARN_BG;    fg = WARN;    }
            case DANGER  -> { bg = DANGER_BG;  fg = DANGER;  }
            case INFO    -> { bg = INFO_BG;    fg = INFO;    }
            default      -> { bg = BG3; fg = TXT2; }
        }
        final Color fbg = bg, ffg = fg;
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fbg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(ffg);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    /** Separator */
    public static JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER);
        s.setBackground(BG);
        return s;
    }

    /** Standard GBC helper */
    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);
        return g;
    }

    public static GridBagConstraints gbc(int x, int y, int w, int h) {
        GridBagConstraints g = gbc(x, y);
        g.gridwidth = w; g.gridheight = h;
        return g;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Dialogs
    // ─────────────────────────────────────────────────────────────────────

    public static void showSuccess(Component parent, String title, String msg) {
        showDialog(parent, title, msg, "success");
    }

    public static void showError(Component parent, String title, String msg) {
        showDialog(parent, title, msg, "error");
    }

    public static void showWarning(Component parent, String title, String msg) {
        showDialog(parent, title, msg, "warning");
    }

    public static void showInfo(Component parent, String title, String msg) {
        showDialog(parent, title, msg, "info");
    }

    public static boolean showConfirm(Component parent, String title, String msg) {
        final boolean[] result = {false};
        JDialog dlg = new JDialog(
            parent != null ? SwingUtilities.getWindowAncestor(parent) : null,
            title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(CARD);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PM_800);
        header.setPreferredSize(new Dimension(420, 54));
        header.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        JPanel hRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hRow.setBackground(PM_800);
        JLabel ico = new JLabel("⚠");
        ico.setFont(new Font("SansSerif", Font.BOLD, 18));
        ico.setForeground(new Color(0xFBBF24));
        JLabel ttl = new JLabel(title);
        ttl.setFont(new Font("SansSerif", Font.BOLD, 15));
        ttl.setForeground(Color.WHITE);
        hRow.add(ico); hRow.add(ttl);
        header.add(hRow, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Message
        JLabel msgLbl = new JLabel("<html><body style='width:310px;line-height:1.6'>" +
            msg.replace("\n", "<br>") + "</body></html>");
        msgLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        msgLbl.setForeground(TXT);
        msgLbl.setBorder(BorderFactory.createEmptyBorder(20, 22, 12, 22));
        root.add(msgLbl, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btns.setBackground(BG);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        JButton no  = ghostButton("Annuler");
        JButton yes = dangerButton("Confirmer");
        no.addActionListener(e  -> { result[0] = false; dlg.dispose(); });
        yes.addActionListener(e -> { result[0] = true;  dlg.dispose(); });
        btns.add(no); btns.add(yes);
        root.add(btns, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setSize(420, 210);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        return result[0];
    }

    private static void showDialog(Component parent, String title, String msg, String type) {
        Color accent; String icon;
        switch (type) {
            case "success": accent = new Color(0x1A6B45); icon = "✓"; break;
            case "error":   accent = DANGER;               icon = "✕"; break;
            case "warning": accent = new Color(0xB45309);  icon = "⚠"; break;
            default:        accent = INFO;                 icon = "ℹ"; break;
        }
        Color accentColor = accent;
        String iconStr = icon;

        JDialog dlg = new JDialog(
            parent != null ? SwingUtilities.getWindowAncestor(parent) : null,
            title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(CARD);

        // Colored header strip
        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(accentColor);
        strip.setPreferredSize(new Dimension(0, 54));
        strip.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 14));
        JPanel stripRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        stripRow.setBackground(accentColor);
        JLabel iconLbl = new JLabel(iconStr);
        iconLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        iconLbl.setForeground(Color.WHITE);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        stripRow.add(iconLbl); stripRow.add(titleLbl);
        strip.add(stripRow, BorderLayout.CENTER);
        root.add(strip, BorderLayout.NORTH);

        // Message
        JLabel msgLbl = new JLabel("<html><body style='width:300px;line-height:1.6'>" +
            msg.replace("\n", "<br>") + "</body></html>");
        msgLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        msgLbl.setForeground(TXT);
        msgLbl.setBorder(BorderFactory.createEmptyBorder(20, 22, 12, 22));
        root.add(msgLbl, BorderLayout.CENTER);

        // OK button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        btnRow.setBackground(BG);
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        JButton ok = primaryButton("OK");
        ok.addActionListener(e -> dlg.dispose());
        dlg.getRootPane().setDefaultButton(ok);
        btnRow.add(ok);
        root.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setSize(380, 200);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Inner helpers
    // ─────────────────────────────────────────────────────────────────────

    /** Rounded border for panels / buttons */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        private final int thickness;

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 10, 6, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(6, 10, 6, 10);
            return insets;
        }
    }

    /** Rounded field border with focus ring */
    public static class RoundedFieldBorder extends AbstractBorder {
        private final int radius;
        public RoundedFieldBorder(int radius) { this.radius = radius; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean focused = c.isFocusOwner();
            if (focused) {
                g2.setColor(new Color(PM_400.getRed(), PM_400.getGreen(), PM_400.getBlue(), 60));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, radius * 2, radius * 2);
            }
            g2.setColor(focused ? PM_400 : BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(7, 11, 7, 11); }

        @Override
        public Insets getBorderInsets(Component c, Insets ins) {
            ins.set(7, 11, 7, 11); return ins;
        }
    }

    /** Mouse hover adapter for buttons */
    private static class HoverAdapter extends MouseAdapter {
        private final JButton btn;
        private final Color bgHov, bgNorm, fgHov, fgNorm;
        HoverAdapter(JButton b, Color bgH, Color bgN, Color fgH, Color fgN) {
            btn = b; bgHov = bgH; bgNorm = bgN; fgHov = fgH; fgNorm = fgN;
        }
        @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bgHov); btn.setForeground(fgHov); }
        @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bgNorm);btn.setForeground(fgNorm);}
    }
}