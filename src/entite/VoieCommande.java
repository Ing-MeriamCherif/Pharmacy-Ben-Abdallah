package entite;

public class VoieCommande {
    private int idLigneCommande;
    private int quantite;
    private double prixUnitaire;
    private double remise;
    private double impotsSurCommande;
    private int numCommande;
    private int refMedicament;
    private double prixTotalVoieCommande;
    public double getPrixTotalVoieCommande() { return prixTotalVoieCommande; }
    public void setPrixTotalVoieCommande() { prixTotalVoieCommande = quantite * prixUnitaire; }
    public VoieCommande() {}
    public VoieCommande(int numCommande, int refMedicament, int quantite, double prixUnitaire, double remise, double impotSurCommande) {
        this.numCommande = numCommande; this.refMedicament = refMedicament;
        this.quantite = quantite; this.prixUnitaire = prixUnitaire;
        this.remise = remise; this.impotsSurCommande = impotSurCommande;
    }
    public double calculerTotal() {
        double sousTotal = quantite * prixUnitaire;
        double apresRemise = sousTotal * (1 - remise / 100);
        return apresRemise * (1 + impotsSurCommande / 100);
    }
    public int getRefMedicament() { return refMedicament; }
    public void setRefMedicament(int refMedicament) { this.refMedicament = refMedicament; }
    public int getNumCommande() { return numCommande; }
    public void setNumCommande(int numCommande) { this.numCommande = numCommande; }
    public int getIdLigneCommande() { return idLigneCommande; }
    public void setIdLigneCommande(int idLigneCommande) { this.idLigneCommande = idLigneCommande; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public double getRemise() { return remise; }
    public void setRemise(double remise) { this.remise = remise; }
    public double getImpotSurCommande() { return impotsSurCommande; }
    public void setImpotSurCommande(double impotSurCommande) { this.impotsSurCommande = impotSurCommande; }
}
