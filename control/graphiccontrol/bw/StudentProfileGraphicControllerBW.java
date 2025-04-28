package logic.control.graphiccontrol.bw;

import logic.model.dao.DaoFactory;
import logic.model.domain.Student;
import logic.model.domain.Account;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentProfileGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(StudentProfileGraphicControllerBW.class.getName());

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    public void show(String studentAccountId) {
        Account acc = DaoFactory.getInstance().getAccountDAO().load(studentAccountId);
        if (!(acc instanceof Student s)) {
            LOGGER.warning("Not a student account!");
            return;
        }

        String studentInfo = String.format(
                "%nStudent: %s %s – Institute: %s – Age: %d",
                s.getName(), s.getSurname(), s.getInstitute(), s.getAge()
        );
        LOGGER.info(studentInfo);

        pressEnter();
    }
}
