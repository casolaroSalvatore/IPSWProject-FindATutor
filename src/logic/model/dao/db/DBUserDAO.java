package logic.model.dao.db;

import logic.model.dao.UserDAO;
import logic.model.dao.AccountDAO;
import logic.model.domain.*;

import java.sql.*;
import java.util.List;

@SuppressWarnings("java:S6548")
// Singleton usato intenzionalmente per DBUserDAO: garantisce un'unica istanza che centralizza
// l’accesso e la gestione dei dati utente nel database.
public class DBUserDAO extends DBDAO<String, User> implements UserDAO {

    private static DBUserDAO instance;

    // Singleton necessario per avere una sola istanza del DAO
    public static synchronized DBUserDAO getInstance() {
        if (instance == null) {
            instance = new DBUserDAO();
        }
        return instance;
    }

    private final AccountDAO accountDAO = new DBAccountDAO();

    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected String getPkColumn() {
        return "email";
    }

    @Override
    protected String getId(User u) {
        return u.getEmail();
    }

    // Mappa una riga del ResultSet in un oggetto User
    @Override
    protected User map(ResultSet rs) throws SQLException {
        String email = rs.getString("email");
        User u = new User(email, rs.getString("username"));
        List<Account> accounts =
                ((DBAccountDAO) accountDAO).loadAllAccountsOfUser(email);
        u.setAccounts(accounts);
        return u;
    }

    // Inserisce un nuovo utente nel DB
    @Override
    protected void insert(User u) throws SQLException {
        String sql = "INSERT INTO users(email, username) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getUsername());
            ps.executeUpdate();
        }
    }

    // Aggiorna lo username dell’utente esistente
    @Override
    protected void update(User u) throws SQLException {
        String sql = "UPDATE users SET username = ? WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.executeUpdate();
        }
    }

    // Factory method per creare un nuovo User dato un'email
    @Override
    public User create(String key) {
        return new User(key);
    }
}
