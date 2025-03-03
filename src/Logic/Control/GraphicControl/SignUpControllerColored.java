package Logic.Control.GraphicControl;

import Logic.Control.LogicControl.SignUpController;
import Logic.Bean.SignUpBean;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SignUpControllerColored {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField subjectField;

    @FXML
    private Label subjectLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink backToLogin;

    @FXML
    public static void showSignUpScene(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(SignUpControllerColored.class.getResource("/fxml/SignUp.fxml"));
            Parent signUpRoot = loader.load();
            Scene scene = new Scene(signUpRoot);
            stage.setScene(scene);
            stage.setTitle("SignUp");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Mostra il campo "Materia" solo se si seleziona "Tutor"
        roleComboBox.setOnAction(event -> {
            if ("Tutor".equals(roleComboBox.getValue())) {
                subjectField.setVisible(true);
                subjectLabel.setVisible(true);
            } else {
                subjectField.setVisible(false);
                subjectLabel.setVisible(false);
            }
        });
    }

    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();
        String subject = subjectField.getText();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showAlert("Error", "All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Errore", "Your passwords do not match.");
            return;
        }

        if ("Tutor".equals(role) && subject.isEmpty()) {
            showAlert("Error", "If you are a tutor, you have to specify a subject");
            return;
        }

        // Creazione del Bean
        SignUpBean bean = new SignUpBean(username,email, password, role, subject);
        SignUpController signUpController = new SignUpController();

        // Controllo se la registrazione dell'account è avvenuta con successo
        boolean success = signUpController.registerUser(bean);

        if (success) {
            showAlert("Sign up completed", "The user has been registered successfully!");
            goToLogin();
        } else {
            showAlert("Error", "An account with this role already exists.");
        }
    }

    // Fai si che la scena di login sia di competenza unicamente del LoginGraphicControllerColored
    @FXML
    public void goToLogin() {
        try {
            // Torna alla schermata di login
            javafx.scene.Parent loginRoot = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) backToLogin.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(loginRoot));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the Login screen.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}