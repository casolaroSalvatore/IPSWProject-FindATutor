package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.SharedReviewBean;
import logic.bean.UserBean;
import logic.model.dao.*;
import logic.model.domain.*;
import logic.model.domain.state.TutoringSession;
import java.util.*;

import static logic.model.domain.ReviewStatus.NOT_STARTED;
import static logic.model.domain.ReviewStatus.PENDING;

public class LeaveASharedReviewController {

    private static final ReviewStatus COMPLETED = ReviewStatus.COMPLETE;
    private static final String ROLE_TUTOR = "Tutor";
    private static final String ROLE_STUDENT = "Student";

    private SharedReviewDAO sharedReviewDAO = DaoFactory.getInstance().getSharedReviewDAO();
    private TutoringSessionDAO tutoringSessionDAO = DaoFactory.getInstance().getTutoringSessionDAO();
    private AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();
    private final LoginController loginCtrl = new LoginController();

    public LeaveASharedReviewController() {}

    public LeaveASharedReviewController(UUID sessionId) {
        this.sessionId = sessionId;
    }

    private UUID sessionId;

    // Carica i tutor con cui lo studente ha almeno una sessione
    public List<String> findAllTutorsForStudent(String studentId) {
        List<TutoringSession> all = tutoringSessionDAO.loadAllTutoringSession();
        Set<String> tutorIds = new HashSet<>();
        for (TutoringSession ts : all) {
            if (studentId.equals(ts.getStudentId())
                    /** && allSessionsFinished(studentId, ts.getTutorId())*/) {
                tutorIds.add(ts.getTutorId());
            }
        }
        return new ArrayList<>(tutorIds);
    }

    // Interrogo TutoringSessionDAO per trovare tutti gli student con cui il tutor ha effettuato dei tutoraggi
    public List<String> findAllStudentsForTutor(String tutorId) {
        List<TutoringSession> all = tutoringSessionDAO.loadAllTutoringSession();
        Set<String> studentIds = new HashSet<>();
        for (TutoringSession ts : all) {
            if (tutorId.equals(ts.getTutorId())
                    /** && allSessionsFinished(ts.getStudentId(), tutorId)*/) {
                studentIds.add(ts.getStudentId());
            }
        }
        return new ArrayList<>(studentIds);
    }

    // Trova o crea la review corrispondente a (studentId, tutorId)
    public SharedReview findOrCreateSharedReview(String studentId, String tutorId) {
        // Cerco se esiste già
        List<SharedReview> studentReviews = sharedReviewDAO.loadForStudent(studentId);
        for (SharedReview sharedReview : studentReviews) {
            if (tutorId.equals(sharedReview.getTutorId())) {
                return sharedReview; // trovata
            }
        }
        // Altrimenti la creo
        SharedReview newSharedReview = new SharedReview(UUID.randomUUID().toString(), studentId, tutorId);
        newSharedReview.setStatus(NOT_STARTED);
        sharedReviewDAO.store(newSharedReview);
        return newSharedReview;
    }

    public SharedReviewBean submitReview(SharedReviewBean bean) {

        bean.checkSyntax();

        SharedReview sr = sharedReviewDAO.load(bean.getReviewId());
        if (sr == null) {
            sr = findOrCreateSharedReview(
                    bean.getSenderRole() == SharedReviewBean.SenderRole.STUDENT
                            ? bean.getStudentId()
                            : bean.getTutorId(),
                    bean.getSenderRole() == SharedReviewBean.SenderRole.STUDENT
                            ? bean.getTutorId()
                            : bean.getStudentId());
        }

        applyBeanToEntity(bean, sr);

        boolean completed = sr.isStudentSubmitted() && sr.isTutorSubmitted();
        sr.setStatus(completed ? COMPLETED : PENDING);
        sharedReviewDAO.store(sr);

        if (completed) {
            updateTutorRating(sr);
        }

        return toBean(sr);
    }

