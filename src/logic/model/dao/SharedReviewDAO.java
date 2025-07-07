package logic.model.dao;

import logic.model.domain.SharedReview;
import java.util.List;

// Interfaccia DAO per la gestione delle SharedReview
public interface SharedReviewDAO extends DAO<String, SharedReview> {

    @Override
    default SharedReview create(String key) {
        // Crea una nuova SharedReview con l'ID dato
        return new SharedReview(key);
    }

    // Salva o aggiorna una review
    void store(SharedReview sr);

    // Carica una review dato l'ID
    SharedReview load(String reviewId);

    // Carica tutte le review
    List<SharedReview> loadAll();

    // Carica tutte le review di uno studente
    List<SharedReview> loadForStudent(String studentId);

    // Carica tutte le review di un tutor
    List<SharedReview> loadForTutor(String tutorId);
}
