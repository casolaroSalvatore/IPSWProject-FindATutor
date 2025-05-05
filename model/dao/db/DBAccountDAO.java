package logic.model.dao.db;

import logic.model.dao.AccountDAO;
import logic.model.domain.Account;
import logic.model.domain.Availability;
import logic.model.domain.Tutor;
import logic.model.domain.Student;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBAccountDAO implements AccountDAO {

    private static Connection conn;
    private static final String PROFILE_PIC = "profile_picture_path";
    private static final String PROFILE_COMMENT  = "profile_comment";

    static {
        conn = ConnectionFactory.getConnection();
    }

    @Override
    public Account load(String accountId) {
        final String sql = "SELECT email, role, password, name, surname, birthday, "
                + "institute, location, educational_title, subject, "
                + "hourly_rate, offers_in_person, offers_online, "
                + "offers_group, first_lesson_free, "
                + "availability_start_date, availability_end_date, "
                + "availability_days_of_week, profile_picture_path, "
                + "profile_comment, rating "
                + "FROM accounts WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String email    = rs.getString("email");
                String role     = rs.getString("role");
                String password = rs.getString("password");
                String name     = rs.getString("name");
                String surname  = rs.getString("surname");
                LocalDate birth = toLocalDate(rs.getDate("birthday"));

                Availability avail = buildAvailability(rs);

                Account account;
                switch (role.toUpperCase()) {
                    case "TUTOR"   -> account = buildTutor(rs, email, name, surname, birth, avail);
                    case "STUDENT" -> account = buildStudent(rs, email, name, surname, birth);
                    default        -> account = new Account(email, role, name, surname, birth,
                            rs.getString(PROFILE_PIC),
                            rs.getString(PROFILE_COMMENT));
                }
                account.setPassword(password);
                return account;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //  Helper privati — ognuno fa una sola cosa (introdotti per ridurre la complessità)
    private static LocalDate toLocalDate(java.sql.Date d) {
        return d == null ? null : d.toLocalDate();
    }

    private static Availability buildAvailability(ResultSet rs) throws SQLException {
        LocalDate start = toLocalDate(rs.getDate("availability_start_date"));
        LocalDate end   = toLocalDate(rs.getDate("availability_end_date"));
        String   days   = rs.getString("availability_days_of_week");

        if (start == null || end == null || days == null) {
            return null;
        }
        List<DayOfWeek> list = new ArrayList<>();
        for (String d : days.split(",")) {
            list.add(DayOfWeek.valueOf(d.trim().toUpperCase()));
        }
        return new Availability(start, end, list);
    }

    private static Tutor buildTutor(ResultSet rs, String email, String name,
                                    String surname, LocalDate birth,
                                    Availability avail) throws SQLException {

        Tutor t = new Tutor(email, name, surname, birth,
                rs.getString("educational_title"),
                rs.getString("location"),
                null,                        // availability sarà settata dopo
                rs.getString("subject"),
                rs.getFloat("hourly_rate"),
                rs.getBoolean("offers_in_person"),
                rs.getBoolean("offers_online"),
                rs.getBoolean("offers_group"),
                rs.getBoolean("first_lesson_free"));

        t.setProfilePicturePath(rs.getString(PROFILE_PIC));
        t.setProfileComment(rs.getString(PROFILE_COMMENT));
        t.setAvailability(avail);
        t.setRating(rs.getFloat("rating"));
        return t;
    }

    private static Student buildStudent(ResultSet rs, String email, String name,
                                        String surname, LocalDate birth) throws SQLException {

        Student s = new Student(email, name, surname, birth, rs.getString("institute"));
        s.setProfilePicturePath(rs.getString(PROFILE_PIC));
        s.setProfileComment(rs.getString(PROFILE_COMMENT));
        return s;
    }


    @Override
    public void store(Account entity) {
        // Se l'account non esiste, INSERT, altrimenti UPDATE
        boolean exists = exists(entity.getAccountId());
        if (!exists) {
            insert(entity);
        } else {
            update(entity);
        }
    }

    private void insert(Account acc) {

        final String SQL = """
        INSERT INTO accounts(
            account_id, email, role, password, name, surname, birthday,
            institute, location, educational_title, subject,
            hourly_rate, offers_in_person, offers_online,
            offers_group, first_lesson_free,
            profile_picture_path, profile_comment,
            availability_start_date, availability_end_date, availability_days_of_week,
            rating)
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setString(1,  acc.getAccountId());
            ps.setString(2,  acc.getEmail());
            ps.setString(3,  acc.getRole());
            ps.setString(4,  acc.getPassword());
            ps.setString(5,  acc.getName());
            ps.setString(6,  acc.getSurname());
            setDateOrNull(ps, 7, acc.getBirthday());

            // Campi Student
            ps.setString(8,  (acc instanceof Student s) ? s.getInstitute() : null);

            // Campi Tutor
            if (acc instanceof Tutor t) {
                fillTutorFields(ps, t);
            } else {
                fillEmptyTutorFields(ps);
            }

            ps.setString(17, acc.getProfilePicturePath());
            ps.setString(18, acc.getProfileComment());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Helper privati (fattorizzazione per ridurre la complessità)
    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date != null)  ps.setDate(index, Date.valueOf(date));
        else               ps.setNull(index, Types.DATE);
    }

     // Riempie tutti i campi specifici del Tutor
    private static void fillTutorFields(PreparedStatement ps, Tutor t) throws SQLException {

        ps.setString (9,  t.getLocation());
        ps.setString (10,  t.getEducationalTitle());
        ps.setString (11, t.getSubject());
        ps.setFloat  (12, t.getHourlyRate());
        ps.setBoolean(13, t.offersInPerson());
        ps.setBoolean(14, t.offersOnline());
        ps.setBoolean(15, t.offersGroup());
        ps.setBoolean(16, t.isFirstLessonFree());

        Availability av = t.getAvailability();
        if (av != null) {
            setDateOrNull(ps, 19, av.getStartDate());
            setDateOrNull(ps, 20, av.getEndDate());
            ps.setString(21, av.getDaysOfWeek().stream()
                    .map(DayOfWeek::name)
                    .collect(Collectors.joining(",")));
        } else {
            ps.setNull(19, Types.DATE);
            ps.setNull(20, Types.DATE);
            ps.setNull(21, Types.VARCHAR);
        }

        ps.setFloat(22, t.getRating());
    }

    // Per gli account non-Tutor: imposta NULL/0 ai campi tutor-specifici
    private static void fillEmptyTutorFields(PreparedStatement ps) throws SQLException {
        for (int idx = 9; idx <= 11; idx++) ps.setNull(idx, Types.VARCHAR);

        ps.setFloat  (12, 0f);
        for (int idx = 13; idx <= 16; idx++) ps.setBoolean(idx, false);

        ps.setNull(19, Types.DATE);
        ps.setNull(20, Types.DATE);
        ps.setNull(21, Types.VARCHAR);

        ps.setFloat(22, 0f);
    }


    private void update(Account acc) {

        final String SQL = """
        UPDATE accounts SET
            name = ?, surname = ?, birthday = ?,
            institute = ?, location = ?, educational_title = ?, subject = ?, hourly_rate = ?,
            offers_in_person = ?, offers_online = ?, offers_group = ?, first_lesson_free = ?,
            profile_picture_path = ?, profile_comment = ?,
            availability_start_date = ?, availability_end_date = ?, availability_days_of_week = ?,
            rating = ?
        WHERE account_id = ?""";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setString(1,  acc.getName());
            ps.setString(2,  acc.getSurname());
            setDateOrNull(ps, 3, acc.getBirthday());

            // Campi Student
            ps.setString(4, (acc instanceof Student s) ? s.getInstitute() : null);

            // Campi Tutor
            if (acc instanceof Tutor t) {
                fillTutorFieldsForUpdate(ps, t);          // indici 5-12, 15-18
            } else {
                fillEmptyTutorFieldsForUpdate(ps);        // azzera campi Tutor
            }

            ps.setString(13, acc.getProfilePicturePath());
            ps.setString(14, acc.getProfileComment());

            ps.setString(19, acc.getAccountId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper privati (fattorizzazione per ridurre la complessità)
    private static void fillTutorFieldsForUpdate(PreparedStatement ps, Tutor t) throws SQLException {

        ps.setString (5,  t.getLocation());
        ps.setString (6,  t.getEducationalTitle());
        ps.setString (7,  t.getSubject());
        ps.setFloat  (8,  t.getHourlyRate());

        ps.setBoolean(9,  t.offersInPerson());
        ps.setBoolean(10, t.offersOnline());
        ps.setBoolean(11, t.offersGroup());
        ps.setBoolean(12, t.isFirstLessonFree());

        Availability av = t.getAvailability();
        if (av != null) {
            setDateOrNull(ps, 15, av.getStartDate());
            setDateOrNull(ps, 16, av.getEndDate());
            ps.setString(17, av.getDaysOfWeek().stream()
                    .map(DayOfWeek::name)
                    .collect(Collectors.joining(",")));
        } else {
            ps.setNull(15, Types.DATE);
            ps.setNull(16, Types.DATE);
            ps.setNull(17, Types.VARCHAR);
        }

        ps.setFloat(18, t.getRating());
    }

    private static void fillEmptyTutorFieldsForUpdate(PreparedStatement ps) throws SQLException {

        for (int idx = 5; idx <= 7; idx++) ps.setNull(idx, Types.VARCHAR);

        ps.setFloat  (8, 0f);        // hourly_rate
        for (int idx = 9; idx <= 12; idx++) ps.setBoolean(idx, false);

        // availability start/end/days
        ps.setNull(15, Types.DATE);
        ps.setNull(16, Types.DATE);
        ps.setNull(17, Types.VARCHAR);

        ps.setFloat(18, 0f);
    }


    @Override
    public void delete(String accountId) {
        String sql = "DELETE FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean exists(String accountId) {
        String sql = "SELECT account_id FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Account create(String key) {
        // Esempio: crea un generico Account
        return new Account(key, null);
    }

    @Override
    public List<Account> loadAllAccountsOfType(String role) {
        List<Account> result = new ArrayList<>();
        String sql = "SELECT account_id FROM accounts WHERE role = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String accountId = rs.getString("account_id");
                    Account a = load(accountId);
                    if (a != null) {
                        result.add(a);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // Metodo comodo per DBUserDAO
    public List<Account> loadAllAccountsOfUser(String email) {
        List<Account> result = new ArrayList<>();
        String sql = "SELECT account_id FROM accounts WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String accountId = rs.getString("account_id");
                    Account a = load(accountId);
                    if (a != null) {
                        result.add(a);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
