package logic.control.graphiccontrol.bw;

import logic.bean.AccountBean;
import logic.bean.AuthResultBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.LoginController;

import java.util.logging.Level;
import java.util.logging.Logger;

// Controller BW per la gestione del login e delle funzionalità collegate (profilo, logout).
public class LoginGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(LoginGraphicControllerBW.class.getName());
    private final LoginController logic = new LoginController();

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    public AuthResultBean start() {
        LOGGER.info("\n=== LOGIN ===");
        String email = ask("Email:");
        String password = ask("Password:");
        String role = ask("Role (Student/Tutor):");

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
            return null;
        }

        userBean.addAccount(acc);
        AuthResultBean result = logic.login(userBean);

        if (result == null) {
            LOGGER.info("Incorrect credentials or role mismatch.");
            pressEnter();
            return null;
        }

        LOGGER.info("Login successful!");
        pressEnter();
        return result;
    }
}

