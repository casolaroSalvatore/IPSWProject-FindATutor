package logic.control.graphiccontrol.bw;

import logic.bean.AccountBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.control.logiccontrol.StudentProfileController;

public class StudentProfileGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(StudentProfileGraphicControllerBW.class.getName());
    private StudentProfileController studentProfileController = new StudentProfileController();

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    public void show(String studentAccountId) {
        AccountBean b;
        try {
            b = studentProfileController.loadStudentBean(studentAccountId);
        } catch (IllegalArgumentException ex){
            LOGGER.warning(ex.getMessage());
            pressEnter(); return;
        }

        String info = String.format(
                "%nStudent: %s %s – Institute: %s – Age: %d",
                b.getName(), b.getSurname(), b.getInstitute(),
                (b.getBirthday()==null?0:
                        java.time.Period.between(b.getBirthday(),java.time.LocalDate.now()).getYears())
        );
        LOGGER.info(info);
        pressEnter();
    }
}
