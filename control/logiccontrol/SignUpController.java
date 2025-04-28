package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.AvailabilityBean;
import logic.bean.UserBean;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.*;

public class SignUpController {

    public boolean registerUser(UserBean userBean) {

        if (userBean == null || userBean.getAccounts().isEmpty()) {
            throw new IllegalArgumentException("UserBean must not be null or empty.");
        }

        UserDAO userDAO = DaoFactory.getInstance().getUserDAO();
        AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();

        // Carichiamo l'utente se esiste già
        User user = userDAO.load(userBean.getEmail());

        if (user == null) {
            // Creiamo un nuovo utente
            user = new User(userBean.getEmail(), userBean.getUsername());
            userDAO.store(user);
        }

        // Supporto a più account (Student/Tutor)
        for (AccountBean accountBean : userBean.getAccounts()) {

            // Controlliamo se ha già un account con quel ruolo
            if (user.hasAccount(accountBean.getRole())) {
                throw new IllegalArgumentException("An account with this role already exists.");
            }

            System.out.println("Tentativo di registrazione: " + userBean.getEmail());
            System.out.println("DEBUG SignUpController: Sto per creare un nuovo Student/Tutor con i seguenti dati:");
            System.out.println("Name=" + accountBean.getName() +
                    ", Surname=" + accountBean.getSurname() +
                    ", Birthday=" + accountBean.getBirthday() +
                    ", Email=" + userBean.getEmail() +
                    ", Institute=" + accountBean.getInstitute());

            // Creiamo un nuovo account e lo associamo all'utente
            Account newAccount;
            if ("Tutor".equalsIgnoreCase(accountBean.getRole())) {

                // Conversione AvailabilityBean -> Availability (solo per Tutor)
                AvailabilityBean ab = accountBean.getAvailabilityBean();
                Availability availEntity = new Availability(ab.getStartDate(), ab.getEndDate(), ab.getDays());

                // Creo un Tutor (passandogli, se vuoi, i campi base: email e role)
                // Se hai altri campi, come subject, location, ecc., puoi aggiungerli ora
                newAccount = new Tutor(
                        userBean.getEmail(),
                        accountBean.getName(),
                        accountBean.getSurname(),
                        accountBean.getBirthday(),
                        accountBean.getEducationalTitle(),
                        accountBean.getLocation(),
                        availEntity,
                        accountBean.getSubject(),
                        accountBean.getHourlyRate(),
                        accountBean.isOffersInPerson(),
                        accountBean.isOffersOnline(),
                        accountBean.isOffersGroup(),
                        accountBean.isFirstLessonFree()
                );
            } else {
                // Altrimenti Student
                newAccount = new Student(
                        userBean.getEmail(),
                        accountBean.getName(),
                        accountBean.getSurname(),
                        accountBean.getBirthday(),
                        accountBean.getInstitute()
                );
            }

            newAccount.setPassword(accountBean.getPassword());

            // Imposta la foto e commento
            newAccount.setProfilePicturePath(accountBean.getProfilePicturePath());
            newAccount.setProfileComment(accountBean.getProfileComment());

            user.addAccount(newAccount);
            // Salviamo nelle DAO
            accountDAO.store(newAccount);

            System.out.println("Utente salvato: " + userDAO.load(newAccount.getEmail()));
            System.out.println("Account salvato: " + accountDAO.load(user.getEmail() + "_" + accountBean.getRole()));
        }

        userDAO.store(user);

        SessionManager.setLoggedUser(userBean);

        // Debug finale per ogni account
        for (AccountBean ab : userBean.getAccounts()) {
            Account reloadedAcc = accountDAO.load(user.getEmail() + "_" + ab.getRole());
            if (reloadedAcc != null) {
                System.out.println("Ricaricato account -> email=" + reloadedAcc.getEmail() +
                        ", name=" + reloadedAcc.getName() +
                        ", surname=" + reloadedAcc.getSurname() +
                        ", role=" + reloadedAcc.getRole());
            } else {
                System.out.println("Ricaricato account -> null");
            }
        }

        return true;
    }
}
