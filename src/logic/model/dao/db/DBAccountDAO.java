package logic.model.dao.db;

import logic.model.dao.AccountDAO;
import logic.model.domain.*;

import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("java:S6548")
// Singleton necessario per garantire un'unica istanza di DBAccountDAO
// che centralizza l'accesso al database e assicura coerenza nelle operazioni sugli account.
public class DBAccountDAO extends DBDAO<String, Account> implements AccountDAO {

    private static DBAccountDAO instance;

    // Singleton necessario per avere una sola istanza del DAO
    public static synchronized DBAccountDAO getInstance() {
        if (instance == null) {
            instance = new DBAccountDAO();
        }
        return instance;
    }

    private static final String PROFILE_PIC = "profile_picture_path";
    private static final String PROFILE_COMMENT = "profile_comment";

    @Override
    protected String getTableName() {
        return "accounts";
    }

    @Override
    protected String getPkColumn() {
        return "account_id";
    }

    @Override
    protected String getId(Account a) {
        return a.getAccountId();
    }

    // Mappa un ResultSet JDBC a un oggetto Account (Student o Tutor)
    @Override
    protected Account map(ResultSet rs) throws SQLException {

        String email = rs.getString("email");
        String role = rs.getString("role");
        String pwd = rs.getString("password");
        String name = rs.getString("name");
        String surname = rs.getString("surname");
        LocalDate birth = toLocal(rs.getDate("birthday"));
        Availability av = buildAvailability(rs);

        Account a = switch (role.toUpperCase()) {
            case "TUTOR" -> buildTutor(rs, email, name, surname, birth, av);
            case "STUDENT" -> buildStudent(rs, email, name, surname, birth);
            default -> new Account(email, role, name, surname, birth,
                    rs.getString(PROFILE_PIC),
                    rs.getString(PROFILE_COMMENT));
        };
        a.setPassword(pwd);
        return a;
    }

