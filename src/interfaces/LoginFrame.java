package interfaces;

import interfaces.theme.PharmTheme;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import entite.Employe;
import entitebd.EmployeBD;
import exception.AuthentificationException;

public class LoginFrame extends JFrame {

    private JTextField     cnssField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         errorLabel;
    private EmployeBD      employeBD;

    public LoginFrame() {
        employeBD = new EmployeBD();
        PharmTheme.install();
        initComponents();
    }

    private void initComponents() {
        setTitle("PharmaScie Ben Abdallah — Connexion");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        JPanel root = new JPanel(new GridLayout(1, 2, 0, 0));
        setContentPane(root);
        root.add(buildBrandPanel());
        root.add(buildFormPanel());
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel buildBrandPanel() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PharmTheme.PM_800); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(PharmTheme.PM_700); g2.fillOval(getWidth()-100,-90,240,240);
                g2.setColor(PharmTheme.PM_900); g2.fillOval(-70,getHeight()-110,210,210);
                g2.setColor(new Color(255,255,255,10)); g2.setStroke(new BasicStroke(0.5f));
                for (int x=0;x<getWidth();x+=36) g2.drawLine(x,0,x,getHeight());
                for (int y=0;y<getHeight();y+=36) g2.drawLine(0,y,getWidth(),y);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        JPanel col = new JPanel(); col.setLayout(new BoxLayout(col,BoxLayout.Y_AXIS)); col.setOpaque(false);
        col.setBorder(BorderFactory.createEmptyBorder(0,44,0,44));
        JPanel cross = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PharmTheme.ACC); g2.fillRoundRect(0,0,56,56,14,14);
                g2.setColor(PharmTheme.PM_900); g2.setStroke(new BasicStroke(3.2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawLine(28,12,28,44); g2.drawLine(12,28,44,28); g2.dispose();
            }
        };
        cross.setOpaque(false); cross.setPreferredSize(new Dimension(56,56)); cross.setMaximumSize(new Dimension(56,56)); cross.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name = new JLabel("Pharmacie Ben Abdallah"); name.setFont(new Font("SansSerif",Font.BOLD,28)); name.setForeground(Color.WHITE); name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tag = new JLabel("<html><center>Système de gestion<br>de pharmacie</center></html>"); tag.setFont(PharmTheme.FONT_BODY); tag.setForeground(new Color(255,255,255,140)); tag.setHorizontalAlignment(SwingConstants.CENTER); tag.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel pills = new JPanel(new FlowLayout(FlowLayout.CENTER,6,0)); pills.setOpaque(false); pills.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (String f : new String[]{"Gestion de stock ","Gestion de clients","Gestion de finance"}) {
            JLabel pill = new JLabel(f); pill.setFont(new Font("SansSerif",Font.BOLD,10)); pill.setForeground(PharmTheme.ACC);
            pill.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(PharmTheme.ACC.getRed(),PharmTheme.ACC.getGreen(),PharmTheme.ACC.getBlue(),90),1),BorderFactory.createEmptyBorder(3,10,3,10)));
            pills.add(pill);
        }
        col.add(Box.createVerticalGlue()); col.add(cross); col.add(Box.createVerticalStrut(18)); col.add(name); col.add(Box.createVerticalStrut(10)); col.add(tag); col.add(Box.createVerticalStrut(28)); col.add(pills); col.add(Box.createVerticalGlue());
        p.add(col, BorderLayout.CENTER);
        JLabel ver = new JLabel("v2.4.0 · 2026"); ver.setFont(PharmTheme.FONT_LABEL); ver.setForeground(new Color(255,255,255,50)); ver.setBorder(BorderFactory.createEmptyBorder(0,20,16,0));
        p.add(ver, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(PharmTheme.CARD);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PharmTheme.CARD);
        form.setPreferredSize(new Dimension(340, 380));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0; gc.gridx = 0;

        JLabel h1 = new JLabel("Bienvenue"); h1.setFont(new Font("SansSerif",Font.BOLD,26)); h1.setForeground(PharmTheme.TXT);
        gc.gridy=0; gc.insets=new Insets(0,0,4,0); form.add(h1,gc);

        JLabel h2 = new JLabel("Connectez-vous à votre espace"); h2.setFont(PharmTheme.FONT_BODY); h2.setForeground(PharmTheme.TXT3);
        gc.gridy=1; gc.insets=new Insets(0,0,30,0); form.add(h2,gc);

        JLabel cnssLbl = new JLabel("Numéro CNSS"); cnssLbl.setFont(PharmTheme.FONT_SM); cnssLbl.setForeground(PharmTheme.TXT2);
        gc.gridy=2; gc.insets=new Insets(0,0,5,0); form.add(cnssLbl,gc);

        cnssField = PharmTheme.textField("Ex: 123456789");
        cnssField.setPreferredSize(new Dimension(340,40));
        cnssField.addActionListener(e -> handleLogin());
        gc.gridy=3; gc.insets=new Insets(0,0,16,0); form.add(cnssField,gc);

        JLabel passLbl = new JLabel("Mot de passe"); passLbl.setFont(PharmTheme.FONT_SM); passLbl.setForeground(PharmTheme.TXT2);
        gc.gridy=4; gc.insets=new Insets(0,0,5,0); form.add(passLbl,gc);

        passwordField = PharmTheme.passwordField();
        passwordField.setPreferredSize(new Dimension(340,40));
        passwordField.addActionListener(e -> handleLogin());
        gc.gridy=5; gc.insets=new Insets(0,0,8,0); form.add(passwordField,gc);

        errorLabel = new JLabel(" "); errorLabel.setFont(PharmTheme.FONT_SM); errorLabel.setForeground(PharmTheme.DANGER);
        gc.gridy=6; gc.insets=new Insets(0,0,14,0); form.add(errorLabel,gc);

        loginButton = new JButton("Se connecter →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.dispose();
                FontMetrics fm=g.getFontMetrics(); int x=(getWidth()-fm.stringWidth(getText()))/2; int y=(getHeight()+fm.getAscent()-fm.getDescent())/2;
                g.setColor(getForeground()); g.setFont(getFont()); g.drawString(getText(),x,y);
            }
        };
        loginButton.setBackground(PharmTheme.PM_800); loginButton.setForeground(PharmTheme.ACC);
        loginButton.setFont(new Font("SansSerif",Font.BOLD,14));
        loginButton.setBorderPainted(false); loginButton.setContentAreaFilled(false); loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(340,44));
        loginButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { loginButton.setBackground(PharmTheme.PM_600); }
            @Override public void mouseExited(MouseEvent e)  { loginButton.setBackground(PharmTheme.PM_800); }
        });
        loginButton.addActionListener(e -> handleLogin());
        gc.gridy=7; gc.insets=new Insets(0,0,22,0); form.add(loginButton,gc);

        JSeparator sep = new JSeparator(); sep.setForeground(PharmTheme.BORDER);
        gc.gridy=8; gc.insets=new Insets(0,0,14,0); form.add(sep,gc);

        JLabel hint = new JLabel("Contactez l'administrateur en cas de problème"); hint.setFont(PharmTheme.FONT_LABEL); hint.setForeground(PharmTheme.TXT3);
        gc.gridy=9; gc.insets=new Insets(0,0,0,0); form.add(hint,gc);

        outer.add(form);
        return outer;
    }

    private void handleLogin() {
        String cnssText = cnssField.getText().trim();
        String password = new String(passwordField.getPassword());
        errorLabel.setText(" ");
        if (cnssText.isEmpty() || password.isEmpty()) { showError("Veuillez remplir tous les champs."); return; }
        int cnss;
        try { cnss = Integer.parseInt(cnssText); }
        catch (NumberFormatException e) { showError("Le numéro CNSS doit être un nombre valide."); return; }
        loginButton.setText("Connexion…"); loginButton.setEnabled(false);
        SwingWorker<Employe,Void> worker = new SwingWorker<>() {
            @Override protected Employe doInBackground() throws Exception { return employeBD.authentifier(cnss, password); }
            @Override protected void done() {
                loginButton.setText("Se connecter →"); loginButton.setEnabled(true);
                try {
                    Employe emp = get();
                    if (emp==null) throw new AuthentificationException("Identifiants incorrects", String.valueOf(cnss));
                    // Animated transition
                    loginButton.setText("Bienvenue…");
                    SplashTransition splash = new SplashTransition(LoginFrame.this, emp);
                    splash.start();
                } catch (AuthentificationException ex) { showError("Identifiants incorrects. Réessayez."); passwordField.setText(""); passwordField.requestFocus(); }
                catch (Exception ex) { showError("Erreur de connexion : " + ex.getMessage()); }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        Timer shake = new Timer(28,null); int[] count={0}; int[] dir={1}; Point orig=getLocation();
        shake.addActionListener(e -> { setLocation(orig.x+dir[0]*5,orig.y); dir[0]*=-1; if(++count[0]>8){shake.stop();setLocation(orig);} });
        shake.start();
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); }
}