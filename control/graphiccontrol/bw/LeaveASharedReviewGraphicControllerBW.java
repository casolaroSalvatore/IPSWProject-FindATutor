package logic.control.graphiccontrol.bw;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import logic.bean.AccountBean;
import logic.bean.SharedReviewBean;
import logic.control.logiccontrol.LeaveASharedReviewController;
import logic.model.domain.SessionManager;
import logic.model.domain.SharedReview;

public class LeaveASharedReviewGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(LeaveASharedReviewGraphicControllerBW.class.getName());

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    private final LeaveASharedReviewController logic = new LeaveASharedReviewController();

    public void start() {
        if (SessionManager.getLoggedUser() == null) {
            LOGGER.info("Please log in first!");
            pressEnter();
            return;
        }

        String role = null;
        String userId = null;

        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                role = "Student";
                userId = account.getAccountId();
                break;
            } else if ("Tutor".equalsIgnoreCase(account.getRole())) {
                role = "Tutor";
                userId = account.getAccountId();
                break;
            }
        }

        if (role == null || userId == null) {
            throw new IllegalStateException("No valid account (Student or Tutor) found for logged user.");
        }


        final boolean isStudent = "Student".equalsIgnoreCase(role);
        final String finalUserId = userId;

        List<SharedReview> reviews = isStudent
                ? logic.findAllTutorsForStudent(finalUserId).stream()
                .map(tid -> logic.findOrCreateSharedReview(finalUserId, tid))
                .toList()
                : logic.findAllStudentsForTutor(finalUserId).stream()
                .map(sid -> logic.findOrCreateSharedReview(sid, finalUserId))
                .toList();

        while (true) {
            LOGGER.info("\n=== SHARED REVIEWS ===");
            IntStream.range(0, reviews.size()).forEach(i -> {
                SharedReview sr = reviews.get(i);
                String info = String.format("%2d) With %s [%s]",
                        i + 1,
                        isStudent ? sr.getTutorId() : sr.getStudentId(),
                        sr.getStatus());
                LOGGER.info(info);
            });

            LOGGER.info("0) Back");
            int idx = askInt("Select a review:") - 1;
            if (idx < 0) {
                return;
            }

            SharedReview selected = reviews.get(idx);

            if (isStudent) {
                handleStudentSide(selected);
            } else {
                handleTutorSide(selected);
            }
        }
    }

    private void handleStudentSide(SharedReview sr) {
        if (sr.isStudentSubmitted()) {
            LOGGER.log(Level.INFO, "You already submitted your part. Status: {0}", sr.getStatus());
            pressEnter();
            return;
        }

        int stars = askInt("Stars (1-5):");
        String title = ask("Title:");
        String comment = ask("Comment:");

        // Creo il nuovo bean
        SharedReviewBean bean = new SharedReviewBean();
        bean.setReviewId(sr.getReviewId());
        bean.setStudentId(SessionManager.getLoggedUser().getAccounts().stream()
                .filter(a -> "Student".equalsIgnoreCase(a.getRole()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Student account not found"))
                .getAccountId());
        bean.setStudentStars(stars);
        bean.setStudentTitle(title);
        bean.setStudentComment(comment);
        bean.setSenderRole(SharedReviewBean.SenderRole.STUDENT);

        logic.submitReview(bean);

        LOGGER.info("Student review submitted!");
        pressEnter();
    }


    private void handleTutorSide(SharedReview sr) {
        if (sr.isTutorSubmitted()) {
            LOGGER.log(Level.INFO, "You already submitted your part. Status: {0}", sr.getStatus());
            pressEnter();
            return;
        }

        String title = ask("Title:");
        String comment = ask("Comment:");

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


