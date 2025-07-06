package logic.control.graphiccontrol.bw;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import logic.bean.TutoringSessionBean;
import logic.bean.AccountBean;
import logic.control.logiccontrol.ManageNoticeBoardController;
import logic.model.domain.state.TutoringSessionStatus;

public class ManageNoticeBoardGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(ManageNoticeBoardGraphicControllerBW.class.getName());
    private static final String ROLE_TUTOR = "Tutor";
    private static final String LABEL_CHOICE = "Choice:";

    private ManageNoticeBoardController manageNoticeBoardController = new ManageNoticeBoardController();

    private UUID sessionId;

    public ManageNoticeBoardGraphicControllerBW(UUID sessionId) {
        this.sessionId = sessionId;
    }

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    private final ManageNoticeBoardController manageController = new ManageNoticeBoardController();

    public void start() {
        if (manageNoticeBoardController.getLoggedUser(sessionId) == null) {
            LOGGER.warning("Please log in first!");
            pressEnter();
            return;
        }

        String role = manageController.getLoggedRole();
        String userId = manageController.getLoggedAccountId();

        if (role == null || userId == null) {
            throw new IllegalStateException("No valid account (Student or Tutor) found for logged user.");
        }

        // Fissiamo role e userId come final (servono per non avere errori nella lambda expression)
        final String fixedRole = role;

        while (true) {
            List<TutoringSessionBean> sessions = manageController.loadSessionsForLoggedUser();
            LOGGER.info("\n=== MANAGE NOTICE BOARD ===");
            if (sessions.isEmpty()) {
                LOGGER.info("No tutoring sessions found.");
                pressEnter();
                return;
            }

            IntStream.range(0, sessions.size()).forEach(i -> {
                TutoringSessionBean s = sessions.get(i);
                String info = String.format("%2d) %s with %s [%s]",
                        i + 1,
                        s.getDate(),
                        ROLE_TUTOR.equalsIgnoreCase(fixedRole) ? s.getStudentId() : s.getTutorId(),
                        s.getStatus());
                LOGGER.info(info);
            });

            LOGGER.info("\nA) Accept / refuse **new** bookings");
            LOGGER.info("B) Request a modification or cancellation");
            LOGGER.info("C) Accept / refuse modification‑or‑cancellation requests");
            LOGGER.info("0) Back to Home");

            String input = ask("Choose an option:");
            if ("0".equals(input)) {
                return;
            }

            switch (input.toUpperCase()) {
                case "A" -> handlePendingBookings(sessions);
                case "B" -> handleModificationOrCancellation(sessions);
                case "C" -> handleIncomingModOrCancRequests(sessions);
                default -> handleDirectRowSelection(sessions, input);
            }
        }
    }

    private void handlePendingBookings(List<TutoringSessionBean> sessions) {
        List<TutoringSessionBean> pending = sessions.stream()
                .filter(s -> s.getStatus() == TutoringSessionStatus.PENDING)
                .toList();

        if (pending.isEmpty()) {
            LOGGER.info("\nNo pending bookings.");
            pressEnter();
            return;
        }

        LOGGER.info("\n=== Pending Bookings ===");
        printRows(pending);

        int idx = askInt("\nSelect a booking (0 = back):") - 1;
        if (idx < 0 || idx >= pending.size()) {
            return;
        }

        TutoringSessionBean sel = pending.get(idx);

        LOGGER.info("[1] Accept   [2] Refuse   [Enter] Back");
        String choice = ask(LABEL_CHOICE);
        if ("1".equals(choice)) {
            manageController.acceptSession(sel.getSessionId());
            LOGGER.info("Booking accepted.");
        } else if ("2".equals(choice)) {
            manageController.refuseSession(sel.getSessionId());
            LOGGER.info("Booking refused.");
        }
        pressEnter();
    }

    private void handleModificationOrCancellation(List<TutoringSessionBean> sessions) {
        int idx = askInt("Select an ACCEPTED session:") - 1;
        if (idx < 0 || idx >= sessions.size()) return;

        TutoringSessionBean sel = sessions.get(idx);
        if (sel.getStatus() != TutoringSessionStatus.ACCEPTED) {
            LOGGER.warning("Only ACCEPTED sessions can be modified or cancelled.");
            pressEnter();
            return;
        }

        int choice = askInt("[1] Request Modification  [2] Request Cancellation :");
        if (choice == 1) {
            var newDate = askDate("New date:");
            var newStart = askTime("New start time:");
            var newEnd = askTime("New end time:");
            String reason = ask("Reason for modification:");
            manageController.requestModification(sel, newDate, newStart, newEnd, reason);
            LOGGER.info("Modification request sent.");
        } else if (choice == 2) {
            String reason = ask("Reason for cancellation:");
            manageController.requestCancellation(sel, reason);
            LOGGER.info("Cancellation request sent.");
        }
        pressEnter();
    }

    private void handleIncomingModOrCancRequests(List<TutoringSessionBean> sessions) {

        String myId = manageController.getLoggedAccountId();

        if (myId == null) {
            throw new IllegalStateException("No valid Student or Tutor account found for logged user.");
        }

        final String finalMyId = myId;

        List<TutoringSessionBean> incoming = sessions.stream()
                .filter(s ->
                        (s.getStatus() == TutoringSessionStatus.MOD_REQUESTED ||
                                s.getStatus() == TutoringSessionStatus.CANCEL_REQUESTED)
                                && (finalMyId.equals(s.getModifiedTo()) || s.getModifiedTo() == null))
                .toList();

        if (incoming.isEmpty()) {
            LOGGER.info("\nNo incoming modification / cancellation requests.");
            pressEnter();
            return;
        }

        LOGGER.info("\n=== Incoming Requests ===");
        printRows(incoming);

        int idx = askInt("\nSelect a request (0 = back):") - 1;
        if (idx < 0 || idx >= incoming.size()) return;

        TutoringSessionBean selected = incoming.get(idx);

        if (ROLE_TUTOR.equalsIgnoreCase(manageController.getLoggedRole())) {
            LOGGER.info("[1] View Student Profile   [2] Continue to decision   [Enter] Back");
            String viewChoice = ask(LABEL_CHOICE);
            if ("1".equals(viewChoice)) {
                new StudentProfileGraphicControllerBW().show(selected.getStudentId());
                pressEnter();
                return;
            } else if (!"2".equals(viewChoice)) {
                return;
            }
        }

        manageModOrCancDecision(incoming.get(idx));
    }

    private void handleDirectRowSelection(List<TutoringSessionBean> all, String input) {
        int idx;
        try {
            idx = Integer.parseInt(input) - 1;
        } catch (Exception e) {
            return;
        }

        if (idx < 0 || idx >= all.size()) {
            return;
        }

        TutoringSessionBean sel = all.get(idx);
        if (sel.getStatus() == TutoringSessionStatus.MOD_REQUESTED || sel.getStatus() == TutoringSessionStatus.CANCEL_REQUESTED) {
            manageModOrCancDecision(sel);
        }
    }

    private void manageModOrCancDecision(TutoringSessionBean s) {

        if (s.getStatus() == TutoringSessionStatus.MOD_REQUESTED) {
            LOGGER.info("[1] Accept modification  [2] Refuse modification  [Enter] Back");
            String choice = ask(LABEL_CHOICE);
            if ("1".equals(choice)) {
                manageController.acceptModification(s);
                LOGGER.info("Modification accepted.");
            } else if ("2".equals(choice)) {
                manageController.refuseModification(s);
                LOGGER.info("Modification refused.");
            }
        } else if (s.getStatus() == TutoringSessionStatus.CANCEL_REQUESTED) {
            LOGGER.info("[1] Accept cancellation  [2] Refuse cancellation  [Enter] Back");
            String choice = ask(LABEL_CHOICE);
            if ("1".equals(choice)) {
                manageController.acceptCancellation(s);
                LOGGER.info("Cancellation accepted.");
            } else if ("2".equals(choice)) {
                manageController.refuseCancellation(s);
                LOGGER.info("Cancellation refused.");
            }
        }
        pressEnter();
    }

    private void printRows(List<TutoringSessionBean> list) {

        String role = null;

        for (AccountBean account : manageNoticeBoardController.getLoggedUser(sessionId).getAccounts()) {
            String r = account.getRole();

            if ("Student".equalsIgnoreCase(r) || ROLE_TUTOR.equalsIgnoreCase(r)) {
                role = r;
                break;
            }
        }

        if (role == null) {
            throw new IllegalStateException("No valid Student or Tutor account found for logged user.");
        }

        for (int i = 0; i < list.size(); i++) {
            TutoringSessionBean s = list.get(i);
            String other = ROLE_TUTOR.equalsIgnoreCase(role) ? s.getStudentId() : s.getTutorId();
            String info = String.format("%2d) %s with %s [%s]", i + 1, s.getDate(), other, s.getStatus());
            LOGGER.info(info);
        }
    }
}



