package logic.model.dao.db;

import logic.exception.dao.DAOException;
import logic.model.dao.DAO;

import java.sql.*;

// DAO generico per entità persistite su database relazionale via JDBC
public abstract class DBDAO<I, E> implements DAO<I, E> {

    private static final String WHERE_CLAUSE = " WHERE ";

    // Connessione singleton – ri-usa la stessa Connection di tutto il layer DB.
    protected static final Connection conn = ConnectionFactory.getConnection();

    // Verifica se un'entità esiste nel database (tramite ID)
    @Override
    public boolean exists(I id) {
        String sql = "SELECT 1 FROM " + getTableName() + WHERE_CLAUSE + getPkColumn() + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("exists() failed", e);
        }
    }

    // Elimina un'entità dal database (tramite ID)
    @Override
    public void delete(I id) {
        String sql = "DELETE FROM " + getTableName() + WHERE_CLAUSE + getPkColumn() + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("delete() failed", e);
        }
    }

    // Carica un'entità dal database (tramite ID)
    @Override
    public E load(I id) {
        String sql = "SELECT * FROM " + getTableName() + WHERE_CLAUSE + getPkColumn() + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("load() failed", e);
        }
    }

    // Salva un'entità nel database: inserisce o aggiorna in base all'esistenza
    @Override
    public void store(E entity) {
        try {
            if (exists(getId(entity))) {
                update(entity);
            } else {
                insert(entity);
            }
        } catch (SQLException e) {
            throw new DAOException("store() failed", e);
        }
    }

    // Nome della tabella associata all'entità
    protected abstract String getTableName();

    // Colonna che rappresenta la chiave primaria
    protected abstract String getPkColumn();

    // Restituisce l'ID dell'entità
    protected abstract I getId(E entity);

    // Mappa un ResultSet JDBC in un'entità del dominio
    protected abstract E map(ResultSet rs) throws SQLException;

    // Inserisce l'entità nel database
    protected abstract void insert(E entity) throws SQLException;

    // Aggiorna l'entità nel database
    protected abstract void update(E entity) throws SQLException;
}
