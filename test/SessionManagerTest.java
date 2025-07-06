package test;

import logic.model.domain.SessionManager;
import logic.model.domain.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/* Verifica il ciclo di vita delle sessioni utente gestite da SessionManager */
class SessionManagerTest {

    private final SessionManager mgr = SessionManager.getInstance();

    @Test
    void testCreateSession() {
        UUID id = mgr.createSession(new User("alice@example.com"));
        assertNotNull(id);
    }

    @Test
    void testIsSessionActive() {
        UUID id = mgr.createSession(new User("bob@example.com"));
        assertEquals(true, mgr.isSessionActive(id));
    }

    @Test
    void testInvalidateSession() {
        UUID id = mgr.createSession(new User("charlie@example.com"));
        mgr.invalidateSession(id);
        assertEquals(false, mgr.isSessionActive(id));
    }
}


