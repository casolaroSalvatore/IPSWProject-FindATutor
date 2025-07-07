package logic.control.logiccontrol;

import logic.bean.*;
import logic.model.domain.SessionManager;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

public class SignUpController {

    /* Cache temporanea per consentire il Sign-Up del tutor in 2 step.
       E' static in quanto deve sopravvivere al cambio di scena */
    private static UserBean partialTutor;

    // Registra un nuovo utente (o aggiunge un nuovo account a utente esistente) e crea la sessione
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

            // Controlli sintattici offerti dal Bean
            accountBean.checkBasicSyntax();
            accountBean.checkPasswordSyntax();

            // Controlli semantici
            if (accountBean.getBirthday() == null ||
                    accountBean.getBirthday().isAfter(java.time.LocalDate.now()) ||
                    Period.between(accountBean.getBirthday(), LocalDate.now()).getYears() < 13) {
                throw new IllegalArgumentException("User must be at least 13 years old.");
            }

            if (accountBean.getRole() == null || accountBean.getRole().isBlank()) {
                throw new IllegalArgumentException("Role is required.");
            }

            if ("Tutor".equalsIgnoreCase(accountBean.getRole())) {

                if (accountBean.getAvailabilityBean() == null) {
                    throw new IllegalArgumentException("Availability is required for tutors.");
                }
                accountBean.getAvailabilityBean().checkSyntax();

                if (accountBean.getSubject() == null || accountBean.getSubject().isBlank()) {
                    throw new IllegalArgumentException("Subject is required for tutors.");
                }

                if (accountBean.getEducationalTitle() == null || accountBean.getEducationalTitle().isBlank()) {
                    throw new IllegalArgumentException("Educational title is required for tutors.");
                }

                if (accountBean.getLocation() == null || accountBean.getLocation().isBlank()) {
                    throw new IllegalArgumentException("Location is required for tutors.");
                }

                if (accountBean.getHourlyRate() < 5 || accountBean.getHourlyRate() > 200) {
                    throw new IllegalArgumentException("Hourly rate must be between 5$ and 200$.");
                }

            } else if ("Student".equalsIgnoreCase(accountBean.getRole())) {

                if (accountBean.getInstitute() == null || accountBean.getInstitute().isBlank()) {
                    throw new IllegalArgumentException("Institute is required for students.");
                }
            }


            // Creiamo un nuovo account e lo associamo all'utente
            Account newAccount;
            if ("Tutor".equalsIgnoreCase(accountBean.getRole())) {

                // Conversione AvailabilityBean -> Availability (solo per Tutor)
                AvailabilityBean ab = accountBean.getAvailabilityBean();

                // Controlli sintattici
                ab.checkSyntax();

                // Controlli semantici
                if (ab.getStartDate() == null || ab.getEndDate() == null) {
                    throw new IllegalArgumentException("Start and end date are required.");
                }
                if (ab.getStartDate().isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("Start date cannot be in the past.");
                }
                if (!ab.getEndDate().isAfter(ab.getStartDate())) {
                    throw new IllegalArgumentException("End date must be after start date.");
                }
                if (java.time.temporal.ChronoUnit.DAYS.between(ab.getStartDate(), ab.getEndDate()) > 365) {
                    throw new IllegalArgumentException("Availability range max 1 year.");
                }

                Availability availEntity = new Availability(ab.getStartDate(), ab.getEndDate(), ab.getDays());

                // Creo un Tutor
                newAccount = new Tutor.Builder(userBean.getEmail())
                        .name(accountBean.getName())
                        .surname(accountBean.getSurname())
                        .birthday(accountBean.getBirthday())
                        .educationalTitle(accountBean.getEducationalTitle())
                        .location(accountBean.getLocation())
                        .availability(availEntity)
                        .subject(accountBean.getSubject())
                        .hourlyRate(accountBean.getHourlyRate())
                        .offersInPerson(accountBean.isOffersInPerson())
                        .offersOnline(accountBean.isOffersOnline())
                        .offersGroup(accountBean.isOffersGroup())
                        .firstLessonFree(accountBean.isFirstLessonFree())
                        .build();
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

    // Salva temporaneamente un UserBean parziale per consentire il Sign-Up del tutor in più step
    public static void cachePartialTutor(logic.bean.UserBean ub) {
        partialTutor = ub;
    }

    // Restituisce il UserBean parziale salvato per il Sign-Up multi-step del tutor
    public static UserBean getPartialTutor() {
        return partialTutor;
    }

    // Svuota la cache temporanea del Sign-Up del tutor
    public static void clearPartialTutor() {
        partialTutor = null;
    }
}
