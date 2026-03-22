package entite;
import java.util.Date;


public class Medicament {

    private int refMedicament;
    private String nom;
    private int numFournisseur; // numFournisseur
    private String descriptio;
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public int  getRefMedicament() {
        return refMedicament;
    }
    public void setRefMedicament(int refMedicament) {
        this.refMedicament = refMedicament;
    }
    public int getNumFournisseur() {
        return numFournisseur;
    }
    public void setNumFournisseur(int numFournisseur) {
        this.numFournisseur = numFournisseur;
    }

    public String getDescriptio() {
        return descriptio;
    }
    public void setDescriptio(String description) {
        this.descriptio = description;
    }


}