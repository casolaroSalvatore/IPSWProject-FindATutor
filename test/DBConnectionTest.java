package test;

import logic.model.dao.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnectionTest {

    public static void main(String[] args) {
        try {
            Connection conn = ConnectionFactory.getConnection();

            if (conn == null || conn.isClosed()) {
                System.out.println("❌ Connessione NULL o CHIUSA");
                return;
            }

            System.out.println("✅ Connessione riuscita!");

            String sql = "SELECT COUNT(*) AS count FROM users";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("📊 Utenti trovati: " + count);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Errore SQL:");
            e.printStackTrace();
        }
    }
}
