package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.SharedReviewBean;
import logic.bean.UserBean;
import logic.model.dao.*;
import logic.model.domain.*;
import logic.model.domain.state.TutoringSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static logic.model.domain.ReviewStatus.NOT_STARTED;
import static logic.model.domain.ReviewStatus.PENDING;

public class LeaveASharedReviewController {

    private static final ReviewStatus COMPLETED = ReviewStatus.COMPLETE;

    private SharedReviewDAO sharedReviewDAO = DaoFactory.getInstance().getSharedReviewDAO();
    private TutoringSessionDAO tutoringSessionDAO = DaoFactory.getInstance().getTutoringSessionDAO();
    private AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();

    // Carica i tutor con cui lo studente ha almeno una sessione
    public List<String> findAllTutorsForStudent(String studentId) {
        List<TutoringSession> all = tutoringSessionDAO.loadAllTutoringSession();
        Set<String> tutorIds = new HashSet<>();
        for (TutoringSession tutoringSession : all) {
            if (studentId.equals(tutoringSession.getStudentId())) {
                tutorIds.add(tutoringSession.getTutorId());
            }
        }
        return new ArrayList<>(tutorIds);
    }

    // Interrogo TutoringSessionDAO per trovare tutti gli student con cui il tutor ha effettuato dei tutoraggi
    public List<String> findAllStudentsForTutor(String tutorId) {
        List<TutoringSession> all = tutoringSessionDAO.loadAllTutoringSession();
        Set<String> studentIds = new HashSet<>();
        for (TutoringSession tutoringSession : all) {
            if (tutorId.equals(tutoringSession.getTutorId())) {
                studentIds.add(tutoringSession.getStudentId());
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
        SharedReview newSharedReview = new SharedReview(null, studentId, tutorId);
        newSharedReview.setStatus(NOT_STARTED);
        sharedReviewDAO.store(newSharedReview);
        return newSharedReview;
    }

    /* Lo studente invia la propria parte di recensione
    public void studentSubmitReview(String reviewId, int stars, String title, String comment) {
        SharedReview sharedReview = sharedReviewDAO.load(reviewId);
        if (sharedReview == null) return;

        sharedReview.setStudentStars(stars);
        sharedReview.setStudentTitle(title);
        sharedReview.setStudentComment(comment);
        sharedReview.setStudentSubmitted(true);

        boolean completed = sharedReview.isTutorSubmitted();
        sharedReview.setStatus(completed ? COMPLETED : PENDING);

        // 1) Persisto subito lo stato aggiornato
        sharedReviewDAO.store(sharedReview);
        // 2) Se la review è completa ricalcolo la media
        if (completed) {
            float newRating = computeAverageTutorRating(sharedReview.getTutorId());
            assignRatingToTutorAccount(sharedReview.getTutorId(), newRating);
        }
    }

    // Il tutor invia la propria parte di recensione
    public void tutorSubmitReview(String reviewId, String title, String comment) {
        SharedReview sharedReview = sharedReviewDAO.load(reviewId);
        if (sharedReview == null) return;

        sharedReview.setTutorTitle(title);
        sharedReview.setTutorComment(comment);
        sharedReview.setTutorSubmitted(true);

        // Se anche lo studente ha inviato => COMPLETA
        boolean completed = sharedReview.isStudentSubmitted();
        sharedReview.setStatus(completed ? COMPLETED : PENDING);

        // 1) Persisto subito lo stato aggiornato
        sharedReviewDAO.store(sharedReview);
        if (completed) {
            // 2) Se la review è completa ricalcolo la media
            float newRating = computeAverageTutorRating(sharedReview.getTutorId());
            assignRatingToTutorAccount(sharedReview.getTutorId(), newRating);
        }
    } */

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
        int sum = 0, count = 0;
        for (SharedReview sr : sharedReviewDAO.loadForTutor(tutorId)) {
            if (sr.getStatus() == COMPLETED) {
                sum += sr.getStudentStars();
                count++;
            }
        }
        return count == 0 ? 0f : (float) sum / count;
    }

    private void assignRatingToTutorAccount(String tutorKeyOrId, float newRating) {
        // System.out.println("DEBUG: assignRatingToTutorAccount() called with " + tutorKeyOrId + " -> newRating=" + newRating);

        AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();
        Account account = accountDAO.load(tutorKeyOrId); // Proviamo con la chiave diretta
        if (account == null) {
            for (Account acc : accountDAO.loadAllAccountsOfType("Tutor")) {
                if (acc.getAccountId().equals(tutorKeyOrId)) {
                    account = acc;
                    break;
                }
            }

            if (account instanceof Tutor t) {
                tutorKeyOrId = t.getEmail() + "_" + t.getRole();
                account = accountDAO.load(tutorKeyOrId);
            }
        }

        if (account instanceof Tutor tutorAcc) {
            // System.out.println("DEBUG: old rating = " + tutorAcc.getRating());
            tutorAcc.setRating(newRating);
            // System.out.println("DEBUG: new rating set to " + newRating);
            accountDAO.store(tutorAcc);
            // System.out.println("DEBUG: stored " + tutorAcc.getEmail() + "_" + tutorAcc.getRole());
        } else {
            // System.out.println("WARNING: Tutor account not found for key: " + tutorKeyOrId);
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

    // Factory che restituisce la lista controparti già impacchettata in bean
    public List<String> loadCounterparts(String userId, SharedReviewBean.SenderRole role) {
        return (role == SharedReviewBean.SenderRole.STUDENT)
                ? findAllTutorsForStudent(userId)
                : findAllStudentsForTutor(userId);
    }

    // Conversioni Bean <--> Entity
    private SharedReviewBean toBean(SharedReview sr) {

        AccountDAO accDao = DaoFactory.getInstance().getAccountDAO();

        boolean isStudent = false;

        // Controllo direttamente se l'utente è uno Studente
        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                isStudent = true;
                break;
            }
        }

        String info = buildNameAgeLabel(
                isStudent ? accDao.load(sr.getTutorId()) : accDao.load(sr.getStudentId())
        );

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

        boolean isStudent = false;
        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                isStudent = true;
                break;
            }
        }

