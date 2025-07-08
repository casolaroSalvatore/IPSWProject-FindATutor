package logic.control.logiccontrol;

import logic.bean.*;
import logic.model.domain.SessionManager;
import logic.model.dao.AccountDAO;
import logic.infrastructure.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SignUpController {

    /* Cache temporanea per consentire il Sign-Up del tutor in 2 step.
    E' static in quanto deve sopravvivere al cambio di scena */
    private static UserBean partialTutor;

    public AuthResultBean registerUser(UserBean userBean) {

        /* 1. Validazione preliminare dell’oggetto principale */
        requireValidUserBean(userBean);

        /* 2. DAO necessari */
        UserDAO    userDAO    = DaoFactory.getInstance().getUserDAO();
        AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();

        /* 3. Carica o crea l’utente */
        User user = loadOrCreateUser(userDAO, userBean);

        /* 4. Per ogni nuovo account da aggiungere */
        for (AccountBean accountBean : userBean.getAccounts()) {
            ensureAccountDoesNotExist(user, accountBean.getRole());

            /* 4a. Validazioni */
            validateCommonAccountData(accountBean);
            validateRoleSpecificData(accountBean);

            /* 4b. Costruzione account + set di campi extra */
            Account newAccount = buildAccount(userBean.getEmail(), accountBean);
            newAccount.setPassword(accountBean.getPassword());
            newAccount.setProfilePicturePath(accountBean.getProfilePicturePath());
            newAccount.setProfileComment(accountBean.getProfileComment());

            /* 4c. Persistenza e associazione all’utente */
            user.addAccount(newAccount);
            accountDAO.store(newAccount);
        }

        /* 5. Aggiorna l’utente complessivo */
        userDAO.store(user);

        /* 6. Crea la sessione di dominio e restituisce il risultato */
        UUID sid = SessionManager.getInstance().createSession(user);
        return new AuthResultBean(sid, userBean);
    }

     // Helper necessari per eliminare l'issues su SonarQube

    private void requireValidUserBean(UserBean userBean) {
        if (userBean == null || userBean.getAccounts().isEmpty()) {
            throw new IllegalArgumentException("UserBean must not be null or empty.");
        }
    }

    private User loadOrCreateUser(UserDAO userDAO, UserBean userBean) {
        User user = userDAO.load(userBean.getEmail());
        if (user == null) {
            user = new User(userBean.getEmail(), userBean.getUsername());
            userDAO.store(user);
        }
        return user;
    }

    private void ensureAccountDoesNotExist(User user, String role) {
        if (user.hasAccount(role)) {
            throw new IllegalArgumentException("An account with this role already exists.");
        }
    }

    private void validateCommonAccountData(AccountBean bean) {
        bean.checkBasicSyntax();
        bean.checkPasswordSyntax();

        if (bean.getBirthday() == null ||
                bean.getBirthday().isAfter(LocalDate.now()) ||
                Period.between(bean.getBirthday(), LocalDate.now()).getYears() < 13) {
            throw new IllegalArgumentException("User must be at least 13 years old.");
        }
        if (isBlank(bean.getRole())) {
            throw new IllegalArgumentException("Role is required.");
        }
    }

    private void validateRoleSpecificData(AccountBean bean) {
        if ("Tutor".equalsIgnoreCase(bean.getRole())) {
            validateTutorData(bean);
        } else if ("Student".equalsIgnoreCase(bean.getRole())) {
            validateStudentData(bean);
        }
    }

    private void validateTutorData(AccountBean bean) {
        AvailabilityBean ab = bean.getAvailabilityBean();
        if (ab == null) {
            throw new IllegalArgumentException("Availability is required for tutors.");
        }
        validateAvailabilityBean(ab);

        if (isBlank(bean.getSubject())) {
            throw new IllegalArgumentException("Subject is required for tutors.");
        }
        if (isBlank(bean.getEducationalTitle())) {
            throw new IllegalArgumentException("Educational title is required for tutors.");
        }
        if (isBlank(bean.getLocation())) {
            throw new IllegalArgumentException("Location is required for tutors.");
        }
        if (bean.getHourlyRate() < 5 || bean.getHourlyRate() > 200) {
            throw new IllegalArgumentException("Hourly rate must be between 5$ and 200$.");
        }
    }

    private void validateStudentData(AccountBean bean) {
        if (isBlank(bean.getInstitute())) {
            throw new IllegalArgumentException("Institute is required for students.");
        }
    }

    private void validateAvailabilityBean(AvailabilityBean ab) {
        ab.checkSyntax();
        if (ab.getStartDate() == null || ab.getEndDate() == null) {
            throw new IllegalArgumentException("Start and end date are required.");
        }
        if (ab.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (!ab.getEndDate().isAfter(ab.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
        }
        if (ChronoUnit.DAYS.between(ab.getStartDate(), ab.getEndDate()) > 365) {
            throw new IllegalArgumentException("Availability range max 1 year.");
        }
    }

    private Account buildAccount(String email, AccountBean bean) {
        if ("Tutor".equalsIgnoreCase(bean.getRole())) {
            Availability availEntity = new Availability(
                    bean.getAvailabilityBean().getStartDate(),
                    bean.getAvailabilityBean().getEndDate(),
                    bean.getAvailabilityBean().getDays());

            return new Tutor.Builder(email)
                    .name(bean.getName())
                    .surname(bean.getSurname())
                    .birthday(bean.getBirthday())
                    .educationalTitle(bean.getEducationalTitle())
                    .location(bean.getLocation())
                    .availability(availEntity)
                    .subject(bean.getSubject())
                    .hourlyRate(bean.getHourlyRate())
                    .offersInPerson(bean.isOffersInPerson())
                    .offersOnline(bean.isOffersOnline())
                    .offersGroup(bean.isOffersGroup())
                    .firstLessonFree(bean.isFirstLessonFree())
                    .build();
        }

        /* Student */
        return new Student(
                email,
                bean.getName(),
                bean.getSurname(),
                bean.getBirthday(),
                bean.getInstitute()
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
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
