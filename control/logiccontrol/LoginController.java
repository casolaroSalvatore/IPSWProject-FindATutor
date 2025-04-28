package logic.control.logiccontrol;

import logic.bean.UserBean;
import logic.bean.AccountBean;
import logic.model.dao.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.Account;
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
        System.out.println("Ruolo selezionato: " + loginBean.getSelectedRole());
        System.out.println("Ruoli disponibili per l'utente:");

        // Cerchiamo l’account con il ruolo richiesto
        Account matchedAccount = null;
        for (Account acc : user.getAccounts()) {
            if (acc.getRole().equalsIgnoreCase(loginBean.getSelectedRole())) {
                matchedAccount = acc;
                break;
            }
        }

        if (matchedAccount == null || !matchedAccount.getPassword().equals(loginBean.getPassword()) ) {
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

        return userBean;
    }
}

