package logic.control.graphiccontrol.bw;

import logic.bean.TutorBean;
import logic.control.logiccontrol.TutorProfileController;

import java.util.logging.Level;
import java.util.logging.Logger;

// Controller BW per la visualizzazione del profilo di un tutor
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

    // Mostra a video il profilo del tutor corrispondente all'accountId
    public void show(String tutorAccountId) {
        TutorBean t;
        try {
            // Carica i dati del tutor tramite il controller logico
            t = tutorProfileController.loadTutorBean(tutorAccountId);
        } catch (IllegalArgumentException ex) {
            LOGGER.warning(ex.getMessage());
            pressEnter();
            return;
        }

        // Costruisce la stringa informativa del tutor
        String info = String.format(
                "%nTutor: %s %s – %s%nTitle: %s – Rating: %.1f",
                t.getName(), t.getSurname(), t.getLocation(),
                t.getEducationalTitle(), t.getRating()
        );

        // Stampa le informazioni sul tutor
        LOGGER.info(info);
        pressEnter();
    }
}
