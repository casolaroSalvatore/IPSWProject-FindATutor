package logic.control.graphiccontrol.bw;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import logic.bean.AccountBean;
import logic.bean.SharedReviewBean;
import logic.control.logiccontrol.LeaveASharedReviewController;

// Controller BW per la gestione e l'invio delle shared reviews (recensioni condivise).
public class LeaveASharedReviewGraphicControllerBW extends BaseCLIControllerBW {

    private UUID sessionId;

    public LeaveASharedReviewGraphicControllerBW(UUID sessionId) {
        this.sessionId = sessionId;
    }

    private static final Logger LOGGER = Logger.getLogger(LeaveASharedReviewGraphicControllerBW.class.getName());
    private static final String ROLE_STUDENT = "Student";

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    private final LeaveASharedReviewController logic = new LeaveASharedReviewController();

    // Avvia il flusso principale della gestione delle recensioni condivise
    public void start() {

        if (logic.getLoggedUser(sessionId) == null) {
            LOGGER.info("Please log in first!");
            pressEnter();
            return;
        }

        String role = null;
        String userId = null;

        for (AccountBean account : logic.getLoggedUser(sessionId).getAccounts()) {
            String r = account.getRole();

            if (ROLE_STUDENT.equalsIgnoreCase(r) || "Tutor".equalsIgnoreCase(r)) {
                role = r;
                userId = account.getAccountId();
                break;
            }
        }

        if (role == null || userId == null) {
            throw new IllegalStateException("No valid account (Student or Tutor) found for logged user.");
        }

        // Carica le recensioni in base al ruolo dell'utente
        final boolean isStudent = ROLE_STUDENT.equalsIgnoreCase(role);
        final String finalUserId = userId;

        List<SharedReviewBean> reviews = isStudent
                ? logic.loadBeansForStudent(finalUserId)
                : logic.loadBeansForTutor(finalUserId);

        while (true) {
            LOGGER.info("\n=== SHARED REVIEWS ===");
            IntStream.range(0, reviews.size()).forEach(i -> {
                SharedReviewBean sr = reviews.get(i);
                String info = String.format("%2d) With %s [%s]",
                        i + 1, sr.getCounterpartyInfo(), sr.getStatus());
                LOGGER.info(info);
            });

            LOGGER.info("0) Back");
            int idx = askInt("Select a review:") - 1;
            if (idx < 0) {
                return;
            }

            SharedReviewBean selected = reviews.get(idx);      // MOD
            if (isStudent) {
                handleStudentSide(selected);
            } else {
                handleTutorSide(selected);
            }
        }
    }

    // Gestisce l'invio della recensione lato studente
    private void handleStudentSide(SharedReviewBean sr) {
        if (sr.isStudentSubmitted()) {
            LOGGER.log(Level.INFO, "You already submitted your part. Status: {0}", sr.getStatus());
            pressEnter();
            return;
        }

        // Raccoglie i dati della recensione
        int stars = askInt("Stars (1-5):");
        String title = ask("Title:");
        String comment = ask("Comment:");

        // Creo il nuovo bean da inviare
        SharedReviewBean bean = new SharedReviewBean();
        bean.setReviewId(sr.getReviewId());

        String studentId = null;
        for (AccountBean account : logic.getLoggedUser(sessionId).getAccounts()) {
            if (ROLE_STUDENT.equalsIgnoreCase(account.getRole())) {
                studentId = account.getAccountId();
                break;
            }
        }


        if (studentId == null) {
            throw new IllegalStateException("Student account not found");
        }

        bean.setStudentId(studentId);
        bean.setStudentStars(stars);
        bean.setStudentTitle(title);
        bean.setStudentComment(comment);
        bean.setSenderRole(SharedReviewBean.SenderRole.STUDENT);

        logic.submitReview(bean);

        LOGGER.info("Student review submitted!");
        pressEnter();
    }


    // Gestisce l'invio della recensione lato tutor
    private void handleTutorSide(SharedReviewBean sr) {

        if (sr.isTutorSubmitted()) {
            LOGGER.log(Level.INFO, "You already submitted your part. Status: {0}", sr.getStatus());
            pressEnter();
            return;
        }

        String title = ask("Title:");
        String comment = ask("Comment:");

        // Creo il bean da inviare
        SharedReviewBean bean = new SharedReviewBean();
        bean.setReviewId(sr.getReviewId());
        bean.setSenderRole(SharedReviewBean.SenderRole.TUTOR);
        bean.setTutorTitle(title);
        bean.setTutorComment(comment);

        logic.submitReview(bean);

        LOGGER.info("Tutor review submitted!");
        pressEnter();
    }

}


