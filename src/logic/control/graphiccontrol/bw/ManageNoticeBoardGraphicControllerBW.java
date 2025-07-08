package logic.control.graphiccontrol.bw;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import logic.bean.TutoringSessionBean;
import logic.bean.AccountBean;
import logic.control.logiccontrol.ManageNoticeBoardController;
import logic.bean.TutoringSessionBean.TutoringSessionStatusBean;

// Controller BW per la gestione della bacheca delle prenotazioni e delle richieste associate.
public class ManageNoticeBoardGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(ManageNoticeBoardGraphicControllerBW.class.getName());
    private static final String ROLE_TUTOR   = "Tutor";
    private static final String LABEL_CHOICE = "Choice:";

    private final ManageNoticeBoardController manageNoticeBoardController;
    private final UUID sessionId;

    public ManageNoticeBoardGraphicControllerBW(UUID sessionId) {
        this.sessionId = sessionId;
        this.manageNoticeBoardController = new ManageNoticeBoardController(sessionId);
    }

    /* ---------- logger CLI ---------- */
    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    public void start() {

        if (manageNoticeBoardController.getLoggedUser(sessionId) == null) {
            LOGGER.warning("Please log in first!");
            pressEnter();
            return;
        }

        String role = manageNoticeBoardController.getLoggedRole();
        String userId = manageNoticeBoardController.getLoggedAccountId();
        if (role == null || userId == null) {
            throw new IllegalStateException("No valid account (Student or Tutor) found for logged user.");
        }

        final String fixedRole = role;

        while (true) {
            List<TutoringSessionBean> sessions = manageNoticeBoardController.loadSessionsForLoggedUser();

            if (!displaySessionList(sessions, fixedRole)) return;

            printMainMenu();
            String input = ask("Choose an option:");

            if ("0".equals(input)) return;
            handleUserChoice(input.toUpperCase(), sessions, fixedRole);
        }
    }

    private boolean displaySessionList(List<TutoringSessionBean> sessions, String fixedRole) {
        LOGGER.info("\n=== MANAGE NOTICE BOARD ===");
        if (sessions.isEmpty()) {
            LOGGER.info("No tutoring sessions found.");
            pressEnter();
            return false;
        }

        for (int i = 0; i < sessions.size(); i++) {
            TutoringSessionBean s = sessions.get(i);
            String otherId = ROLE_TUTOR.equalsIgnoreCase(fixedRole) ? s.getStudentId() : s.getTutorId();
            String other = manageNoticeBoardController.counterpartLabel(otherId);
            String start = (s.getStartTime() != null) ? s.getStartTime().toString() : "??";
            String end = (s.getEndTime() != null) ? s.getEndTime().toString() : "??";
            String timeInfo = start + "–" + end;

            String info = String.format("%2d) %s %s with %s [%s]",
                    i + 1,
                    s.getDate(),
                    timeInfo,
                    other,
                    s.getStatus());
            LOGGER.info(info);
        }

        return true;
    }

    private void printMainMenu() {
        LOGGER.info("\nA) Accept / refuse **new** bookings");
        LOGGER.info("B) Request a modification or cancellation");
        LOGGER.info("C) Accept / refuse modification-or-cancellation requests");
        LOGGER.info("0) Back to Home");
    }

    private void handleUserChoice(String input, List<TutoringSessionBean> sessions, String fixedRole) {
        switch (input) {
            case "A" -> {
                if (ROLE_TUTOR.equalsIgnoreCase(fixedRole)) {
                    handlePendingBookings(sessions);
                } else {
                    LOGGER.warning("Only tutors can accept or refuse new bookings.");
                    pressEnter();
                }
            }
            case "B" -> handleModificationOrCancellation(sessions);
            case "C" -> handleIncomingModOrCancRequests(sessions);
            default -> handleDirectRowSelection(sessions, input);
        }
    }

    /* ---------- A) PENDING BOOKINGS ---------- */
    private void handlePendingBookings(List<TutoringSessionBean> all) {

        List<TutoringSessionBean> pending = new ArrayList<>();
        for (TutoringSessionBean s : all) {
            if (s.getStatus() == TutoringSessionStatusBean.PENDING) pending.add(s);
        }

        if (pending.isEmpty()) {
            LOGGER.info("\nNo pending bookings.");
            pressEnter();
            return;
        }

        LOGGER.info("\n=== Pending Bookings ===");
        printRows(pending);

        int idx = askInt("\nSelect a booking (0 = back):") - 1;
        if (idx < 0 || idx >= pending.size()) return;

        TutoringSessionBean sel = pending.get(idx);

        LOGGER.info("[1] Accept   [2] Refuse   [Enter] Back");
        String choice = ask(LABEL_CHOICE);
        if ("1".equals(choice)) {
            manageNoticeBoardController.acceptSession(sel.getSessionId());
            LOGGER.info("Booking accepted.");
        } else if ("2".equals(choice)) {
            manageNoticeBoardController.refuseSession(sel.getSessionId());
            LOGGER.info("Booking refused.");
        }
        pressEnter();
    }

    /* ---------- B) RICHIESTA MOD / CANC ---------- */
    private void handleModificationOrCancellation(List<TutoringSessionBean> sessions) {

        int idx = askInt("Select an ACCEPTED session:") - 1;
        if (idx < 0 || idx >= sessions.size()) return;

        TutoringSessionBean sel = sessions.get(idx);
        if (sel.getStatus() != TutoringSessionStatusBean.ACCEPTED) {
            LOGGER.warning("Only ACCEPTED sessions can be modified or cancelled.");
            pressEnter();
            return;
        }

        int choice = askInt("[1] Request Modification  [2] Request Cancellation :");
        if (choice == 1) {
            var newDate  = askDate("New date:");
            var newStart = askTime("New start time:");
            var newEnd   = askTime("New end time:");
            String reason = ask("Reason for modification:");
            manageNoticeBoardController.requestModification(sel, newDate, newStart, newEnd, reason);
            LOGGER.info("Modification request sent.");
        } else if (choice == 2) {
            String reason = ask("Reason for cancellation:");
            manageNoticeBoardController.requestCancellation(sel, reason);
            LOGGER.info("Cancellation request sent.");
        }
        pressEnter();
    }

    /* ---------- C) GESTIONE RICHIESTE IN ARRIVO ---------- */
    private void handleIncomingModOrCancRequests(List<TutoringSessionBean> sessions) {

        String myId = manageNoticeBoardController.getLoggedAccountId();
        if (myId == null) {
            throw new IllegalStateException("No valid Student or Tutor account found for logged user.");
        }

        final String finalMyId = myId;

        List<TutoringSessionBean> incoming = new ArrayList<>();
        for (TutoringSessionBean s : sessions) {
            boolean isModOrCanc = s.getStatus() == TutoringSessionStatusBean.MOD_REQUESTED
                    || s.getStatus() == TutoringSessionStatusBean.CANCEL_REQUESTED;
            boolean isForMe = finalMyId.equals(s.getModifiedTo()) || s.getModifiedTo() == null;
            if (isModOrCanc && isForMe) {
                incoming.add(s);
            }
        }

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

        if (ROLE_TUTOR.equalsIgnoreCase(manageNoticeBoardController.getLoggedRole())) {
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

        manageModOrCancDecision(selected);
    }

    private void manageModOrCancDecision(TutoringSessionBean s) {
        if (s.getStatus() == TutoringSessionStatusBean.MOD_REQUESTED) {
            LOGGER.info("[1] Accept modification  [2] Refuse modification  [Enter] Back");
            String choice = ask(LABEL_CHOICE);
            if ("1".equals(choice)) {
                manageNoticeBoardController.acceptModification(s);
                LOGGER.info("Modification accepted.");
            } else if ("2".equals(choice)) {
                manageNoticeBoardController.refuseModification(s);
                LOGGER.info("Modification refused.");
            }
        } else if (s.getStatus() == TutoringSessionStatusBean.CANCEL_REQUESTED) {
            LOGGER.info("[1] Accept cancellation  [2] Refuse cancellation  [Enter] Back");
            String choice = ask(LABEL_CHOICE);
            if ("1".equals(choice)) {
                manageNoticeBoardController.acceptCancellation(s);
                LOGGER.info("Cancellation accepted.");
            } else if ("2".equals(choice)) {
                manageNoticeBoardController.refuseCancellation(s);
                LOGGER.info("Cancellation refused.");
            }
        }
        pressEnter();
    }

    /* ---------- SELEZIONE DIRETTA ---------- */
    private void handleDirectRowSelection(List<TutoringSessionBean> all, String input) {
        int idx;
        try {
            idx = Integer.parseInt(input) - 1;
        } catch (Exception e) {
            return;
        }
        if (idx < 0 || idx >= all.size()) return;

        TutoringSessionBean sel = all.get(idx);
        if (sel.getStatus() == TutoringSessionStatusBean.MOD_REQUESTED ||
                sel.getStatus() == TutoringSessionStatusBean.CANCEL_REQUESTED) {
            manageModOrCancDecision(sel);
        }
    }

    /* ---------- PRINT UTILITY ---------- */
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
            String otherId = ROLE_TUTOR.equalsIgnoreCase(role) ? s.getStudentId() : s.getTutorId();
            String other   = manageNoticeBoardController.counterpartLabel(otherId);

            String start = (s.getStartTime() != null) ? s.getStartTime().toString() : "??";
            String end   = (s.getEndTime() != null)   ? s.getEndTime().toString()   : "??";
            String timeInfo = start + "–" + end;

            String info = String.format("%2d) %s %s with %s [%s]",
                    i + 1,
                    s.getDate(),
                    timeInfo,
                    other,
                    s.getStatus());
            LOGGER.info(info);
        }
    }

}
