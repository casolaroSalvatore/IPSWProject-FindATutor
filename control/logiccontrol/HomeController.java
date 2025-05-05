package logic.control.logiccontrol;

import logic.bean.UserBean;
import logic.control.graphiccontrol.colored.BookingSessionGraphicControllerColored;
import logic.model.domain.SessionManager;
import logic.bean.AvailabilityBean;

public class HomeController {
    public void logout() {
        SessionManager.logout();
    }
    public void cacheSearchParams(String location, String subject, AvailabilityBean availabilityBean) {
        BookingSessionGraphicControllerColored.setSearchParameters(location, subject, availabilityBean);
    }
    public UserBean getLoggedUser() {
        return SessionManager.getLoggedUser();
    }
}

