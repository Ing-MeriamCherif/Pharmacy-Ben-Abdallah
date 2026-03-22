package exception;

public class StockInsuffisantException extends Exception {
    private int refMedicament;
    private int quantiteDemandee;
    private int quantiteDisponible;
    public StockInsuffisantException(int refMedicament, int quantiteDemandee, int quantiteDisponible) {
        super("Stock insuffisant pour le médicament " + refMedicament + ". Demandé: " + quantiteDemandee + ", Disponible: " + quantiteDisponible);
        this.refMedicament = refMedicament; this.quantiteDemandee = quantiteDemandee; this.quantiteDisponible = quantiteDisponible;
    }
    public int getRefMedicament() { return refMedicament; }
    public int getQuantiteDemandee() { return quantiteDemandee; }
    public int getQuantiteDisponible() { return quantiteDisponible; }
}
