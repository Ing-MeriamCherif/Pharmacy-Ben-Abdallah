package entite;

public class Vente {
    private int numVente;
    private String dateVente;
    private String dateLimRendreProduit;
    private double montantTotalVente;
    private int numClient;
    private int numCarteEmp;
    public int getNumClient() { return numClient; }
    public void setNumClient(int numClient) { this.numClient = numClient; }
    public int getNumVente() { return numVente; }
    public String getDateVente() { return dateVente; }
    public String getDateLimRendreProduit() { return dateLimRendreProduit; }
    public double getMontantTotalVente() { return montantTotalVente; }
    public void setNumVente(int numVente) { this.numVente = numVente; }
    public void setDateVente(String dateVente) { this.dateVente = dateVente; }
    public void setDateLimRendreProduit(String dateLimRendreProduit) { this.dateLimRendreProduit = dateLimRendreProduit; }
    public void setMontantTotalVente(double montantTotal) { this.montantTotalVente = montantTotal; }
    public int getNumCarteEmp() { return numCarteEmp; }
    public void setNumEmp(int numEmp) { this.numCarteEmp = numEmp; }
}
