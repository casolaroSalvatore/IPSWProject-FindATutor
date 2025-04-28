package logic.control.graphiccontrol.colored;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
import logic.bean.UserBean;
import logic.control.logiccontrol.LoginController;
import logic.model.domain.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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

    @FXML
    public static void showLoginScene(ActionEvent event) {
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

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter email and password.");
            return;
        }

        // Controlliamo quale ruolo è stato selezionato dall'utente
        RadioButton selectedButton = (RadioButton) roleToggleGroup.getSelectedToggle();
        if (selectedButton == null) {
            showAlert("Error", "Please select a role.");
            return;
        }

        String selectedRole = selectedButton.getText(); // "Student" o "Tutor"

        // Creazione del Bean e chiamata al controller logico
        UserBean userBean = new UserBean();
        userBean.setEmail(email);
        userBean.setPassword(password);
        userBean.setSelectedRole(selectedRole);

        // Controllo sintattico
        try {
            userBean.checkLoginSyntax();
        }

        catch (IllegalArgumentException ex) { showAlert("Error", ex.getMessage()); return; }

        UserBean loggedProfile = loginController.login(userBean);

        if (loggedProfile != null) {
            showAlert("Success", "Logged in as " + selectedRole);

            // Salva il nome dell'utente nella Home
            SessionManager.setLoggedUser(loggedProfile);

            showAlert("Success", "Redirecting to the " + userBean.getSelectedRole() + " dashboard...");
            goToHome(event);

        } else {
            showAlert("Error", "The selected role is not associated with this account.");
        }
    }

    @FXML
    public void goToHome(ActionEvent event) {
        try {
            // 1) Carica l’FXML della Home
            Parent homeRoot = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(homeRoot, screenBounds.getWidth(), screenBounds.getHeight());
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
        showAlert("Booking", "You must be logged in to manage the notice board.");
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
