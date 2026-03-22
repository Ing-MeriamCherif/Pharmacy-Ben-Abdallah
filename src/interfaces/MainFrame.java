package interfaces;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import entite.Employe;
import interfaces.produit.*;
import interfaces.stock.*;
import interfaces.commande.*;
import interfaces.vente.*;
import interfaces.client.*;
import interfaces.fournisseur.*;
import interfaces.rapport.*;
import interfaces.employe.*;

/**
 * MainFrame — application shell with persistent sidebar navigation
 *
 * Layout: sidebar (220px fixed) | content stack (CardLayout)
 * The sidebar drives content swaps with zero page reloads.
 * Module panels are created lazily on first access.
 */
public class MainFrame extends JFrame {

    private final Employe          employe;
    private final JPanel           contentStack;
    private final CardLayout       cardLayout;
    private final PharmSidebar     sidebar;
    private final JPanel           topbar;
    private final JLabel           topbarTitle;
    private final JLabel           topbarSub;

    // Lazy module panels
    private JPanel panelProduits;
    private JPanel panelVentes;
    private JPanel panelClients;
    private JPanel panelCommandes;
    private JPanel panelStock;
    private JPanel panelEmployes;
    private JPanel panelFournisseurs;
    private JPanel panelRapports;
    private JPanel panelAccueil;

