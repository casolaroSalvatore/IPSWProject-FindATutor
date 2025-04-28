package logic.model.dao.db;

import logic.model.dao.SharedReviewDAO;
import logic.model.domain.ReviewStatus;
import logic.model.domain.SharedReview;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBSharedReviewDAO implements SharedReviewDAO {

    private static Connection conn;
    private static final String REVIEW_ID  = "review_id";

    static {
        conn = ConnectionFactory.getConnection();
    }

    @Override
    public SharedReview load(String reviewId) {
        String sql = "SELECT review_id, student_id, tutor_id, student_stars, student_title, student_comment, student_submitted, " +
                "tutor_title, tutor_comment, tutor_submitted, status " +
                "FROM shared_reviews WHERE review_id = ?";

        SharedReview sr = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sr = new SharedReview();
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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sr;
    }

    // Per memorizzare o aggiornare la SharedReview
    @Override
    public void store(SharedReview sr) {
        if (sr.getReviewId() == null || sr.getReviewId().isEmpty()) {
            // Genera un ID per la review, se necessario
            sr.setReviewId(generateReviewId());
        }

        if (exists(sr.getReviewId())) {
            update(sr);
        } else {
            insert(sr);
        }
    }

    public boolean exists(String reviewId) {
        String sql = "SELECT review_id FROM shared_reviews WHERE review_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void insert(SharedReview sr) {
        String sql = "INSERT INTO shared_reviews(review_id, student_id, tutor_id, student_stars, student_title, student_comment, " +
                "student_submitted, tutor_title, tutor_comment, tutor_submitted, status) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sr.getReviewId());
            pstmt.setString(2, sr.getStudentId());
            pstmt.setString(3, sr.getTutorId());

            pstmt.setInt(4, sr.getStudentStars());
            pstmt.setString(5, sr.getStudentTitle());
            pstmt.setString(6, sr.getStudentComment());
            pstmt.setBoolean(7, sr.isStudentSubmitted());

            pstmt.setString(8, sr.getTutorTitle());
            pstmt.setString(9, sr.getTutorComment());
            pstmt.setBoolean(10, sr.isTutorSubmitted());

            pstmt.setString(11, sr.getStatus().name());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void update(SharedReview sr) {
        String sql = "UPDATE shared_reviews SET " +
                "student_id = ?, tutor_id = ?, student_stars = ?, student_title = ?, student_comment = ?, " +
                "student_submitted = ?, tutor_title = ?, tutor_comment = ?, tutor_submitted = ?, status = ? " +
                "WHERE review_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sr.getStudentId());
            pstmt.setString(2, sr.getTutorId());
            pstmt.setInt(3, sr.getStudentStars());
            pstmt.setString(4, sr.getStudentTitle());
            pstmt.setString(5, sr.getStudentComment());
            pstmt.setBoolean(6, sr.isStudentSubmitted());
            pstmt.setString(7, sr.getTutorTitle());
            pstmt.setString(8, sr.getTutorComment());
            pstmt.setBoolean(9, sr.isTutorSubmitted());
            pstmt.setString(10, sr.getStatus().name());

            pstmt.setString(11, sr.getReviewId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String reviewId) {
        String sql = "DELETE FROM shared_reviews WHERE review_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reviewId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SharedReview> loadAll() {
        String sql = "SELECT review_id FROM shared_reviews";
        List<SharedReview> result = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
             ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String reviewId = rs.getString(REVIEW_ID);
                SharedReview sr = load(reviewId);
                if (sr != null) {
                    result.add(sr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<SharedReview> loadForStudent(String studentId) {
        List<SharedReview> result = new ArrayList<>();
        String sql = "SELECT review_id FROM shared_reviews WHERE student_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String revId = rs.getString(REVIEW_ID);
                    SharedReview sr = load(revId);
                    if (sr != null) {
                        result.add(sr);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<SharedReview> loadForTutor(String tutorId) {
        List<SharedReview> result = new ArrayList<>();
        String sql = "SELECT review_id FROM shared_reviews WHERE tutor_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tutorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String revId = rs.getString(REVIEW_ID);
                    SharedReview sr = load(revId);
                    if (sr != null) {
                        result.add(sr);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // Metodo di utilità per generare un ID univoco
    private String generateReviewId() {
        // Ad esempio potresti usare un UUID.randomUUID().toString()
        return "SR-" + System.currentTimeMillis();
    }
}
