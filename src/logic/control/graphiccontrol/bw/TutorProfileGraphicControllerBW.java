package logic.control.graphiccontrol.bw;

import logic.bean.TutorBean;
import logic.control.logiccontrol.TutorProfileController;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TutorProfileGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(TutorProfileGraphicControllerBW.class.getName());
    private TutorProfileController tutorProfileController = new TutorProfileController();


    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    public void show(String tutorAccountId) {
        TutorBean t;
        try{
            t = tutorProfileController.loadTutorBean(tutorAccountId);
        }catch(IllegalArgumentException ex){
            LOGGER.warning(ex.getMessage());
            pressEnter(); return;
        }

        String info = String.format(
                "%nTutor: %s %s – %s%nTitle: %s – Rating: %.1f",
                t.getName(), t.getSurname(), t.getLocation(),
                t.getEducationalTitle(), t.getRating()
        );
        LOGGER.info(info);
        pressEnter();
    }
}
