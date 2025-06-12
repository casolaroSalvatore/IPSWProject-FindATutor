package test;

import logic.model.dao.db.ConnectionFactory;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import static org.junit.Assert.*;

/* Verifica l’integrità della connessione restituendo un singolo assert per ciascun metodo di test. */
class DBConnectionTest {

    /* La stessa connessione condivisa da tutti i test */
    private final Connection conn = ConnectionFactory.getConnection();

    @Test
    void connectionIsNotNull() {
        assertNotNull(String.valueOf(conn), Optional.of("The connection should not be null"));
    }

    @Test
    void connectionIsOpen() throws SQLException {
        assertFalse("The connection should be open", conn.isClosed());
    }

    @Test
    void connectionIsValid() throws SQLException {
        assertTrue("The connection should be valid", conn.isValid(2));
    }
}