    public MainFrame(Employe employe) {
        this.employe = employe;
        PharmTheme.install();

        setTitle("PharmaSys — " + employe.getPrenom() + " " + employe.getNom());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        try { setOpacity(0f); } catch (Exception ignored) {}

        // Root layout
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(PharmTheme.BG);
        setContentPane(root);

        // Sidebar
        sidebar = new PharmSidebar(
                employe.getPrenom() + " " + employe.getNom(),
                employe.getPoste(),
                employe.admin());
        sidebar.addNavListener(this::onNavSelected);
        root.add(sidebar, BorderLayout.WEST);

        // Right column
        JPanel rightCol = new JPanel(new BorderLayout(0, 0));
        rightCol.setBackground(PharmTheme.BG);

        // Top bar
        topbar = new JPanel(new BorderLayout());
        topbar.setBackground(PharmTheme.BG2);
        topbar.setPreferredSize(new Dimension(0, 64));
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PharmTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 20)));

        // Title block
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(PharmTheme.BG2);
        titleBlock.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        topbarTitle = new JLabel("Tableau de bord");
        topbarTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        topbarTitle.setForeground(PharmTheme.TXT);
        topbarSub = new JLabel("Vue d'ensemble");
        topbarSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        topbarSub.setForeground(PharmTheme.TXT3);
        titleBlock.add(topbarTitle);
        titleBlock.add(topbarSub);
        topbar.add(titleBlock, BorderLayout.WEST);

        // Right side of topbar: logout + user indicator
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setBackground(PharmTheme.BG2);

        JLabel connectedAs = new JLabel("Connecté : " + employe.getPrenom());
        connectedAs.setFont(PharmTheme.FONT_SM);
        connectedAs.setForeground(PharmTheme.TXT3);

        JButton logoutBtn = PharmTheme.ghostButton("Déconnexion");
        logoutBtn.addActionListener(e -> handleLogout());

        rightActions.add(connectedAs);
        rightActions.add(logoutBtn);
        topbar.add(rightActions, BorderLayout.EAST);

        rightCol.add(topbar, BorderLayout.NORTH);

        // Content stack
        cardLayout = new CardLayout();
        contentStack = new JPanel(cardLayout);
        contentStack.setBackground(PharmTheme.BG);
        rightCol.add(contentStack, BorderLayout.CENTER);

        root.add(rightCol, BorderLayout.CENTER);

        // Boot to Accueil
        showPanel("accueil");
        sidebar.setActive("accueil");
    }

    // ── Navigation ────────────────────────────────────────────────────────
    private void onNavSelected(String id) {
        showPanel(id);
        String[] meta = switch (id) {
            case "produits"     -> new String[]{"Gestion des Produits",   "Médicaments · Stock · Fournisseurs"};
            case "ventes"       -> new String[]{"Gestion des Ventes",     "Ventes · Historique · Factures"};
            case "clients"      -> new String[]{"Gestion des Clients",    "Clients · Fidélité · Historique"};
            case "commandes"    -> new String[]{"Gestion des Commandes",  "Commandes · Réception · Historique"};
            case "stock"        -> new String[]{"Gestion du Stock",       "Consultation · Ajustement · Alertes · Rapports"};
            case "employes"     -> new String[]{"Gestion des Employés",   "Équipe · Salaires · CV"};
            case "fournisseurs" -> new String[]{"Gestion des Fournisseurs","Fournisseurs · Évaluation"};
            case "rapports"     -> new String[]{"Rapports & Statistiques","Chiffres d'affaires · Performance"};
            case "accueil"     -> new String[]{"Accueil", "Bienvenue sur PharmaSys"};
            default             -> new String[]{"PharmaSys", ""};
        };
        topbarTitle.setText(meta[0]);
        topbarSub.setText(meta[1]);
    }

    private void showPanel(String id) {
        // Lazy create
        switch (id) {
            case "produits"     -> { if (panelProduits     == null) { panelProduits     = buildProduitsPanel();     contentStack.add(panelProduits,     "produits");     } }
            case "ventes"       -> { if (panelVentes       == null) { panelVentes       = buildVentesPanel();       contentStack.add(panelVentes,       "ventes");       } }
            case "clients"      -> { if (panelClients      == null) { panelClients      = buildClientsPanel();      contentStack.add(panelClients,      "clients");      } }
            case "commandes"    -> { if (panelCommandes    == null) { panelCommandes    = buildCommandesPanel();    contentStack.add(panelCommandes,    "commandes");    } }
            case "stock"        -> { if (panelStock        == null) { panelStock        = buildStockPanel();        contentStack.add(panelStock,        "stock");        } }
            case "employes"     -> { if (panelEmployes     == null) { panelEmployes     = buildEmployesPanel();     contentStack.add(panelEmployes,     "employes");     } }
            case "fournisseurs" -> { if (panelFournisseurs == null) { panelFournisseurs = buildFournisseursPanel(); contentStack.add(panelFournisseurs, "fournisseurs"); } }
            case "rapports"     -> { if (panelRapports     == null) { panelRapports     = buildRapportsPanel();     contentStack.add(panelRapports,     "rapports");     } }
            case "accueil"     -> { if (panelAccueil     == null) { panelAccueil     = buildAccueilPanel();     contentStack.add(panelAccueil,       "accueil");      } }
        }
        cardLayout.show(contentStack, id);
    }

    // ── Module panel builders ─────────────────────────────────────────────
    private JPanel buildProduitsPanel() {
        return buildModulePanel(
            new String[][] {
                {"➕", "Ajouter un médicament",   "Enregistrer un nouveau produit"},
                {"✎",  "Modifier un médicament",  "Mettre à jour les informations"},
                {"✕",  "Supprimer un médicament", "Retirer un produit du catalogue"},
                {"⊙",  "Rechercher un médicament","Trouver par nom ou référence"}
            },
            new Runnable[] {
                () -> new AjouterMedicamentFrame().setVisible(true),
                () -> new ModifierMedicamentFrame().setVisible(true),
                () -> new SupprimerMedicamentFrame().setVisible(true),
                () -> new RechercheMedicamentFrame().setVisible(true)
            }
        );
    }

    private JPanel buildVentesPanel() {
        return buildModulePanel(
            new String[][] {
                {"🛒", "Nouvelle vente",      "Enregistrer une transaction"},
                {"⊙",  "Historique ventes",   "Consulter les ventes passées"},
                {"⊟",  "Imprimer facture",    "Générer et imprimer un ticket"}
            },
            new Runnable[] {
                () -> new NouvelleVenteFrame().setVisible(true),
                () -> new HistoriqueVentesFrame().setVisible(true),
                () -> new ImprimerFactureFrame().setVisible(true)
            }
        );
    }

    private JPanel buildClientsPanel() {
        return buildModulePanel(
            new String[][] {
                {"➕", "Ajouter client",       "Créer un nouveau dossier client"},
                {"⊙",  "Rechercher client",    "Rechercher par nom ou CNAM"},
                {"⊠",  "Historique achats",    "Consulter l'historique d'un client"},
                {"★",  "Fidélité client",      "Gérer les points de fidélité"}
            },
            new Runnable[] {
                () -> new AjouterClientFrame().setVisible(true),
                () -> new RechercheClientFrame().setVisible(true),
                () -> new HistoriqueClientFrame().setVisible(true),
                () -> new GestionFideliteFrame().setVisible(true)
            }
        );
    }

    private JPanel buildCommandesPanel() {
        return buildModulePanel(
            new String[][] {
                {"➕", "Créer commande",         "Passer une nouvelle commande"},
                {"✎",  "Modifier commande",       "Modifier une commande existante"},
                {"✕",  "Annuler commande",        "Annuler et rembourser"},
                {"📦", "Réceptionner commande",   "Valider réception et mise en stock"},
                {"⊙",  "Lister commandes",        "Historique toutes commandes"}
            },
            new Runnable[] {
                () -> new CreerCommandeFrame().setVisible(true),
                () -> new ModifierCommandeFrame().setVisible(true),
                () -> new AnnulerCommandeFrame().setVisible(true),
                () -> new ReceptionnerCommandeFrame().setVisible(true),
                () -> new ListerCommandesFrame().setVisible(true)
            }
        );
    }

    private JPanel buildStockPanel() {
        return buildModulePanel(
            new String[][] {
                {"⊙",  "Consulter stock",     "Vue complète du stock actuel"},
                {"✎",  "Ajuster stock",       "Augmenter ou diminuer manuellement"},
                {"⚠",  "Alertes stock",       "Produits sous seuil minimal"},
                {"⊟",  "Rapport stock",       "État et statistiques du stock"}
            },
            new Runnable[] {
                () -> new ConsulterStockFrame().setVisible(true),
                () -> new AjusterStockFrame().setVisible(true),
                () -> new AlertesStockFrame().setVisible(true),
                () -> new RapportStockFrame().setVisible(true)
            }
        );
    }

    private JPanel buildEmployesPanel() {
        return buildModulePanel(
            new String[][] {
                {"➕", "Ajouter employé",    "Créer un nouveau dossier"},
                {"⊙",  "Liste des employés", "Consulter et gérer l'équipe"},
                {"💰", "Gérer salaires",     "Modifier et analyser les rémunérations"}
            },
            new Runnable[] {
                () -> new AjouterEmployeFrame().setVisible(true),
                () -> new ListerEmployesFrame().setVisible(true),
                () -> new GererSalaireFrame().setVisible(true)
            }
        );
    }

    private JPanel buildFournisseursPanel() {
        return buildModulePanel(
            new String[][] {
                {"➕", "Ajouter fournisseur",   "Enregistrer un nouveau partenaire"},
                {"✎",  "Modifier fournisseur",  "Mettre à jour les coordonnées"},
                {"✕",  "Supprimer fournisseur", "Retirer un fournisseur"},
                {"★",  "Évaluer fournisseur",   "Attribuer une note de performance"}
            },
            new Runnable[] {
                () -> new AjouterFournisseurFrame().setVisible(true),
                () -> new ModifierFournisseurFrame().setVisible(true),
                () -> new SupprimerFournisseurFrame().setVisible(true),
                () -> new EvaluerFournisseurFrame().setVisible(true)
            }
        );
    }

    private JPanel buildRapportsPanel() {
        return buildModulePanel(
            new String[][] {
                {"📦", "Rapport stock",              "État et valeur du stock"},
                {"💵", "Chiffre d'affaires",         "CA par période"},
                {"★",  "Performance fournisseurs",   "Classement et évaluation"},
                {"🏆", "Top clients",                "Meilleurs clients (fidélité)"},
                {"⊟",  "Ventes par produit",         "Analyse des ventes produit"}
            },
            new Runnable[] {
                () -> new RapportStockFrame().setVisible(true),
                () -> new ChiffresAffairesFrame().setVisible(true),
                () -> new PerformanceFournisseursFrame().setVisible(true),
                () -> JOptionPane.showMessageDialog(this, "En développement", "Info", JOptionPane.INFORMATION_MESSAGE),
                () -> JOptionPane.showMessageDialog(this, "En développement", "Info", JOptionPane.INFORMATION_MESSAGE)
            }
        );
    }

    // ── Generic module panel grid ─────────────────────────────────────────
    // ── Module panel — grid of action cards ─────────────────────────────
    private JPanel buildModulePanel(String[][] cards, Runnable[] actions) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(PharmTheme.BG);
        outer.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        int cols = cards.length <= 2 ? cards.length : 2;
        JPanel grid = new JPanel(new GridLayout(0, cols, 18, 18));
        grid.setBackground(PharmTheme.BG);

        for (int i = 0; i < cards.length; i++) {
            grid.add(buildModuleCard(cards[i][0], cards[i][1], cards[i][2], actions[i]));
        }

        outer.add(grid, BorderLayout.NORTH);
        return outer;
    }

    private JPanel buildModuleCard(String icon, String title, String desc, Runnable onClick) {
        // Accent color bar on left edge — painted in paintComponent
        final boolean[] hov = {false};
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background
                g2.setColor(hov[0] ? new Color(0xF7FFF0) : PharmTheme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Left accent bar
                g2.setColor(hov[0] ? PharmTheme.PM_500 : PharmTheme.PM_200);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 150));
        card.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12,
            hov[0] ? PharmTheme.PM_400 : PharmTheme.BORDER, 0));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                hov[0] = true;
                card.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.PM_400, 0));
                card.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                hov[0] = false;
                card.setBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0));
                card.repaint();
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onClick.run(); }
        });

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 22));

        // Icon
        JLabel iconL = new JLabel(icon);
        iconL.setFont(new Font("SansSerif", Font.BOLD, 24));
        iconL.setForeground(PharmTheme.PM_500);
        iconL.setAlignmentX(LEFT_ALIGNMENT);

        // Title — bold, large, dark
        JLabel titleL = new JLabel(title);
        titleL.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleL.setForeground(PharmTheme.TXT);
        titleL.setAlignmentX(LEFT_ALIGNMENT);

        // Desc — medium, secondary colour
        JLabel descL = new JLabel(desc);
        descL.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descL.setForeground(PharmTheme.TXT2);
        descL.setAlignmentX(LEFT_ALIGNMENT);

        inner.add(iconL);
        inner.add(Box.createVerticalStrut(10));
        inner.add(titleL);
        inner.add(Box.createVerticalStrut(5));
        inner.add(descL);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ── Accueil (home page) ───────────────────────────────────────────────
    private JPanel buildAccueilPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(PharmTheme.BG);

        // ── Hero banner ──
        JPanel hero = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PharmTheme.PM_800, getWidth(), getHeight(), new Color(0x12543F));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255,255,255,12));
                g2.fillOval(getWidth()-260, -100, 400, 400);
                g2.fillOval(-80, getHeight()-120, 260, 260);
                g2.setColor(new Color(200,245,106,18));
                g2.fillOval(getWidth()/2 + 60, -60, 240, 240);
                g2.dispose();
            }
        };
        hero.setPreferredSize(new Dimension(0, 210));
        hero.setBorder(BorderFactory.createEmptyBorder(40, 52, 40, 52));

        JPanel heroText = new JPanel();
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        heroText.setOpaque(false);

        JLabel cross = new JLabel("✚");
        cross.setFont(new Font("SansSerif", Font.BOLD, 38));
        cross.setForeground(PharmTheme.ACC);
        cross.setAlignmentX(LEFT_ALIGNMENT);

        JLabel welcome = new JLabel("Bienvenue, " + employe.getPrenom() + " " + employe.getNom());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 30));
        welcome.setForeground(Color.WHITE);
        welcome.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("PharmaSys · Gestion complète de votre pharmacie");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(new Color(255,255,255,175));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter
            .ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH);
        JLabel dateL = new JLabel(java.time.LocalDate.now().format(dtf));
        dateL.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dateL.setForeground(new Color(200,245,106,210));
        dateL.setAlignmentX(LEFT_ALIGNMENT);

        heroText.add(cross);
        heroText.add(Box.createVerticalStrut(10));
        heroText.add(welcome);
        heroText.add(Box.createVerticalStrut(6));
        heroText.add(subtitle);
        heroText.add(Box.createVerticalStrut(12));
        heroText.add(dateL);
        hero.add(heroText, BorderLayout.CENTER);
        outer.add(hero, BorderLayout.NORTH);

        // ── Quick-access grid ──
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(PharmTheme.BG);
        body.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        JLabel secTitle = new JLabel("Accès rapide");
        secTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        secTitle.setForeground(PharmTheme.TXT);
        body.add(secTitle, BorderLayout.NORTH);

        JPanel quickGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        quickGrid.setBackground(PharmTheme.BG);
        quickGrid.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        // Palette: all use app green family — bg is a tint, accent is a shade
        // [emoji, label, desc, bgHex, accentHex, navId]
        String[][] tiles = {
            {"💊", "Produits",   "Médicaments & catalogue",     "#EEF6F1", "#1A6B45", "produits"},
            {"🛒", "Ventes",     "Enregistrer une transaction",  "#E9F3EE", "#0A3D2E", "ventes"},
            {"👥", "Clients",    "Dossiers & fidélité",          "#F0F8F3", "#2E7D52", "clients"},
            {"📦", "Commandes",  "Suivi & réception",            "#EBF5EF", "#145A32", "commandes"},
            {"📊", "Stock",      "Consultation & alertes",       "#E6F2EC", "#196F3D", "stock"},
            {"📋", "Rapports",   "Statistiques & analyses",      "#EDF7F2", "#117A45", "rapports"}
        };

        for (String[] tile : tiles) {
            final String navId = tile[5];
            quickGrid.add(buildQuickTile(tile[0], tile[1], tile[2],
                Color.decode(tile[3]), Color.decode(tile[4]),
                () -> { onNavSelected(navId); sidebar.setActive(navId); }));
        }
        body.add(quickGrid, BorderLayout.CENTER);
        outer.add(body, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildQuickTile(String emoji, String title, String desc,
                                   Color bg, Color accent, Runnable onClick) {
        final boolean[] hov = {false};
        JPanel tile = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card fill: slightly darker on hover
                Color fill = hov[0] ? bg.darker() : bg;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Thin bottom accent bar (3px)
                g2.setColor(hov[0] ? accent : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
                g2.fillRoundRect(0, getHeight()-4, getWidth(), 4, 3, 3);
                // Subtle border
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        tile.setOpaque(false);
        tile.setPreferredSize(new Dimension(0, 140));
        tile.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { hov[0]=true;  tile.repaint(); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { hov[0]=false; tile.repaint(); }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onClick.run(); }
        });

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel emojiL = new JLabel(emoji);
        emojiL.setFont(new Font("SansSerif", Font.PLAIN, 28));
        emojiL.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleL = new JLabel(title);
        titleL.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleL.setForeground(accent);
        titleL.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descL = new JLabel(desc);
        descL.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descL.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
        descL.setAlignmentX(LEFT_ALIGNMENT);

        inner.add(emojiL);
        inner.add(Box.createVerticalStrut(9));
        inner.add(titleL);
        inner.add(Box.createVerticalStrut(4));
        inner.add(descL);
        tile.add(inner, BorderLayout.CENTER);
        return tile;
    }



    // ── Logout ────────────────────────────────────────────────────────────
    private void handleLogout() {
        boolean confirm = PharmTheme.showConfirm(this,
                "Déconnexion",
                "Voulez-vous vraiment vous déconnecter ?\nTous les formulaires ouverts seront fermés.");
        if (confirm) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}