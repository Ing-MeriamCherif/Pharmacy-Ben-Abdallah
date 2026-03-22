package interfaces.vente;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import entite.*;
import entitebd.*;

public class ImprimerFactureFrame extends PharmBaseFrame {
    private JTextField txtNum;
    private JTextArea txtFacture;
    private JButton btnImprimer, btnExporter;
    private final VenteBD venteBD = new VenteBD();
    private final VoieVenteBD voieVenteBD = new VoieVenteBD();
    private final ClientBD clientBD = new ClientBD();
    private final MedicamentBD medicamentBD = new MedicamentBD();

    public ImprimerFactureFrame() {
        super("Imprimer une facture", "Générer le ticket de vente", 720, 680);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        btnImprimer = PharmTheme.ghostButton("🖨 Imprimer"); btnImprimer.setEnabled(false); btnImprimer.addActionListener(e -> imprimer());
        btnExporter = PharmTheme.ghostButton("💾 Exporter"); btnExporter.setEnabled(false); btnExporter.addActionListener(e -> exporter());
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        p.add(btnImprimer); p.add(btnExporter); p.add(close);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 10));

        JPanel searchRow = new JPanel(new BorderLayout(6, 0)); searchRow.setBackground(PharmTheme.BG); searchRow.setPreferredSize(new Dimension(0, 36));
        txtNum = PharmTheme.textField("Numéro de vente…"); txtNum.addActionListener(e -> generer());
        JButton btn = PharmTheme.primaryButton("Générer →"); btn.addActionListener(e -> generer());
        searchRow.add(txtNum, BorderLayout.CENTER); searchRow.add(btn, BorderLayout.EAST);
        contentArea.add(searchRow, BorderLayout.NORTH);

        txtFacture = PharmTheme.textArea(24, 0); txtFacture.setEditable(false); txtFacture.setFont(PharmTheme.FONT_MONO); txtFacture.setText("Entrez un numéro de vente et cliquez sur Générer…");
        contentArea.add(PharmTheme.scrollTextArea(txtFacture), BorderLayout.CENTER);
    }

    private void generer() {
        try {
            int num = Integer.parseInt(txtNum.getText().trim());
            Vente v = venteBD.getVenteById(num);
            if (v == null) { PharmTheme.showWarning(this, "Introuvable", "Aucune vente #" + num); return; }
            ArrayList<VoieVente> lignes = voieVenteBD.getLignesParVente(num);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
            StringBuilder sb = new StringBuilder();
            sb.append("╔══════════════════════════════════════════════╗\n");
            sb.append("║          PHARMACIE — FACTURE DE VENTE        ║\n");
            sb.append("╚══════════════════════════════════════════════╝\n\n");
            sb.append("Facture N°  : ").append(v.getNumVente()).append("\n");
            sb.append("Date        : ").append(v.getDateVente()).append("\n");
            sb.append("Imprimée le : ").append(sdf.format(new Date())).append("\n\n");
            sb.append("──────────────────────────────────────────────\nCLIENT\n──────────────────────────────────────────────\n");
            if (v.getNumClient() > 0) {
                try { Client c = clientBD.rechercherParId(v.getNumClient()); if (c != null) { sb.append("Nom     : ").append(c.getNom()).append(" ").append(c.getPrenom()).append("\nCNAM    : ").append(c.getCodeCnam()).append("\nTél     : ").append(c.getTelephone()).append("\n"); } }
                catch (Exception ignored) { sb.append("Client N°").append(v.getNumClient()).append("\n"); }
            } else sb.append("Vente directe (sans client enregistré)\n");
            sb.append("\n──────────────────────────────────────────────\nPRODUITS\n──────────────────────────────────────────────\n");
            sb.append(String.format("%-28s %4s %8s %9s%n", "Produit", "Qté", "Prix U.", "Total"));
            sb.append("──────────────────────────────────────────────\n");
            for (VoieVente lv : lignes) {
                String nom = "Produit #" + lv.getRefMedicament();
                try { Medicament m = medicamentBD.rechercherParRef(lv.getRefMedicament()); if (m != null) nom = m.getNom().length() > 26 ? m.getNom().substring(0, 23) + "..." : m.getNom(); } catch (Exception ignored) {}
                sb.append(String.format("%-28s %4d %8.2f %9.2f%n", nom, lv.getQuantite(), lv.getPrixUnitaire(), lv.getPrixTotalVoieVente()));
            }
            sb.append("──────────────────────────────────────────────\n");
            sb.append(String.format("%40s : %8.2f DT%n", "MONTANT TOTAL", v.getMontantTotalVente()));
            if (v.getNumClient() > 0) sb.append(String.format("%40s : %8d pts%n", "Points fidélité gagnés", (int)(v.getMontantTotalVente()/10)));
            sb.append("\nDate limite de retour : ").append(v.getDateLimRendreProduit()).append("\n\n");
            sb.append("══════════════════════════════════════════════\n       Merci de votre confiance !\n══════════════════════════════════════════════\n");
            txtFacture.setText(sb.toString()); txtFacture.setCaretPosition(0);
            btnImprimer.setEnabled(true); btnExporter.setEnabled(true);
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Invalide", "Entrez un numéro de vente valide."); }
        catch (SQLException ex) { PharmTheme.showError(this, "Erreur BD", ex.getMessage()); }
    }

    private void imprimer() {
        try { boolean ok = txtFacture.print(); if (ok) PharmTheme.showSuccess(this, "Impression", "Impression envoyée avec succès."); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur impression", ex.getMessage()); }
    }

    private void exporter() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("facture_" + txtNum.getText() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) { pw.print(txtFacture.getText()); PharmTheme.showSuccess(this, "Exportée", fc.getSelectedFile().getName()); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }
}
