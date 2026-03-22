package entitebd;

import entite.VoieVente;
import java.sql.*;
import java.util.ArrayList;

public class VoieVenteBD {
    public void ajouterLigne(VoieVente lv) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "INSERT INTO voie_vente (quantite, prix_unitaire, prix_total_voie_vente, num_vente, ref_medicament) VALUES (" +
                lv.getQuantite() + ", " + lv.getPrixUnitaire() + ", " + lv.getPrixTotalVoieVente() + ", " +
                lv.getNumVente() + ", " + lv.getRefMedicament() + ")";
        st.executeUpdate(sql); st.close();
    }

    public void modifierLigne(VoieVente lv) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "UPDATE voie_vente SET num_vente = " + lv.getNumVente() + ", ref_medicament = " + lv.getRefMedicament() +
                ", quantite = " + lv.getQuantite() + ", prix_unitaire = " + lv.getPrixUnitaire() +
                " WHERE id_ligne_vente = " + lv.getIdLigneVente();
        st.executeUpdate(sql); st.close();
    }

    public void supprimerLigne(int idLigneVente) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        st.executeUpdate("DELETE FROM voie_vente WHERE id_ligne_vente = " + idLigneVente); st.close();
    }

    public ArrayList<VoieVente> getLignesParVente(int numVente) throws SQLException {
        ArrayList<VoieVente> lignes = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM voie_vente WHERE num_vente = " + numVente);
        while (rs.next()) { lignes.add(map(rs)); }
        rs.close(); st.close();
        return lignes;
    }

    public VoieVente getLigneById(int idLigneVente) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM voie_vente WHERE id_ligne_vente = " + idLigneVente);
        VoieVente lv = null;
        if (rs.next()) { lv = map(rs); }
        rs.close(); st.close();
        return lv;
    }

    private VoieVente map(ResultSet rs) throws SQLException {
        VoieVente lv = new VoieVente();
        lv.setIdLigneVente(rs.getInt("id_ligne_vente")); lv.setNumVente(rs.getInt("num_vente"));
        lv.setRefMedicament(rs.getInt("ref_medicament")); lv.setQuantite(rs.getInt("quantite"));
        lv.setPrixUnitaire(rs.getDouble("prix_unitaire"));
        lv.setPrixTotalVoieVente();
        return lv;
    }
}
