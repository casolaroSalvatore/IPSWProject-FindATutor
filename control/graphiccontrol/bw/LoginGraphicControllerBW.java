package logic.control.graphiccontrol.bw;

import logic.bean.AccountBean;
import logic.bean.AuthResultBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.LoginController;
import logic.exception.NoTutorFoundException;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(LoginGraphicControllerBW.class.getName());
    private final LoginController ctrl = new LoginController();

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    private final LoginController logic = new LoginController();

    public void start() throws NoTutorFoundException {

        LOGGER.info("\n=== LOGIN ===");
        String email    = ask("Email:");
        String password = ask("Password:");
        String role     = ask("Role (Student/Tutor):");

        UserBean userBean = new UserBean();
        userBean.setEmail(email);

        AccountBean acc = new AccountBean();
        acc.setRole(role);
        acc.setPassword(password);
        try {
            userBean.checkEmailSyntax();
            acc.checkPasswordSyntax();
        } catch (IllegalArgumentException ex) {
            LOGGER.warning(ex.getMessage());
            pressEnter();
            return;
        }
        userBean.addAccount(acc);

        AuthResultBean authResultBean = logic.login(userBean);

         if (authResultBean == null) {
            LOGGER.info("Incorrect credentials or role mismatch.");
            pressEnter();
            return;
         }

        UUID sid = authResultBean.getSessionId();
        LOGGER.info("Login successful!");
        pressEnter();
        HomeGraphicControllerBW homeGraphicControllerBW = new HomeGraphicControllerBW(sid);
        homeGraphicControllerBW.start();
    }

    private void showNotLoggedMenu() throws NoTutorFoundException {
        LOGGER.info("\n1) Log In\n2) Sign Up\n0) Exit");
        switch (askInt("> ")) {
            case 1 -> start();
            case 2 -> new SignUpGraphicControllerBW().start();
            case 0 -> System.exit(0);
            default -> LOGGER.warning("Scelta non valida");
        }
    }

    private void showLoggedMenu(UUID sid) throws NoTutorFoundException {
        UserBean ub = rebuildBean(sid);
        if (ub == null) {
            showNotLoggedMenu();
            return;
        }

        LOGGER.info("\nBenvenuto " + ub.getUsername());
        LOGGER.info("1) Profilo\n2) Logout\n0) Exit");
        switch (askInt("> ")) {
            case 1 -> {
                printProfile(ub);
                showLoggedMenu(sid);
            }
            case 2 -> {
                // Logout solo via controller di logica
                ctrl.logout(sid);
                LOGGER.info("Logout effettuato.");
                showNotLoggedMenu();
            }
            case 0 -> System.exit(0);
            default -> LOGGER.warning("Scelta non valida");
        }
    }

    private UserBean rebuildBean(UUID sid) {
        logic.model.domain.User u = ctrl.getUserFromSession(sid);
        if (u == null) return null;
        UserBean b = new UserBean();
        b.setEmail(u.getEmail());
        b.setUsername(u.getUsername());
        return b;
    }

    private void printProfile(UserBean ub) {
        LOGGER.info("\nUSERNAME : " + ub.getUsername());
        LOGGER.info("E-MAIL   : " + ub.getEmail());
        ask("\nPremi Invio …");
    }
}
