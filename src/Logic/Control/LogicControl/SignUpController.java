package Logic.Control.LogicControl;

import Logic.Bean.SignUpBean;
import Logic.Model.Dao.AccountDAO;
import Logic.Model.Dao.DaoFactory;
import Logic.Model.Dao.UserDAO;
import Logic.Model.Domain.Account;
import Logic.Model.Domain.User;

public class SignUpController {

    public boolean registerUser(SignUpBean signUpBean) {
        UserDAO userDAO = DaoFactory.getInstance().getUserDAO();
        AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();

        // Carichiamo l'utente se esiste già
        User user = userDAO.load(signUpBean.getEmail());

        if (user == null) {
            // Creiamo un nuovo utente
            user = new User(signUpBean.getEmail(), signUpBean.getUsername(), signUpBean.getPassword());
            userDAO.store(user);
        }

        // Controlliamo se ha già un account con quel ruolo
        if (user.hasAccount(signUpBean.getRole())) {
            return false; // Già esistente
        }

        System.out.println("Tentativo di registrazione: " + signUpBean.getEmail());

        // Creiamo un nuovo account e lo associamo all'utente
        Account newAccount = new Account(signUpBean.getEmail(), signUpBean.getRole());
        user.addAccount(newAccount);

        // Salviamo nelle DAO
        accountDAO.store(newAccount);
        userDAO.store(user);

        System.out.println("Utente salvato: " + userDAO.load(newAccount.getEmail()));
        System.out.println("Account salvato: " + accountDAO.load(user.getEmail() + "_" + signUpBean.getRole()));

        return true;
    }
}
