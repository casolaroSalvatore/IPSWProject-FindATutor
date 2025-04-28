package logic.control.graphiccontrol.bw;

import logic.model.dao.DaoFactory;
import logic.model.domain.Tutor;
import logic.model.domain.Account;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TutorProfileGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(TutorProfileGraphicControllerBW.class.getName());

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    public void show(String tutorAccountId) {
        Account acc = DaoFactory.getInstance().getAccountDAO().load(tutorAccountId);
        if (!(acc instanceof Tutor t)) {
            LOGGER.warning("It is not a tutor!!");
            return;
        }

        String tutorInfo = String.format(
                "\nTutor: %s %s – %s%nTitle: %s – Rating: %.1f",
                t.getName(), t.getSurname(), t.getLocation(), t.getEducationalTitle(), t.getRating()
        );
        LOGGER.info(tutorInfo);

        pressEnter();
    }
}
