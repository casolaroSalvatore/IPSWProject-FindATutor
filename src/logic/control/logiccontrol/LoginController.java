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

    // Esegue il login: verifica credenziali e ruolo, crea sessione, restituisce AuthResultBean
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

    // Restituisce UserBean per l'utente loggato associato alla sessione
    public UserBean getLoggedUser(UUID sid) {
        Session session = SessionManager.getInstance().getSession(sid);
        return (session == null) ? null : toUserBean(session.getUser());
    }

    // Effettua il logout, invalidando la sessione nel SessionManager
    public void logout(UUID sessionId) {
        SessionManager.getInstance().invalidateSession(sessionId);
    }

    // Verifica se una sessione è ancora attiva
    public boolean isSessionActive(UUID sessionId) {
        return SessionManager.getInstance().isSessionActive(sessionId);
    }

    // Restituisce il dominio User associato alla sessione, oppure null se non valido.
    // Utile a HomeController o alle View per recuperare UserBean da dominio.
    public User getUserFromSession(UUID sessionId) {
        Session s = SessionManager.getInstance().getSession(sessionId);
        return (s != null) ? s.getUser() : null;
    }

    // Helper di mapping User-UserBean
    private static UserBean toUserBean(User u) {
        if (u == null) return null;

        UserBean b = new UserBean();
        b.setEmail(u.getEmail());
        b.setUsername(u.getUsername());

        for (Account acc : u.getAccounts()) {
            b.addAccount(toAccountBean(acc));
        }
        return b;
    }

    // Helper di mapping Account-AccountBean
    private static AccountBean toAccountBean(Account a) {
        if (a == null) return null;

        AccountBean b = new AccountBean();
        b.setAccountId(a.getAccountId());
        b.setRole(a.getRole());
        b.setPassword(a.getPassword());
        b.setName(a.getName());
        b.setSurname(a.getSurname());
        b.setBirthday(a.getBirthday());
        b.setProfilePicturePath(a.getProfilePicturePath());
        b.setProfileComment(a.getProfileComment());
        /* copia gli altri campi che ti servono … */
        return b;
    }
}

