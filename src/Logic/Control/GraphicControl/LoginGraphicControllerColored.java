package Logic.Control.GraphicControl;

import Logic.Bean.LoginBean;
import Logic.Control.LogicControl.LoginController;
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
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Login");
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
        LoginBean bean = new LoginBean(email, password);
        bean.setSelectedRole(selectedRole);

        boolean success = loginController.login(bean);

        if (success) {
            showAlert("Success", "Logged in as " + selectedRole);
            proceedToDashboard(bean, event);
        } else {
            showAlert("Error", "The selected role is not associated with this account.");
        }
    }

    private void proceedToDashboard(LoginBean bean, ActionEvent event) {
        // Qui potresti caricare la pagina principale dell'utente in base al ruolo scelto
        showAlert("Success", "Redirecting to the " + bean.getSelectedRole() + " dashboard...");
        goToHome(event);
    }

    @FXML
    public void goToHome(ActionEvent event) {
        try {
            // Torna alla schermata di login
            Parent homeRoot = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the Home screen.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goToSignUp(ActionEvent event) {
        try {
            // Carica la schermata Login
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/SignUp.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Sign Up");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the SignUp screen.");
        }
    }
}
