package gestion;

import entitebd.CommandeBD;
import entitebd.VoieCommandeBD;
import entitebd.RapportAnalyseBD;
import entitebd.StockBD;
import entitebd.MedicamentBD;
import entite.Commande;
import entite.VoieCommande;
import entite.RapportAnalyse;
import entite.StockMedicament;
import java.sql.SQLException;
import java.util.ArrayList;

public class GestionCommande {
    private CommandeBD commandeBD;
    private VoieCommandeBD voieCommandeBD;
    private RapportAnalyseBD rapportBD;

    public GestionCommande() { this.commandeBD = new CommandeBD(); this.voieCommandeBD = new VoieCommandeBD(); this.rapportBD = new RapportAnalyseBD(); }

    public int creerCommande(Commande commande, ArrayList<VoieCommande> lignes) throws SQLException {
        if (commande == null) { throw new IllegalArgumentException("La commande ne peut pas être null"); }
        if (lignes == null || lignes.isEmpty()) { throw new IllegalArgumentException("Une commande doit avoir au moins une ligne"); }
        commande.setStatut("En attente");
        commande.setMontantTotalCommande(calculerTotalCommande(lignes));
        commandeBD.ajouterCommande(commande);
        int numCommande = commande.getNumCommande();
        for (VoieCommande ligne : lignes) { ligne.setNumCommande(numCommande); ligne.setPrixTotalVoieCommande(); voieCommandeBD.ajouterLigne(ligne); }
        return numCommande;
    }

    public void modifierCommande(int numCommande, ArrayList<VoieCommande> nouvellesLignes) throws SQLException {
        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) { throw new IllegalArgumentException("Commande #" + numCommande + " introuvable"); }
        if ("Reçue".equals(commande.getStatut()) || "Annulée".equals(commande.getStatut())) { throw new IllegalArgumentException("Impossible de modifier une commande " + commande.getStatut()); }
        for (VoieCommande ligne : nouvellesLignes) { ligne.setPrixTotalVoieCommande(); voieCommandeBD.modifierLigne(ligne); }
        commande.setMontantTotalCommande(calculerTotalCommande(nouvellesLignes));
        commandeBD.modifierCommande(commande);
    }

    public void annulerCommande(int numCommande) throws SQLException {
        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) { throw new IllegalArgumentException("Commande #" + numCommande + " introuvable"); }
        if ("Reçue".equals(commande.getStatut())) { throw new IllegalArgumentException("Impossible d'annuler une commande déjà reçue"); }
        ArrayList<VoieCommande> lignes = voieCommandeBD.getLignesParCommande(numCommande);
        for (VoieCommande ligne : lignes) { voieCommandeBD.supprimerLigne(ligne.getIdLigneCommande()); }
        commande.setStatut("Annulée"); commande.setMontantTotalCommande(0);
        commandeBD.modifierCommande(commande);
    }

    public void marquerCommeRecue(int numCommande, int numCarteEmp) throws SQLException {
        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) { throw new IllegalArgumentException("Commande #" + numCommande + " introuvable"); }
        if ("Reçue".equals(commande.getStatut())) { throw new IllegalArgumentException("Commande déjà réceptionnée"); }
        if ("Annulée".equals(commande.getStatut())) { throw new IllegalArgumentException("Impossible de réceptionner une commande annulée"); }
        ArrayList<VoieCommande> lignes = voieCommandeBD.getLignesParCommande(numCommande);
        StockBD stockBD = new StockBD();
        for (VoieCommande ligne : lignes) {
            try {
                StockMedicament nouveauStock = new StockMedicament();
                nouveauStock.setRefMedicament(ligne.getRefMedicament()); nouveauStock.setQuantiteProduit(ligne.getQuantite());
                nouveauStock.setPrixAchat(ligne.getPrixUnitaire()); nouveauStock.setPrixVente(ligne.getPrixUnitaire() * 1.3); nouveauStock.setSeuilMin(10);
                stockBD.ajouter(nouveauStock);
            } catch (SQLException e) { System.err.println("⚠ Erreur stock pour médicament #" + ligne.getRefMedicament() + ": " + e.getMessage()); }
        }
        commande.setStatut("Reçue"); commandeBD.modifierCommande(commande);
        mettreAJourChiffreAffaires(commande.getMontantTotalCommande(), numCarteEmp);
    }

    public BilanCommande obtenirBilanCommande(int numCommande) throws SQLException {
        Commande commande = commandeBD.getCommandeById(numCommande);
        if (commande == null) { throw new IllegalArgumentException("Commande #" + numCommande + " introuvable"); }
        ArrayList<VoieCommande> lignes = voieCommandeBD.getLignesParCommande(numCommande);
        return new BilanCommande(commande, lignes);
    }

    public ArrayList<Commande> listerToutesCommandes() throws SQLException { return commandeBD.afficherToutesCommandes(); }
    public ArrayList<VoieCommande> getLignesCommande(int numCommande) throws SQLException { return voieCommandeBD.getLignesParCommande(numCommande); }

    private double calculerTotalCommande(ArrayList<VoieCommande> lignes) {
        double total = 0;
        for (VoieCommande ligne : lignes) { total += ligne.calculerTotal(); }
        return total;
    }

    private void mettreAJourChiffreAffaires(double montant, int numCarteEmp) throws SQLException {
        try {
            RapportAnalyse rapport = rapportBD.getRapportByEmploye(numCarteEmp);
            if (rapport != null) { rapport.setChiffreAffaire(rapport.getChiffreAffaire() + montant); rapportBD.modifier(rapport); }
            else { rapport = new RapportAnalyse(); rapport.setId((int)(System.currentTimeMillis() % 1000000)); rapport.setNumCarteEmp(numCarteEmp); rapport.setChiffreAffaire(montant); rapportBD.ajouter(rapport); }
        } catch (Exception e) { System.out.println("⚠️ Impossible de mettre à jour le chiffre d'affaires"); }
    }

    public static class BilanCommande {
        private Commande commande;
        private ArrayList<VoieCommande> lignes;
        public BilanCommande(Commande commande, ArrayList<VoieCommande> lignes) { this.commande = commande; this.lignes = lignes; }
        public Commande getCommande() { return commande; }
        public ArrayList<VoieCommande> getLignes() { return lignes; }
        public double getTotal() { return commande.getMontantTotalCommande(); }
        public int getNombreLignes() { return lignes.size(); }
    }
}
