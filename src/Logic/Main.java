package Logic;

import Logic.Control.GraphicControl.HomeController;
import Logic.View.ConfigurationBoundary;

public class Main {

    public static void main(String[] args) {

        // Passo 0: Disattivo i Warning relativi alla differenza di versione di Java e JavaFx

        // Passo 1: Configura il livello di persistenza
        ConfigurationBoundary.configurePersistence();

        // Passo 2: Avvia l'interfaccia grafica tramite il HomeController
        HomeController.launchGUI();

    }
}
