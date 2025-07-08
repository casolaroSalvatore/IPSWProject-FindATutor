package logic.model.dao.inmemory;

import logic.model.dao.SharedReviewDAO;
import logic.model.domain.SharedReview;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("java:S6548")
// Singleton usato intenzionalmente per InMemorySharedReviewDAO: garantisce un'unica istanza per
// centralizzare la gestione delle shared review in memoria, evitando duplicazioni e
// assicurando coerenza tramite DaoFactory.
public class InMemorySharedReviewDAO extends InMemoryDAO<String, SharedReview> implements SharedReviewDAO {

    private static InMemorySharedReviewDAO instance;

    // Restituisce l'istanza singleton
    public static synchronized InMemorySharedReviewDAO getInstance() {
        if (instance == null) {
            instance = new InMemorySharedReviewDAO();
        }
        return instance;
    }
    // Restituisce la chiave della review (ID)
    @Override
    protected String getKey(SharedReview review) {
        return review.getReviewId();
    }

    // Salva la review, generando un ID se assente
    @Override
    public void store(SharedReview review) {
        if (review.getReviewId() == null) {
            review.setReviewId("review" + (allValues().size() + 1));
        }
        super.store(review);
    }

    // Restituisce tutte le review
    @Override
    public List<SharedReview> loadAll() {
        return new ArrayList<>(allValues());
    }

    // Restituisce le review per uno specifico studente
    @Override
    public List<SharedReview> loadForStudent(String studentId) {
        List<SharedReview> result = new ArrayList<>();
        for (SharedReview sr : allValues()) {
            if (sr.getStudentId() != null && sr.getStudentId().equals(studentId)) {
                result.add(sr);
            }
        }
        return result;
    }

    // Restituisce le review per uno specifico tutor
    @Override
    public List<SharedReview> loadForTutor(String tutorId) {
        List<SharedReview> result = new ArrayList<>();
        for (SharedReview sr : allValues()) {
            if (sr.getTutorId() != null && sr.getTutorId().equals(tutorId)) {
                result.add(sr);
            }
        }
        return result;
    }
}
