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


    public ManageNoticeBoardController() {
    }

    public ManageNoticeBoardController(UUID sessionId) {
        this.sessionId = sessionId;
    }

    // Conta le richieste nuove/non viste per l'utente specificato
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

    //  Helper privati – Ognuno con complessità minima (introdotti per eliminare gli issues su SonarQube)

    // Verifica se una sessione appartiene all'utente in base al ruolo
    private boolean belongsToUser(TutoringSession s, String userId, boolean tutorRole) {
        return tutorRole ? userId.equals(s.getTutorId()) : userId.equals(s.getStudentId());
    }

    // Verifica se la sessione ha una richiesta pendente non ancora vista dall'utente
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

    // Restituisce etichetta "Nome Cognome (età)" per la controparte
    public String getCounterpartLabel(String accountId) {
        Account a = accountDAO.load(accountId);
        return (a == null) ? "" : a.getName() + " " + a.getSurname() + " (" + a.getAge() + ")";
    }

    // Verifica se ci sono sessioni accettate nel giorno specificato
    // Metodo per colorare o meno la cella del calendario a seconda
    // della presenza o meno di una sessione accettata
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

    // Verifica se ci sono sessioni in attesa/modifica/cancellazione nel giorno specificato
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

    // Carica tutte le sessioni per l'utente loggato
    public List<TutoringSessionBean> loadSessionsForLoggedUser() {

        UserBean me = getLoggedUser();
        if (me == null) {
            return new ArrayList<>();
        }

        String role = null;
        String userId = null;

        for (AccountBean account : me.getAccounts()) {
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

    // Verifica se l'utente loggato deve rispondere alla sessione
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

    // Richiede una modifica della sessione di tutoraggio
    public void requestModification(TutoringSessionBean b,
                                    LocalDate newDate,
                                    LocalTime newStart,
                                    LocalTime newEnd,
                                    String reason) {

        TutoringSession s = loadEntity(b);
        if (s == null) return;

        String me = getLoggedAccountId();
        s.askModification(newDate, newStart, newEnd, reason, me);
        flagUnseen(s);

        tutoringSessionDAO.store(s);
    }

    // Accettazione della modifica della sessione di tutoraggio
    public void acceptModification(TutoringSessionBean b) {

        TutoringSession s = loadEntity(b);
        if (s == null) return;

        s.respondModification(true);
        flagUnseen(s);
        tutoringSessionDAO.store(s);
    }

    // Rifiuto della modifica della sessione di tutoraggio
    public void refuseModification(TutoringSessionBean b) {
        TutoringSession s = loadEntity(b);
        if (s == null) return;

        s.respondModification(false);  // ⇢ FSM
        flagUnseen(s);

        tutoringSessionDAO.store(s);
    }

    // Richiesta della cancellazione della sessione di tutoraggio
    public void requestCancellation(TutoringSessionBean b, String reason) {

        TutoringSession s = loadEntity(b);
        if (s == null) return;

        String me = getLoggedAccountId();
        s.askCancellation(reason, me);
        flagUnseenAndWho(s);
        tutoringSessionDAO.store(s);
    }

    // Accettazione della cancellazione della sessione di tutoraggio
    public void acceptCancellation(TutoringSessionBean b) {
        TutoringSession s = loadEntity(b);
        if (s == null) return;

        s.respondCancellation(true);
        flagUnseen(s);
        tutoringSessionDAO.store(s);
    }

    // Rifiuto della cancellazione della sessione di tutoraggio
    public void refuseCancellation(TutoringSessionBean b) {

        TutoringSession s = loadEntity(b);
        if (s == null) return;

        s.respondCancellation(false);  // ⇢ FSM
        flagUnseen(s);
        tutoringSessionDAO.store(s);
    }

    // Carica la TutoringSession dal DAO
    private TutoringSession loadEntity(TutoringSessionBean b) {
        return (b == null) ? null : tutoringSessionDAO.load(b.getSessionId());
    }

    // Imposta modifiedBy/modifiedTo e resetta flag seen
    private void flagUnseenAndWho(TutoringSession s) {

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

    // Imposta i flag seen in base a chi ha modificato
    private void flagUnseen(TutoringSession s) {

        String me = getLoggedAccountId();
        if (me == null) {
            throw new IllegalStateException("No valid account (Tutor/Student) found for logged user.");
        }

        boolean iAmTutor = me.equals(s.getTutorId());

        s.setTutorSeen(iAmTutor);
        s.setStudentSeen(!iAmTutor);
    }

    // Restituisce ruolo utente loggato
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

    // Restituisce accountId utente loggato
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

    // Wrapper: accetta la sessione delegando al booking controller
    public void acceptSession(String id) {
        bookingCtrl.acceptSession(id);
    }

    // Wrapper: rifiuta la sessione delegando al booking controller
    public void refuseSession(String id) {
        bookingCtrl.refuseSession(id);
    }

    // Restituisce UserBean utente loggato
    public UserBean getLoggedUser() {
        return getLoggedUser(sessionId);
    }

    // Effettua il logout della sessione
    public void logout() {
        logout(sessionId);
    }

    // Restituisce UserBean utente loggato per sessionId
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

    // Effettua il logout per sessionId
    public void logout(UUID sid) {
        if (sid != null) {
            loginCtrl.logout(sid);
        }
    }
}

