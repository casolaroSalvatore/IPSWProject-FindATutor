package logic.control.logiccontrol;

import logic.bean.*;
import logic.model.domain.Session;
import logic.model.domain.SessionManager;
import logic.model.dao.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.Account;
import logic.model.domain.User;

import java.util.UUID;

public class LoginController {

    public AuthResultBean login(UserBean loginBean) {

        UserDAO userDAO = DaoFactory.getInstance().getUserDAO();
        User user = userDAO.load(loginBean.getEmail());

        // Utente non trovato o password errata
        if (user == null) {
            return null;
        }

        String role = loginBean.getAccounts().get(0).getRole();

        // Cerchiamo l’account con il ruolo richiesto
        Account matchedAccount = null;
        for (Account acc : user.getAccounts()) {
            if (acc.getRole().equalsIgnoreCase(role)) {
                matchedAccount = acc;
                break;
            }
        }

        String inputPassword = loginBean.getAccounts().get(0).getPassword();
        if (matchedAccount == null || !inputPassword.equals(matchedAccount.getPassword())) {
            return null;
        }

        // Creo una nuova Session nel singleton SessionManager
        UUID sid = SessionManager.getInstance().createSession(user);

        // Costruiamo lo UserBean da restituire al controller grafico LoginGraphicControllerColored
        UserBean userBean = new UserBean();
        userBean.setUsername(user.getUsername());
        userBean.setEmail(user.getEmail());

        // Creo AccountBean per rappresentare SOLO l'account selezionato
        AccountBean accountBean = new AccountBean();
        accountBean.setAccountId(matchedAccount.getAccountId());
        accountBean.setRole(matchedAccount.getRole());

        userBean.addAccount(accountBean);

        return new AuthResultBean(sid, userBean);
    }

    public UserBean getLoggedUser(UUID sid) {
        Session session = SessionManager.getInstance().getSession(sid);
        if (session == null || session.getUser() == null) {
            return null;
        }
        return new UserBean(session.getUser());  // CORRETTO: conversione Domain -> Bean
    }

    public void logout(UUID sessionId) {
        // Invalida la sessione nel SessionManager
        SessionManager.getInstance().invalidateSession(sessionId);
    }

    /* Controlla se la sessione con quell'UUID è ancora attiva. */
    public boolean isSessionActive(UUID sessionId) {
        return SessionManager.getInstance().isSessionActive(sessionId);
    }

    /* Restituisce il dominio User associato alla sessione, oppure null se non valido.
     * Utile a HomeController o alle View per recuperare UserBean da dominio. */
    public User getUserFromSession(UUID sessionId) {
        Session s = SessionManager.getInstance().getSession(sessionId);
        return (s != null) ? s.getUser() : null;
    }

    public UserBean getUserBeanFromSession(UUID sessionId) {
        if (!isSessionActive(sessionId)) return null;

        User dom = getUserFromSession(sessionId);
        if (dom == null) return null;

        UserBean ub = new UserBean();
        ub.setEmail(dom.getEmail());
        ub.setUsername(dom.getUsername());

        for (Account acc : dom.getAccounts()) {
            AccountBean ab = new AccountBean();
            ab.setAccountId(acc.getAccountId());
            ab.setRole(acc.getRole());
            ab.setName(acc.getName());
            ab.setSurname(acc.getSurname());
            ub.addAccount(ab);
        }
        return ub;
    }
}

