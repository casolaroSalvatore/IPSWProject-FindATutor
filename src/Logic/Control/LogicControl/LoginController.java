package Logic.Control.LogicControl;

import Logic.Bean.LoginBean;
import Logic.Model.Dao.DaoFactory;
import Logic.Model.Dao.UserDAO;
import Logic.Model.Domain.Account;
import Logic.Model.Domain.User;

public class LoginController {

    public boolean login(LoginBean loginBean) {

        UserDAO userDAO = DaoFactory.getInstance().getUserDAO();
        User user = userDAO.load(loginBean.getEmail());

        if (user == null || !user.getPassword().equals(loginBean.getPassword())) {
            return false; // Utente non trovato o password errata
        }

        System.out.println("Utente trovato: " + user.getEmail());
        System.out.println("Password corretta.");
        System.out.println("Ruolo selezionato: " + loginBean.getSelectedRole());
        System.out.println("Ruoli disponibili per l'utente:");

        // Controlla se il ruolo selezionato dall'utente è effettivamente associato all'account
        boolean roleExists = false;
        for (Account account : user.getAccounts()) {
            if (account.getRole().equals(loginBean.getSelectedRole())) {
                roleExists = true;
                break; // Uscire dal ciclo appena troviamo un ruolo corrispondente
            }
        }

        if (!roleExists) {
            System.out.println("Errore: il ruolo selezionato non corrisponde a nessuno di quelli disponibili.");
        }

        return roleExists; // Ritorna la lista dei ruoli disponibili per la scelta
    }
}

