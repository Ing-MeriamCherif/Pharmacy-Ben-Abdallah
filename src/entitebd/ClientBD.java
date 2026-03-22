package entitebd;

import entite.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientBD {
    public int ajouter(Client client) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "INSERT INTO client (num_carte_identite, point_fidelite, code_cnam, dernier_date_achat) VALUES (" +
                client.getNumCarteIdentite() + ", " + client.getPointFidelite() + ", '" + client.getCodeCnam() + "', " +
                (client.getDernierDateAchat() != null ? "'" + new java.sql.Date(client.getDernierDateAchat().getTime()) + "'" : "NULL") + ")";
        int result = st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        int generatedId = -1;
        if (result > 0) { ResultSet keys = st.getGeneratedKeys(); if (keys.next()) { generatedId = keys.getInt(1); } keys.close(); }
        st.close();
        return generatedId;
    }

    public Client rechercherParId(int numClient) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT c.*, p.nom, p.prenom, p.age, p.adresse, p.adresse_mail, p.telephone FROM client c JOIN personne p ON c.num_carte_identite = p.num_carte_identite WHERE c.num_client = " + numClient;
        ResultSet rs = st.executeQuery(sql);
        Client client = null;
        if (rs.next()) { client = mapResultSetToClient(rs); }
        rs.close(); st.close();
        return client;
    }

    public Client rechercherParCodeCnam(String codeCnam) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT c.*, p.nom, p.prenom, p.age, p.adresse, p.adresse_mail, p.telephone FROM client c JOIN personne p ON c.num_carte_identite = p.num_carte_identite WHERE c.code_cnam = '" + codeCnam + "'";
        ResultSet rs = st.executeQuery(sql);
        Client client = null;
        if (rs.next()) { client = mapResultSetToClient(rs); }
        rs.close(); st.close();
        return client;
    }

    public List<Client> rechercherParNom(String nom) throws SQLException {
        List<Client> clients = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT c.*, p.nom, p.prenom, p.age, p.adresse, p.adresse_mail, p.telephone FROM client c JOIN personne p ON c.num_carte_identite = p.num_carte_identite WHERE p.nom LIKE '%" + nom + "%' OR p.prenom LIKE '%" + nom + "%' ORDER BY p.nom, p.prenom";
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) { clients.add(mapResultSetToClient(rs)); }
        rs.close(); st.close();
        return clients;
    }

    public boolean mettreAJourPoints(int numClient, int points) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        int result = st.executeUpdate("UPDATE client SET point_fidelite = " + points + " WHERE num_client = " + numClient);
        st.close();
        return result > 0;
    }

    public List<Client> listerTous() throws SQLException {
        List<Client> clients = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT c.*, p.nom, p.prenom, p.age, p.adresse, p.adresse_mail, p.telephone FROM client c JOIN personne p ON c.num_carte_identite = p.num_carte_identite ORDER BY p.nom, p.prenom";
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) { clients.add(mapResultSetToClient(rs)); }
        rs.close(); st.close();
        return clients;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setNumClient(rs.getInt("num_client"));
        client.setNumCarteIdentite(rs.getInt("num_carte_identite"));
        client.setPointFidelite(rs.getInt("point_fidelite"));
        client.setCodeCnam(rs.getString("code_cnam"));
        Date dernierAchat = rs.getDate("dernier_date_achat");
        if (dernierAchat != null) { client.setDernierDateAchat(dernierAchat); }
        client.setNom(rs.getString("nom")); client.setPrenom(rs.getString("prenom"));
        client.setAge(rs.getInt("age")); client.setAdresse(rs.getString("adresse"));
        client.setAdresseMail(rs.getString("adresse_mail")); client.setTelephone(rs.getString("telephone"));
        return client;
    }
}
