package entitebd;

import entite.Fournisseur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurBD {
    private String escapeSql(String value) {
        if (value == null) return "";
        return value.replace("'", "''");
    }

    public int ajouter(Fournisseur f) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        int numFournisseur = f.getNumFournisseur();
        if (numFournisseur <= 0) {
            ResultSet rsMax = st.executeQuery("SELECT COALESCE(MAX(num_fournisseur), 0) + 1 as next_id FROM fournisseur");
            if (rsMax.next()) { numFournisseur = rsMax.getInt("next_id"); }
            rsMax.close();
        }
        String sql = "INSERT INTO fournisseur (num_fournisseur, nom_fournisseur, adresse, telephone, adresse_mail, rate) VALUES (" +
                numFournisseur + ", '" + escapeSql(f.getNomFournisseur()) + "', '" + escapeSql(f.getAdresse()) + "', '" +
                escapeSql(f.getTelephone()) + "', '" + escapeSql(f.getAdresseEmail()) + "', " + f.getRate() + ")";
        int result = st.executeUpdate(sql);
        st.close();
        return result > 0 ? numFournisseur : -1;
    }

    public Fournisseur rechercherParId(int numFournisseur) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM fournisseur WHERE num_fournisseur = " + numFournisseur);
        Fournisseur f = null;
        if (rs.next()) { f = map(rs); }
        rs.close(); st.close();
        return f;
    }

    public List<Fournisseur> listerTous() throws SQLException {
        List<Fournisseur> list = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM fournisseur ORDER BY nom_fournisseur");
        while (rs.next()) { list.add(map(rs)); }
        rs.close(); st.close();
        return list;
    }

    public boolean modifier(Fournisseur f) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "UPDATE fournisseur SET nom_fournisseur = '" + escapeSql(f.getNomFournisseur()) + "', adresse = '" +
                escapeSql(f.getAdresse()) + "', telephone = '" + escapeSql(f.getTelephone()) + "', adresse_mail = '" +
                escapeSql(f.getAdresseEmail()) + "', rate = " + f.getRate() + " WHERE num_fournisseur = " + f.getNumFournisseur();
        int result = st.executeUpdate(sql);
        st.close();
        return result > 0;
    }

    public boolean supprimer(int numFournisseur) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        int result = st.executeUpdate("DELETE FROM fournisseur WHERE num_fournisseur = " + numFournisseur);
        st.close();
        return result > 0;
    }

    public double calculerPerformance(int numFournisseur) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT COUNT(*) as nb_commandes, f.rate as rate FROM commande c JOIN fournisseur f ON c.num_fournisseur = f.num_fournisseur WHERE f.num_fournisseur = " + numFournisseur;
        ResultSet rs = st.executeQuery(sql);
        double performance = 0.0;
        if (rs.next()) {
            int nbCommandes = rs.getInt("nb_commandes");
            double rate = rs.getDouble("rate");
            performance = (nbCommandes * rate) / 100.0;
        }
        rs.close(); st.close();
        return performance;
    }

    private Fournisseur map(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setNumFournisseur(rs.getInt("num_fournisseur"));
        f.setNomFournisseur(rs.getString("nom_fournisseur"));
        f.setAdresse(rs.getString("adresse"));
        f.setTelephone(rs.getString("telephone"));
        f.setAdresseEmail(rs.getString("adresse_mail"));
        f.setRate(rs.getFloat("rate"));
        return f;
    }
}
