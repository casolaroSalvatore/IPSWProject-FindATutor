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

        }

        userDAO.store(user);

        // Creo la sessione di dominio e la salvo nel SessionManager
        UUID sid = SessionManager.getInstance().createSession(user);

        return new AuthResultBean(sid, userBean);
    }

    // Salva i dati preliminari (step 1)
    public static void cachePartialTutor(logic.bean.UserBean ub) {
        partialTutor = ub;
    }

    // Recupera la cache (step 2).
    public static UserBean getPartialTutor() {
        return partialTutor;
    }

    // Svuota la cache dopo il completamento o l’annullamento
    public static void clearPartialTutor() {
        partialTutor = null;
    }
}
