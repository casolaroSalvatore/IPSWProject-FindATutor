package logic.bean;

import java.util.UUID;

// Bean immutabile restituito da Login-/SignUp-controller.
public class AuthResultBean {

    private final UUID sessionId;
    private final UserBean user;

    public AuthResultBean(UUID sessionId, UserBean user) {
        this.sessionId = sessionId;
        this.user      = user;
    }

    public UUID getSessionId() { return sessionId; }
    public UserBean getUser()  { return user;        }
}

