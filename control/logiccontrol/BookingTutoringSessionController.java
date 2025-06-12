package logic.control.logiccontrol;

import logic.bean.*;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.*;
import logic.model.domain.state.TutoringSession;
import java.time.DayOfWeek;
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

    public BookingTutoringSessionController() {}

    public BookingTutoringSessionController(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public List<TutorBean> searchTutors(TutorSearchCriteriaBean bean) {
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
        return candidates;
    }

    private boolean isSelf(String loggedTutorId, Tutor tutor) {
        return loggedTutorId != null && tutor.getAccountId().equals(loggedTutorId);
    }

    private boolean matchesAllFilters(Tutor t, TutorSearchCriteria c) {
        return matchesBasicFilters(t, c.subject(), c.location(), c.userAvailability())
                && (!c.inPerson() || t.offersInPerson())
                && (!c.online() || t.offersOnline())
                && (!c.group() || t.offersGroup())
                && (!c.rating4Plus() || t.getRating() >= 4.0)
                && (!c.firstLessonFree() || t.isFirstLessonFree());
    }

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
        LocalDate end   = (userReq != null && userReq.getEndDate() != null)
                ? userReq.getEndDate()   : LocalDate.now();
        List<DayOfWeek> reqDays = (userReq != null) ? userReq.getDays() : List.of();

        List<DayBookingBean> out = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            boolean okUser = reqDays.isEmpty() || reqDays.contains(d.getDayOfWeek());
            boolean okTut  = tutorAvail.getDaysOfWeek().contains(d.getDayOfWeek());
            if (okUser && okTut) out.add(new DayBookingBean(d));
        }
        return out;
    }


    // Label “Nome Cognome (età)” per la controparte
    public String counterpartLabel(String accountId) {
        Account a = DaoFactory.getInstance().getAccountDAO().load(accountId);
        return (a == null) ? "" :
                a.getName() + " " + a.getSurname() + " (" + a.getAge() + ")";
    }

    // Costruzione del bean di prenotazione (usato dalla GUI)
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

    public TutoringSessionBean buildBookingBean(
            TutorBean tutor,
            LocalDate date, LocalTime start, LocalTime end,
            String location, String subject, String comment) {

        // ri‑usa la logica esistente basata su DayBookingBean
        DayBookingBean row = new DayBookingBean(date);
        row.setStartTime(start.toString());
        row.setEndTime(end.toString());
        row.setComment(comment);

        return buildBookingBean(tutor, row, getStudentAccountId(), location, subject);
    }


    // Mapping Tutor --> TutorBean
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

    public void bookSession(TutoringSessionBean bean) {

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


    public void acceptSession(String sessionId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        TutoringSession tutoringSession = dao.load(sessionId);
        if (tutoringSession != null) {
            tutoringSession.tutorAccept();
            dao.store(tutoringSession); // store per aggiornare
        }
    }

    public void refuseSession(String sessionId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        TutoringSession tutoringSession = dao.load(sessionId);
        if (tutoringSession != null) {
            tutoringSession.tutorRefuse();
            dao.store(tutoringSession); // store per aggiornare
        }
    }

    private boolean matchesBasicFilters(Tutor t, String subject,
                                        String location, AvailabilityBean reqAv) {
        boolean matchLoc = location == null || location.isBlank()
                || location.equalsIgnoreCase(t.getLocation());

        boolean matchSubj = subject == null || subject.isBlank()
                || subject.equalsIgnoreCase(t.getSubject());

        return matchLoc && matchSubj && checkTutorAvailability(t, reqAv);
    }

    private boolean checkTutorAvailability(Tutor tutor, AvailabilityBean req) {
        if (tutor.getAvailability() == null || req == null) return true;

        Availability av = tutor.getAvailability();

        /* range date */
        if (req.getStartDate() != null && av.getStartDate() != null &&
                av.getStartDate().isAfter(req.getStartDate())) return false;
        if (req.getEndDate() != null && av.getEndDate() != null &&
                av.getEndDate().isBefore(req.getEndDate())) return false;

        /* Giorni — basta 1 giorno in comune se l’utente ne ha scelti */
        List<DayOfWeek> days = req.getDays();
        return (days == null || days.isEmpty()) ||
                days.stream().anyMatch(av.getDaysOfWeek()::contains);
    }

    private void sort(List<TutorBean> list, String key) {
        if (key == null) return;
        switch (key) {
            case "Hourly Rate (asc)" -> list.sort(Comparator.comparingDouble(TutorBean::getHourlyRate));
            case "Hourly Rate (desc)" -> list.sort(Comparator.comparingDouble(TutorBean::getHourlyRate).reversed());
            case "Rating (asc)" -> list.sort(Comparator.comparingDouble(TutorBean::getRating));
            case "Rating (desc)" -> list.sort(Comparator.comparingDouble(TutorBean::getRating).reversed());
            default -> {}
        }
    }

    public Availability getTutorAvailability(String tutorAccountId) {
        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
        Account acc = accDao.load(tutorAccountId);

        // Se esiste ed è davvero un Tutor restituiamo la sua availability, altrimenti null
        if (acc instanceof Tutor tutor) {
            return tutor.getAvailability();
        }
        return null;
    }

    public UserBean getLoggedUser() {
        return getLoggedUser(sessionId);
    }

    // Helper utente loggato
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

    private String getLoggedTutorId() {
        UserBean me = getLoggedUser();
        if (me == null) return null;

        for (AccountBean ab : me.getAccounts()) {
            if (ROLE_TUTOR.equalsIgnoreCase(ab.getRole()))
                return ab.getAccountId();
        }
        return null;
    }

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
