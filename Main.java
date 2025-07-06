package logic;

import java.util.logging.Level;
import java.util.logging.Logger;

import logic.control.graphiccontrol.bw.HomeGraphicControllerBW;
import logic.control.graphiccontrol.colored.HomeGraphicControllerColored;
import logic.exception.NoTutorFoundException;
import logic.model.domain.InterfaceProvider;
import logic.view.ConfigurationBoundary;

public class Main {

    public static void main(String[] args) throws NoTutorFoundException {

        // Step 0: Remove JavaFX warnings
        // (non posso uniformare le 2 versioni in quanto SonarQube Community Edition supporta
        // fino a Java 17
        Logger.getLogger("javafx").setLevel(Level.SEVERE);
        Logger.getLogger("javafx.fxml").setLevel(Level.SEVERE);

        // Step 1: Configure persistence and interface
        ConfigurationBoundary.configureAll();

        // Step 2: Launch the selected interface
        if (ConfigurationBoundary.getInterfaceProvider() == InterfaceProvider.COLORED) {
            HomeGraphicControllerColored.launchGUI();
        } else {
            new HomeGraphicControllerBW().start();
        }
    }
}
