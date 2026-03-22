package entitebd;

import entite.Employe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeBD {
    public Employe authentifier(int numCNSS, String motDePasse) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT e.num_carte_emp, e.num_carte_identite, e.num_cnss, e.salaire, e.poste, e.mot_de_passe, e.heure_debut_travail, e.heure_sortie, e.nb_jours_par_semaine, e.date_rejoind_travail, p.nom, p.prenom, p.age, p.adresse, p.adresse_mail, p.telephone FROM employe e JOIN personne p ON e.num_carte_identite = p.num_carte_identite WHERE e.num_cnss = " + numCNSS + " AND e.mot_de_passe = '" + motDePasse + "'";
        ResultSet rs = st.executeQuery(sql);
        Employe emp = null;
        if (rs.next()) { emp = mapResultSetToEmploye(rs); }
        rs.close(); st.close();
        return emp;
    }

    public int ajouter(Employe emp) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "INSERT INTO employe (num_carte_identite, num_cnss, salaire, poste, mot_de_passe, heure_debut_travail, heure_sortie, nb_jours_par_semaine, date_rejoind_travail) VALUES (" +
                emp.getNumCarteIdentite() + ", " + emp.getNumCNSS() + ", " + emp.getSalaire() + ", '" + emp.getPoste() + "', '" + emp.getMotDePasse() + "', '" + emp.getHeureDebutTravail() + "', '" + emp.getHeureSortie() + "', " + emp.getNbJoursParSemaine() + ", '" + new java.sql.Date(emp.getDateRejoindTravail().getTime()) + "')";
        int result = st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        int generatedId = -1;
        if (result > 0) { ResultSet keys = st.getGeneratedKeys(); if (keys.next()) { generatedId = keys.getInt(1); } keys.close(); }
        st.close();
        return generatedId;
    }

    public List<Employe> listerTous() throws SQLException {
        List<Employe> employes = new ArrayList<>();
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT e.num_carte_emp, e.num_carte_identite, e.num_cnss, e.salaire, e.poste, e.mot_de_passe, e.heure_debut_travail, e.heure_sortie, e.nb_jours_par_semaine, e.date_rejoind_travail, p.nom, p.prenom, p.age, p.adresse, p.adresse_mail, p.telephone FROM employe e JOIN personne p ON e.num_carte_identite = p.num_carte_identite ORDER BY p.nom, p.prenom";
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) { employes.add(mapResultSetToEmploye(rs)); }
        rs.close(); st.close();
        return employes;
    }

    public boolean modifier(Employe emp) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "UPDATE employe SET num_cnss = " + emp.getNumCNSS() + ", salaire = " + emp.getSalaire() + ", poste = '" + emp.getPoste() + "', mot_de_passe = '" + emp.getMotDePasse() + "', heure_debut_travail = '" + emp.getHeureDebutTravail() + "', heure_sortie = '" + emp.getHeureSortie() + "', nb_jours_par_semaine = " + emp.getNbJoursParSemaine() + ", date_rejoind_travail = '" + new java.sql.Date(emp.getDateRejoindTravail().getTime()) + "' WHERE num_carte_emp = " + emp.getNumCarteEmp();
        int result = st.executeUpdate(sql);
        st.close();
        return result > 0;
    }

    public boolean supprimer(int numCarteEmp) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        int result = st.executeUpdate("DELETE FROM employe WHERE num_carte_emp = " + numCarteEmp);
        st.close();
        return result > 0;
    }

    public Employe rechercherParId(int numCarteEmp) throws SQLException {
        Connection con = ConnectionBD.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT e.num_carte_emp, e.num_carte_identite, e.num_cnss, e.salaire, e.poste, e.mot_de_passe, e.heure_debut_travail, e.heure_sortie, e.nb_jours_par_semaine, e.date_rejoind_travail, p.nom, p.prenom, p.age, p.adresse, p.adresse_mail, p.telephone FROM employe e JOIN personne p ON e.num_carte_identite = p.num_carte_identite WHERE e.num_carte_emp = " + numCarteEmp;
        ResultSet rs = st.executeQuery(sql);
        Employe emp = null;
        if (rs.next()) { emp = mapResultSetToEmploye(rs); }
        rs.close(); st.close();
        return emp;
    }

    private Employe mapResultSetToEmploye(ResultSet rs) throws SQLException {
        Employe emp = new Employe();
        emp.setNumCarteEmp(rs.getInt("num_carte_emp")); emp.setNumCarteIdentite(rs.getInt("num_carte_identite"));
        emp.setNumCNSS(rs.getInt("num_cnss")); emp.setSalaire(rs.getDouble("salaire"));
        emp.setPoste(rs.getString("poste")); emp.setMotDePasse(rs.getString("mot_de_passe"));
        emp.setHeureDebutTravail(rs.getString("heure_debut_travail")); emp.setHeureSortie(rs.getString("heure_sortie"));
        emp.setNbJoursParSemaine(rs.getInt("nb_jours_par_semaine")); emp.setDateRejoindTravail(rs.getDate("date_rejoind_travail"));
        emp.setNom(rs.getString("nom")); emp.setPrenom(rs.getString("prenom"));
        emp.setAge(rs.getInt("age")); emp.setAdresse(rs.getString("adresse"));
        emp.setAdresseMail(rs.getString("adresse_mail")); emp.setTelephone(rs.getString("telephone"));
        return emp;
    }
}
