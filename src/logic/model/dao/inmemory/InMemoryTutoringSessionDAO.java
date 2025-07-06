package logic.model.dao.inmemory;

import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.state.TutoringSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("java:S6548")
// Singleton usato intenzionalmente per InMemoryTutoringSessionDAO: garantisce un'unica
// istanza che centralizza la gestione delle sessioni di tutoraggio in memoria,
// assicurando coerenza dei dati e accesso unico tramite DaoFactory.
public class InMemoryTutoringSessionDAO extends InMemoryDAO<String, TutoringSession> implements TutoringSessionDAO {

    private static InMemoryTutoringSessionDAO instance;

    // Contatore per generare ID univoci (es. "session1", "session2", ecc.)
    private static final AtomicInteger idGenerator = new AtomicInteger(1);

    public static synchronized InMemoryTutoringSessionDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryTutoringSessionDAO();
        }
        return instance;
    }

    @Override
    protected String getKey(TutoringSession session) {
        // Ritorniamo la stringa sessionId dell’oggetto
        return session.getSessionId();
    }

    @Override
    public void store(TutoringSession session) {
        // Se la sessionId è ancora null, generiamo un ID
        if (session.getSessionId() == null) {
            session.setSessionId("session" + idGenerator.getAndIncrement());
        }
        super.store(session);
    }

    @Override
    public void delete(String sessionId) {
        super.delete(sessionId);
    }

    @Override
    public List<TutoringSession> loadAllTutoringSession() {
        return new ArrayList<>(allValues());
    }
}