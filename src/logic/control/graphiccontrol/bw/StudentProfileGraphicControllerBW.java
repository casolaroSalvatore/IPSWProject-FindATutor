package logic.control.graphiccontrol.bw;

import logic.bean.AccountBean;

import java.util.logging.Level;
import java.util.logging.Logger;

import logic.control.logiccontrol.StudentProfileController;

// Controller CLI BW per la visualizzazione del profilo di uno studente
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

    // Mostra a video il profilo del tutor corrispondente all'accountId
    public void show(String studentAccountId) {
        AccountBean b;
        try {
            // Carica i dati dello studente tramite il controller logico
            b = studentProfileController.loadStudentBean(studentAccountId);
        } catch (IllegalArgumentException ex) {
            LOGGER.warning(ex.getMessage());
            pressEnter();
            return;
        }

        // Costruisce la stringa informativa dello studente
        String info = String.format(
                "%nStudent: %s %s – Institute: %s – Age: %d",
                b.getName(), b.getSurname(), b.getInstitute(),
                (b.getBirthday() == null ? 0 :
                        java.time.Period.between(b.getBirthday(), java.time.LocalDate.now()).getYears())
        );

        // Stampa le informazioni sullo student
        LOGGER.info(info);
        pressEnter();
    }
}
