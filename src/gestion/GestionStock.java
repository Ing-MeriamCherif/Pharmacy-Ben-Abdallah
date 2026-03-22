package gestion;

import entite.StockMedicament;
import entitebd.StockBD;
import exception.StockInsuffisantException;
import exception.ProduitNonTrouveException;
import java.sql.SQLException;
import java.util.List;

public class GestionStock {
    private StockBD stockBD = new StockBD();

    /** Diminue le stock via FEFO */
    public void diminuerStock(int refMedicament, int quantite) throws SQLException, StockInsuffisantException, ProduitNonTrouveException {
        List<StockMedicament> stocks = stockBD.getStocksParExpiration(refMedicament);
        if (stocks == null || stocks.isEmpty()) throw new ProduitNonTrouveException(refMedicament);
        int total = stocks.stream().mapToInt(StockMedicament::getQuantiteProduit).sum();
        if (total < quantite) throw new StockInsuffisantException(refMedicament, quantite, total);
        stockBD.retirerQuantite(refMedicament, quantite);
    }

    /** Augmente le stock du premier lot trouvé (lot le plus proche expiration) */
    public void augmenterStock(int refMedicament, int quantite) throws SQLException, ProduitNonTrouveException {
        List<StockMedicament> stocks = stockBD.getStocksParExpiration(refMedicament);
        if (stocks == null || stocks.isEmpty()) throw new ProduitNonTrouveException(refMedicament);
        StockMedicament s = stocks.get(0);
        s.setQuantiteProduit(s.getQuantiteProduit() + quantite);
        stockBD.modifier(s);
    }

    public List<StockMedicament> obtenirAlertes() throws SQLException { return stockBD.getProduitsEnAlerte(); }

    public boolean estEnAlerte(int refMedicament) throws SQLException, ProduitNonTrouveException {
        List<StockMedicament> stocks = stockBD.getStocksParExpiration(refMedicament);
        if (stocks == null || stocks.isEmpty()) throw new ProduitNonTrouveException(refMedicament);
        return stocks.stream().anyMatch(StockMedicament::Alerte);
    }

    public double calculerValeurTotaleStock() throws SQLException {
        return stockBD.listerTous().stream().mapToDouble(s -> s.getQuantiteProduit() * s.getPrixAchat()).sum();
    }

    public int obtenirStockTotal(int refMedicament) throws SQLException {
        return stockBD.getStocksParExpiration(refMedicament).stream().mapToInt(StockMedicament::getQuantiteProduit).sum();
    }

    public String genererRapportStock() throws SQLException {
        List<StockMedicament> stocks  = stockBD.listerTous();
        List<StockMedicament> alertes = stockBD.getProduitsEnAlerte();
        StringBuilder r = new StringBuilder();
        r.append("========== RAPPORT D'ÉTAT DU STOCK ==========\n");
        r.append("Nombre total de lots : ").append(stocks.size()).append("\n");
        r.append("Lots en alerte       : ").append(alertes.size()).append("\n");
        r.append(String.format("Valeur totale stock  : %.2f DT\n", calculerValeurTotaleStock()));
        r.append("\n--- LOTS EN ALERTE ---\n");
        if (alertes.isEmpty()) r.append("Aucun lot en alerte\n");
        else for (StockMedicament s : alertes)
            r.append(String.format("- Lot #%d (Réf %d): %d unités (Seuil: %d)\n", s.getNumStock(), s.getRefMedicament(), s.getQuantiteProduit(), s.getSeuilMin()));
        return r.toString();
    }
}