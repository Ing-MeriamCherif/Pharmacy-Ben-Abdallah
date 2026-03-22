package entitebd;

import entite.Commande;
import java.sql.*;
import java.util.ArrayList;

public class CommandeBD {
    public void ajouterCommande(Commande c) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "INSERT INTO commande (statut, montant_total_commande, date_achat, date_lim_rendre_produit, num_fournisseur, num_carte_emp) VALUES ('" +
                c.getStatut() + "', " + c.getMontantTotalCommande() + ", '" + c.getDateAchat() + "', '" +
                c.getDateLimRendreProduit() + "', " + c.getNumFournisseur() + ", " + c.getNumCarteEmp() + ")";
        st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = st.getGeneratedKeys();
        if (rs.next()) { c.setNumCommande(rs.getInt(1)); }
        rs.close(); st.close();
    }

    public Commande getCommandeById(int numCommande) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM commande WHERE num_commande = " + numCommande);
        Commande c = null;
        if (rs.next()) { c = map(rs); }
        rs.close(); st.close();
        return c;
    }

    public void modifierCommande(Commande c) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "UPDATE commande SET statut = '" + c.getStatut() + "', montant_total_commande = " + c.getMontantTotalCommande() +
                ", date_achat = '" + c.getDateAchat() + "', date_lim_rendre_produit = '" + c.getDateLimRendreProduit() +
                "', num_fournisseur = " + c.getNumFournisseur() + ", num_carte_emp = " + c.getNumCarteEmp() +
                " WHERE num_commande = " + c.getNumCommande();
        st.executeUpdate(sql); st.close();
    }

    public void modifierStatut(int numCommande, String statut) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        st.executeUpdate("UPDATE commande SET statut = '" + statut + "' WHERE num_commande = " + numCommande);
        st.close();
    }

    public ArrayList<Commande> afficherToutesCommandes() throws SQLException {
        ArrayList<Commande> commandes = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM commande ORDER BY date_achat DESC");
        while (rs.next()) { commandes.add(map(rs)); }
        rs.close(); st.close();
        return commandes;
    }

    private Commande map(ResultSet rs) throws SQLException {
        Commande c = new Commande();
        c.setNumCommande(rs.getInt("num_commande")); c.setStatut(rs.getString("statut"));
        c.setMontantTotalCommande(rs.getDouble("montant_total_commande")); c.setDateAchat(rs.getString("date_achat"));
        c.setDateLimRendreProduit(rs.getString("date_lim_rendre_produit")); c.setNumFournisseur(rs.getInt("num_fournisseur"));
        c.setNumCarteEmp(rs.getInt("num_carte_emp"));
        return c;
    }
}
