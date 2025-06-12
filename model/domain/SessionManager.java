package logic.model.domain;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Gestore centralizzato delle Session
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    private SessionManager() { }

    public static SessionManager getInstance() { return INSTANCE; }

    public UUID createSession(User u) {
        Session s = new Session(u);
        sessions.put(s.getSessionId(), s);
        return s.getSessionId();
    }

    public Session getSession(UUID id)        { return (id==null)? null : sessions.get(id); }
    public boolean isSessionActive(UUID id)   { return id!=null && sessions.containsKey(id); }
    public void    invalidateSession(UUID id) { if (id!=null) sessions.remove(id); }

    Session findSessionByEmail(String email) {
        return sessions.values().stream()
                .filter(s -> s.getUser().getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }
}

