package interfaces.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * PharmBaseFrame — base JFrame all screens should extend
 *
 * Provides:
 *   - Warm parchment background
 *   - Standard window sizing / centering
 *   - Rounded card content area
 *   - Consistent header bar with title and optional back button
 *   - Escape-to-close
 */
public class PharmBaseFrame extends JFrame {

    protected final JPanel contentArea;
    private   final JPanel headerBar;
    private   final JLabel titleLabel;
    private   final JLabel subtitleLabel;

    public PharmBaseFrame(String title) {
        this(title, null, 900, 620);
    }

    public PharmBaseFrame(String title, String subtitle) {
        this(title, subtitle, 900, 620);
    }

    public PharmBaseFrame(String title, String subtitle, int w, int h) {
        setTitle(title);
        setSize(w, h);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Warm background
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PharmTheme.BG);
        setContentPane(root);

        // Header bar
        headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(PharmTheme.BG2);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PharmTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 20)));
        headerBar.setPreferredSize(new Dimension(0, PharmTheme.TOPBAR_H));

        // Left side: title block
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(PharmTheme.BG2);
        titleBlock.setAlignmentY(CENTER_ALIGNMENT);
        titleBlock.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        titleLabel = new JLabel(title);
        titleLabel.setFont(PharmTheme.FONT_H3);
        titleLabel.setForeground(PharmTheme.TXT);
        titleBlock.add(titleLabel);

        subtitleLabel = new JLabel(subtitle != null ? subtitle : "");
        subtitleLabel.setFont(PharmTheme.FONT_LABEL);
        subtitleLabel.setForeground(PharmTheme.TXT3);
        titleBlock.add(subtitleLabel);
        if (subtitle == null) subtitleLabel.setVisible(false);

        headerBar.add(titleBlock, BorderLayout.WEST);

        // Right side: action area (subclasses populate via headerActions())
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setBackground(PharmTheme.BG2);
        actionsPanel.setAlignmentY(CENTER_ALIGNMENT);
        populateHeaderActions(actionsPanel);
        headerBar.add(actionsPanel, BorderLayout.EAST);

        root.add(headerBar, BorderLayout.NORTH);

        // Content area — padded
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(PharmTheme.BG);
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        root.add(contentArea, BorderLayout.CENTER);

        // Escape to close
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Subclasses override to add buttons to the header bar */
    protected void populateHeaderActions(JPanel actionsPanel) {}

    /** Update title / subtitle dynamically */
    protected void setFrameTitle(String title, String subtitle) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle != null ? subtitle : "");
        subtitleLabel.setVisible(subtitle != null && !subtitle.isEmpty());
        setTitle(title);
    }

    /** Add accent tag to header (e.g. record count) */
    protected JLabel addHeaderBadge(String text) {
        JLabel badge = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PharmTheme.BG3);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setForeground(PharmTheme.TXT2);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return badge;
    }

    /**
     * Utility: wrap any JPanel content in a white card panel with padding
     */
    protected static JPanel wrapCard(JComponent inner, int padding) {
        JPanel card = PharmTheme.card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /**
     * Utility: build a standard two-column form row
     * Returns: [row panel]
     */
    protected static JPanel formRow(String labelText, JComponent field, boolean required) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(PharmTheme.BG2);
        GridBagConstraints gc = new GridBagConstraints();

        JLabel lbl = required
                ? PharmTheme.requiredLabel(labelText)
                : PharmTheme.formLabel(labelText);
        lbl.setPreferredSize(new Dimension(140, 20));
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 0, 12);
        row.add(lbl, gc);

        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        gc.insets = new Insets(0, 0, 0, 0);
        row.add(field, gc);
        return row;
    }

    /**
     * Build a full-width form panel with labelled rows
     * Subclasses call buildFormPanel(rows) where rows is a String[][]
     * rows[i] = { labelText, ... } — use with addFormRow below
     */
    protected JPanel formPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PharmTheme.CARD);
        p.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        return p;
    }

    /** Add a divider section header inside a form panel */
    protected static void addFormSection(JPanel formPanel, String sectionTitle) {
        if (formPanel.getComponentCount() > 0) {
            formPanel.add(Box.createVerticalStrut(8));
            JSeparator sep = PharmTheme.separator();
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            formPanel.add(sep);
            formPanel.add(Box.createVerticalStrut(12));
        }
        JLabel l = new JLabel(sectionTitle.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(PharmTheme.TXT3);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        formPanel.add(l);
    }

    /** Standard bottom button bar */
    protected JPanel buttonBar(JButton... buttons) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bar.setBackground(PharmTheme.BG2);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, PharmTheme.BORDER));
        for (JButton b : buttons) bar.add(b);
        return bar;
    }

    /** Status bar at bottom for feedback messages */
    private JLabel statusBar;
    protected JPanel statusBarPanel() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PharmTheme.BG2);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PharmTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        statusBar = new JLabel(" ");
        statusBar.setFont(PharmTheme.FONT_SM);
        statusBar.setForeground(PharmTheme.TXT3);
        bar.add(statusBar, BorderLayout.WEST);
        return bar;
    }

    protected void setStatus(String msg, PharmTheme.BadgeType type) {
        if (statusBar == null) return;
        Color col = switch (type) {
            case SUCCESS -> PharmTheme.SUCCESS;
            case DANGER  -> PharmTheme.DANGER;
            case WARN    -> PharmTheme.WARN;
            case INFO    -> PharmTheme.INFO;
            default      -> PharmTheme.TXT3;
        };
        statusBar.setForeground(col);
        statusBar.setText(msg);
    }
}
