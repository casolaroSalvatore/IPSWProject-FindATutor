package logic.model.domain;

import logic.bean.AccountBean;
import logic.bean.UserBean;

public class SessionManager {

    private SessionManager() {}

    private static UserBean loggedUser; // Usa SignUpBean come "contenitore" dell'utente

    public static UserBean getLoggedUser() {
        return loggedUser;
    }

    public static String getLoggedUserAccountId() {
        if (getLoggedUser() == null) return null;
        for (AccountBean acc : getLoggedUser().getAccounts()) {
            if ("Tutor".equalsIgnoreCase(acc.getRole()) ||
                    "Student".equalsIgnoreCase(acc.getRole()))
                return acc.getAccountId();
        }
        return null;
    }

    // (Solo a scopo demo) Memorizza i dati di registrazione del Tutor
    // nel passaggio dalla prima form (username, email, password)
    // alla seconda form (location, subject, date).
    private static UserBean partialSignUpBean;

    // (Facoltativo) Memorizza l'eventuale Tutor selezionato
    private static Tutor selectedTutor;

    // Se in futuro hai altre necessità, potrai aggiungere campi
    // o usare un Map<String, Object>.

    // GETTER/SETTER per partialSignUpBean
    public static void setUserBean(UserBean bean) {
        partialSignUpBean = bean;
    }
    public static UserBean getUserBean() {
        return partialSignUpBean;
    }
    public static void clearUserBean() {
        partialSignUpBean = null;
    }

    // GETTER/SETTER per selectedTutor (prenotazione) --
    public static void setSelectedTutor(Tutor t) {
        selectedTutor = t;
    }
    public static Tutor getSelectedTutor() {
        return selectedTutor;
    }
    public static void clearSelectedTutor() {
        selectedTutor = null;
    }

    public static void setLoggedUser(UserBean user) {
        loggedUser = user;
    }

    public static void logout() {
        loggedUser = null;
    }
}
