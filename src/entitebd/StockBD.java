package entitebd;

import entite.StockMedicament;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.util.Date;

/**
 * Refactored StockBD class with:
 * - PreparedStatements to prevent SQL injection
 * - Proper resource management with try-with-resources
 * - Better error handling
 * - Matches the PHARMACIE database schema
 */
public class StockBD {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Adds a new stock entry to the database
     * @param stock The stock medicament to add
     * @return The generated stock number (num_stock), or -1 if failed
     * @throws SQLException If database error occurs
     */
    public int ajouter(StockMedicament stock) throws SQLException {
        String sql = "INSERT INTO stock_medicament (ref_medicament, quantite_produit, prix_achat, " +
                "prix_vente, seuil_min, date_fabrication, date_expiration) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, stock.getRefMedicament());
            pst.setInt(2, stock.getQuantiteProduit());
            pst.setDouble(3, stock.getPrixAchat());
            pst.setDouble(4, stock.getPrixVente());
            pst.setInt(5, stock.getSeuilMin());

            // Handle nullable date fields
            if (stock.getDateFabrication() != null) {
                pst.setDate(6, new java.sql.Date(stock.getDateFabrication().getTime()));
            } else {
                pst.setNull(6, Types.DATE);
            }

            if (stock.getDateExpiration() != null) {
                pst.setDate(7, new java.sql.Date(stock.getDateExpiration().getTime()));
            } else {
                pst.setNull(7, Types.DATE);
            }

            int result = pst.executeUpdate();

            if (result > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        System.out.println("✅ Nouveau stock créé avec ID: " + generatedId);
                        return generatedId;
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Searches for stock by medicament reference
     * @param refMedicament The medicament reference
     * @return StockMedicament object if found, null otherwise
     * @throws SQLException If database error occurs
     */
    public StockMedicament rechercherParRef(int refMedicament) throws SQLException {
        String sql = "SELECT * FROM stock_medicament WHERE ref_medicament = ?";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, refMedicament);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStock(rs);
                }
            }
        }

        return null;
    }

    /**
     * Searches for stock by stock number
     * @param numStock The stock number
     * @return StockMedicament object if found, null otherwise
     * @throws SQLException If database error occurs
     */
    public StockMedicament rechercherParNumStock(int numStock) throws SQLException {
        String sql = "SELECT * FROM stock_medicament WHERE num_stock = ?";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, numStock);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStock(rs);
                }
            }
        }

        return null;
    }

    /**
     * Gets all stock batches for a medicament ordered by expiration date (FIFO)
     * @param refMedicament The medicament reference
     * @return List of stock batches ordered by expiration date
     * @throws SQLException If database error occurs
     */
    public List<StockMedicament> getStocksParExpiration(int refMedicament) throws SQLException {
        List<StockMedicament> stocks = new ArrayList<>();

        String sql = "SELECT * FROM stock_medicament " +
                "WHERE ref_medicament = ? " +
                "ORDER BY date_expiration ASC";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, refMedicament);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    stocks.add(mapResultSetToStock(rs));
                }
            }
        }

        return stocks;
    }

    /**
     * Removes quantity from stock using FIFO (First In First Out) based on expiration date
     * @param refMedicament The medicament reference
     * @param quantiteDemandee The quantity to remove
     * @throws SQLException If database error occurs or insufficient stock
     */
    public void retirerQuantite(int refMedicament, int quantiteDemandee) throws SQLException {
        List<StockMedicament> stocks = getStocksParExpiration(refMedicament);
        int reste = quantiteDemandee;

        for (StockMedicament stock : stocks) {
            if (reste <= 0) break;

            int qteStock = stock.getQuantiteProduit();

            if (qteStock <= reste) {
                // Empty the batch completely
                supprimerParNumStock(stock.getNumStock());
                reste -= qteStock;
            } else {
                // Take partial quantity
                stock.setQuantiteProduit(qteStock - reste);
                modifier(stock);
                reste = 0;
            }
        }

        if (reste > 0) {
            throw new SQLException("Stock insuffisant pour ce médicament ! Manque: " + reste + " unités");
        }
    }

    /**
     * Updates quantity for a medicament (updates the batch closest to expiration)
     * @param refMedicament The medicament reference
     * @param nouvelleQuantite The new quantity
     * @return true if update successful, false otherwise
     * @throws SQLException If database error occurs
     */
    public boolean mettreAJourQuantite(int refMedicament, int nouvelleQuantite) throws SQLException {
        if (nouvelleQuantite < 0) {
            throw new SQLException("Quantité invalide (négative)");
        }

        try (Connection con = ConnectionBD.getConnection()) {
            // Find the batch closest to expiration
            String findSql = "SELECT num_stock FROM stock_medicament " +
                    "WHERE ref_medicament = ? " +
                    "ORDER BY COALESCE(date_expiration, '9999-12-31') ASC " +
                    "LIMIT 1";

            Integer numStock = null;
            try (PreparedStatement pst = con.prepareStatement(findSql)) {
                pst.setInt(1, refMedicament);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        numStock = rs.getInt("num_stock");
                    }
                }
            }

            if (numStock != null) {
                if (nouvelleQuantite == 0) {
                    // Delete if quantity is 0
                    String deleteSql = "DELETE FROM stock_medicament WHERE num_stock = ?";
                    try (PreparedStatement pst = con.prepareStatement(deleteSql)) {
                        pst.setInt(1, numStock);
                        int result = pst.executeUpdate();
                        System.out.println("🗑 Stock supprimé (quantité = 0)");
                        return result > 0;
                    }
                } else {
                    // Update quantity
                    String updateSql = "UPDATE stock_medicament SET quantite_produit = ? WHERE num_stock = ?";
                    try (PreparedStatement pst = con.prepareStatement(updateSql)) {
                        pst.setInt(1, nouvelleQuantite);
                        pst.setInt(2, numStock);
                        int result = pst.executeUpdate();
                        System.out.println("✅ Quantité mise à jour");
                        return result > 0;
                    }
                }
            } else if (nouvelleQuantite > 0) {
                // Create new stock if none exists
                String insertSql = "INSERT INTO stock_medicament (ref_medicament, quantite_produit) VALUES (?, ?)";
                try (PreparedStatement pst = con.prepareStatement(insertSql)) {
                    pst.setInt(1, refMedicament);
                    pst.setInt(2, nouvelleQuantite);
                    int result = pst.executeUpdate();
                    System.out.println("➕ Nouveau stock créé");
                    return result > 0;
                }
            }
        }

        return false;
    }

    /**
     * Updates all fields of a stock entry
     * @param stock The stock object with updated values
     * @return true if update successful, false otherwise
     * @throws SQLException If database error occurs
     */
    public boolean modifier(StockMedicament stock) throws SQLException {
        String sql = "UPDATE stock_medicament SET " +
                "ref_medicament = ?, " +
                "quantite_produit = ?, " +
                "prix_achat = ?, " +
                "prix_vente = ?, " +
                "seuil_min = ?, " +
                "date_fabrication = ?, " +
                "date_expiration = ? " +
                "WHERE num_stock = ?";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, stock.getRefMedicament());
            pst.setInt(2, stock.getQuantiteProduit());
            pst.setDouble(3, stock.getPrixAchat());
            pst.setDouble(4, stock.getPrixVente());
            pst.setInt(5, stock.getSeuilMin());

            if (stock.getDateFabrication() != null) {
                pst.setDate(6, new java.sql.Date(stock.getDateFabrication().getTime()));
            } else {
                pst.setNull(6, Types.DATE);
            }

            if (stock.getDateExpiration() != null) {
                pst.setDate(7, new java.sql.Date(stock.getDateExpiration().getTime()));
            } else {
                pst.setNull(7, Types.DATE);
            }

            pst.setInt(8, stock.getNumStock());

            return pst.executeUpdate() > 0;
        }
    }



    /**
     * Gets all products that are below or at their minimum threshold
     * @return List of stocks in alert status
     * @throws SQLException If database error occurs
     */
    public List<StockMedicament> getProduitsEnAlerte() throws SQLException {
        List<StockMedicament> stocks = new ArrayList<>();

        String sql = "SELECT * FROM stock_medicament " +
                "WHERE quantite_produit <= seuil_min " +
                "ORDER BY quantite_produit ASC";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        }

        return stocks;
    }

    /**
     * Lists all stock entries
     * @return List of all stocks ordered by medicament reference
     * @throws SQLException If database error occurs
     */
    public List<StockMedicament> listerTous() throws SQLException {
        List<StockMedicament> stocks = new ArrayList<>();

        String sql = "SELECT * FROM stock_medicament ORDER BY ref_medicament";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        }

        return stocks;
    }

    /**
     * Deletes all stock entries for a medicament
     * @param refMedicament The medicament reference
     * @return true if deletion successful, false otherwise
     * @throws SQLException If database error occurs
     */
    public boolean supprimer(int refMedicament) throws SQLException {
        String sql = "DELETE FROM stock_medicament WHERE ref_medicament = ?";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, refMedicament);
            int result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Stock(s) supprimé(s) !");
            }

            return result > 0;
        }
    }

    /**
     * Deletes a specific stock entry by its stock number
     * @param numStock The stock number
     * @return true if deletion successful, false otherwise
     * @throws SQLException If database error occurs
     */
    public boolean supprimerParNumStock(int numStock) throws SQLException {
        String sql = "DELETE FROM stock_medicament WHERE num_stock = ?";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, numStock);
            int result = pst.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Stock supprimé !");
            }

            return result > 0;
        }
    }

    /**
     * Gets total quantity available for a medicament across all batches
     * @param refMedicament The medicament reference
     * @return Total quantity available
     * @throws SQLException If database error occurs
     */
    public int getQuantiteTotale(int refMedicament) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantite_produit), 0) as total " +
                "FROM stock_medicament WHERE ref_medicament = ?";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, refMedicament);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    /**
     * Gets stock batches that are expired or expiring soon
     * @param daysBeforeExpiration Number of days to consider as "expiring soon"
     * @return List of expired or expiring stocks
     * @throws SQLException If database error occurs
     */
    public List<StockMedicament> getStocksExpires(int daysBeforeExpiration) throws SQLException {
        List<StockMedicament> stocks = new ArrayList<>();

        String sql = "SELECT * FROM stock_medicament " +
                "WHERE date_expiration IS NOT NULL " +
                "AND date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY date_expiration ASC";

        try (Connection con = ConnectionBD.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, daysBeforeExpiration);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    stocks.add(mapResultSetToStock(rs));
                }
            }
        }

        return stocks;
    }

    /**
     * Maps a ResultSet row to a StockMedicament object
     * @param rs The ResultSet positioned at the current row
     * @return StockMedicament object populated with data
     * @throws SQLException If database error occurs
     */
    private StockMedicament mapResultSetToStock(ResultSet rs) throws SQLException {
        StockMedicament stock = new StockMedicament();
        stock.setNumStock(rs.getInt("num_stock"));
        stock.setRefMedicament(rs.getInt("ref_medicament"));
        stock.setQuantiteProduit(rs.getInt("quantite_produit"));
        stock.setPrixAchat(rs.getDouble("prix_achat"));
        stock.setPrixVente(rs.getDouble("prix_vente"));
        stock.setSeuilMin(rs.getInt("seuil_min"));
        stock.setDateFabrication(rs.getDate("date_fabrication"));
        stock.setDateExpiration(rs.getDate("date_expiration"));
        return stock;
    }
}