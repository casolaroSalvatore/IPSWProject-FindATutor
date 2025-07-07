package logic.control.graphiccontrol.bw;

import logic.bean.AccountBean;
import logic.bean.AuthResultBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.LoginController;
import logic.exception.NoTutorFoundException;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

// Controller BW per la gestione del login e delle funzionalità collegate (profilo, logout).
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

    // Avvia il processo di login
    public void start() throws NoTutorFoundException {

        LOGGER.info("\n=== LOGIN ===");
        String email = ask("Email:");
        String password = ask("Password:");
        String role = ask("Role (Student/Tutor):");

        UserBean userBean = new UserBean();
        userBean.setEmail(email);

        AccountBean acc = new AccountBean();
        acc.setRole(role);
        acc.setPassword(password);

        // Verifica sintassi email e password
        try {
            userBean.checkEmailSyntax();
            acc.checkPasswordSyntax();
        } catch (IllegalArgumentException ex) {
            LOGGER.warning(ex.getMessage());
            pressEnter();
            return;
        }
        userBean.addAccount(acc);

        // Tenta il login
        AuthResultBean authResultBean = logic.login(userBean);

        if (authResultBean == null) {
            LOGGER.info("Incorrect credentials or role mismatch.");
            pressEnter();
            return;
        }

        // Login riuscito: avvia la home
        UUID sid = authResultBean.getSessionId();
        LOGGER.info("Login successful!");
        pressEnter();
        HomeGraphicControllerBW homeGraphicControllerBW = new HomeGraphicControllerBW(sid);
        homeGraphicControllerBW.start();
    }
}
