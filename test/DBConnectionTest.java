package test;

import logic.model.dao.db.ConnectionFactory;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;

/* Verifica rapida sull’integrità della connessione JDBC ottenuta da ConnectionFactory */
class DBConnectionTest {

    private final Connection conn = ConnectionFactory.getConnection();

    @Test
    void testGetConnection() {
        assertNotNull(conn);
    }

    @Test
    void testIsClosed() throws SQLException {
        assertEquals(false, conn.isClosed());
    }

    @Test
    void testIsValid() throws SQLException {
        assertEquals(true, conn.isValid(2));
    }
}



