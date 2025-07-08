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

        // Step 0: Rimuove i warning legati a JavaFX nel logger,
        // limitando i log di JavaFX ai soli messaggi SEVERE (nasconde info e warning)
        Logger.getLogger("javafx").setLevel(Level.SEVERE);
        Logger.getLogger("javafx.fxml").setLevel(Level.SEVERE);

        // Step 1: Configura persistenza e interfaccia
        // Questo metodo dovrebbe inizializzare DaoFactory, scegliere tipo interfaccia, ecc.
        ConfigurationBoundary.configureAll();

        // Step 2: Lancia l'interfaccia grafica selezionata in fase di configurazione
        if (ConfigurationBoundary.getInterfaceProvider() == InterfaceProvider.COLORED) {
            HomeGraphicControllerColored.launchGUI();
        } else {
            new HomeGraphicControllerBW().start();
        }
    }
}