    // Inserisce un nuovo account nel DB
    @Override
    protected void insert(Account acc) throws SQLException {

        final String SQL = """
                INSERT INTO accounts(
                  account_id,email,role,password,name,surname,birthday,
                  institute,location,educational_title,subject,
                  hourly_rate,offers_in_person,offers_online,
                  offers_group,first_lesson_free,
                  profile_picture_path,profile_comment,
                  availability_start_date,availability_end_date,availability_days_of_week,
                  rating)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            fillInsertOrUpdate(ps, acc, /* isUpdate */ false);
            ps.executeUpdate();
        }
    }

    // Aggiorna un account esistente nel DB
    @Override
    protected void update(Account acc) throws SQLException {

        final String SQL = """
                UPDATE accounts SET
                  email = ?, role = ?, password = ?,
                  name  = ?, surname = ?, birthday = ?,
                  institute = ?, location = ?, educational_title = ?, subject = ?, hourly_rate = ?,
                  offers_in_person = ?, offers_online = ?, offers_group = ?, first_lesson_free = ?,
                  profile_picture_path = ?, profile_comment = ?,
                  availability_start_date = ?, availability_end_date = ?, availability_days_of_week = ?,
                  rating = ?
                WHERE account_id = ?""";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            fillInsertOrUpdate(ps, acc, /* isUpdate */ true);
            ps.executeUpdate();
        }
    }

    // Helper per aumentare la modularità ed elimianare gli issues su SonarQube
    // Metodo di supporto che unisce inserimento e aggiornamento
    private void fillInsertOrUpdate(PreparedStatement ps, Account acc, boolean update) throws SQLException {
        List<Object> params = new ArrayList<>();

        addCommonParams(params, acc, update);
        addTutorOrStudentParams(params, acc);
        addProfileAndAvailabilityParams(params, acc);
        if (update) {
            params.add(acc.getAccountId());
        }
        bindParams(ps, params);
    }

    // Aggiunge i parametri comuni (id, email, ruolo, nome, ecc.)
    private void addCommonParams(List<Object> params, Account acc, boolean update) {
        if (update) {
            params.addAll(Arrays.asList(
                    acc.getEmail(), acc.getRole(), acc.getPassword(),
                    acc.getName(), acc.getSurname(), acc.getBirthday()
            ));
        } else {
            params.add(acc.getAccountId());
            params.addAll(Arrays.asList(
                    acc.getEmail(), acc.getRole(), acc.getPassword(),
                    acc.getName(), acc.getSurname(), acc.getBirthday()
            ));
        }
    }

    // Aggiunge i parametri specifici per tutor o student
    private void addTutorOrStudentParams(List<Object> params, Account acc) {
        params.add(acc instanceof Student s ? s.getInstitute() : null);

        if (acc instanceof Tutor t) {
            params.addAll(Arrays.asList(
                    t.getLocation(), t.getEducationalTitle(), t.getSubject(),
                    t.getHourlyRate(), t.offersInPerson(), t.offersOnline(),
                    t.offersGroup(), t.isFirstLessonFree()
            ));
        } else {
            params.addAll(Arrays.asList(
                    null, null, null, 0f, false, false, false, false
            ));
        }
    }

    // Aggiunge i parametri di profilo e disponibilità
    private void addProfileAndAvailabilityParams(List<Object> params, Account acc) {
        params.addAll(Arrays.asList(
                acc.getProfilePicturePath(), acc.getProfileComment()
        ));

        if (acc instanceof Tutor t && t.getAvailability() != null) {
            Availability av = t.getAvailability();
            params.addAll(Arrays.asList(
                    av.getStartDate(), av.getEndDate(),
                    av.getDaysOfWeek().stream()
                            .map(DayOfWeek::name)
                            .collect(Collectors.joining(","))
            ));
            params.add(t.getRating());
        } else {
            params.addAll(Arrays.asList(null, null, null, 0f));
        }
    }

    // Associa i parametri alla PreparedStatement, gestendo tipi diversi
    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object o = params.get(i);
            int idx = i + 1;

            if (o instanceof LocalDate d) {
                ps.setDate(idx, Date.valueOf(d));
            } else if (o instanceof LocalTime t) {
                ps.setTime(idx, Time.valueOf(t));
            } else if (o instanceof Float f) {
                ps.setFloat(idx, f);
            } else if (o instanceof Boolean b) {
                ps.setBoolean(idx, b);
            } else {
                ps.setObject(idx, o);
            }
        }
    }

    // Carica tutti gli account di un certo tipo (es. STUDENT o TUTOR)
    @Override
    public List<Account> loadAllAccountsOfType(String role) {
        return loadMany("SELECT account_id FROM accounts WHERE role = ?", role);
    }

    // Carica tutti gli account associati a un dato utente (email)
    public List<Account> loadAllAccountsOfUser(String email) {
        return loadMany("SELECT account_id FROM accounts WHERE email = ?", email);
    }

    // Crea un nuovo oggetto Account con l'ID specificato e password null.
    // Utilizzato nei metodi che richiedono un'istanza base pronta per essere popolata.
    @Override
    public Account create(String key) {
        return new Account(key, null);
    }

    // Converte Date in LocalDate (accetta null). */
    private static LocalDate toLocal(Date d) {
        return d == null ? null : d.toLocalDate();
    }

    // Esegue una query che restituisce n account_id e li mappa in oggetti Account.
    private List<Account> loadMany(String sql, String param) {
        List<Account> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account a = load(rs.getString("account_id"));
                    if (a != null) out.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    // Ricostruisce un oggetto Availability a partire dai campi del ResultSet.
    private static Availability buildAvailability(ResultSet rs) throws SQLException {
        LocalDate s = toLocal(rs.getDate("availability_start_date"));
        LocalDate e = toLocal(rs.getDate("availability_end_date"));
        String dws = rs.getString("availability_days_of_week");
        if (s == null || e == null || dws == null) return null;

        List<DayOfWeek> days = Arrays.stream(dws.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .toList();
        return new Availability(s, e, days);
    }

    // Crea e popola un oggetto Tutor a partire dai dati presenti nel ResultSet.
    private static Tutor buildTutor(ResultSet rs,
                                    String email, String n, String sn,
                                    LocalDate b, Availability av) throws SQLException {
        Tutor t = new Tutor.Builder(email)
                .name(n)
                .surname(sn)
                .birthday(b)
                .educationalTitle(rs.getString("educational_title"))
                .location(rs.getString("location"))
                .availability(null)
                .subject(rs.getString("subject"))
                .hourlyRate(rs.getFloat("hourly_rate"))
                .offersInPerson(rs.getBoolean("offers_in_person"))
                .offersOnline(rs.getBoolean("offers_online"))
                .offersGroup(rs.getBoolean("offers_group"))
                .firstLessonFree(rs.getBoolean("first_lesson_free"))
                .build();

        t.setProfilePicturePath(rs.getString(PROFILE_PIC));
        t.setProfileComment(rs.getString(PROFILE_COMMENT));
        t.setAvailability(av);
        t.setRating(rs.getFloat("rating"));
        return t;
    }

    // Crea e popola un oggetto Student a partire dai dati del ResultSet.
    private static Student buildStudent(ResultSet rs,
                                        String email, String n, String sn,
                                        LocalDate b) throws SQLException {
        Student s = new Student(email, n, sn, b, rs.getString("institute"));
        s.setProfilePicturePath(rs.getString(PROFILE_PIC));
        s.setProfileComment(rs.getString(PROFILE_COMMENT));
        return s;
    }
}

