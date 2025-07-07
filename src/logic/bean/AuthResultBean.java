package logic.bean;

import java.util.UUID;

// Bean immutabile restituito da Login-/SignUp-controller. Viene restituito dai controller
// (LoginController, SignUpController) ai controller grafici per consentire la gestione della sessione lato View.
public class AuthResultBean {

    // Identificativo univoco della sessione creata
    private final UUID sessionId;

    // Informazioni utente associate alla sessione
    private final UserBean user;

    public AuthResultBean(UUID sessionId, UserBean user) {
        this.sessionId = sessionId;
        this.user = user;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UserBean getUser() {
        return user;
    }
}

