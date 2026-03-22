package gestion;

import entite.Fournisseur;
import entitebd.FournisseurBD;
import java.sql.SQLException;
import java.util.List;

public class GestionFournisseur {
    private FournisseurBD fournisseurBD;
    public GestionFournisseur() { this.fournisseurBD = new FournisseurBD(); }

    public int ajouterFournisseur(Fournisseur fournisseur) throws SQLException {
        validerFournisseur(fournisseur);
        return fournisseurBD.ajouter(fournisseur);
    }

    public boolean modifierFournisseur(Fournisseur fournisseur) throws SQLException {
        validerFournisseur(fournisseur);
        if (fournisseur.getNumFournisseur() <= 0) { throw new IllegalArgumentException("Numéro de fournisseur invalide"); }
        Fournisseur existing = fournisseurBD.rechercherParId(fournisseur.getNumFournisseur());
        if (existing == null) { throw new IllegalArgumentException("Fournisseur introuvable: " + fournisseur.getNumFournisseur()); }
        return fournisseurBD.modifier(fournisseur);
    }

    public boolean supprimerFournisseur(int numFournisseur) throws SQLException {
        if (numFournisseur <= 0) { throw new IllegalArgumentException("Numéro de fournisseur invalide"); }
        Fournisseur fournisseur = fournisseurBD.rechercherParId(numFournisseur);
        if (fournisseur == null) { throw new IllegalArgumentException("Fournisseur introuvable: " + numFournisseur); }
        return fournisseurBD.supprimer(numFournisseur);
    }

    public Fournisseur rechercherParId(int numFournisseur) throws SQLException { return fournisseurBD.rechercherParId(numFournisseur); }
    public List<Fournisseur> listerTous() throws SQLException { return fournisseurBD.listerTous(); }
    public double calculerPerformance(int numFournisseur) throws SQLException { return fournisseurBD.calculerPerformance(numFournisseur); }

    public boolean evaluerFournisseur(int numFournisseur, double nouveauTaux) throws SQLException {
        if (nouveauTaux < 0 || nouveauTaux > 100) { throw new IllegalArgumentException("Le taux doit être entre 0 et 100"); }
        Fournisseur fournisseur = fournisseurBD.rechercherParId(numFournisseur);
        if (fournisseur == null) { throw new IllegalArgumentException("Fournisseur introuvable: " + numFournisseur); }
        fournisseur.setRate(nouveauTaux);
        return fournisseurBD.modifier(fournisseur);
    }

    public List<Fournisseur> getTopFournisseurs(int limit) throws SQLException {
        List<Fournisseur> fournisseurs = fournisseurBD.listerTous();
        fournisseurs.sort((f1, f2) -> Double.compare(f2.getRate(), f1.getRate()));
        if (limit > 0 && fournisseurs.size() > limit) { return fournisseurs.subList(0, limit); }
        return fournisseurs;
    }

    private void validerFournisseur(Fournisseur fournisseur) {
        if (fournisseur == null) { throw new IllegalArgumentException("Le fournisseur ne peut pas être null"); }
        if (fournisseur.getNomFournisseur() == null || fournisseur.getNomFournisseur().trim().isEmpty()) { throw new IllegalArgumentException("Le nom du fournisseur est obligatoire"); }
        if (fournisseur.getTelephone() == null || fournisseur.getTelephone().trim().isEmpty()) { throw new IllegalArgumentException("Le téléphone est obligatoire"); }
        if (fournisseur.getAdresseEmail() == null || fournisseur.getAdresseEmail().trim().isEmpty()) { throw new IllegalArgumentException("L'email est obligatoire"); }
        if (!fournisseur.getAdresseEmail().contains("@")) { throw new IllegalArgumentException("Format d'email invalide"); }
        if (fournisseur.getRate() < 0 || fournisseur.getRate() > 100) { throw new IllegalArgumentException("Le taux doit être entre 0 et 100"); }
    }
}
