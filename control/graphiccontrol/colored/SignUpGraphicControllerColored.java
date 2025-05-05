package logic.control.graphiccontrol.colored;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import logic.bean.AccountBean;
import logic.bean.AvailabilityBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.SignUpController;
import logic.model.domain.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SignUpGraphicControllerColored {

    private static final String STUDENT = "Student";
    private static final String TUTOR = "Tutor";
    private static final String ERROR = "Error";

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private DatePicker birthdayPicker;

    @FXML
    private HBox instituteHBox;

    @FXML
    private TextField profilePictureField;

    @FXML
    private TextField profileCommentField;

    @FXML
    private Label instituteLabel;

    @FXML
    private TextField instituteField;

    @FXML
    private TextField educationalTitleField;

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
    private TextField locationField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private CheckBox mondayCheck;

    @FXML
    private CheckBox tuesdayCheck;

    @FXML
    private CheckBox wednesdayCheck;

    @FXML
    private CheckBox thursdayCheck;

    @FXML
    private CheckBox fridayCheck;

    @FXML
    private CheckBox saturdayCheck;

    @FXML
    private CheckBox sundayCheck;

    @FXML
    private ComboBox<String> subjectComboBox;

    @FXML
    private TextField hourlyRateField;

    @FXML
    private CheckBox inPersonTutorCheck;

    @FXML
    private CheckBox onlineTutorCheck;

    @FXML
    private CheckBox groupTutorCheck;

    @FXML
    private CheckBox firstLessonFreeCheck;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink backToLogin;

    @FXML
    public void initialize() {
        if (roleComboBox != null) {
            roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                boolean isStudent = STUDENT.equalsIgnoreCase(newVal);
                instituteHBox.setVisible(isStudent);
                instituteHBox.setManaged(isStudent);
            });

            String currentRole = roleComboBox.getValue();
            if (currentRole != null) {
                boolean isStudent = STUDENT.equalsIgnoreCase(currentRole);
                instituteHBox.setVisible(isStudent);
                instituteHBox.setManaged(isStudent);
            }
        }
    }


    @FXML
    public static void showSignUpScene(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(SignUpGraphicControllerColored.class.getResource("/fxml/SignUp.fxml"));
            Parent signUpRoot = loader.load();
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
    public void handleRegister(ActionEvent event) {

        String name = nameField.getText();
        String surname = surnameField.getText();
        LocalDate birthday = birthdayPicker.getValue();
        String institute = instituteField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();
        String profilePic = profilePictureField.getText();
        String comment = profileCommentField.getText();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showAlert(ERROR, "All fields are required.");
            return;
        }

        // Creazione UserBean + AccountBean
        UserBean userBean = new UserBean();
        userBean.setUsername(username);
        userBean.setEmail(email);

        AccountBean accountBean = new AccountBean();
        accountBean.setRole(role);
        accountBean.setPassword(passwordField.getText());
        accountBean.setConfirmPassword(confirmPasswordField.getText());
        accountBean.setRole(role);
        accountBean.setName(name);
        accountBean.setSurname(surname);
        accountBean.setBirthday(birthday);
        accountBean.setProfilePicturePath(profilePic);
        accountBean.setProfileComment(comment);

        // VALIDATION: always basic checks first
        try {
            userBean.checkEmailSyntax();
            userBean.checkUsernameSyntax();
            accountBean.checkBasicSyntax();
            accountBean.checkPasswordSyntax();
        } catch (IllegalArgumentException ex) {
            showAlert("Error", ex.getMessage());
            return;
        }

        if (STUDENT.equalsIgnoreCase(role)) {
            accountBean.setInstitute(institute);

            try {
                accountBean.checkStudentSyntax();
            } catch (IllegalArgumentException ex) { showAlert(ERROR, ex.getMessage()); return; }

            userBean.addAccount(accountBean);

            try {
                accountBean.checkStudentSyntax();
            } catch (IllegalArgumentException ex) {
                showAlert("Error", ex.getMessage());
                return;
            }

            // Registrazione diretta per Studente
            SignUpController signUpController = new SignUpController();
            boolean success = signUpController.registerUser(userBean);
            if (success) {
                showAlert("Sign up completed", "The user has been registered successfully!");
                goToLogin();
            } else {
                showAlert(ERROR, "An account with this role already exists.");
            }

        } else if (TUTOR.equalsIgnoreCase(role)) {
            userBean.addAccount(accountBean);
            // Modifica: salvo nel SessionManager UserBean parziale
            // SessionManager.setUserBean(userBean);
            SignUpController signUpController = new SignUpController();
            signUpController.cachePartialTutor(userBean);
            goToSignUpTutor(event);
        }
    }

    @FXML
    public void handleTutorData(ActionEvent event) {

        // 1) Recupero i dati base dal SessionManager
        UserBean userBean = SessionManager.getUserBean();

        if (userBean == null) {
            showAlert(ERROR, "No partial data found. Did you skip the first step?");
            return;
        }

        AccountBean accountBean = userBean.getAccounts().stream()
                .filter(acc -> TUTOR.equalsIgnoreCase(acc.getRole()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Tutor account found."));

        String location = locationField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String subject = subjectComboBox.getValue();
        String educationalTitle = educationalTitleField.getText();
        float hourlyRate = Float.parseFloat(hourlyRateField.getText());
        boolean offersInPerson = inPersonTutorCheck.isSelected();
        boolean offersOnline = onlineTutorCheck.isSelected();
        boolean offersGroup = groupTutorCheck.isSelected();
        boolean firstLessonFree = firstLessonFreeCheck.isSelected();

        // Costruzione dell'elemento Availability del Tutor
        List<DayOfWeek> selectedDays = new ArrayList<>();
        if (mondayCheck.isSelected()) selectedDays.add(DayOfWeek.MONDAY);
        if (tuesdayCheck.isSelected()) selectedDays.add(DayOfWeek.TUESDAY);
        if (wednesdayCheck.isSelected()) selectedDays.add(DayOfWeek.WEDNESDAY);
        if (thursdayCheck.isSelected()) selectedDays.add(DayOfWeek.THURSDAY);
        if (fridayCheck.isSelected()) selectedDays.add(DayOfWeek.FRIDAY);
        if (saturdayCheck.isSelected()) selectedDays.add(DayOfWeek.SATURDAY);
        if (sundayCheck.isSelected()) selectedDays.add(DayOfWeek.SUNDAY);

        accountBean.setLocation(location);

        AvailabilityBean availB = new AvailabilityBean();

        availB.setStartDate(startDate);
        availB.setEndDate(endDate);
        availB.setDays(selectedDays);
        accountBean.setAvailabilityBean(availB);
        accountBean.setSubject(subject);
        accountBean.setEducationalTitle(educationalTitle);
        accountBean.setHourlyRate(hourlyRate);
        accountBean.setOffersInPerson(offersInPerson);
        accountBean.setOffersOnline(offersOnline);
        accountBean.setOffersGroup(offersGroup);
        accountBean.setFirstLessonFree(firstLessonFree);

        try {
            accountBean.checkBasicSyntax();
            accountBean.checkPasswordSyntax();
            accountBean.checkTutorSyntax();
        } catch (IllegalArgumentException ex) {
          showAlert(ERROR, ex.getMessage());
          return;
        }

        SignUpController signUpController = new SignUpController();
        // Controllo se la registrazione dell'account è avvenuta con successo
        boolean success = signUpController.registerUser(userBean);

        if (success) {
            showAlert("Sign up completed", "The user has been registered successfully!");
            signUpController.clearPartialTutor();
            goToLogin1();
        } else {
            showAlert(ERROR, "An account with this role already exists.");
        }
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        // Imposta filtri, ad es. solo immagini
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            profilePictureField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    public void goToLogin() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) backToLogin.getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(loginRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the Login screen.");
        }
    }

    @FXML
    public void goToLogin1() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(loginRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the Login screen.");
        }
    }

    @FXML
    public void goToHome(ActionEvent event) {
        try {
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
    private void goToManageNoticeBoard(ActionEvent event) {
        showAlert("Booking", "You must be logged in to manage the notice board.");
    }

    @FXML
    public void goToSignUpTutor(ActionEvent event) {
        try {
            Parent tutorRoot = FXMLLoader.load(getClass().getResource("/fxml/SignUpTutor.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(tutorRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Sign Up Tutor");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the SignUpTutor screen.");
        }
    }

    @FXML
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Recupera lo Stage dal DialogPane
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();

        // Imposta l'icona personalizzata per la finestra (in alto a sinistra)
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        alert.showAndWait();
    }
}
