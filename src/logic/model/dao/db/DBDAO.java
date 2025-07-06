package logic.model.dao.db;

import logic.exception.dao.DAOException;
import logic.model.dao.DAO;
import java.sql.*;

public abstract class DBDAO<I, E> implements DAO<I, E> {

    private static final String WHERE_CLAUSE = " WHERE ";

    // Connessione singleton – ri-usa la stessa Connection di tutto il layer DB.
    protected static final Connection conn = ConnectionFactory.getConnection();

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

    protected abstract String getTableName();
    protected abstract String getPkColumn();
    protected abstract I getId(E entity);

    protected abstract E map(ResultSet rs) throws SQLException;
    protected abstract void insert(E entity) throws SQLException;
    protected abstract void update(E entity) throws SQLException;
}
