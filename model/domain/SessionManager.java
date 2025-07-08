package logic.model.domain;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("java:S6548")
// Singleton usato intenzionalmente per SessionManager: garantisce un'unica istanza che centralizza
// la gestione e la coerenza delle sessioni utente durante l'intero ciclo di vita dell'applicazione.
public class SessionManager {

    private static SessionManager instance;
    // Mappa che associa sessionId alla relativa Session
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    private SessionManager() {
    }

    // Accesso all’unica istanza disponibile
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Crea una nuova sessione per l’utente specificato e la registra nella mappa
    public UUID createSession(User u) {
        Session s = new Session(u);
        sessions.put(s.getSessionId(), s);
        return s.getSessionId();
    }

    // Restituisce la sessione associata all’ID (null-safe)
    public Session getSession(UUID id) {
        return (id == null) ? null : sessions.get(id);
    }

    // Verifica se una sessione con l’ID specificato è attiva
    public boolean isSessionActive(UUID id) {
        return id != null && sessions.containsKey(id);
    }

    // Invalida (rimuove) una sessione dalla mappa
    public void invalidateSession(UUID id) {
        if (id != null) sessions.remove(id);
    }

}