    private float computeAverageTutorRating(String tutorId) {
        int sum = 0;
        int count = 0;

        for (SharedReview sr : sharedReviewDAO.loadForTutor(tutorId)) {
            if (sr.getStatus() == COMPLETED) {
                sum += sr.getStudentStars();
                count++;
            }
        }
        return count == 0 ? 0f : (float) sum / count;
    }

    private void assignRatingToTutorAccount(String tutorKeyOrId, float newRating) {

        AccountDAO accountDAO1 = DaoFactory.getInstance().getAccountDAO();
        Account account = accountDAO1.load(tutorKeyOrId); // Proviamo con la chiave diretta
        if (account == null) {
            for (Account acc : accountDAO1.loadAllAccountsOfType(ROLE_TUTOR)) {
                if (acc.getAccountId().equals(tutorKeyOrId)) {
                    account = acc;
                    break;
                }
            }

            if (account instanceof Tutor t) {
                tutorKeyOrId = t.getEmail() + "_" + t.getRole();
                account = accountDAO1.load(tutorKeyOrId);
            }
        }

        if (account instanceof Tutor tutorAcc) {
            tutorAcc.setRating(newRating);
            accountDAO1.store(tutorAcc);
        }
    }

    private void updateTutorRating(SharedReview sr) {
        float avg = computeAverageTutorRating(sr.getTutorId());
        assignRatingToTutorAccount(sr.getTutorId(), avg);
    }

    // Per la "notifica" (pallino rosso) per studente
    public int countPendingForStudent(String studentId) {
        int n = 0;
        for (SharedReview sr : sharedReviewDAO.loadForStudent(studentId)) {
            if (sr.getStatus() != COMPLETED && !sr.isStudentSubmitted()) n++;
        }
        return n;
    }

    // Per la "notifica" (pallino rosso) per il tutor
    public int countPendingForTutor(String tutorId) {
        int n = 0;
        for (SharedReview sr : sharedReviewDAO.loadForTutor(tutorId)) {
            if (sr.getStatus() != COMPLETED && !sr.isTutorSubmitted()) n++;
        }
        return n;
    }

    // Conversioni Bean <--> Entity
    private SharedReviewBean toBean(SharedReview sr) {

        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();

        // Determino se l’utente loggato è uno studente usando il metodo già presente
        boolean isStudent = isLoggedUserStudent();

        // Id della controparte: se sono studente --> tutorId, altrimenti studentId
        String counterpartId = isStudent ? sr.getTutorId() : sr.getStudentId();
        Account counterpartAcc = accDao.load(counterpartId);

        String info = buildNameAgeLabel(counterpartAcc);

        return new SharedReviewBean(sr, info);
    }

    private String buildNameAgeLabel(Account acc) {
        return acc == null ? "" : acc.getName() + " " + acc.getSurname() + " (" + acc.getAge() + ")";
    }

    private void applyBeanToEntity(SharedReviewBean b, SharedReview sr) {
        b.copyToEntity(sr);
    }

    public List<SharedReviewBean> loadBeansForStudent(String studentId) {
        List<SharedReviewBean> out = new ArrayList<>();
        for (SharedReview sr : sharedReviewDAO.loadForStudent(studentId)) {
            out.add(toBean(sr));
        }
        return out;
    }

    public List<SharedReviewBean> loadBeansForTutor(String tutorId) {
        List<SharedReviewBean> out = new ArrayList<>();
        for (SharedReview sr : sharedReviewDAO.loadForTutor(tutorId)) {
            out.add(toBean(sr));
        }
        return out;
    }

    public SharedReviewBean findOrCreateSharedReviewBean(String studentId, String tutorId) {
        SharedReview sr = findOrCreateSharedReview(studentId, tutorId);
        boolean isStudent = isLoggedUserStudent();

        String counterpartId = isStudent ? tutorId : studentId;
        Account counterpartAccount = accountDAO.load(counterpartId);
        SharedReviewBean bean = new SharedReviewBean(sr, buildNameAgeLabel(counterpartAccount));

        if (counterpartAccount != null) {
            AccountBean counterpartBean = toAccountBean(counterpartAccount);
            bean.setCounterpartAccount(counterpartBean);

            Account tutorAcc = accountDAO.load(sr.getTutorId());
            if (tutorAcc != null) {
                AccountBean tutorBean = toAccountBean(tutorAcc);
                bean.setTutorAccount(tutorBean);
            }
        }

        return bean;
    }

