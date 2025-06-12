package logic.control.logiccontrol;

import logic.bean.*;
import logic.model.domain.SessionManager;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.*;

import java.util.UUID;

public class SignUpController {

    /* Cache temporanea per consentire il Sign-Up del tutor in 2 step.
       E' static in quanto deve sopravvivere al cambio di scena */
    private static UserBean partialTutor;

    public AuthResultBean registerUser(UserBean userBean) {

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

            /* System.out.println("Tentativo di registrazione: " + userBean.getEmail());
            System.out.println("DEBUG SignUpController: Sto per creare un nuovo Student/Tutor con i seguenti dati:");
            System.out.println("Name=" + accountBean.getName() +
                    ", Surname=" + accountBean.getSurname() +
                    ", Birthday=" + accountBean.getBirthday() +
                    ", Email=" + userBean.getEmail() +
                    ", Institute=" + accountBean.getInstitute()); */

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

            /* System.out.println("Utente salvato: " + userDAO.load(newAccount.getEmail()));
            System.out.println("Account salvato: " + accountDAO.load(user.getEmail() + "_" + accountBean.getRole())); */
        }

        userDAO.store(user);

        // Creo la sessione di dominio e la salvo nel SessionManager
        UUID sid = SessionManager.getInstance().createSession(user);

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
        return new AuthResultBean(sid, userBean);
    }

    // Salva i dati preliminari (step 1)
    public void cachePartialTutor(logic.bean.UserBean ub) {
        partialTutor = ub;
    }

    // Recupera la cache (step 2).
    public static UserBean getPartialTutor() {
        return partialTutor;
    }

    // Svuota la cache dopo il completamento o l’annullamento
    public void clearPartialTutor() {
        partialTutor = null;
    }
}
