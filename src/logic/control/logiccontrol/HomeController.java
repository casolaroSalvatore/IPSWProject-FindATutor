package logic.control.logiccontrol;

import logic.bean.AccountBean;
import logic.bean.AvailabilityBean;
import logic.bean.UserBean;
import logic.model.domain.Session;
import logic.model.domain.SessionManager;
import logic.model.domain.Account;
import logic.model.domain.User;
import java.util.*;

public class HomeController {

    private final Map<UUID, List<AccountBean>> accountCache = new HashMap<>();
    private final Map<UUID, AvailabilityBean> availCache   = new HashMap<>();

    public void logout(UUID sessionId) {
        SessionManager.getInstance().invalidateSession(sessionId);
        accountCache.remove(sessionId);
        availCache.remove(sessionId);
    }

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
