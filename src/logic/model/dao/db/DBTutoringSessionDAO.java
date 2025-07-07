package logic.model.dao.db;

import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.state.TutoringSession;
import logic.model.domain.state.TutoringSessionStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("java:S6548")
// Singleton usato intenzionalmente per DBTutoringSessionDAO: garantisce un'unica
// istanza che centralizza l'accesso alle sessioni di tutoraggio nel DB,
// evitando duplicazioni e assicurando coerenza tramite DaoFactory e connessione condivisa.
public class DBTutoringSessionDAO extends DBDAO<String, TutoringSession> implements TutoringSessionDAO {

    private static final String SESSION_ID_COLUMN = "session_id";

    private static DBTutoringSessionDAO instance;

    // Singleton necessario per avere una sola istanza del DAO
    public static synchronized DBTutoringSessionDAO getInstance() {
        if (instance == null) {
            instance = new DBTutoringSessionDAO();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "tutoring_sessions";
    }

    @Override
    protected String getPkColumn() {
        return SESSION_ID_COLUMN;
    }

    @Override
    protected String getId(TutoringSession ts) {
        return ts.getSessionId();
    }

    // Mappa una riga del ResultSet a un oggetto TutoringSession
    @Override
    protected TutoringSession map(ResultSet rs) throws SQLException {
        TutoringSession s = new TutoringSession();
        s.setSessionId(rs.getString(SESSION_ID_COLUMN));
        s.setTutorId(rs.getString("tutor_id"));
        s.setStudentId(rs.getString("student_id"));
        s.setLocation(rs.getString("location"));
        s.setSubject(rs.getString("subject"));

        Date d = rs.getDate("date");
        Time st = rs.getTime("start_time");
        Time et = rs.getTime("end_time");
        if (d != null) s.setDate(d.toLocalDate());
        if (st != null) s.setStartTime(st.toLocalTime());
        if (et != null) s.setEndTime(et.toLocalTime());

        s.setComment(rs.getString("comment"));
        s.restoreStatusFromPersistence(
                TutoringSessionStatus.valueOf(rs.getString("status")));
        s.setModifiedBy(rs.getString("modified_by"));
        s.setModifiedTo(rs.getString("modified_to"));

        Date pd = rs.getDate("proposed_date");
        Time pst = rs.getTime("proposed_start_time");
        Time pet = rs.getTime("proposed_end_time");
        if (pd != null) s.setProposedDate(pd.toLocalDate());
        if (pst != null) s.setProposedStartTime(pst.toLocalTime());
        if (pet != null) s.setProposedEndTime(pet.toLocalTime());

        s.setTutorSeen(rs.getBoolean("tutor_seen"));
        s.setStudentSeen(rs.getBoolean("student_seen"));
        return s;
    }

    // Inserimento nel DB
    @Override
    protected void insert(TutoringSession s) throws SQLException {

        final String SQL = """
                INSERT INTO tutoring_sessions(
                  session_id, tutor_id, student_id, location, subject, date,
                  start_time, end_time, comment, status, modified_by, modified_to,
                  proposed_date, proposed_start_time, proposed_end_time,
                  tutor_seen, student_seen)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            fill(ps, s, false);
            ps.executeUpdate();
        }
    }

    // Aggiornamento di una sessione
    @Override
    protected void update(TutoringSession s) throws SQLException {

        final String SQL = """
                UPDATE tutoring_sessions SET
                  tutor_id = ?, student_id = ?, location = ?, subject = ?, date = ?,
                  start_time = ?, end_time = ?, comment = ?, status = ?, modified_by = ?, modified_to = ?,
                  proposed_date = ?, proposed_start_time = ?, proposed_end_time = ?,
                  tutor_seen = ?, student_seen = ?
                WHERE session_id = ?""";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            fill(ps, s, true);
            ps.executeUpdate();
        }
    }

    // Metodo comune per preparare i parametri del PreparedStatement
    private void fill(PreparedStatement ps,
                      TutoringSession s,
                      boolean update) throws SQLException {

        if (s.getSessionId() == null || s.getSessionId().isBlank()) {
            s.setSessionId(UUID.randomUUID().toString());
        }

        List<Object> p = new ArrayList<>();

        if (update) {
            p.addAll(Arrays.asList(
                    s.getTutorId(), s.getStudentId(), s.getLocation(), s.getSubject(), s.getDate(),
                    s.getStartTime(), s.getEndTime(), s.getComment(),
                    s.getStatus().name(), s.getModifiedBy(), s.getModifiedTo(),
                    s.getProposedDate(), s.getProposedStartTime(), s.getProposedEndTime(),
                    s.isTutorSeen(), s.isStudentSeen(),
                    s.getSessionId()
            ));
        } else {
            p.add(s.getSessionId());
            p.addAll(Arrays.asList(
                    s.getTutorId(), s.getStudentId(), s.getLocation(), s.getSubject(), s.getDate(),
                    s.getStartTime(), s.getEndTime(), s.getComment(),
                    s.getStatus().name(), s.getModifiedBy(), s.getModifiedTo(),
                    s.getProposedDate(), s.getProposedStartTime(), s.getProposedEndTime(),
                    s.isTutorSeen(), s.isStudentSeen()
            ));
        }

        for (int i = 0; i < p.size(); i++) {
            Object o = p.get(i);
            int idx = i + 1;
            if (o instanceof LocalDate d) ps.setDate(idx, Date.valueOf(d));
            else if (o instanceof LocalTime t) ps.setTime(idx, Time.valueOf(t));
            else if (o instanceof Boolean b) ps.setBoolean(idx, b);
            else ps.setObject(idx, o);
        }
    }

    // Carica tutte le sessioni (solo gli ID, poi chiama load())
    @Override
    public List<TutoringSession> loadAllTutoringSession() {
        List<TutoringSession> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT session_id FROM tutoring_sessions");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(load(rs.getString(SESSION_ID_COLUMN)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}





