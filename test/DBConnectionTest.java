package test;

import logic.model.dao.db.ConnectionFactory;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DBConnectionTest {

    private final Connection conn = ConnectionFactory.getConnection();

    @Test
    void testGetConnection() {
        assertNotNull(conn, "Connection should not be null");
    }

    @Test
    void testIsClosed() throws SQLException {
        assertEquals(false, conn.isClosed(), "Connection should be open");
    }

    @Test
    void testIsValid() throws SQLException {
        assertEquals(true, conn.isValid(2), "Connection should be valid");
    }
}
