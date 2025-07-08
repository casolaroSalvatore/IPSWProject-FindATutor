package logic.model.dao.filesystem;

import logic.model.dao.SharedReviewDAO;
import logic.model.domain.ReviewStatus;
import logic.model.domain.SharedReview;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

// DAO filesystem per la gestione delle SharedReview
public class FileSystemSharedReviewDAO extends FileSystemDAO<String, SharedReview> implements SharedReviewDAO {

    private static FileSystemSharedReviewDAO instance;

    // Costruttore: inizializza nella cartella "reviews"
    FileSystemSharedReviewDAO(Path root) throws IOException {
        super(root, "reviews");
    }

    // Restituisce l'istanza singleton
    public static synchronized FileSystemSharedReviewDAO getInstance(Path root) throws IOException {
        if (instance == null) {
            instance = new FileSystemSharedReviewDAO(root);
        }
        return instance;
    }

    // Restituisce l'ID della review, generandolo se assente
    @Override
    protected String getId(SharedReview r) {
        if (r.getReviewId() == null || r.getReviewId().isBlank())
            r.setReviewId(UUID.randomUUID().toString());
        return r.getReviewId();
    }

    // Codifica una SharedReview in righe di file
    @Override
    protected List<String> encode(SharedReview r) {
        return List.of(
                "reviewId:" + r.getReviewId(),
                "studentId:" + r.getStudentId(),
                "tutorId:" + r.getTutorId(),
                "studentStars:" + r.getStudentStars(),
                "studentTitle:" + nullSafe(r.getStudentTitle()),
                "studentComment:" + nullSafe(r.getStudentComment()),
                "tutorTitle:" + nullSafe(r.getTutorTitle()),
                "tutorComment:" + nullSafe(r.getTutorComment()),
                "studentSubmitted:" + r.isStudentSubmitted(),
                "tutorSubmitted:" + r.isTutorSubmitted(),
                "status:" + r.getStatus()
        );
    }

    // Decodifica righe di file in SharedReview
    @Override
    protected SharedReview decode(List<String> l) {
        Map<String, String> m = toMap(l);
        SharedReview r = new SharedReview(m.get("reviewId"), m.get("studentId"), m.get("tutorId"));
        r.setStudentStars(Integer.parseInt(def(m.get("studentStars"), "0")));
        r.setStudentTitle(m.get("studentTitle"));
        r.setStudentComment(m.get("studentComment"));
        r.setTutorTitle(m.get("tutorTitle"));
        r.setTutorComment(m.get("tutorComment"));
        r.setStudentSubmitted(Boolean.parseBoolean(def(m.get("studentSubmitted"), "false")));
        r.setTutorSubmitted(Boolean.parseBoolean(def(m.get("tutorSubmitted"), "false")));
        r.setStatus(ReviewStatus.valueOf(m.get("status")));
        return r;
    }

    // Carica tutte le review
    @Override
    public List<SharedReview> loadAll() {
        return scan(e -> e);
    }

    // Carica le review per uno studente
    @Override
    public List<SharedReview> loadForStudent(String sid) {
        return filterByStudentId(sid);
    }

    // Carica le review per un tutor
    @Override
    public List<SharedReview> loadForTutor(String tid) {
        return filterByTutorId(tid);
    }

    // Filtra per studentId
    private List<SharedReview> filterByStudentId(String studentId) {
        List<SharedReview> result = new ArrayList<>();
        for (SharedReview review : scan(e -> e)) {
            if (studentId.equals(review.getStudentId())) {
                result.add(review);
            }
        }
        return result;
    }

    // Filtra per tutorId
    private List<SharedReview> filterByTutorId(String tutorId) {
        List<SharedReview> result = new ArrayList<>();
        for (SharedReview review : scan(e -> e)) {
            if (tutorId.equals(review.getTutorId())) {
                result.add(review);
            }
        }
        return result;
    }

    // Helper per aumentare la modularità del codice
    // Converte lista key:value in mappa
    private Map<String, String> toMap(List<String> ls) {
        Map<String, String> m = new HashMap<>();
        for (String s : ls) {
            int i = s.indexOf(':');
            if (i > 0) m.put(s.substring(0, i), s.substring(i + 1));
        }
        return m;
    }

    // Restituisce stringa vuota se null
    private String nullSafe(Object o) {
        return o == null ? "" : o.toString();
    }

    // Restituisce valore di default se vuoto
    private String def(String v, String d) {
        return v == null || v.isBlank() ? d : v;
    }
}
