package logic.control.graphiccontrol.colored;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
import logic.bean.*;
import logic.control.logiccontrol.LoginController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.UUID;

public class LoginGraphicControllerColored {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private RadioButton studentButton;

    @FXML
    private RadioButton tutorButton;

    @FXML
    private ToggleGroup roleToggleGroup; // Gruppo per i RadioButton

    @FXML
    private LoginController loginController = new LoginController();

    // Campo per memorizzare la sessione e bean utente
    private UUID sessionId;
    private UserBean userBean;

    private static final String ERROR = "Error";

    @FXML
    public void showLoginScene(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(LoginGraphicControllerColored.class.getResource("/fxml/Login.fxml"));
            Parent loginRoot = loader.load();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(loginRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Controlliamo quale ruolo è stato selezionato dall'utente
        RadioButton selectedButton = (RadioButton) roleToggleGroup.getSelectedToggle();
        if (selectedButton == null) {
            showAlert(ERROR, "Please select a role.");
            return;
        }

        String selectedRole = selectedButton.getText(); // "Student" o "Tutor"

        // Creazione del Bean e chiamata al controller logico
        userBean = new UserBean();
        userBean.setEmail(email);

        AccountBean accountBean = new AccountBean();
        accountBean.setRole(selectedRole);
        accountBean.setPassword(password);
        userBean.addAccount(accountBean);

        try {
            AuthResultBean loggedProfile = loginController.login(userBean);
            if (loggedProfile == null) {
                showAlert(ERROR, "Wrong credentials or role mismatch.");
                return;
            }

            // Memorizzo sia sessionId sia UserBean completo
            sessionId = loggedProfile.getSessionId();
            userBean = loggedProfile.getUser();

            showAlert("Success", "Logged in as " + selectedRole);
            showAlert("Success", "Redirecting to the " + accountBean.getRole() + " dashboard...");
            goToHome(event);

        } catch (IllegalArgumentException ex) {
            showAlert(ERROR, ex.getMessage());
        }
    }

    @FXML
    public void goToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Parent root = loader.load();

            // Inizializza la Home con entrambi i parametri
            HomeGraphicControllerColored controller = loader.getController();

            // Se l'utente è loggato, passiamo i dati
            if (sessionId != null && userBean != null) {
                controller.initData(sessionId, userBean);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Home");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the Home screen.");
        }
    }

    @FXML
    public void goToSignUp(ActionEvent event) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/fxml/SignUp.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(signUpRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Sign Up");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the SignUp screen.");
        }
    }


    @FXML
    private void goToManageNoticeBoard(ActionEvent event) {
        showAlert("Manage Notice Board", "You must be logged in to manage the notice board.");
    }

    @FXML
    private void goToLeaveASharedReview(ActionEvent event) {
        showAlert("Leave a Shared Review", "You must be logged in to leave a shared review.");
    }

    @FXML
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
