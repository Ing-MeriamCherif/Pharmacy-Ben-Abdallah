package entite;
import java.util.Date;
public class Employe extends Personne {
    private int numCarteEmp;
    private String heureDebutTravail;
    private String heureSortie;
    private double salaire;
    private int numCNSS;
    private Date dateRejoindTravail;
    private int nbJoursParSemaine;
    private String poste;
    private String motDePasse;
    private CvEmployee cv;
    public int getNumCNSS() { return numCNSS; }
    public void setNumCNSS(int numCNSS) { this.numCNSS = numCNSS; }
    public int getNumCarteEmp() { return numCarteEmp; }
    public void setNumCarteEmp(int numCarteEmp) { this.numCarteEmp = numCarteEmp; }
    public String getHeureDebutTravail() { return heureDebutTravail; }
    public void setHeureDebutTravail(String heureDebutTravail) { this.heureDebutTravail = heureDebutTravail; }
    public String getHeureSortie() { return heureSortie; }
    public void setHeureSortie(String heureSortie) { this.heureSortie = heureSortie; }
    public double getSalaire() { return salaire; }
    public void setSalaire(double salaire) { this.salaire = salaire; }
    public Date getDateRejoindTravail() { return dateRejoindTravail; }
    public void setDateRejoindTravail(Date dateRejoindTravail) { this.dateRejoindTravail = dateRejoindTravail; }
    public int getNbJoursParSemaine() { return nbJoursParSemaine; }
    public void setNbJoursParSemaine(int nbJoursParSemaine) { this.nbJoursParSemaine = nbJoursParSemaine; }
    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public boolean admin() { return "administrateur".equalsIgnoreCase(poste); }
    public CvEmployee getCv() { return cv; }
    public void setCv(CvEmployee cv) { this.cv = cv; }
}
