package gestion;

import entite.Client;
import entitebd.ClientBD;
import entitebd.PersonneBD;
import entite.Personne;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class GestionClient {
    private ClientBD clientBD;
    private PersonneBD personneBD;

    public GestionClient() { this.clientBD = new ClientBD(); this.personneBD = new PersonneBD(); }

    public int ajouterClient(Client client) throws SQLException {
        validerClient(client);
        Personne personneExistante = personneBD.rechercherParNumCarte(client.getNumCarteIdentite());
        if (personneExistante == null) { int numCarte = personneBD.ajouter(client); client.setNumCarteIdentite(numCarte); }
        return clientBD.ajouter(client);
    }

    public boolean modifierClient(Client client) throws SQLException {
        validerClient(client);
        personneBD.modifier(client);
        return clientBD.mettreAJourPoints(client.getNumClient(), client.getPointFidelite());
    }

    public Client rechercherParId(int numClient) throws SQLException { return clientBD.rechercherParId(numClient); }

    public List<Client> rechercherParNom(String nom) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) { throw new IllegalArgumentException("Le nom de recherche ne peut pas être vide"); }
        return clientBD.rechercherParNom(nom);
    }

    public Client rechercherParCodeCnam(String codeCnam) throws SQLException {
        if (codeCnam == null || codeCnam.trim().isEmpty()) { throw new IllegalArgumentException("Le code CNAM ne peut pas être vide"); }
        return clientBD.rechercherParCodeCnam(codeCnam);
    }

    public List<Client> listerTous() throws SQLException { return clientBD.listerTous(); }

    public void ajouterPoints(int numClient, int points) throws SQLException {
        if (points <= 0) { throw new IllegalArgumentException("Le nombre de points doit être positif"); }
        Client client = clientBD.rechercherParId(numClient);
        if (client == null) { throw new IllegalArgumentException("Client introuvable: " + numClient); }
        clientBD.mettreAJourPoints(numClient, client.getPointFidelite() + points);
    }

    public void utiliserPoints(int numClient, int points) throws SQLException {
        if (points <= 0) { throw new IllegalArgumentException("Le nombre de points doit être positif"); }
        Client client = clientBD.rechercherParId(numClient);
        if (client == null) { throw new IllegalArgumentException("Client introuvable: " + numClient); }
        if (client.getPointFidelite() < points) { throw new IllegalArgumentException("Points insuffisants! Client a " + client.getPointFidelite() + " points, " + points + " demandés"); }
        clientBD.mettreAJourPoints(numClient, client.getPointFidelite() - points);
    }

    public double calculerReduction(int points, double tauxConversion) {
        if (tauxConversion <= 0) { tauxConversion = 0.1; }
        return points * tauxConversion;
    }

    public List<Client> getTopClients(int limit) throws SQLException {
        List<Client> clients = clientBD.listerTous();
        clients.sort((c1, c2) -> Integer.compare(c2.getPointFidelite(), c1.getPointFidelite()));
        if (limit > 0 && clients.size() > limit) { return clients.subList(0, limit); }
        return clients;
    }

    private void validerClient(Client client) {
        if (client == null) { throw new IllegalArgumentException("Le client ne peut pas être null"); }
        if (client.getNom() == null || client.getNom().trim().isEmpty()) { throw new IllegalArgumentException("Le nom est obligatoire"); }
        if (client.getPrenom() == null || client.getPrenom().trim().isEmpty()) { throw new IllegalArgumentException("Le prénom est obligatoire"); }
        if (client.getNumCarteIdentite() <= 0) { throw new IllegalArgumentException("Le numéro de carte d'identité est invalide"); }
        if (client.getTelephone() == null || client.getTelephone().trim().isEmpty()) { throw new IllegalArgumentException("Le téléphone est obligatoire"); }
        if (client.getPointFidelite() < 0) { throw new IllegalArgumentException("Les points de fidélité ne peuvent pas être négatifs"); }
    }
}
