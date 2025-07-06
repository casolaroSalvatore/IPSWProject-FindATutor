package logic.model.domain;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("java:S6548")
// Singleton usato intenzionalmente per SessionManager: garantisce un'unica istanza che centralizza
// la gestione e la coerenza delle sessioni utente durante l'intero ciclo di vita dell'applicazione.
public class SessionManager {

    private static final SessionManager instance = new SessionManager();
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    private SessionManager() { }

    public static SessionManager getInstance() { return instance; }

    public UUID createSession(User u) {
        Session s = new Session(u);
        sessions.put(s.getSessionId(), s);
        return s.getSessionId();
    }

    public Session getSession(UUID id)        { return (id==null)? null : sessions.get(id); }
    public boolean isSessionActive(UUID id)   { return id!=null && sessions.containsKey(id); }
    public void    invalidateSession(UUID id) { if (id!=null) sessions.remove(id); }

}

