package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.TutoringSessionBean;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.Account;
import logic.model.domain.SessionManager;
import logic.model.domain.TutoringSession;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ManageNoticeBoardController {

    private static final String STATUS_MOD_REQUEST = "MOD_REQUESTED";
    private static final String STATUS_CANCEL_REQUEST = "CANCEL_REQUESTED";
    private static final String STATUS_ACCEPTED = "ACCEPTED";

    private final TutoringSessionDAO tutoringSessionDAO = DaoFactory.getInstance().getTutoringSessionDAO();
    private final AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();

    /* Conta quante sessioni "nuove" devono essere viste o approvate dall'utente specificato.
       Esempio: sessioni con status PENDING, MOD_REQUESTED, CANCEL_REQUESTED
       e quell'utente è la "controparte" nonSeen. */

    public int countNewRequests(String userId, String role) {
        boolean tutorRole = "Tutor".equalsIgnoreCase(role);
        int count = 0;

        for (TutoringSession s : tutoringSessionDAO.loadAllTutoringSession()) {
            if (!userId.equals(s.getModifiedBy())
                    && belongsToUser(s, userId, tutorRole)
                    && hasPendingUnseenRequest(s, tutorRole)) {
                count++;
            }
        }

        return count;
    }

    //  Helper privati – Ognuno con complessità minima
    private boolean belongsToUser(TutoringSession s, String userId, boolean tutorRole) {
        return tutorRole ? userId.equals(s.getTutorId())
                : userId.equals(s.getStudentId());
    }

    private boolean hasPendingUnseenRequest(TutoringSession s, boolean tutorRole) {
        // Se è tutor guardo tutorSeen, altrimenti studentSeen
        if (tutorRole ? s.isTutorSeen() : s.isStudentSeen()) {
            return false;
        }

        String st = s.getStatus();
        return  "PENDING".equalsIgnoreCase(st)
                ||  STATUS_MOD_REQUEST.equalsIgnoreCase(st)
                ||  STATUS_CANCEL_REQUEST.equalsIgnoreCase(st);
    }

    // Ritorna “Nome Cognome (età)” della controparte
    public String getCounterpartLabel(String accountId) {
        Account a = accountDAO.load(accountId);
        return (a == null) ? "" : a.getName() + " " + a.getSurname() + " (" + a.getAge() + ")";
    }

    // Metodo per colorare o meno la cella del calendario a seconda della presenza o meno di una sessione accettata
    public boolean hasAcceptedSessionsOn(LocalDate date) {
        // Carica TUTTE le sessioni per l'utente (Tutor o Studente)
        List<TutoringSessionBean> sessions = loadSessionsForLoggedUser();

        for (TutoringSessionBean s : sessions) {
            if (STATUS_ACCEPTED.equalsIgnoreCase(s.getStatus())
                    && date.equals(s.getDate())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWaitingSessionsOn(LocalDate date) {
        // Carichiamo TUTTE le sessioni per l’utente loggato
        List<TutoringSessionBean> sessions = loadSessionsForLoggedUser();

        for (TutoringSessionBean s : sessions) {
            if (s.getDate() != null
                    && s.getDate().equals(date)
                    && ("PENDING".equalsIgnoreCase(s.getStatus())
                    || STATUS_MOD_REQUEST.equalsIgnoreCase(s.getStatus())
                    || STATUS_CANCEL_REQUEST.equalsIgnoreCase(s.getStatus()))) {
                return true;
            }
        }
        return false;
    }

    // Metodo per caricare le sessioni relative all’utente loggato
    public List<TutoringSessionBean> loadSessionsForLoggedUser() {
        if (SessionManager.getLoggedUser() == null) {
            return new ArrayList<>();
        }
        String role = null;
        String userId = null;

        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Tutor".equalsIgnoreCase(account.getRole())) {
                role = "Tutor";
                userId = account.getAccountId();
                break;
            } else if ("Student".equalsIgnoreCase(account.getRole())) {
                role = "Student";
                userId = account.getAccountId();
                break;
            }
        }

        if (userId == null || role == null) {
            throw new IllegalStateException("No valid account found for logged user.");
        }

        BookingTutoringSessionController bookingController = new BookingTutoringSessionController();
        if ("Tutor".equalsIgnoreCase(role)) {
            return bookingController.loadAllSessionsForTutor(userId);
        } else {
            return bookingController.loadAllSessionsForStudent(userId);
        }
    }

    // Ritorna True se l’utente loggato è colui che deve rispondere.
    public boolean canRespond(String sessionId) {                      // *** NEW ***
        if (SessionManager.getLoggedUser()==null) return false;
        TutoringSession s = tutoringSessionDAO.load(sessionId);
        String myId = null;

        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Tutor".equalsIgnoreCase(account.getRole()) || "Student".equalsIgnoreCase(account.getRole())) {
                myId = account.getAccountId();
                break;
            }
        }
        if (myId == null) {
            throw new IllegalStateException("No valid account (Tutor/Student) found for logged user.");
        }
        return myId.equals(s.getModifiedTo());
    }

    // Richiesta di modifica della sessione di tutoraggio
    public void requestModification(TutoringSessionBean b,
                                    LocalDate newDate,
                                    LocalTime newStart,
                                    LocalTime newEnd,
                                    String reason){
        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.setStatus(STATUS_MOD_REQUEST);
        s.setProposedDate(newDate);
        s.setProposedStartTime(newStart);
        s.setProposedEndTime(newEnd);
        s.setComment(reason);
        flagUnseenAndWho(s);

        tutoringSessionDAO.store(s);
    }

    // Accettazione della modifica della sessione di tutoraggio
    public void acceptModification(TutoringSessionBean b){

        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.setDate(s.getProposedDate());
        s.setStartTime(s.getProposedStartTime());
        s.setEndTime(s.getProposedEndTime());
        s.setProposedDate(null);
        s.setProposedStartTime(null);
        s.setProposedEndTime(null);
        s.setStatus(STATUS_ACCEPTED);
        flagUnseen(s);
        tutoringSessionDAO.store(s);
    }

    // Rifiuto della modifica della sessione di tutoraggio
    public void refuseModification(TutoringSessionBean b){
        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.setStatus(STATUS_ACCEPTED);
        s.setProposedDate(null);
        s.setProposedStartTime(null);
        s.setProposedEndTime(null);
        flagUnseen(s);

        tutoringSessionDAO.store(s);
    }

    // Richiesta della cancellazione della sessione di tutoraggio
    public void requestCancellation(TutoringSessionBean b, String reason){

        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.setStatus(STATUS_CANCEL_REQUEST);
        s.setComment(reason);
        flagUnseenAndWho(s);
        tutoringSessionDAO.store(s);
    }

    /* Metodo tramite cui riesco a capire l'id
    private String getOtherUserId(TutoringSession s) {
        String currentId = SessionManager.getLoggedUser().getId();
        if (currentId.equals(s.getStudentId())) {
            return s.getTutorId();
        }
        else {
            return s.getStudentId();
        }
    } */

    // Accettazione della cancellazione della sessione di tutoraggio
    public void acceptCancellation(TutoringSessionBean b){             // ★ BEAN
        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.setStatus("CANCELLED");
        flagUnseen(s);
        tutoringSessionDAO.store(s);     // In demo teniamo la traccia; se vuoi eliminarla: tsDao.delete(s.getSessionId());
    }

    // Rifiuto della cancellazione della sessione di tutoraggio
    public void refuseCancellation(TutoringSessionBean b){

        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.setStatus(STATUS_ACCEPTED);
        flagUnseen(s);
        tutoringSessionDAO.store(s);
    }

    // Conversione Entity → Bean
    private TutoringSessionBean toBean(TutoringSession s) {           // *** NEW ***
        if (s == null) return null;
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
        b.setModifiedBy(s.getModifiedBy());
        b.setModifiedTo(s.getModifiedTo());
        return b;
    }

    private TutoringSession loadEntity(TutoringSessionBean b){
        return (b==null)? null : tutoringSessionDAO.load(b.getSessionId());
    }

    // Imposta modifiedBy / modifiedTo e resetta i flag seen. */
    private void flagUnseenAndWho(TutoringSession s){
        String myId = null;
        String myRole = null;

        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                myId = account.getAccountId();
                myRole = "Student";
                break;
            } else if ("Tutor".equalsIgnoreCase(account.getRole())) {
                myId = account.getAccountId();
                myRole = "Tutor";
                break;
            }
        }

        if (myId == null) {
            throw new IllegalStateException("No Student or Tutor account found for logged user.");
        }

        String other = myId.equals(s.getStudentId()) ? s.getTutorId() : s.getStudentId();
        s.setModifiedBy(myId);
        s.setModifiedTo(other);
        flagUnseen(s);
    }

    private void flagUnseen(TutoringSession s){

        String me = null;
        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Tutor".equalsIgnoreCase(account.getRole()) || "Student".equalsIgnoreCase(account.getRole())) {
                me = account.getAccountId();
                break;
            }
        }
        if (me == null) {
            throw new IllegalStateException("No valid account (Tutor/Student) found for logged user.");
        }
        boolean iAmTutor = me.equals(s.getTutorId());

        s.setTutorSeen(iAmTutor);
        s.setStudentSeen(!iAmTutor);
    }
}

