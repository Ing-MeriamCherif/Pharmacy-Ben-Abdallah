package gestion;

import entite.Medicament;
import entite.StockMedicament;
import entitebd.MedicamentBD;
import entitebd.StockBD;
import exception.ProduitNonTrouveException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class GestionProduit {
    private MedicamentBD medicamentBD = new MedicamentBD();
    private StockBD stockBD = new StockBD();

    /**
     * ✅ REFACTORED: Ajouter un médicament SANS créer de stock
     * Le stock sera créé séparément lors de la réception des commandes
     * @return La référence du médicament créé
     */
    public int ajouterMedicament(Medicament medicament)
            throws SQLException, IllegalArgumentException {

        // Validations métier (sans dates)
        validerMedicament(medicament);

        // Ajouter le médicament
        int refMedicament = medicamentBD.ajouter(medicament);

        if (refMedicament > 0) {
            System.out.println("✓ Médicament créé avec succès (ref: " + refMedicament + ")");
            System.out.println("  Note: Le stock sera créé lors de la réception des commandes");
            return refMedicament;
        }

        throw new SQLException("Échec de la création du médicament");
    }


    public boolean modifierMedicament(Medicament medicament)
            throws SQLException, ProduitNonTrouveException, IllegalArgumentException {

        // Vérifier que le médicament existe
        Medicament existing = medicamentBD.rechercherParRef(medicament.getRefMedicament());
        if (existing == null) {
            throw new ProduitNonTrouveException(medicament.getRefMedicament());
        }

        // Validations
        validerMedicament(medicament);

        // Mettre à jour le médicament
        boolean updated = medicamentBD.modifier(medicament);

        if (updated) {
            System.out.println("✓ Médicament modifié (ref: " + medicament.getRefMedicament() + ")");
        }

        return updated;
    }

    /**
     * ✅ REFACTORED: Supprimer un médicament et tous ses stocks associés
     */
    public boolean supprimerMedicament(int refMedicament)
            throws SQLException, ProduitNonTrouveException {

        // Vérifier que le médicament existe
        Medicament med = medicamentBD.rechercherParRef(refMedicament);
        if (med == null) {
            throw new ProduitNonTrouveException(refMedicament);
        }

        // ✅ Vérifier s'il y a du stock (peut avoir plusieurs lots)
        List<StockMedicament> stocks = stockBD.getStocksParExpiration(refMedicament);
        int stockTotal = 0;

        if (stocks != null && !stocks.isEmpty()) {
            for (StockMedicament stock : stocks) {
                stockTotal += stock.getQuantiteProduit();
            }

            if (stockTotal > 0) {
                throw new IllegalStateException(
                        "Impossible de supprimer un médicament avec du stock restant! " +
                                "Quantité totale: " + stockTotal + " unités dans " + stocks.size() + " lot(s)"
                );
            }

            // Supprimer tous les stocks (même vides)
            for (StockMedicament stock : stocks) {
                stockBD.supprimerParNumStock(stock.getNumStock());
            }
        }

        // Puis supprimer le médicament
        boolean deleted = medicamentBD.supprimer(refMedicament);

        if (deleted) {
            System.out.println("✓ Médicament supprimé (ref: " + refMedicament + ")");
        }

        return deleted;
    }

    /**
     * ✅ REFACTORED: Rechercher un médicament par référence avec tous ses stocks
     */
    public MedicamentAvecStocks rechercherMedicamentComplet(int refMedicament)
            throws SQLException, ProduitNonTrouveException {

        Medicament med = medicamentBD.rechercherParRef(refMedicament);
        if (med == null) {
            throw new ProduitNonTrouveException(refMedicament);
        }

        List<StockMedicament> stocks = stockBD.getStocksParExpiration(refMedicament);
        return new MedicamentAvecStocks(med, stocks);
    }

    /**
     * Rechercher des médicaments par nom
     */
    public List<Medicament> rechercherParNom(String nom) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de recherche ne peut pas être vide");
        }
        return medicamentBD.rechercherParNom(nom);
    }

    /**
     * Lister tous les médicaments
     */
    public List<Medicament> listerTousMedicaments() throws SQLException {
        return medicamentBD.listerTous();
    }

    /**
     * Lister les médicaments d'un fournisseur
     */

    /**
     * ✅ REFACTORED: Obtenir les stocks périmés (dates maintenant dans StockMedicament)
     */
    public List<StockMedicament> obtenirStocksPerimes() throws SQLException {
        List<StockMedicament> tous = stockBD.listerTous();
        return tous.stream()
                .filter(StockMedicament::estPerime)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * ✅ REFACTORED: Obtenir les stocks proches de l'expiration (moins de X jours)
     */
    public List<StockMedicament> obtenirStocksProchesExpiration(int joursAvant) throws SQLException {
        List<StockMedicament> tous = stockBD.listerTous();
        Date now = new Date();
        long millisParJour = 24 * 60 * 60 * 1000;
        long seuilMillis = now.getTime() + (joursAvant * millisParJour);

        return tous.stream()
                .filter(stock -> {
                    if (stock.getDateExpiration() == null) return false;
                    return stock.getDateExpiration().getTime() <= seuilMillis
                            && !stock.estPerime();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Calculer le nombre total de médicaments (distincts)
     */
    public int compterMedicaments() throws SQLException {
        return medicamentBD.listerTous().size();
    }

    /**
     * ✅ NOUVEAU: Calculer le nombre total de lots en stock
     */
    public int compterLotsStock() throws SQLException {
        return stockBD.listerTous().size();
    }

    /**
     * Calculer la valeur totale des médicaments en stock
     */
    public double calculerValeurTotaleMedicaments() throws SQLException {
        List<StockMedicament> stocks = stockBD.listerTous();
        double total = 0.0;

        for (StockMedicament stock : stocks) {
            total += stock.getQuantiteProduit() * stock.getPrixVente();
        }

        return total;
    }

    // ============ MÉTHODES DE VALIDATION PRIVÉES ============

    private void validerMedicament(Medicament med) throws IllegalArgumentException {
        if (med == null) {
            throw new IllegalArgumentException("Le médicament ne peut pas être null");
        }

        if (med.getNom() == null || med.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du médicament est obligatoire");
        }


        // ✅ REFACTORED: Plus de validation de dates ici - elles sont maintenant dans le stock
        // Les dates seront validées lors de la création du stock
    }

    private void validerStock(StockMedicament stock) throws IllegalArgumentException {
        if (stock == null) {
            throw new IllegalArgumentException("Le stock ne peut pas être null");
        }

        if (stock.getQuantiteProduit() < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }

        if (stock.getPrixAchat() <= 0) {
            throw new IllegalArgumentException("Le prix d'achat doit être positif");
        }

        if (stock.getPrixVente() <= 0) {
            throw new IllegalArgumentException("Le prix de vente doit être positif");
        }

        if (stock.getSeuilMin() < 0) {
            throw new IllegalArgumentException("Le seuil minimal ne peut pas être négatif");
        }

        if (stock.getPrixVente() < stock.getPrixAchat()) {
            System.out.println("⚠ Attention: Le prix de vente est inférieur au prix d'achat!");
        }

        // ✅ REFACTORED: Validation des dates (maintenant dans le stock)
        if (stock.getDateFabrication() == null) {
            throw new IllegalArgumentException("La date de fabrication est obligatoire");
        }

        if (stock.getDateExpiration() == null) {
            throw new IllegalArgumentException("La date d'expiration est obligatoire");
        }

        if (stock.getDateExpiration().before(stock.getDateFabrication())) {
            throw new IllegalArgumentException(
                    "La date d'expiration doit être après la date de fabrication"
            );
        }
    }

    // ============ CLASSE INTERNE POUR RETOURNER MÉDICAMENT + STOCK(S) ============

    /**
     * ✅ ANCIENNE CLASSE (conservée pour compatibilité avec code existant)
     * @deprecated Utiliser MedicamentAvecStocks à la place
     */
    @Deprecated
    public static class MedicamentAvecStock {
        private Medicament medicament;
        private StockMedicament stock;

        public MedicamentAvecStock(Medicament medicament, StockMedicament stock) {
            this.medicament = medicament;
            this.stock = stock;
        }

        public Medicament getMedicament() {
            return medicament;
        }

        public StockMedicament getStock() {
            return stock;
        }

        public boolean aStock() {
            return stock != null;
        }

        public int getQuantiteStock() {
            return stock != null ? stock.getQuantiteProduit() : 0;
        }

        public boolean estEnAlerte() {
            return stock != null && stock.Alerte();
        }

        public boolean estPerime() {
            // ✅ REFACTORED: Vérifie maintenant le stock au lieu du médicament
            return stock != null && stock.estPerime();
        }
    }

    /**
     * ✅ NOUVELLE CLASSE: Un médicament peut avoir plusieurs stocks (lots)
     * Cette classe représente mieux la réalité d'un système de gestion de pharmacie
     */
    public static class MedicamentAvecStocks {
        private Medicament medicament;
        private List<StockMedicament> stocks;

        public MedicamentAvecStocks(Medicament medicament, List<StockMedicament> stocks) {
            this.medicament = medicament;
            this.stocks = stocks;
        }

        public Medicament getMedicament() {
            return medicament;
        }

        public List<StockMedicament> getStocks() {
            return stocks;
        }

        public boolean aStock() {
            return stocks != null && !stocks.isEmpty() && getQuantiteStockTotal() > 0;
        }

        public int getQuantiteStockTotal() {
            if (stocks == null || stocks.isEmpty()) return 0;
            return stocks.stream()
                    .mapToInt(StockMedicament::getQuantiteProduit)
                    .sum();
        }

        public int getNombreLots() {
            return stocks != null ? stocks.size() : 0;
        }

        public boolean estEnAlerte() {
            if (stocks == null || stocks.isEmpty()) return false;
            return stocks.stream().anyMatch(StockMedicament::Alerte);
        }

        public boolean aStockPerime() {
            if (stocks == null || stocks.isEmpty()) return false;
            return stocks.stream().anyMatch(StockMedicament::estPerime);
        }

        public StockMedicament getStockLePlusAncien() {
            if (stocks == null || stocks.isEmpty()) return null;
            // Les stocks sont déjà triés par date d'expiration (FEFO)
            return stocks.get(0);
        }

        /**
         * Obtenir le premier stock non périmé (FEFO - First Expired, First Out)
         */
        public StockMedicament getPremierStockValide() {
            if (stocks == null || stocks.isEmpty()) return null;
            return stocks.stream()
                    .filter(s -> !s.estPerime())
                    .findFirst()
                    .orElse(null);
        }

        /**
         * Vérifier si tous les stocks sont périmés
         */
        public boolean tousStocksPerimes() {
            if (stocks == null || stocks.isEmpty()) return false;
            return stocks.stream().allMatch(StockMedicament::estPerime);
        }

        /**
         * Obtenir la quantité totale non périmée
         */
        public int getQuantiteNonPerimee() {
            if (stocks == null || stocks.isEmpty()) return 0;
            return stocks.stream()
                    .filter(s -> !s.estPerime())
                    .mapToInt(StockMedicament::getQuantiteProduit)
                    .sum();
        }
    }
}