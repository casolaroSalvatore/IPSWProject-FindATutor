package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.AvailabilityBean;
import logic.bean.UserBean;
import logic.model.domain.Session;
import logic.model.domain.SessionManager;
import logic.model.domain.Account;
import logic.model.domain.User;

import java.util.*;

// NOTA: Questa parte viola il principio MVC/BCE perché ad ogni Controller logico abbiamo fatto corrispondere
// un Caso d'uso. In questo caso però la classe mi serve per mantenere la sessione di un utente,
// evitando che la View comunichi direttamente con il SessionManager.

public class HomeController {

    // Cache locale per memorizzare la lista degli AccountBean associati a ogni sessione
    private final Map<UUID, List<AccountBean>> accountCache = new HashMap<>();

    // Cache locale per memorizzare la AvailabilityBean associata a ogni sessione
    private final Map<UUID, AvailabilityBean> availCache = new HashMap<>();

    // Invalida la sessione e rimuove i dati utente memorizzati in cache
    public void logout(UUID sessionId) {
        SessionManager.getInstance().invalidateSession(sessionId);
        accountCache.remove(sessionId);
        availCache.remove(sessionId);
    }

    // Restituisce un UserBean dell'utente loggato (se la sessione è attiva) e aggiorna la cache
    public UserBean getLoggedUser(UUID sessionId) {
        Session s = SessionManager.getInstance().getSession(sessionId);
        if (s == null) return null;

        User dom = s.getUser();
        if (dom == null) return null;

        UserBean ub = new UserBean();
        ub.setUsername(dom.getUsername());
        ub.setEmail(dom.getEmail());

        List<AccountBean> accList = new ArrayList<>();
        for (Account acc : dom.getAccounts()) {
            AccountBean ab = new AccountBean();
            ab.setAccountId(acc.getAccountId());
            ab.setRole(acc.getRole());
            ab.setName(acc.getName());
            ab.setSurname(acc.getSurname());
            accList.add(ab);
        }
        accountCache.put(sessionId, accList);
        return ub;
    }
}
