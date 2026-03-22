package entite;

public class VoieVente {
    private int idLigneVente;
    private int quantite;
    private double prixUnitaire;
    private int numVente;
    private int refMedicament;
    private double prixTotalVoieVente;
    public double getPrixTotalVoieVente() { return prixTotalVoieVente; }
    public void setPrixTotalVoieVente() { this.prixTotalVoieVente = prixUnitaire * quantite; }
    public int getRefMedicament() { return refMedicament; }
    public void setRefMedicament(int refMedicament) { this.refMedicament = refMedicament; }
    public int getNumVente() { return numVente; }
    public void setNumVente(int numVente) { this.numVente = numVente; }
    public int getIdLigneVente() { return idLigneVente; }
    public void setIdLigneVente(int idLigneVente) { this.idLigneVente = idLigneVente; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
}
