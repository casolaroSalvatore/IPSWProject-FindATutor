package logic.control.logiccontrol;

import logic.bean.*;
import logic.exception.NoTutorFoundException;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.*;
import logic.model.domain.state.TutoringSession;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BookingTutoringSessionController {

    private static final String ROLE_TUTOR = "Tutor";

    private final LoginController loginCtrl = new LoginController();

    private UUID sessionId;

    public BookingTutoringSessionController() {
    }

    public BookingTutoringSessionController(UUID sessionId) {
        this.sessionId = sessionId;
    }

    // Cerca i tutor in base ai criteri e restituisce una lista di TutorBean
    public List<TutorBean> searchTutors(TutorSearchCriteriaBean bean) throws NoTutorFoundException {
        TutorSearchCriteria crit = new TutorSearchCriteria(
                bean.getSubject(),
                bean.getLocation(),
                bean.getAvailability(),
                bean.isInPerson(),
                bean.isOnline(),
                bean.isGroup(),
                bean.isRating4Plus(),
                bean.isFirstLessonFree(),
                bean.getOrderCriteria()
        );

        String loggedTutorId = getLoggedTutorId();
        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
        List<Account> tutorAccounts = accDao.loadAllAccountsOfType(ROLE_TUTOR);
        List<TutorBean> candidates = new ArrayList<>();

        for (Account a : tutorAccounts) {
            Tutor tutor = (Tutor) a;
            if (isSelf(loggedTutorId, tutor) || !matchesAllFilters(tutor, crit)) {
                continue;
            }
            candidates.add(toTutorBean(tutor));
        }

        sort(candidates, crit.orderCriteria());
        if (candidates.isEmpty()) {
            throw new NoTutorFoundException("No tutor meets the selected filters. Please try again!");
        }
        return candidates;
    }

    // Verifica se il tutor è l'utente loggato
    private boolean isSelf(String loggedTutorId, Tutor tutor) {
        return loggedTutorId != null && tutor.getAccountId().equals(loggedTutorId);
    }

    // Verifica se un tutor soddisfa tutti i filtri
    private boolean matchesAllFilters(Tutor t, TutorSearchCriteria c) {
        return matchesBasicFilters(t, c.subject(), c.location(), c.userAvailability())
                && (!c.inPerson() || t.offersInPerson())
                && (!c.online() || t.offersOnline())
                && (!c.group() || t.offersGroup())
                && (!c.rating4Plus() || t.getRating() >= 4.0)
                && (!c.firstLessonFree() || t.isFirstLessonFree());
    }

    // Calcola i giorni prenotabili per un tutor in base alla disponibilità utente
    public List<DayBookingBean> computeDayBookingsForTutor(String tutorId,
                                                           AvailabilityBean userReq) {

        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
        Tutor tutor = (Tutor) accDao.load(tutorId);
        Availability tutorAvail = (tutor != null) ? tutor.getAvailability() : null;

        if (tutorAvail == null || tutorAvail.getDaysOfWeek() == null
                || tutorAvail.getDaysOfWeek().isEmpty()) {
            return List.of();
        }

        LocalDate start = (userReq != null && userReq.getStartDate() != null)
                ? userReq.getStartDate() : LocalDate.now();
        LocalDate end = (userReq != null && userReq.getEndDate() != null)
                ? userReq.getEndDate() : LocalDate.now();
        List<DayOfWeek> reqDays = (userReq != null) ? userReq.getDays() : List.of();

        List<DayBookingBean> out = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            boolean okUser = reqDays.isEmpty() || reqDays.contains(d.getDayOfWeek());
            boolean okTut = tutorAvail.getDaysOfWeek().contains(d.getDayOfWeek());
            if (okUser && okTut) out.add(new DayBookingBean(d));
        }
        return out;
    }


    // Genera etichetta Nome Cognome (età) per una controparte
    public String counterpartLabel(String accountId) {
        Account a = DaoFactory.getInstance().getAccountDAO().load(accountId);
        return (a == null) ? "" :
                a.getName() + " " + a.getSurname() + " (" + a.getAge() + ")";
    }

    // Costruisce un bean di prenotazione a partire da TutorBean e DayBookingBean
    public TutoringSessionBean buildBookingBean(TutorBean tutor, DayBookingBean row,
                                                String studentId,
                                                String location, String subject) {

        TutoringSessionBean b = new TutoringSessionBean();
        b.setTutorId(tutor.getAccountId());
        b.setStudentId(studentId);
        b.setDate(row.getDate());
        b.setStartTime(row.getStartTimeParsed());
        b.setEndTime(row.getEndTimeParsed());
        b.setLocation(location);
        b.setSubject(subject);
        b.setComment(row.getComment());
        return b;
    }

    // Costruisce un bean di prenotazione con parametri diretti
    public TutoringSessionBean buildBookingBean(
            TutorBean tutor,
            LocalDate date, LocalTime start, LocalTime end,
            String location, String subject, String comment) {

        // ri‑usa la logica esistente basata su DayBookingBean
        DayBookingBean row = new DayBookingBean(date);

        // Controlli sintattici offerti dal DayBookingBean
        row.checkSyntax();

        // Controlli semantici tipici di un Controller
        LocalTime s = row.getStartTimeParsed();
        LocalTime e = row.getEndTimeParsed();

        if (s == null || e == null) {
            throw new IllegalArgumentException("Start/End time invalid.");
        }

        if (s.isBefore(LocalTime.of(7, 0)) || e.isAfter(LocalTime.of(22, 0))) {
            throw new IllegalArgumentException("Time must be between 07:00 and 22:00.");
        }

        if (Duration.between(s, e).toMinutes() < 60) {
            throw new IllegalArgumentException("Minimum slot is 1 hour.");
        }

        if (row.getDate() == null || row.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date must be today or later.");
        }

        row.setStartTime(start.toString());
        row.setEndTime(end.toString());
        row.setComment(comment);

        return buildBookingBean(tutor, row, getStudentAccountId(), location, subject);
    }


    // Converte Tutor in TutorBean
    private TutorBean toTutorBean(Tutor t) {
        TutorBean b = new TutorBean();
        b.setAccountId(t.getAccountId());
        b.setRole(ROLE_TUTOR);
        b.setName(t.getName());
        b.setSurname(t.getSurname());
        b.setAge(t.getAge());
        b.setLocation(t.getLocation());
        b.setSubject(t.getSubject());
        b.setHourlyRate(t.getHourlyRate());
        b.setRating(t.getRating());
        b.setOffersInPerson(t.offersInPerson());
        b.setOffersOnline(t.offersOnline());
        b.setOffersGroup(t.offersGroup());
        b.setFirstLessonFree(t.isFirstLessonFree());
        return b;
    }

    // Converte TutoringSession in TutoringSessionBean
    public TutoringSessionBean toTutoringSessionBean(TutoringSession s) {
        TutoringSessionBean b = new TutoringSessionBean();
        b.setSessionId(s.getSessionId());
        b.setTutorId(s.getTutorId());
        b.setStudentId(s.getStudentId());
        b.setDate(s.getDate());
        b.setStartTime(s.getStartTime());
        b.setEndTime(s.getEndTime());
        b.setLocation(s.getLocation());
        b.setSubject(s.getSubject());
        b.setComment(s.getComment());
        b.setStatus(s.getStatus());
        b.setProposedDate(s.getProposedDate());
        b.setProposedStartTime(s.getProposedStartTime());
        b.setProposedEndTime(s.getProposedEndTime());
        return b;
    }

    // Effettua la prenotazione: crea TutoringSession e la salva nel DAO
    public void bookSession(TutoringSessionBean bean) {

        // Controlli sintattici offerti dal TutoringSessionBean
        bean.checkSyntax();

        TutoringSession tutoringSession = new TutoringSession();
        tutoringSession.setTutorId(bean.getTutorId());
        tutoringSession.setStudentId(bean.getStudentId());

        // Controlliamo che "tutorId" e "studentId" esistano davvero nella DAO
        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
        Account tutorAcc = accDao.load(bean.getTutorId());
        Account studentAcc = accDao.load(bean.getStudentId());

        if (tutorAcc == null) {
            throw new IllegalArgumentException("TutorId inesistente: " + bean.getTutorId());
        }
        if (studentAcc == null) {
            throw new IllegalArgumentException("StudentId inesistente: " + bean.getStudentId());
        }

        tutoringSession.setDate(bean.getDate());
        tutoringSession.setStartTime(bean.getStartTime());
        tutoringSession.setEndTime(bean.getEndTime());
        tutoringSession.setLocation(bean.getLocation());
        tutoringSession.setSubject(bean.getSubject());
        tutoringSession.setComment(bean.getComment());
        // Evento FSM: passa allo stato PENDING
        tutoringSession.book();
        tutoringSession.setTutorSeen(false);
        tutoringSession.setStudentSeen(true);

        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        dao.store(tutoringSession);
    }

    // Carica tutte le sessioni di un tutor
    public List<TutoringSessionBean> loadAllSessionsForTutor(String tutorId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        List<TutoringSession> allSessions = dao.loadAllTutoringSession();
        List<TutoringSessionBean> result = new ArrayList<>();

        for (TutoringSession session : allSessions) {
            if (session.getTutorId().equals(tutorId)) {
                result.add(toTutoringSessionBean(session));
            }
        }

        return result;
    }

    // Carica tutte le sessioni di uno studente
    public List<TutoringSessionBean> loadAllSessionsForStudent(String studentId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        List<TutoringSession> allSessions = dao.loadAllTutoringSession();
        List<TutoringSessionBean> result = new ArrayList<>();

        for (TutoringSession session : allSessions) {
            if (session.getStudentId().equals(studentId)) {
                result.add(toTutoringSessionBean(session));
            }
        }

        return result;
    }

    // Accetta una prenotazione come tutor
    public void acceptSession(String sessionId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        TutoringSession tutoringSession = dao.load(sessionId);
        if (tutoringSession != null) {
            tutoringSession.tutorAccept();
            dao.store(tutoringSession);
        }
    }

    // Rifiuta una prenotazione come tutor
    public void refuseSession(String sessionId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        TutoringSession tutoringSession = dao.load(sessionId);
        if (tutoringSession != null) {
            tutoringSession.tutorRefuse();
            dao.store(tutoringSession);
        }
    }

    // Verifica filtri base (location, materia, disponibilità)
    private boolean matchesBasicFilters(Tutor t, String subject,
                                        String location, AvailabilityBean reqAv) {
        boolean matchLoc = location == null || location.isBlank()
                || location.equalsIgnoreCase(t.getLocation());

        boolean matchSubj = subject == null || subject.isBlank()
                || subject.equalsIgnoreCase(t.getSubject());

        return matchLoc && matchSubj && checkTutorAvailability(t, reqAv);
    }

    // Controlla disponibilità tutor rispetto a richiesta
    private boolean checkTutorAvailability(Tutor tutor, AvailabilityBean req) {
        if (tutor.getAvailability() == null || req == null) return true;

        Availability av = tutor.getAvailability();

        /* Range di date */
        if (req.getStartDate() != null && av.getStartDate() != null &&
                av.getStartDate().isAfter(req.getStartDate())) return false;
        if (req.getEndDate() != null && av.getEndDate() != null &&
                av.getEndDate().isBefore(req.getEndDate())) return false;

        /* Giorni — basta 1 giorno in comune se l’utente ne ha scelti */
        List<DayOfWeek> days = req.getDays();
        if (days == null || days.isEmpty()) {
            return true;
        }
        for (DayOfWeek d : days) {
            if (av.getDaysOfWeek().contains(d)) {
                return true;
            }
        }
        return false;
    }

    // Ordina la lista tutor in base alla chiave specificata
    private void sort(List<TutorBean> list, String key) {
        if (key == null) return;
        switch (key) {
            case "Hourly Rate (asc)" -> list.sort(Comparator.comparingDouble(TutorBean::getHourlyRate));
            case "Hourly Rate (desc)" -> list.sort(Comparator.comparingDouble(TutorBean::getHourlyRate).reversed());
            case "Rating (asc)" -> list.sort(Comparator.comparingDouble(TutorBean::getRating));
            case "Rating (desc)" -> list.sort(Comparator.comparingDouble(TutorBean::getRating).reversed());
            default -> { /* Nessun filtraggio applicato */ }
        }
    }

    public UserBean getLoggedUser() {
        return getLoggedUser(sessionId);
    }

    // Restituisce UserBean utente loggato con sessionId specifico
    public UserBean getLoggedUser(UUID sid) {
        if (sid == null || !loginCtrl.isSessionActive(sid)) return null;
        User dom = loginCtrl.getUserFromSession(sid);
        if (dom == null) return null;

        UserBean ub = new UserBean();
        ub.setEmail(dom.getEmail());
        ub.setUsername(dom.getUsername());
        for (Account a : dom.getAccounts()) {
            AccountBean ab = new AccountBean();
            ab.setAccountId(a.getAccountId());
            ab.setRole(a.getRole());
            ab.setName(a.getName());
            ab.setSurname(a.getSurname());
            ub.addAccount(ab);
        }
        return ub;
    }

    // Restituisce l'accountId del tutor loggato
    private String getLoggedTutorId() {
        UserBean me = getLoggedUser();
        if (me == null) return null;

        for (AccountBean ab : me.getAccounts()) {
            if (ROLE_TUTOR.equalsIgnoreCase(ab.getRole()))
                return ab.getAccountId();
        }
        return null;
    }

    // Restituisce l'accountId dello studente loggato
    public String getStudentAccountId() {
        UserBean me = getLoggedUser();
        if (me == null) return null;

        for (AccountBean ab : me.getAccounts()) {
            if ("Student".equalsIgnoreCase(ab.getRole()))
                return ab.getAccountId();
        }
        return null;
    }
}
