package logic.control.logiccontrol;

import logic.bean.UserBean;
import logic.bean.AccountBean;
import logic.model.dao.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.Account;
import logic.model.domain.SessionManager;
import logic.model.domain.User;

public class LoginController {

    public UserBean login(UserBean loginBean) {

        UserDAO userDAO = DaoFactory.getInstance().getUserDAO();
        User user = userDAO.load(loginBean.getEmail());

        // Utente non trovato o password errata
        if (user == null) {
            return null;
        }

        System.out.println("Utente trovato: " + user.getEmail());
        System.out.println("Password corretta.");
        String role          = loginBean.getAccounts().get(0).getRole();
        String inputPassword = loginBean.getAccounts().get(0).getPassword();
        System.out.println("Ruolo selezionato: " + role);
        System.out.println("Tentativo di accesso con password: " + inputPassword);

        // Cerchiamo l’account con il ruolo richiesto
        Account matchedAccount = null;
        for (Account acc : user.getAccounts()) {
            if (acc.getRole().equalsIgnoreCase(role)) {
                matchedAccount = acc;
                break;
            }
        }

        String inputPassword1 = loginBean.getAccounts().get(0).getPassword();
        if (matchedAccount == null || !inputPassword1.equals(matchedAccount.getPassword())) {
            return null;
        }

        // Costruiamo lo UserBean da restituire al controller grafico LoginGraphicControllerColored
        UserBean userBean = new UserBean();
        userBean.setUsername(user.getUsername());
        userBean.setEmail(user.getEmail());

        // Creo AccountBean per rappresentare SOLO l'account selezionato
        AccountBean accountBean = new AccountBean();
        accountBean.setAccountId(matchedAccount.getAccountId());
        accountBean.setRole(matchedAccount.getRole());

        userBean.addAccount(accountBean);
        // Unica sede in cui viene impostata la sessione
        SessionManager.setLoggedUser(userBean);

        return userBean;
    }
}

