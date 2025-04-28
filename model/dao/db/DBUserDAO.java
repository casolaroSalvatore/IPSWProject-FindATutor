package logic.model.dao.db;

import logic.model.dao.UserDAO;
import logic.model.domain.User;
import logic.model.domain.Account;
import logic.model.dao.AccountDAO;

import java.sql.*;
import java.util.List;

public class DBUserDAO implements UserDAO {

    private static Connection conn;

    static {
        conn = ConnectionFactory.getConnection();
    }

    @Override
    public User load(String email) {
        User user = null;
        String sql = "SELECT email, username, password FROM users WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Costruisci l'oggetto User
                    String usr = rs.getString("username");
                    String pwd = rs.getString("password");
                    user = new User(email, usr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Se l'utente esiste, carico i suoi Account
        if (user != null) {
            // Recuperiamo l’AccountDAO per caricare la lista di account di quest’utente
            AccountDAO accountDAO = new DBAccountDAO();
            List<Account> accounts = ((DBAccountDAO) accountDAO).loadAllAccountsOfUser(email);
            user.setAccounts(accounts);
        }

        return user;
    }

    @Override
    public void store(User user) {
        // Se l'utente non esiste, INSERT, altrimenti UPDATE (o nessun update)
        // In genere si controlla se esiste:
        boolean exists = exists(user.getEmail());
        if (!exists) {
            // Insert
            String sql = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getEmail());
                pstmt.setString(2, user.getUsername());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Se vuoi fare un update di username/password
            String sql = "UPDATE users SET username = ?, password = ? WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(3, user.getEmail());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(String email) {
        String sql = "DELETE FROM users WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean exists(String email) {
        String sql = "SELECT email FROM users WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public User create(String key) {
        return new User(key);
    }
}