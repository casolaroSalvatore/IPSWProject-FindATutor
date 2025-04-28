package logic.control.graphiccontrol.bw;

import logic.bean.AccountBean;
import logic.model.domain.SessionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(HomeGraphicControllerBW.class.getName());

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    public void start() {
        while (true) {
            LOGGER.info("\n=== HOME ===");

            if (SessionManager.getLoggedUser() == null) {
                LOGGER.info("1) Log In");
                LOGGER.info("2) Sign Up");
                LOGGER.info("0) Exit");

                switch (askInt("Choice:")) {
                    case 1 -> new LoginGraphicControllerBW().start();
                    case 2 -> new SignUpGraphicControllerBW().start();
                    case 0 -> { return; }
                    default -> LOGGER.warning("Invalid choice. Please try again.");
                }

            } else {
                String role = null;

                // Ciclo tra gli account e scelgo il ruolo corretto
                for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
                    if ("Student".equalsIgnoreCase(account.getRole())) {
                        role = "Student";
                        break;
                    } else if ("Tutor".equalsIgnoreCase(account.getRole())) {
                        role = "Tutor";
                        break;
                    }
                }

                LOGGER.log(Level.INFO, "Logged in as: {0} ({1})",
                        new Object[]{SessionManager.getLoggedUser().getUsername(),
                                role});

                LOGGER.info("1) Book a Tutoring Session");
                LOGGER.info("2) Manage Notice Board");
                LOGGER.info("3) Leave/Manage Shared Reviews");
                LOGGER.info("4) Log Out");
                LOGGER.info("0) Exit");

                switch (askInt("Choice:")) {
                    case 1 -> new BookingSessionGraphicControllerBW().start();
                    case 2 -> new ManageNoticeBoardGraphicControllerBW().start();
                    case 3 -> new LeaveASharedReviewGraphicControllerBW().start();
                    case 4 -> {
                        SessionManager.logout();
                        LOGGER.info("Logged out successfully.");
                    }
                    case 0 -> { return; }
                    default -> LOGGER.warning("Invalid choice. Please try again.");
                }
            }
        }
    }
}

