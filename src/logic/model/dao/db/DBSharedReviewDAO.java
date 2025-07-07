package logic.model.dao.db;

import logic.model.dao.SharedReviewDAO;
import logic.model.domain.*;

import java.sql.*;
import java.util.*;

@SuppressWarnings("java:S6548")
// Singleton necessario per garantire un'unica istanza di DBSharedReviewDAO che centralizza
// l'accesso alle recensioni condivise e assicura coerenza nelle operazioni sul database
public class DBSharedReviewDAO extends DBDAO<String, SharedReview> implements SharedReviewDAO {

    private static final String REVIEW_ID = "review_id";

    private static DBSharedReviewDAO instance;

    // Singleton necessario per avere una sola istanza del DAO
    public static synchronized DBSharedReviewDAO getInstance() {
        if (instance == null) {
            instance = new DBSharedReviewDAO();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "shared_reviews";
    }

    @Override
    protected String getPkColumn() {
        return REVIEW_ID;
    }

    @Override
    protected String getId(SharedReview sr) {
        return sr.getReviewId();
    }

    // Mappa un ResultSet JDBC a un oggetto SharedReview
    @Override
    protected SharedReview map(ResultSet rs) throws SQLException {
        SharedReview sr = new SharedReview();
        sr.setReviewId(rs.getString(REVIEW_ID));
        sr.setStudentId(rs.getString("student_id"));
        sr.setTutorId(rs.getString("tutor_id"));
        sr.setStudentStars(rs.getInt("student_stars"));
        sr.setStudentTitle(rs.getString("student_title"));
        sr.setStudentComment(rs.getString("student_comment"));
        sr.setStudentSubmitted(rs.getBoolean("student_submitted"));
        sr.setTutorTitle(rs.getString("tutor_title"));
        sr.setTutorComment(rs.getString("tutor_comment"));
        sr.setTutorSubmitted(rs.getBoolean("tutor_submitted"));
        sr.setStatus(ReviewStatus.valueOf(rs.getString("status")));
        return sr;
    }

    // Inserisce una nuova recensione condivisa nel DB
    @Override
    protected void insert(SharedReview sr) throws SQLException {
        String sql = """
                INSERT INTO shared_reviews(
                  review_id, student_id, tutor_id,
                  student_stars, student_title, student_comment, student_submitted,
                  tutor_title, tutor_comment, tutor_submitted, status)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)""";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, sr, /* update */ false);
            ps.executeUpdate();
        }
    }

    // Aggiorna una recensione esistente
    @Override
    protected void update(SharedReview sr) throws SQLException {
        String sql = """
                UPDATE shared_reviews SET
                  student_id = ?, tutor_id = ?, student_stars = ?, student_title = ?, student_comment = ?,
                  student_submitted = ?, tutor_title = ?, tutor_comment = ?, tutor_submitted = ?, status = ?
                WHERE review_id = ?""";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, sr, /* update */ true);
            ps.executeUpdate();
        }
    }

    // Associa i parametri alla PreparedStatement in modo dinamico
    private void bind(PreparedStatement ps, SharedReview sr, boolean update)
            throws SQLException {

        List<Object> p = new ArrayList<>(Arrays.asList(
                sr.getStudentId(), sr.getTutorId(), sr.getStudentStars(),
                sr.getStudentTitle(), sr.getStudentComment(), sr.isStudentSubmitted(),
                sr.getTutorTitle(), sr.getTutorComment(), sr.isTutorSubmitted(),
                sr.getStatus().name()));

        if (update) p.add(sr.getReviewId());
        else p.add(0, sr.getReviewId());

        for (int i = 0; i < p.size(); i++) ps.setObject(i + 1, p.get(i));
    }

    // Carica tutte le review dal database
    @Override
    public List<SharedReview> loadAll() {
        return loadMany("SELECT review_id FROM shared_reviews");
    }

    // Carica le review scritte da uno studente
    @Override
    public List<SharedReview> loadForStudent(String studentId) {
        return loadMany("""
                SELECT review_id FROM shared_reviews
                 WHERE student_id = ?""", studentId);
    }

    // Carica le review ricevute da un tutor
    @Override
    public List<SharedReview> loadForTutor(String tutorId) {
        return loadMany("""
                SELECT review_id FROM shared_reviews
                 WHERE tutor_id = ?""", tutorId);
    }

    // Metodo di supporto per eseguire query multiple parametrizzate
    private List<SharedReview> loadMany(String sql, String... param) {
        List<SharedReview> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param.length > 0) ps.setString(1, param[0]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(load(rs.getString(REVIEW_ID)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

