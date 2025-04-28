package logic.control.graphiccontrol.bw;

import logic.bean.UserBean;
import logic.control.logiccontrol.LoginController;
import logic.bean.SignUpBean;
import logic.model.dao.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.Account;
import logic.model.domain.User;
import logic.model.domain.SessionManager;
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
        userBean.setPassword(password);
        userBean.setSelectedRole(role);

        // Faccio il login
        UserBean loggedUser = logic.login(userBean);

        if (logic.login(userBean) != null) {
            LOGGER.warning("Incorrect credentials or role mismatch.");
            pressEnter();
            return;
        }

        SessionManager.setLoggedUser(loggedUser);

        LOGGER.info("Login successful!");
        pressEnter();
    }
}
