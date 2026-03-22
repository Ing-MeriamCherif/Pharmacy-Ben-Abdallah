package entitebd;

import entite.VoieCommande;
import java.sql.*;
import java.util.ArrayList;

public class VoieCommandeBD {
    public void ajouterLigne(VoieCommande lc) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        lc.setPrixTotalVoieCommande();
        String sql = "INSERT INTO voie_commande (num_commande, ref_medicament, quantite, prix_unitaire, prix_total_voie_commande, remise, impots_sur_commande) VALUES (" +
                lc.getNumCommande() + ", " + lc.getRefMedicament() + ", " + lc.getQuantite() + ", " + lc.getPrixUnitaire() + ", " +
                lc.getPrixTotalVoieCommande() + ", " + lc.getRemise() + ", " + lc.getImpotSurCommande() + ")";
        st.executeUpdate(sql); st.close();
    }

    public void modifierLigne(VoieCommande lc) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        lc.setPrixTotalVoieCommande();
        String sql = "UPDATE voie_commande SET num_commande = " + lc.getNumCommande() + ", ref_medicament = " + lc.getRefMedicament() +
                ", quantite = " + lc.getQuantite() + ", prix_unitaire = " + lc.getPrixUnitaire() + ", prix_total_voie_commande = " +
                lc.getPrixTotalVoieCommande() + ", remise = " + lc.getRemise() + ", impots_sur_commande = " + lc.getImpotSurCommande() +
                " WHERE id_ligne_commande = " + lc.getIdLigneCommande();
        st.executeUpdate(sql); st.close();
    }

    public void supprimerLigne(int idLigneCommande) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        st.executeUpdate("DELETE FROM voie_commande WHERE id_ligne_commande = " + idLigneCommande); st.close();
    }

    public ArrayList<VoieCommande> getLignesParCommande(int numCommande) throws SQLException {
        ArrayList<VoieCommande> lignes = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM voie_commande WHERE num_commande = " + numCommande);
        while (rs.next()) { lignes.add(map(rs)); }
        rs.close(); st.close();
        return lignes;
    }

    public VoieCommande getLigneById(int idLigneCommande) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM voie_commande WHERE id_ligne_commande = " + idLigneCommande);
        VoieCommande lc = null;
        if (rs.next()) { lc = map(rs); }
        rs.close(); st.close();
        return lc;
    }

    private VoieCommande map(ResultSet rs) throws SQLException {
        VoieCommande lc = new VoieCommande();
        lc.setIdLigneCommande(rs.getInt("id_ligne_commande")); lc.setNumCommande(rs.getInt("num_commande"));
        lc.setRefMedicament(rs.getInt("ref_medicament")); lc.setQuantite(rs.getInt("quantite"));
        lc.setPrixUnitaire(rs.getDouble("prix_unitaire")); lc.setRemise(rs.getDouble("remise"));
        lc.setImpotSurCommande(rs.getDouble("impots_sur_commande"));
        return lc;
    }
}
