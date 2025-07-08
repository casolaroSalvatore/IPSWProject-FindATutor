package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.SharedReviewBean;
import logic.bean.UserBean;
import logic.infrastructure.DaoFactory;
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

    public LeaveASharedReviewController() {
    }

    public LeaveASharedReviewController(UUID sessionId) {
        this.sessionId = sessionId;
    }

    private UUID sessionId;

    // Trova tutti i tutor con cui lo studente ha avuto almeno una sessione
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

    // Trova tutti gli studenti con cui il tutor ha avuto almeno una session
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

    // Trova o crea una SharedReview per la coppia student-tutor
    public SharedReview findOrCreateSharedReview(String studentId, String tutorId) {
        // Cerco se esiste già
        List<SharedReview> studentReviews = sharedReviewDAO.loadForStudent(studentId);
        for (SharedReview sharedReview : studentReviews) {
            if (tutorId.equals(sharedReview.getTutorId())) {
                return sharedReview;
            }
        }
        // Altrimenti la creo
        SharedReview newSharedReview = new SharedReview(UUID.randomUUID().toString(), studentId, tutorId);
        newSharedReview.setStatus(NOT_STARTED);
        sharedReviewDAO.store(newSharedReview);
        return newSharedReview;
    }

    // Gestisce l'invio della recensione e aggiorna lo stato
    public SharedReviewBean submitReview(SharedReviewBean bean) {

        // Controlli sintattici offerti dal Bean
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

        beanToDomain(bean, sr);

        boolean completed = sr.isStudentSubmitted() && sr.isTutorSubmitted();
        sr.setStatus(completed ? COMPLETED : PENDING);
        sharedReviewDAO.store(sr);

        if (completed) {
            updateTutorRating(sr);
        }

        return toBean(sr);
    }

    // Calcola la media delle valutazioni ricevute da un tutor
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

    // Aggiorna il rating del tutor nel DAO
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

    // Aggiorna il rating di un tutor dopo una nuova recensione
    private void updateTutorRating(SharedReview sr) {
        float avg = computeAverageTutorRating(sr.getTutorId());
        assignRatingToTutorAccount(sr.getTutorId(), avg);
    }

    // Conta le recensioni pendenti per lo studente (per notifica)
    public int countPendingForStudent(String studentId) {
        int n = 0;
        for (SharedReview sr : sharedReviewDAO.loadForStudent(studentId)) {
            if (sr.getStatus() != COMPLETED && !sr.isStudentSubmitted()) n++;
        }
        return n;
    }

    // Conta le recensioni pendenti per il tutor (per notifica)
    public int countPendingForTutor(String tutorId) {
        int n = 0;
        for (SharedReview sr : sharedReviewDAO.loadForTutor(tutorId)) {
            if (sr.getStatus() != COMPLETED && !sr.isTutorSubmitted()) n++;
        }
        return n;
    }

    // Converte una SharedReview in SharedReviewBean
    private SharedReviewBean toBean(SharedReview sr) {

        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();

        // Determino se l’utente loggato è uno studente usando il metodo già presente
        boolean isStudent = isLoggedUserStudent();

        // Id della controparte: se sono studente --> tutorId, altrimenti studentId
        String counterpartId = isStudent ? sr.getTutorId() : sr.getStudentId();
        Account counterpartAcc = accDao.load(counterpartId);

        String info = buildNameAgeLabel(counterpartAcc);

        return domainToBean(sr, info);
    }

    // Costruisce stringa "Nome Cognome (età)" per la controparte
    private String buildNameAgeLabel(Account acc) {
        return acc == null ? "" : acc.getName() + " " + acc.getSurname() + " (" + acc.getAge() + ")";
    }

    // Carica le SharedReviewBean per uno studente
    public List<SharedReviewBean> loadBeansForStudent(String studentId) {
        List<SharedReviewBean> out = new ArrayList<>();
        for (SharedReview sr : sharedReviewDAO.loadForStudent(studentId)) {
            out.add(toBean(sr));
        }
        return out;
    }

    // Carica le SharedReviewBean per un tutor
    public List<SharedReviewBean> loadBeansForTutor(String tutorId) {
        List<SharedReviewBean> out = new ArrayList<>();
        for (SharedReview sr : sharedReviewDAO.loadForTutor(tutorId)) {
            out.add(toBean(sr));
        }
        return out;
    }

    // Trova o crea SharedReviewBean per la coppia student-tutor
    public SharedReviewBean findOrCreateSharedReviewBean(String studentId, String tutorId) {
        SharedReview sr = findOrCreateSharedReview(studentId, tutorId);

        boolean iAmStudent = isLoggedUserStudent();
        String counterpartId = iAmStudent ? tutorId : studentId;
        Account counterpartAcc = accountDAO.load(counterpartId);
        String label = buildNameAgeLabel(counterpartAcc);

        SharedReviewBean bean = domainToBean(sr, label);

        if (counterpartAcc != null) {
            bean.setCounterpartAccount(toAccountBean(counterpartAcc));
        }
        Account tutorAcc = accountDAO.load(sr.getTutorId());
        if (tutorAcc != null) {
            bean.setTutorAccount(toAccountBean(tutorAcc));
        }

        return bean;
    }

    // Verifica se l'utente loggato è uno studente
    private boolean isLoggedUserStudent() {

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

    // Converte SharedReview in SharedReviewBean
    private SharedReviewBean domainToBean(SharedReview sr, String counterpartInfo) {
        SharedReviewBean b = new SharedReviewBean();
        b.setReviewId(sr.getReviewId());
        b.setStudentId(sr.getStudentId());
        b.setTutorId(sr.getTutorId());
        b.setStudentStars(sr.getStudentStars());
        b.setStudentTitle(sr.getStudentTitle());
        b.setStudentComment(sr.getStudentComment());
        b.setStudentSubmitted(sr.isStudentSubmitted());
        b.setTutorTitle(sr.getTutorTitle());
        b.setTutorComment(sr.getTutorComment());
        b.setTutorSubmitted(sr.isTutorSubmitted());
        b.setStatus(SharedReviewBean.ReviewStatus.valueOf(sr.getStatus().name()));
        b.setCounterpartyInfo(counterpartInfo);
        return b;
    }

    // Converte SharedReviewBean in SharedReview
    private void beanToDomain(SharedReviewBean b, SharedReview sr) {
        if (b.getSenderRole() == SharedReviewBean.SenderRole.STUDENT) {
            sr.setStudentStars(b.getStudentStars());
            sr.setStudentTitle(b.getStudentTitle());
            sr.setStudentComment(b.getStudentComment());
            sr.setStudentSubmitted(true);
        } else {
            sr.setTutorTitle(b.getTutorTitle());
            sr.setTutorComment(b.getTutorComment());
            sr.setTutorSubmitted(true);
        }
        sr.setStatus(ReviewStatus.valueOf(b.getStatus().name()));
    }

    // Converte Account in AccountBean
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
    // permettere di scrivere una recensione condivisa. In questo caso
    // ho deciso di disattivarla per permettere un testing della funzionalità
    // senza dover aspettare

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

    // Restituisce il bean dell'utente loggato
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

    // Restituisce il ruolo dell'utente loggato
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

    // Restituisce accountId dell'utente loggato
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

    // Esegue il logout della sessione
    public void logout(UUID sessionId) {
        if (sessionId != null) {
            loginCtrl.logout(sessionId);
        }
    }
}
