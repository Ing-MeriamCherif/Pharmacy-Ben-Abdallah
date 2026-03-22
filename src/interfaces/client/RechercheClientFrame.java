package interfaces.client;

import interfaces.theme.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import entite.Client;
import gestion.GestionClient;

public class RechercheClientFrame extends PharmBaseFrame {
    private JTextField txtSearch, txtCodeCnam;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtDetails;
    private final GestionClient gestionClient = new GestionClient();

    public RechercheClientFrame() {
        super("Rechercher un client", "Par nom, prénom ou code CNAM", 1100, 640);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton all = PharmTheme.ghostButton("Tous les clients"); all.addActionListener(e -> loadAll());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(all); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(12, 0));

        JPanel left = new JPanel(new BorderLayout(0, 8)); left.setBackground(PharmTheme.BG);

        JPanel filters = new JPanel(new GridLayout(1, 2, 8, 0)); filters.setBackground(PharmTheme.BG);
        JPanel n1 = new JPanel(new BorderLayout(4, 0)); n1.setBackground(PharmTheme.BG);
        txtSearch = PharmTheme.textField("Nom ou prénom…"); txtSearch.addActionListener(e -> searchByName());
        JButton b1 = PharmTheme.ghostButton("Chercher"); b1.addActionListener(e -> searchByName());
        n1.add(txtSearch, BorderLayout.CENTER); n1.add(b1, BorderLayout.EAST);
        JPanel n2 = new JPanel(new BorderLayout(4, 0)); n2.setBackground(PharmTheme.BG);
        txtCodeCnam = PharmTheme.textField("Code CNAM…"); txtCodeCnam.addActionListener(e -> searchByCnam());
        JButton b2 = PharmTheme.ghostButton("Chercher"); b2.addActionListener(e -> searchByCnam());
        n2.add(txtCodeCnam, BorderLayout.CENTER); n2.add(b2, BorderLayout.EAST);
        filters.add(n1); filters.add(n2);
        left.add(filters, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"N° Client", "N° Carte", "Nom", "Prénom", "Téléphone", "Points", "CNAM"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        PharmTheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showDetails(); });
        left.add(PharmTheme.tableScrollPane(table), BorderLayout.CENTER);
        contentArea.add(left, BorderLayout.CENTER);

        JPanel right = PharmTheme.card();
        right.setLayout(new BorderLayout());
        right.setPreferredSize(new Dimension(280, 0));
        right.setBorder(BorderFactory.createCompoundBorder(new PharmTheme.RoundedBorder(PharmTheme.R12, PharmTheme.BORDER, 0), BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        txtDetails = PharmTheme.textArea(20, 0); txtDetails.setEditable(false); txtDetails.setFont(PharmTheme.FONT_MONO); txtDetails.setText("Sélectionnez un client…");
        right.add(PharmTheme.scrollTextArea(txtDetails), BorderLayout.CENTER);
        contentArea.add(right, BorderLayout.EAST);
    }

    private void searchByName() {
        tableModel.setRowCount(0);
        String q = txtSearch.getText().trim();
        if (q.isEmpty()) return;
        try { for (Client c : gestionClient.rechercherParNom(q)) addRow(c); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }

    private void searchByCnam() {
        tableModel.setRowCount(0);
        String q = txtCodeCnam.getText().trim();
        if (q.isEmpty()) return;
        try { Client c = gestionClient.rechercherParCodeCnam(q); if (c != null) addRow(c); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }

    private void loadAll() {
        tableModel.setRowCount(0);
        try { for (Client c : gestionClient.listerTous()) addRow(c); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }

    private void addRow(Client c) {
        tableModel.addRow(new Object[]{ c.getNumClient(), c.getNumCarteIdentite(), c.getNom(), c.getPrenom(), c.getTelephone(), c.getPointFidelite(), c.getCodeCnam() != null ? c.getCodeCnam() : "—" });
    }

    private void showDetails() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (Integer) tableModel.getValueAt(row, 0);
        try {
            Client c = gestionClient.rechercherParId(id);
            if (c == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━\n").append("  ").append(c.getPrenom()).append(" ").append(c.getNom()).append("\n").append("━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            sb.append("N° Client    : ").append(c.getNumClient()).append("\n");
            sb.append("N° Carte     : ").append(c.getNumCarteIdentite()).append("\n");
            sb.append("Âge          : ").append(c.getAge()).append(" ans\n");
            sb.append("Téléphone    : ").append(c.getTelephone()).append("\n");
            sb.append("Email        : ").append(c.getAdresseMail() != null ? c.getAdresseMail() : "—").append("\n");
            sb.append("Adresse      : ").append(c.getAdresse() != null ? c.getAdresse() : "—").append("\n\n");
            sb.append("Points       : ").append(c.getPointFidelite()).append("\n");
            sb.append("Code CNAM    : ").append(c.getCodeCnam() != null ? c.getCodeCnam() : "—").append("\n");
            txtDetails.setText(sb.toString()); txtDetails.setCaretPosition(0);
        } catch (SQLException ex) { txtDetails.setText("Erreur : " + ex.getMessage()); }
    }
}
