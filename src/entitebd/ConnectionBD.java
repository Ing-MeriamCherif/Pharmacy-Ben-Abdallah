package entitebd;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionBD {

    private static Connection instance;

    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream("config.properties"));
                String url  = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String pass = props.getProperty("db.password");
                instance = DriverManager.getConnection(url, user, pass);
            } catch (IOException e) {
                // Fallback to hardcoded values if config file not found
                System.err.println("⚠ config.properties not found, using default connection.");
                instance = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pharmacie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    "root", "mdp"
                );
            }
        }
        return instance;
    }

    public static void closeConnection() throws SQLException {
        if (instance != null && !instance.isClosed()) {
            instance.close();
        }
    }
}