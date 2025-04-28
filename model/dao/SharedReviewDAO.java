package logic.model.dao;

import logic.model.domain.SharedReview;

import java.util.List;

public interface SharedReviewDAO extends DAO<String, SharedReview> {

    @Override
    default SharedReview create(String key) {
        return new SharedReview(key);
    }

    // Inserisce o aggiorna la review
    void store(SharedReview sr);

    // Carica una review in base a ID
    SharedReview load(String reviewId);

    // Carica tutte le review
    List<SharedReview> loadAll();

    // Carica tutte le review in cui lo studente è studentId
    List<SharedReview> loadForStudent(String studentId);

    // Carica tutte le review in cui il tutor è tutorId
    List<SharedReview> loadForTutor(String tutorId);
}
