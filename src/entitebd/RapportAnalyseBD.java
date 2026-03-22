package entitebd;

import entite.RapportAnalyse;
import java.sql.*;

public class RapportAnalyseBD {
    public void ajouter(RapportAnalyse rapport) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        st.executeUpdate("INSERT INTO rapport_analyse (id, chiffre_affaire, num_carte_emp) VALUES (" +
                rapport.getId() + ", " + rapport.getChiffreAffaire() + ", " + rapport.getNumCarteEmp() + ")");
        st.close();
    }

    public RapportAnalyse getRapportByEmploye(int numCarteEmp) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM rapport_analyse WHERE num_carte_emp = " + numCarteEmp);
        RapportAnalyse rapport = null;
        if (rs.next()) { rapport = map(rs); }
        rs.close(); st.close();
        return rapport;
    }

    public void modifier(RapportAnalyse rapport) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        st.executeUpdate("UPDATE rapport_analyse SET chiffre_affaire = " + rapport.getChiffreAffaire() +
                ", num_carte_emp = " + rapport.getNumCarteEmp() + " WHERE id = " + rapport.getId());
        st.close();
    }

    private RapportAnalyse map(ResultSet rs) throws SQLException {
        RapportAnalyse rapport = new RapportAnalyse();
        rapport.setId(rs.getInt("id")); rapport.setChiffreAffaire(rs.getDouble("chiffre_affaire"));
        rapport.setNumCarteEmp(rs.getInt("num_carte_emp"));
        return rapport;
    }
}
