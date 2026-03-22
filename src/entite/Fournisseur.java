package entite;
public class Fournisseur {
    private int numFournisseur;
    private String nomFournisseur;
    private String adresse;
    private String telephone;
    private String AdresseEmail;
    private double rate;
    public int getNumFournisseur() { return numFournisseur; }
    public String getNomFournisseur() { return nomFournisseur; }
    public String getTelephone() { return telephone; }
    public String getAdresseEmail() { return AdresseEmail; }
    public void setNumFournisseur(int numFournisseur) { this.numFournisseur = numFournisseur; }
    public void setNomFournisseur(String nomFournisseur) { this.nomFournisseur = nomFournisseur; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setAdresseEmail(String AdresseEmail) { this.AdresseEmail = AdresseEmail; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
}
