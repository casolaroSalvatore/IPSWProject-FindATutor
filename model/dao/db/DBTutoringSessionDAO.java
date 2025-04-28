package logic.model.dao.db;

import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.TutoringSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBTutoringSessionDAO implements TutoringSessionDAO {

    private static Connection conn;

    static {
        conn = ConnectionFactory.getConnection();
    }

    @Override
    public TutoringSession load(String sessionId) {
        String sql = "SELECT session_id, tutor_id, student_id, location, subject, date, start_time, end_time, " +
                "comment, status, modified_by, modified_to, proposed_date, proposed_start_time, proposed_end_time, " +
                "tutor_seen, student_seen " +
                "FROM tutoring_sessions WHERE session_id = ?";

        TutoringSession session = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    session = new TutoringSession();
                    session.setSessionId(rs.getString("session_id"));
                    session.setTutorId(rs.getString("tutor_id"));
                    session.setStudentId(rs.getString("student_id"));
                    session.setLocation(rs.getString("location"));
                    session.setSubject(rs.getString("subject"));

                    Date dbDate = rs.getDate("date");
                    if (dbDate != null) {
                        session.setDate(dbDate.toLocalDate());
                    }
                    Time dbStart = rs.getTime("start_time");
                    if (dbStart != null) {
                        session.setStartTime(dbStart.toLocalTime());
                    }
                    Time dbEnd = rs.getTime("end_time");
                    if (dbEnd != null) {
                        session.setEndTime(dbEnd.toLocalTime());
                    }

                    session.setComment(rs.getString("comment"));
                    session.setStatus(rs.getString("status"));
                    session.setModifiedBy(rs.getString("modified_by"));
                    session.setModifiedTo(rs.getString("modified_to"));

                    Date dbProposedDate = rs.getDate("proposed_date");
                    if (dbProposedDate != null) {
                        session.setProposedDate(dbProposedDate.toLocalDate());
                    }
                    Time dbProposedStart = rs.getTime("proposed_start_time");
                    if (dbProposedStart != null) {
                        session.setProposedStartTime(dbProposedStart.toLocalTime());
                    }
                    Time dbProposedEnd = rs.getTime("proposed_end_time");
                    if (dbProposedEnd != null) {
                        session.setProposedEndTime(dbProposedEnd.toLocalTime());
                    }

                    session.setTutorSeen(rs.getBoolean("tutor_seen"));
                    session.setStudentSeen(rs.getBoolean("student_seen"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return session;
    }

    // Verifica se esiste già la tutoring session con un certo sessionId
    public boolean exists(String sessionId) {
        String sql = "SELECT session_id FROM tutoring_sessions WHERE session_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void store(TutoringSession entity) {
        // se esiste -> update, altrimenti -> insert
        if (entity.getSessionId() == null || entity.getSessionId().isEmpty()) {
            // In molti casi, genereresti qui un sessionId (ad es. UUID random)
            entity.setSessionId(generateSessionId());
        }

        if (exists(entity.getSessionId())) {
            update(entity);
        } else {
            insert(entity);
        }
    }

    // Inserimento nuovo
    private void insert(TutoringSession s) {
        String sql = "INSERT INTO tutoring_sessions(session_id, tutor_id, student_id, location, subject, date, start_time, end_time, " +
                "comment, status, modified_by, modified_to, proposed_date, proposed_start_time, proposed_end_time, " +
                "tutor_seen, student_seen) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getSessionId());
            pstmt.setString(2, s.getTutorId());
            pstmt.setString(3, s.getStudentId());
            pstmt.setString(4, s.getLocation());
            pstmt.setString(5, s.getSubject());

            if (s.getDate() != null) {
                pstmt.setDate(6, Date.valueOf(s.getDate()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            if (s.getStartTime() != null) {
                pstmt.setTime(7, Time.valueOf(s.getStartTime()));
            } else {
                pstmt.setNull(7, Types.TIME);
            }
            if (s.getEndTime() != null) {
                pstmt.setTime(8, Time.valueOf(s.getEndTime()));
            } else {
                pstmt.setNull(8, Types.TIME);
            }

            pstmt.setString(9, s.getComment());
            pstmt.setString(10, s.getStatus());
            pstmt.setString(11, s.getModifiedBy());
            pstmt.setString(12, s.getModifiedTo());

            if (s.getProposedDate() != null) {
                pstmt.setDate(13, Date.valueOf(s.getProposedDate()));
            } else {
                pstmt.setNull(13, Types.DATE);
            }
            if (s.getProposedStartTime() != null) {
                pstmt.setTime(14, Time.valueOf(s.getProposedStartTime()));
            } else {
                pstmt.setNull(14, Types.TIME);
            }
            if (s.getProposedEndTime() != null) {
                pstmt.setTime(15, Time.valueOf(s.getProposedEndTime()));
            } else {
                pstmt.setNull(15, Types.TIME);
            }

            pstmt.setBoolean(16, s.isTutorSeen());
            pstmt.setBoolean(17, s.isStudentSeen());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Aggiornamento
    private void update(TutoringSession s) {
        String sql = "UPDATE tutoring_sessions " +
                "SET tutor_id = ?, student_id = ?, location = ?, subject = ?, date = ?, start_time = ?, end_time = ?, " +
                "comment = ?, status = ?, modified_by = ?, modified_to = ?, proposed_date = ?, proposed_start_time = ?, proposed_end_time = ?, " +
                "tutor_seen = ?, student_seen = ? " +
                "WHERE session_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getTutorId());
            pstmt.setString(2, s.getStudentId());
            pstmt.setString(3, s.getLocation());
            pstmt.setString(4, s.getSubject());

            if (s.getDate() != null) {
                pstmt.setDate(5, Date.valueOf(s.getDate()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            if (s.getStartTime() != null) {
                pstmt.setTime(6, Time.valueOf(s.getStartTime()));
            } else {
                pstmt.setNull(6, Types.TIME);
            }

            if (s.getEndTime() != null) {
                pstmt.setTime(7, Time.valueOf(s.getEndTime()));
            } else {
                pstmt.setNull(7, Types.TIME);
            }

            pstmt.setString(8, s.getComment());
            pstmt.setString(9, s.getStatus());
            pstmt.setString(10, s.getModifiedBy());
            pstmt.setString(11, s.getModifiedTo());

            if (s.getProposedDate() != null) {
                pstmt.setDate(12, Date.valueOf(s.getProposedDate()));
            } else {
                pstmt.setNull(12, Types.DATE);
            }
            if (s.getProposedStartTime() != null) {
                pstmt.setTime(13, Time.valueOf(s.getProposedStartTime()));
            } else {
                pstmt.setNull(13, Types.TIME);
            }
            if (s.getProposedEndTime() != null) {
                pstmt.setTime(14, Time.valueOf(s.getProposedEndTime()));
            } else {
                pstmt.setNull(14, Types.TIME);
            }

            pstmt.setBoolean(15, s.isTutorSeen());
            pstmt.setBoolean(16, s.isStudentSeen());

            // session_id nel WHERE
            pstmt.setString(17, s.getSessionId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String sessionId) {
        String sql = "DELETE FROM tutoring_sessions WHERE session_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<TutoringSession> loadAllTutoringSession() {
        String sql = "SELECT session_id FROM tutoring_sessions";
        List<TutoringSession> result = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
             ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String sessionId = rs.getString("session_id");
                TutoringSession s = load(sessionId);
                if (s != null) {
                    result.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // Metodo di utilità per generare un ID univoco
    private String generateSessionId() {
        // Esempio: potrei usare un UUID, o un contatore, etc.
        return "TS-" + System.currentTimeMillis();
    }
}

