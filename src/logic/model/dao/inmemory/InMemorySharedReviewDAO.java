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

    public static synchronized InMemorySharedReviewDAO getInstance() {
        if (instance == null) {
            instance = new InMemorySharedReviewDAO();
        }
        return instance;
    }

    @Override
    protected String getKey(SharedReview review) {
        return review.getReviewId();
    }

    @Override
    public void store(SharedReview review) {
        // Se sr.getReviewId() è null, genero un ID univoco
        // es. "review" + contatore. Oppure lascio a carico del logic controller
        if (review.getReviewId() == null) {
            review.setReviewId("review" + allValues().size() + 1);
        }
        super.store(review); // usa la store di InMemoryDAO
    }

    @Override
    public SharedReview load(String reviewId) {
        return super.load(reviewId);
    }

    @Override
    public List<SharedReview> loadAll() {
        return new ArrayList<>(allValues());
    }

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
