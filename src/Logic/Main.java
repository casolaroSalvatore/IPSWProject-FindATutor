package Logic;

import java.util.logging.Level;
import java.util.logging.Logger;
import Logic.Control.GraphicControl.HomeController;
import Logic.View.ConfigurationBoundary;

public class Main {

    public static void main(String[] args) {

        // Passo 0: Rimuoviamo i warning dovuti alla differenti versioni di Java a JavaFX
        // (non posso uniformare le 2 versioni in quanto SonarQube Community Edition supporta
        // fino a Java 17
        Logger.getLogger("javafx").setLevel(Level.SEVERE);
        Logger.getLogger("javafx.fxml").setLevel(Level.SEVERE);

        // Passo 1: Configura il livello di persistenza
        ConfigurationBoundary.configurePersistence();

        // Passo 2: Avvia l'interfaccia grafica tramite il HomeController
        HomeController.launchGUI();

    }
}
