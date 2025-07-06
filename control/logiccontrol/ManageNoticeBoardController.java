package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.TutoringSessionBean;
import logic.bean.UserBean;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.TutoringSessionDAO;
import logic.model.domain.Account;
import logic.model.domain.User;
import logic.model.domain.state.TutoringSession;
import logic.model.domain.state.TutoringSessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManageNoticeBoardController {

    private static final String ROLE_TUTOR = "Tutor";
    private static final String ROLE_STUDENT = "Student";

    private final TutoringSessionDAO tutoringSessionDAO = DaoFactory.getInstance().getTutoringSessionDAO();
    private final AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();
    private final LoginController loginCtrl = new LoginController();
    private final BookingTutoringSessionController bookingCtrl = new BookingTutoringSessionController();

    private UUID sessionId;

    /* Conta quante sessioni "nuove" devono essere viste o approvate dall'utente specificato.
       Esempio: sessioni con status PENDING, MOD_REQUESTED, CANCEL_REQUESTED
       e quell'utente è la "controparte" nonSeen. */
    public ManageNoticeBoardController() {}

    public ManageNoticeBoardController(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public int countNewRequests(String userId, String role) {
        boolean tutorRole = ROLE_TUTOR.equalsIgnoreCase(role);
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
        return tutorRole ? userId.equals(s.getTutorId()): userId.equals(s.getStudentId());
    }

    private boolean hasPendingUnseenRequest(TutoringSession s, boolean tutorRole) {
        // Se è tutor guardo tutorSeen, altrimenti studentSeen
        if (tutorRole ? s.isTutorSeen() : s.isStudentSeen()) {
            return false;
        }

        TutoringSessionStatus st = s.getStatus();
        return st == TutoringSessionStatus.PENDING
                || st == TutoringSessionStatus.MOD_REQUESTED
                || st == TutoringSessionStatus.CANCEL_REQUESTED;
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
            if (s.getStatus() == TutoringSessionStatus.ACCEPTED && date.equals(s.getDate())) {
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
                    && (s.getStatus() == TutoringSessionStatus.PENDING
                    || s.getStatus() == TutoringSessionStatus.MOD_REQUESTED
                    || s.getStatus() == TutoringSessionStatus.CANCEL_REQUESTED)) {
                return true;
            }
        }
        return false;
    }

    // Metodo per caricare le sessioni relative all’utente loggato
    public List<TutoringSessionBean> loadSessionsForLoggedUser() {

        UserBean me = getLoggedUser();
        if (me == null) {
            return new ArrayList<>();
        }

        String role = null;
        String userId = null;

        for (AccountBean account: me.getAccounts()) {
            String r = account.getRole();

            if (ROLE_TUTOR.equalsIgnoreCase(r) || ROLE_STUDENT.equalsIgnoreCase(r)) {
                role = r;
                userId = account.getAccountId();
                break;
            }
        }

        if (userId == null || role == null) {
            throw new IllegalStateException("No valid account found for logged user.");
        }

        BookingTutoringSessionController bookingController = new BookingTutoringSessionController();
        if (ROLE_TUTOR.equalsIgnoreCase(role)) {
            return bookingController.loadAllSessionsForTutor(userId);
        } else {
            return bookingController.loadAllSessionsForStudent(userId);
        }
    }

    // Ritorna True se l’utente loggato è colui che deve rispondere.
    public boolean canRespond(String sessionId) {

        UserBean me = getLoggedUser();
        if (me == null) return false;

        TutoringSession s = tutoringSessionDAO.load(sessionId);
        String myId = null;

        for (AccountBean account : me.getAccounts()) {
            if (ROLE_TUTOR.equalsIgnoreCase(account.getRole()) || ROLE_STUDENT.equalsIgnoreCase(account.getRole())) {
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

        String me = getLoggedAccountId();
        s.askModification(newDate, newStart, newEnd, reason, me);
        flagUnseen(s);

        tutoringSessionDAO.store(s);
    }

    // Accettazione della modifica della sessione di tutoraggio
    public void acceptModification(TutoringSessionBean b){

        TutoringSession s = loadEntity(b);
        if(s == null) return;

        s.respondModification(true);
        flagUnseen(s);
        tutoringSessionDAO.store(s);
    }

    // Rifiuto della modifica della sessione di tutoraggio
    public void refuseModification(TutoringSessionBean b){
        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.respondModification(false);  // ⇢ FSM
        flagUnseen(s);

        tutoringSessionDAO.store(s);
    }

    // Richiesta della cancellazione della sessione di tutoraggio
    public void requestCancellation(TutoringSessionBean b, String reason){

        TutoringSession s = loadEntity(b);
        if(s == null) return;

        String me = getLoggedAccountId();
        s.askCancellation(reason, me);
        flagUnseenAndWho(s);
        tutoringSessionDAO.store(s);
    }

    // Accettazione della cancellazione della sessione di tutoraggio
    public void acceptCancellation(TutoringSessionBean b){
        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.respondCancellation(true);
        flagUnseen(s);
        tutoringSessionDAO.store(s);
    }

    // Rifiuto della cancellazione della sessione di tutoraggio
    public void refuseCancellation(TutoringSessionBean b){

        TutoringSession s = loadEntity(b);
        if(s==null) return;

        s.respondCancellation(false);  // ⇢ FSM
        flagUnseen(s);
        tutoringSessionDAO.store(s);
    }


    private TutoringSession loadEntity(TutoringSessionBean b){
        return (b==null)? null : tutoringSessionDAO.load(b.getSessionId());
    }

    // Imposta modifiedBy / modifiedTo e resetta i flag seen. */
    private void flagUnseenAndWho(TutoringSession s){

        UserBean me = getLoggedUser();
        String myId = null;

        for (AccountBean account : me.getAccounts()) {
            String role = account.getRole();

            if (ROLE_STUDENT.equalsIgnoreCase(role) || ROLE_TUTOR.equalsIgnoreCase(role)) {
                myId = account.getAccountId();
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

        String me = getLoggedAccountId();
        if (me == null) {
            throw new IllegalStateException("No valid account (Tutor/Student) found for logged user.");
        }

        boolean iAmTutor = me.equals(s.getTutorId());

        s.setTutorSeen(iAmTutor);
        s.setStudentSeen(!iAmTutor);
    }

    public String getLoggedRole() {

        UserBean me = getLoggedUser();
        if (me == null) return null;

        for (AccountBean account : me.getAccounts()) {
            String role = account.getRole();
            if (ROLE_TUTOR.equalsIgnoreCase(role) || ROLE_STUDENT.equalsIgnoreCase(role)) {
                return role;
            }
        }
        return null;
    }

    public String getLoggedAccountId() {

        UserBean me = getLoggedUser();
        if (me == null) return null;

        for (AccountBean account : me.getAccounts()) {
            String role = account.getRole();
            if (ROLE_TUTOR.equalsIgnoreCase(role) || ROLE_STUDENT.equalsIgnoreCase(role)) {
                return account.getAccountId();
            }
        }
        return null;
    }

    // Wrapper che delegano al BookingTutoringSessionController
    public void acceptSession (String id){ bookingCtrl.acceptSession(id);   }
    public void refuseSession (String id){ bookingCtrl.refuseSession(id);   }

    // Interazioni dirette con il SessionManager, in maniera tale che il controller grafico non lo conosca
    public UserBean getLoggedUser() {
        return getLoggedUser(sessionId);
    }

    public void logout() {
        logout(sessionId);
    }

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

    public void logout(UUID sid) {
        if (sid != null) {
            loginCtrl.logout(sid);
        }
    }
}

