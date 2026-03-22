package entitebd;

import entite.CvEmployee;
import java.sql.*;

public class CvEmployeeBD {
    public boolean ajouter(CvEmployee cv) throws SQLException {
        Connection con = ConnectionBD.getConnection(); Statement st = con.createStatement();
        String sql = "INSERT INTO cv_employee (num_employee, diplome, nb_annee_experience, formation, stage) VALUES (" + cv.getNumEmployee() + ", '" + cv.getDiplome() + "', " + cv.getNbAnneeExperience() + ", '" + cv.getFormation() + "', '" + cv.getStage() + "')";
        int result = st.executeUpdate(sql); st.close(); return result > 0;
    }
    public CvEmployee rechercherParNumEmployee(int numEmployee) throws SQLException {
        Connection con = ConnectionBD.getConnection(); Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM cv_employee WHERE num_employee = " + numEmployee);
        CvEmployee cv = null; if (rs.next()) { cv = map(rs); } rs.close(); st.close(); return cv;
    }
    public boolean modifier(CvEmployee cv) throws SQLException {
        Connection con = ConnectionBD.getConnection(); Statement st = con.createStatement();
        String sql = "UPDATE cv_employee SET diplome = '" + cv.getDiplome() + "', nb_annee_experience = " + cv.getNbAnneeExperience() + ", formation = '" + cv.getFormation() + "', stage = '" + cv.getStage() + "' WHERE num_employee = " + cv.getNumEmployee();
        int result = st.executeUpdate(sql); st.close(); return result > 0;
    }
    public boolean supprimer(int numEmployee) throws SQLException {
        Connection con = ConnectionBD.getConnection(); Statement st = con.createStatement();
        int result = st.executeUpdate("DELETE FROM cv_employee WHERE num_employee = " + numEmployee); st.close(); return result > 0;
    }
    public boolean existe(int numEmployee) throws SQLException {
        Connection con = ConnectionBD.getConnection(); Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) as count FROM cv_employee WHERE num_employee = " + numEmployee);
        boolean existe = false; if (rs.next()) { existe = rs.getInt("count") > 0; } rs.close(); st.close(); return existe;
    }
    private CvEmployee map(ResultSet rs) throws SQLException {
        CvEmployee cv = new CvEmployee();
        cv.setNumEmployee(rs.getInt("num_employee")); cv.setDiplome(rs.getString("diplome"));
        cv.setNbAnneeExperience(rs.getInt("nb_annee_experience")); cv.setFormation(rs.getString("formation")); cv.setStage(rs.getString("stage"));
        return cv;
    }
}
