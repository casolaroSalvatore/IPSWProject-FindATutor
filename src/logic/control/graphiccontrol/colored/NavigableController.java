package logic.control.graphiccontrol.colored;

import logic.bean.UserBean;
import java.util.UUID;

public interface NavigableController {
    void initData(UUID sessionId, UserBean userBean);
}
