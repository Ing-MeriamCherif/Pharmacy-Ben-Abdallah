package exception;

public class ProduitNonTrouveException extends Exception {
    private int reference;

    public ProduitNonTrouveException(int reference) {
        super("Aucun produit trouvé avec la référence: " + reference);
        this.reference = reference;
    }

    public ProduitNonTrouveException(String message, int reference) {
        super(message);
        this.reference = reference;
    }

    public int getReference() {
        return reference;
    }
}
