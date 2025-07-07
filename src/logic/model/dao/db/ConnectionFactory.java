package logic.model.dao.db;

import java.io.*;
import java.sql.*;
import java.util.Properties;

// Factory che fornisce una connessione JDBC singleton al database
public class ConnectionFactory {

    private static Connection connection;

    // Blocco statico per inizializzare la connessione una sola volta
    private ConnectionFactory() {
    }

    static {
        try (InputStream input = new FileInputStream("resources/DB/db.properties")) {
            Properties properties = new Properties();
            properties.load(input);

            String connectionUrl = properties.getProperty("CONNECTION_URL");
            String user = properties.getProperty("LOGIN_USER");
            String pass = properties.getProperty("LOGIN_PASS");

            connection = DriverManager.getConnection(connectionUrl, user, pass);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    // Restituisce la connessione singleton
    public static Connection getConnection() {
        return connection;
    }
}