        Account counterpartAccount;
        if (isStudent) {
            counterpartAccount = accountDAO.load(tutorId);    // lo Studente guarda il Tutor
        } else {
            counterpartAccount = accountDAO.load(studentId);  // il Tutor guarda lo Studente
        }

        SharedReviewBean bean = new SharedReviewBean(sr, buildNameAgeLabel(counterpartAccount));

        if (counterpartAccount != null) {
            AccountBean accBean = new AccountBean();
            accBean.setAccountId(counterpartAccount.getAccountId());
            accBean.setName(counterpartAccount.getName());
            accBean.setSurname(counterpartAccount.getSurname());
            accBean.setBirthday(counterpartAccount.getBirthday());
            accBean.setRole(counterpartAccount.getRole());
            accBean.setLocation(counterpartAccount instanceof Tutor t ? t.getLocation() : null);
            accBean.setSubject(counterpartAccount instanceof Tutor t ? t.getSubject() : null);
            accBean.setProfilePicturePath(counterpartAccount.getProfilePicturePath());
            accBean.setProfileComment(counterpartAccount.getProfileComment());

            bean.setCounterpartAccount(accBean);

            // Carico qui sempre il tutor, indipendentemente da chi guarda
            Account tutorAcc = accountDAO.load(sr.getTutorId());
            if (tutorAcc != null) {
                AccountBean tutorBean = new AccountBean();
                tutorBean.setAccountId(tutorAcc.getAccountId());
                tutorBean.setName(tutorAcc.getName());
                tutorBean.setSurname(tutorAcc.getSurname());
                tutorBean.setBirthday(tutorAcc.getBirthday());
                tutorBean.setRole(tutorAcc.getRole());
                tutorBean.setSubject(tutorAcc instanceof Tutor t ? t.getSubject() : null);
                tutorBean.setLocation(tutorAcc instanceof Tutor t ? t.getLocation() : null);
                tutorBean.setProfilePicturePath(tutorAcc.getProfilePicturePath());
                tutorBean.setProfileComment(tutorAcc.getProfileComment());
                bean.setTutorAccount(tutorBean);
            }
        }
        return bean;
    }

    public String getLoggedRole() {
        return SessionManager.getLoggedUser().getAccounts().stream()
                .map(AccountBean::getRole)
                .filter(r -> "Tutor".equalsIgnoreCase(r) || "Student".equalsIgnoreCase(r))
                .findFirst().orElse(null);
    }
    public String getLoggedAccountId() {
        return SessionManager.getLoggedUser().getAccounts().stream()
                .filter(a -> "Tutor".equalsIgnoreCase(a.getRole()) || "Student".equalsIgnoreCase(a.getRole()))
                .map(AccountBean::getAccountId)
                .findFirst().orElse(null);
    }

    // Interazioni dirette con il SessionManager, in maniera tale che il controller grafico non lo conosca
    public UserBean getLoggedUser() {
        return SessionManager.getLoggedUser();
    }

    public void logout() {
        SessionManager.logout();
    }
}
