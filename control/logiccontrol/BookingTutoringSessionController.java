package logic.control.logiccontrol;

import logic.bean.TutorBean;
import logic.bean.TutoringSessionBean;
import logic.bean.DayBookingBean;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BookingTutoringSessionController {

    private static final String STATUS_PENDING = "PENDING";
    private static final String ROLE_TUTOR     = "Tutor";

    // Ricerca del tutor a seconda dei filtri applicati
    public List<TutorBean> searchTutors(String subject,
                                    String location,
                                    Availability userAv,
                                    boolean inPerson, boolean online, boolean group,
                                    boolean rating4Plus, boolean firstLessonFree,
                                    String orderCriteria) {

        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
        List<Account> tutorAccounts = accDao.loadAllAccountsOfType(ROLE_TUTOR);

        List<TutorBean> candidates = new ArrayList<>();
        for (Account a : tutorAccounts) {
            Tutor t = (Tutor) a;                         // safe cast in-memory
            if (!matchesBasicFilters(t, subject, location, userAv)) continue;
            if (inPerson && !t.offersInPerson()) continue;
            if (online && !t.offersOnline()) continue;
            if (group && !t.offersGroup()) continue;
            if (rating4Plus && t.getRating() < 4.0) continue;
            if (firstLessonFree && !t.isFirstLessonFree()) continue;

            candidates.add(toTutorBean(t));
        }
        sort(candidates, orderCriteria);
        return candidates;
    }


    // --- ADD : label “Nome Cognome (età)” per la controparte
    public String counterpartLabel(String accountId) {
        Account a = DaoFactory.getInstance()
                .getAccountDAO()
                .load(accountId);
        return (a == null) ? "" :
                a.getName() + " " + a.getSurname() + " (" + a.getAge() + ")";
    }

    // Costruzione del bean di prenotazione (usato dalla GUI)
    public TutoringSessionBean buildBookingBean(TutorBean tutor, DayBookingBean row,
                                                String studentId,
                                                String location, String subject) {

        TutoringSessionBean b = new TutoringSessionBean();
        b.setTutorId   (tutor.getAccountId());
        b.setStudentId (studentId);
        b.setDate      (row.getDate());
        b.setStartTime (row.getStartTimeParsed());
        b.setEndTime   (row.getEndTimeParsed());
        b.setLocation  (location);
        b.setSubject   (subject);
        b.setComment   (row.getComment());
        return b;
    }

    // >>> Mapping Tutor → TutorBean
    private TutorBean toTutorBean(Tutor t) {
        TutorBean b = new TutorBean();
        b.setAccountId(t.getAccountId());
        b.setRole("Tutor");
        b.setName     (t.getName());
        b.setSurname  (t.getSurname());
        b.setAge      (t.getAge());
        b.setLocation (t.getLocation());
        b.setSubject  (t.getSubject());
        b.setHourlyRate(t.getHourlyRate());
        b.setRating   (t.getRating());
        b.setOffersInPerson(t.offersInPerson());
        b.setOffersOnline (t.offersOnline());
        b.setOffersGroup  (t.offersGroup());
        b.setFirstLessonFree(t.isFirstLessonFree());
        return b;
    }

    public TutoringSessionBean toTutoringSessionBean(TutoringSession s) {
        TutoringSessionBean b = new TutoringSessionBean();
        b.setSessionId (s.getSessionId());
        b.setTutorId   (s.getTutorId());
        b.setStudentId (s.getStudentId());
        b.setDate      (s.getDate());
        b.setStartTime (s.getStartTime());
        b.setEndTime   (s.getEndTime());
        b.setLocation  (s.getLocation());
        b.setSubject   (s.getSubject());
        b.setComment   (s.getComment());
        b.setStatus    (s.getStatus());
        b.setProposedDate     (s.getProposedDate());
        b.setProposedStartTime(s.getProposedStartTime());
        b.setProposedEndTime  (s.getProposedEndTime());
        return b;
    }

    public void bookSession(TutoringSessionBean bean) {

        TutoringSession tutoringSession = new TutoringSession();

        tutoringSession.setTutorId(bean.getTutorId());
        tutoringSession.setStudentId(bean.getStudentId());

        // (VERIFICA facoltativa) Controlliamo che "tutorId" e "studentId" esistano davvero nella DAO
        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();
        Account tutorAcc = accDao.load(bean.getTutorId());
        Account studentAcc = accDao.load(bean.getStudentId());

        if( tutorAcc == null) {
            throw new IllegalArgumentException("TutorId inesistente: " + bean.getTutorId());
        }
        if(studentAcc == null) {
            throw new IllegalArgumentException("StudentId inesistente: " + bean.getStudentId());
        }

        tutoringSession.setDate(bean.getDate());
        tutoringSession.setStartTime(bean.getStartTime());
        tutoringSession.setEndTime(bean.getEndTime());
        tutoringSession.setLocation(bean.getLocation());
        tutoringSession.setSubject(bean.getSubject());
        tutoringSession.setComment(bean.getComment());
        tutoringSession.setStatus(STATUS_PENDING);

        // Alla prenotazione da parte dello studente, il tutor non ha ancora visto la richiesta
        tutoringSession.setStatus(STATUS_PENDING);
        tutoringSession.setTutorSeen(false);
        tutoringSession.setStudentSeen(true);

        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        dao.store(tutoringSession);
    }


    // Carica tutte le sessioni (PENDING, ACCEPTED, REFUSED) del tutor, ordinate a piacere
    public List<TutoringSessionBean> loadAllSessionsForTutor(String tutorId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        return dao.loadAllTutoringSession().stream()
                .filter(s -> s.getTutorId().equals(tutorId))
                .map(this::toTutoringSessionBean)
                .toList();
    }

    // Carica tutte le sessioni (PENDING, ACCEPTED, REFUSED) dello studente
    public List<TutoringSessionBean> loadAllSessionsForStudent(String studentId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        return dao.loadAllTutoringSession().stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .map(this::toTutoringSessionBean)
                .toList();
    }


    public void acceptSession(String sessionId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        TutoringSession tutoringSession = dao.load(sessionId);
        if (tutoringSession != null) {
            tutoringSession.setStatus("ACCEPTED");
            dao.store(tutoringSession); // store per aggiornare
        }
    }

    public void refuseSession(String sessionId) {
        TutoringSessionDAO dao = DaoFactory.getInstance().getTutoringSessionDAO();
        TutoringSession tutoringSession = dao.load(sessionId);
        if (tutoringSession != null) {
            tutoringSession.setStatus("REFUSED");
            dao.store(tutoringSession); // store per aggiornare
        }
    }

    private boolean matchesBasicFilters(Tutor t, String subject,
                                        String location, Availability reqAv) {
        boolean matchLoc  = location == null || location.isBlank()
                || location.equalsIgnoreCase(t.getLocation());

        boolean matchSubj = subject == null || subject.isBlank()
                || subject.equalsIgnoreCase(t.getSubject());

        return matchLoc && matchSubj && checkTutorAvailability(t, reqAv);
    }

    private boolean checkTutorAvailability(Tutor tutor, Availability req) {
        if (tutor.getAvailability() == null || req == null) return true;

        Availability av = tutor.getAvailability();

        /* range date */
        if (req.getStartDate() != null && av.getStartDate() != null &&
                av.getStartDate().isAfter(req.getStartDate())) return false;
        if (req.getEndDate()   != null && av.getEndDate()   != null &&
                av.getEndDate().isBefore(req.getEndDate()))     return false;

        /* giorni   — basta 1 giorno in comune se l’utente ne ha scelti */
        List<DayOfWeek> days = req.getDaysOfWeek();
        return (days == null || days.isEmpty()) ||
                days.stream().anyMatch(av.getDaysOfWeek()::contains);
    }

    private void sort(List<TutorBean> list, String key) {
        if (key == null) return;
        switch (key) {
            case "Hourly Rate (asc)"  -> list.sort(Comparator.comparingDouble(TutorBean::getHourlyRate));
            case "Hourly Rate (desc)" -> list.sort(Comparator.comparingDouble(TutorBean::getHourlyRate).reversed());
            case "Rating (asc)"       -> list.sort(Comparator.comparingDouble(TutorBean::getRating));
            case "Rating (desc)"      -> list.sort(Comparator.comparingDouble(TutorBean::getRating).reversed());
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
}
