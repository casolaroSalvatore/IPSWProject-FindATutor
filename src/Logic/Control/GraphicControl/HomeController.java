package Logic.Control.GraphicControl;

import Logic.Model.Domain.User;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class HomeController extends Application {

    @FXML private HBox authBox;  // Box con i pulsanti Sign Up e Log In
    @FXML private Button loginButton;
    @FXML private Button signUpButton;

    private static User loggedUser = null;  // Variabile per tenere traccia dell'utente loggato

    // Metodo statico per avviare JavaFX (richiamato dal Main)
    public static void launchGUI() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Carica e imposta la schermata Home
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            primaryStage.setTitle("Schermata Home");
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the Home screen.");
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        LoginGraphicControllerColored.showLoginScene(event);
    }

    @FXML
    private void goToSignUp(ActionEvent event) {
        SignUpControllerColored.showSignUpScene(event);
    }
}

