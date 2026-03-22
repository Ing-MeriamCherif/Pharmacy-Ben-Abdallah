package entitebd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionBD {
   private static final String URL = "jdbc:mysql://localhost:3306/pharmacie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
	    private static final String USER = "root";
	    private static final String PASSWORD = "gl2mc";
    private static Connection instance;

    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instance;
    }

    public static void closeConnection() throws SQLException {
        if (instance != null && !instance.isClosed()) {
            instance.close();
        }
    }
}
