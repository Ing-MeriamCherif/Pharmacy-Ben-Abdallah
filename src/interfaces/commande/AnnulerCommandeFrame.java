package interfaces.commande;

import interfaces.theme.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import entite.*;
import gestion.GestionCommande;

public class AnnulerCommandeFrame extends PharmBaseFrame {
    private JTextField txtNum;
    private JTextArea txtInfo;
    private final GestionCommande gestionCommande = new GestionCommande();

    public AnnulerCommandeFrame() {
        super("Annuler une commande", "Annuler et supprimer les lignes", 680, 480);
        buildUI();
    }

    @Override
    protected void populateHeaderActions(JPanel p) {
        JButton close = PharmTheme.ghostButton("Fermer"); close.addActionListener(e -> dispose());
        JButton annuler = PharmTheme.dangerButton("Annuler la commande"); annuler.addActionListener(e -> annuler());
        p.add(close); p.add(annuler);
    }

    private void buildUI() {
        contentArea.setLayout(new BorderLayout(0, 12));

        JPanel searchRow = new JPanel(new BorderLayout(6, 0)); searchRow.setBackground(PharmTheme.BG); searchRow.setPreferredSize(new Dimension(0, 36));
        txtNum = PharmTheme.textField("Numéro de commande"); txtNum.addActionListener(e -> rechercher());
        JButton btn = PharmTheme.ghostButton("Chercher"); btn.addActionListener(e -> rechercher());
        searchRow.add(txtNum, BorderLayout.CENTER); searchRow.add(btn, BorderLayout.EAST);
        contentArea.add(searchRow, BorderLayout.NORTH);

        txtInfo = PharmTheme.textArea(14, 0); txtInfo.setEditable(false); txtInfo.setFont(PharmTheme.FONT_MONO); txtInfo.setText("Recherchez une commande pour voir ses informations…");
        contentArea.add(PharmTheme.scrollTextArea(txtInfo), BorderLayout.CENTER);

        JPanel warn = new JPanel(new FlowLayout(FlowLayout.LEFT)); warn.setBackground(PharmTheme.DANGER_BG); warn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        JLabel wl = new JLabel("⚠  L'annulation supprime toutes les lignes et est irréversible."); wl.setFont(PharmTheme.FONT_SM); wl.setForeground(PharmTheme.DANGER); warn.add(wl);
        contentArea.add(warn, BorderLayout.SOUTH);
    }

    private void rechercher() {
        try {
            GestionCommande.BilanCommande bilan = gestionCommande.obtenirBilanCommande(Integer.parseInt(txtNum.getText().trim()));
            Commande c = bilan.getCommande();
            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n  Commande #").append(c.getNumCommande()).append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            sb.append("Date     : ").append(c.getDateAchat()).append("\nStatut   : ").append(c.getStatut()).append("\nLignes   : ").append(bilan.getNombreLignes()).append("\nTotal    : ").append(String.format("%.2f DT", bilan.getTotal())).append("\n\n");
            if ("Reçue".equals(c.getStatut())) sb.append("⛔ Commande déjà reçue — ne peut pas être annulée.\n");
            else if ("Annulée".equals(c.getStatut())) sb.append("ℹ️  Commande déjà annulée.\n");
            else sb.append("✅ Cette commande peut être annulée.\n");
            txtInfo.setText(sb.toString());
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Invalide", "Entrez un numéro de commande valide."); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); txtInfo.setText("Commande introuvable."); }
    }

    private void annuler() {
        try {
            int num = Integer.parseInt(txtNum.getText().trim());
            if (!PharmTheme.showConfirm(this, "Confirmer annulation", "Annuler la commande #" + num + " ?\nCette action est irréversible.")) return;
            gestionCommande.annulerCommande(num);
            PharmTheme.showSuccess(this, "Annulée", "Commande #" + num + " annulée avec succès.");
            rechercher();
        } catch (NumberFormatException ex) { PharmTheme.showError(this, "Invalide", "Entrez un numéro de commande valide."); }
        catch (Exception ex) { PharmTheme.showError(this, "Erreur", ex.getMessage()); }
    }
}
