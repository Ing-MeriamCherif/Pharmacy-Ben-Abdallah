package entite;
import java.util.Date;


public class StockMedicament {
    private int numStock;
    private int refMedicament;
    private int quantiteProduit;
    private double prixAchat;
    private double prixVente;
    private int seuilMin;
    private Date dateExpiration;

    private Date dateFabrication;

    public Date getDateFabrication() {
        return dateFabrication;
    }

    public void setDateFabrication(Date dateFabrication) {
        this.dateFabrication = dateFabrication;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Date dateExpriration) {
        this.dateExpiration = dateExpriration;
    }
    public int getNumStock() {
        return numStock;
    }
    public void setNumStock(int numStock) {
        this.numStock = numStock;
    }
    public int getRefMedicament() {
        return refMedicament;
    }
    public void setRefMedicament(int i) {
        this.refMedicament = i;
    }
    public double getPrixAchat() {
        return prixAchat;
    }
    public void setPrixAchat(double prixAchat) {
        this.prixAchat = prixAchat;
    }
    public int  getQuantiteProduit() {
        return quantiteProduit;
    }
    public void setQuantiteProduit(int quantiteProduit) {
        this.quantiteProduit = quantiteProduit;
    }
    public double getPrixVente() {
        return prixVente;
    }
    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }
    public int getSeuilMin() {
        return seuilMin;
    }
    public void setSeuilMin(int seuilMin) {
        this.seuilMin = seuilMin;
    }
    public void ajouterStock(double quantite) {
        this.quantiteProduit += quantite;
    }
    public boolean Alerte() {
        return quantiteProduit <= seuilMin;
    }
    public boolean estPerime() {
        if (dateExpiration == null) return false;
        return new Date().after(dateExpiration);
    }


}