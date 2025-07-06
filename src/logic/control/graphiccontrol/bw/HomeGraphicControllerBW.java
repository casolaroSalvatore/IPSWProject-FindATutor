package logic.control.graphiccontrol.bw;

import logic.bean.AccountBean;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.bean.UserBean;
import logic.control.logiccontrol.HomeController;
import logic.exception.NoTutorFoundException;

public class HomeGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(HomeGraphicControllerBW.class.getName());
    private final HomeController homeController = new HomeController();
    private UUID sessionId;

    public HomeGraphicControllerBW() {}

    public HomeGraphicControllerBW(UUID sid) {
        this.sessionId = sid;
    }

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    public void start() throws NoTutorFoundException {
        boolean running = true;

        while (running) {
            LOGGER.info("\n=== HOME ===");

            UserBean user = homeController.getLoggedUser(sessionId);
            if (user == null) {
                LOGGER.warning("Session expired – returning to Login.");
                running = showNotLoggedMenu();
            } else {
                running = showLoggedMenu(user);
            }
        }

        LOGGER.info("Application terminated.");
    }

    private boolean showNotLoggedMenu() throws NoTutorFoundException {
        LOGGER.info("1) Log In");
        LOGGER.info("2) Sign Up");
        LOGGER.info("0) Exit");

        switch (askInt("Choice:")) {
            case 1 -> new LoginGraphicControllerBW().start();
            case 2 -> new SignUpGraphicControllerBW().start();
            case 0 -> {
                LOGGER.info("Exiting application.");
                return false;  // ferma il ciclo
            }
            default -> LOGGER.warning("Invalid choice. Please try again.");
        }
        return true;
    }

    private boolean showLoggedMenu(UserBean user) throws NoTutorFoundException {
        String role = getLoggedRole(user);

        LOGGER.log(Level.INFO, "Logged in as: {0} ({1})",
                new Object[]{user.getUsername(), role});

        LOGGER.info("1) Book a Tutoring Session");
        LOGGER.info("2) Manage Notice Board");
        LOGGER.info("3) Leave/Manage Shared Reviews");
        LOGGER.info("4) Log Out");
        LOGGER.info("0) Exit");

        switch (askInt("Choice:")) {
            case 1 -> new BookingSessionGraphicControllerBW(sessionId).start();
            case 2 -> new ManageNoticeBoardGraphicControllerBW(sessionId).start();
            case 3 -> new LeaveASharedReviewGraphicControllerBW(sessionId).start();
            case 4 -> {
                homeController.logout(sessionId);
                LOGGER.info("Logged out successfully.");
            }
            case 0 -> {
                LOGGER.info("Exiting application.");
                return false;
            }
            default -> LOGGER.warning("Invalid choice. Please try again.");
        }
        return true;
    }

    private String getLoggedRole(UserBean user) {
        for (AccountBean a : user.getAccounts()) {
            String r = a.getRole();
            if ("Student".equalsIgnoreCase(r) || "Tutor".equalsIgnoreCase(r)) return r;
        }
        return null;
    }
}