    private boolean isLoggedUserStudent() {
        // >>> MOD: passiamo la sessione al metodo
        UserBean me = getLoggedUser(sessionId);
        if (me == null) {
            return false;
        }

        for (AccountBean account : me.getAccounts()) {
            if (ROLE_STUDENT.equalsIgnoreCase(account.getRole())) {
                return true;
            }
        }

        return false;
    }


    private AccountBean toAccountBean(Account account) {

        AccountBean bean = new AccountBean();
        bean.setAccountId(account.getAccountId());
        bean.setName(account.getName());
        bean.setSurname(account.getSurname());
        bean.setBirthday(account.getBirthday());
        bean.setRole(account.getRole());
        bean.setProfilePicturePath(account.getProfilePicturePath());
        bean.setProfileComment(account.getProfileComment());

        if (account instanceof Tutor tutor) {
            bean.setLocation(tutor.getLocation());
            bean.setSubject(tutor.getSubject());
        } else {
            bean.setLocation(null);
            bean.setSubject(null);
        }

        return bean;
    }


    // Controllo che tutte le sessioni di tutoraggio siano terminate prima di
    // permettere di scrivere una recensione condivisa
    /** private boolean allSessionsFinished(String studentId, String tutorId) {
        LocalDate today = LocalDate.now();

        for (TutoringSession session : tutoringSessionDAO.loadAllTutoringSession()) {
            boolean isBetweenSameUsers = studentId.equals(session.getStudentId())
                    && tutorId.equals(session.getTutorId());
            boolean isInFutureOrToday = !session.getDate().isBefore(today);

            if (isBetweenSameUsers && isInFutureOrToday) {
                return false;
            }
        }
        return true;
    } */

    public UserBean getLoggedUser(UUID sessionId) {
        if (sessionId == null || !loginCtrl.isSessionActive(sessionId)) {
            return null;
        }

        logic.model.domain.User dom = loginCtrl.getUserFromSession(sessionId);
        if (dom == null) {
            return null;
        }

        UserBean ub = new UserBean();
        ub.setEmail(dom.getEmail());
        ub.setUsername(dom.getUsername());

        for (logic.model.domain.Account acc : dom.getAccounts()) {
            AccountBean ab = new AccountBean();
            ab.setAccountId(acc.getAccountId());
            ab.setRole(acc.getRole());
            ab.setName(acc.getName());
            ab.setSurname(acc.getSurname());
            ub.addAccount(ab);
        }

        return ub;
    }

    // Restituisce il ruolo “Student” o “Tutor” per la sessione, oppure null
    public String getLoggedRole(UUID sessionId) {
        UserBean me = getLoggedUser(sessionId);
        if (me == null) {
            return null;
        }
        for (AccountBean ab : me.getAccounts()) {
            String r = ab.getRole();
            if (ROLE_TUTOR.equalsIgnoreCase(r) || ROLE_STUDENT.equalsIgnoreCase(r)) {
                return r;
            }
        }
        return null;
    }

    // Restituisce l’accountId corrispondente al ruolo “Student” o “Tutor”, oppure null
    public String getLoggedAccountId(UUID sessionId) {
        UserBean me = getLoggedUser(sessionId);
        if (me == null) {
            return null;
        }
        for (AccountBean ab : me.getAccounts()) {
            String r = ab.getRole();
            if (ROLE_TUTOR.equalsIgnoreCase(r) || ROLE_STUDENT.equalsIgnoreCase(r)) {
                return ab.getAccountId();
            }
        }
        return null;
    }

    // Esegue il logout della sessione specificata
    public void logout(UUID sessionId) {
        if (sessionId != null) {
            loginCtrl.logout(sessionId);
        }
    }
}
