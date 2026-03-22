package entite;

public class Personne {
    private int numCarteIdentite;
    private String nom;
    private String prenom;
    private String adresse;
    private String adresseMail;
    private String telephone;
    private int age;
    public int  getNumCarteIdentite() { return numCarteIdentite; }
    public void setNumCarteIdentite(int numCarteIdentite) { this.numCarteIdentite = numCarteIdentite; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getAdresseMail() { return adresseMail; }
    public void setAdresseMail(String adresseMail) { this.adresseMail = adresseMail; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
