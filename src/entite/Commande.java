package entite;

public class Commande {
    private int numCommande;
    private String dateAchat;
    private String dateLimRendreProduit;
    private String statut;
    private int numFournisseur;
    private int numCarteEmp;
    private double montantTotalCommande;
    public double getMontantTotalCommande() { return montantTotalCommande; }
    public void setMontantTotalCommande(double montantTotalCommande) { this.montantTotalCommande = montantTotalCommande; }
    public int getNumFournisseur() { return numFournisseur; }
    public void setNumFournisseur(int numFournisseur) { this.numFournisseur = numFournisseur; }
    public int getNumCommande() { return numCommande; }
    public void setNumCommande(int numCommande) { this.numCommande = numCommande; }
    public String getDateAchat() { return dateAchat; }
    public void setDateAchat(String dateAchat) { this.dateAchat = dateAchat; }
    public String getDateLimRendreProduit() { return dateLimRendreProduit; }
    public void setDateLimRendreProduit(String dateLimRendreProduit) { this.dateLimRendreProduit = dateLimRendreProduit; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getNumCarteEmp() { return numCarteEmp; }
    public void setNumCarteEmp(int numCarteEmp) { this.numCarteEmp = numCarteEmp; }
}
