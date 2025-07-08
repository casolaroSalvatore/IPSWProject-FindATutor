package logic.model.dao;

import logic.model.domain.state.TutoringSession;
import java.util.List;

// Interfaccia DAO per la gestione delle TutoringSession
public interface TutoringSessionDAO extends DAO<String, TutoringSession> {

    @Override
    default TutoringSession create(String key) {
        // Crea una nuova TutoringSession con l'ID dato
        return new TutoringSession(key);
    }

    // Carica tutte le sessioni di tutoraggio
    List<TutoringSession> loadAllTutoringSession();
}
