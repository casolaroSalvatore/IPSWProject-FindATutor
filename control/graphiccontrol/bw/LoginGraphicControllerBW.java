package logic.control.graphiccontrol.bw;

import logic.bean.AccountBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.LoginController;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(LoginGraphicControllerBW.class.getName());

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    private final LoginController logic = new LoginController();

    public void start() {

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

        UserBean loggedUser = logic.login(userBean);

         if (loggedUser == null) {
            LOGGER.warning("Incorrect credentials or role mismatch.");
            pressEnter();
            return;
         }

        LOGGER.info("Login successful!");
        pressEnter();
    }
}
