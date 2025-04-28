package logic.control.graphiccontrol.colored;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import logic.bean.AccountBean;
import logic.bean.SignUpBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.LeaveASharedReviewController;
import logic.control.logiccontrol.ManageNoticeBoardController;
import logic.model.domain.Availability;
import logic.model.domain.SessionManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HomeGraphicControllerColored extends Application {

    @FXML
    private Button logInButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logOutButton;

    @FXML
    private Button manageNoticeBoardButton;

    @FXML
    private Button leaveASharedReviewButton;

    @FXML private TextField locationField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField subjectField;
    @FXML private CheckBox mondayCheck;
    @FXML private CheckBox tuesdayCheck;
    @FXML private CheckBox wednesdayCheck;
    @FXML private CheckBox thursdayCheck;
    @FXML private CheckBox fridayCheck;
    @FXML private CheckBox saturdayCheck;
    @FXML private CheckBox sundayCheck;

    @FXML private Circle redCircle1;
    @FXML private Label redDotLabel1;
    @FXML private Circle redCircle2;
    @FXML private Label redDotLabel2;

    @FXML
    public void initialize() {
        if (SessionManager.getLoggedUser() == null) {
            showLoggedOutUI();
            return;
        }
        UserBean user = SessionManager.getLoggedUser();
        showLoggedInUI(user);
    }

    // Helper privati per una migliore fattorizzazione
    private void showLoggedOutUI() {
        welcomeLabel.setVisible(false);
        logOutButton.setVisible(false);
        logInButton.setVisible(true);
        signUpButton.setVisible(true);
    }

    private void showLoggedInUI(UserBean user) {
        // Messaggio di benvenuto + pulsanti
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        welcomeLabel.setVisible(true);
        logOutButton.setVisible(true);
        logInButton.setVisible(false);
        signUpButton.setVisible(false);

        String accountId = null;
        String role = null;

        for (AccountBean account : user.getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                accountId = account.getAccountId();
                role = "Student";
                break;
            } else if ("Tutor".equalsIgnoreCase(account.getRole())) {
                accountId = account.getAccountId();
                role = "Tutor";
                break;
            }
        }

        if (accountId == null || role == null) {
            throw new IllegalStateException("No valid account (Student or Tutor) found for logged user.");
        }

        // Pallino rosso “Notice Board”
        ManageNoticeBoardController mCtrl = new ManageNoticeBoardController();
        toggleRedDot(mCtrl.countNewRequests(accountId, role), redDotLabel1, redCircle1);

        // Pallino rosso “Shared Reviews”
        LeaveASharedReviewController rCtrl = new LeaveASharedReviewController();
        int pending = "Student".equalsIgnoreCase(role)
                ? rCtrl.countPendingForStudent(accountId)
                : rCtrl.countPendingForTutor(accountId);
        toggleRedDot(pending, redDotLabel2, redCircle2);
    }

    private void toggleRedDot(int count, Label dot, Circle circle) {
        boolean visible = count > 0;
        dot.setVisible(visible);
        circle.setVisible(visible);
        if (visible) {
            dot.setText(String.valueOf(count));
        }
    }


    @FXML
    public void handleLogOut(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("Click OK to end your current session.");

        // Mostriamo la finestra di dialogo e attendiamo la risposta
        Optional<ButtonType> result = alert.showAndWait();

        // Se l'utente ha premuto OK, effettuiamo il logout
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 1) Azzeri la sessione, se hai un SessionManager
            SessionManager.logout();

            welcomeLabel.setVisible(false);
            logOutButton.setVisible(false);
            logInButton.setVisible(true);
            signUpButton.setVisible(true);
            redDotLabel1.setVisible(false);
            redCircle1.setVisible(false);
            redDotLabel2.setVisible(false);
            redCircle2.setVisible(false);

        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            primaryStage.setTitle("Schermata Home");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo statico per avviare JavaFX (richiamato dal Main)
    public static void launchGUI() {
        launch();
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        LoginGraphicControllerColored.showLoginScene(event);
    }

    @FXML
    private void goToSignUp(ActionEvent event) {
        SignUpGraphicControllerColored.showSignUpScene(event);
    }

    @FXML
    private void goToBookingTutoringSession(ActionEvent event) {
        BookingSessionGraphicControllerColored bookingSessionGraphicControllerColored = new BookingSessionGraphicControllerColored();
        bookingSessionGraphicControllerColored.showTutorListScene(event);
    }

    @FXML
    private void goToManageNoticeBoard(ActionEvent event) {
        // 1) Verifichiamo se l’utente è loggato (SessionManager.getLoggedUser() != null)
        if (SessionManager.getLoggedUser() == null) {
            // Utente NON loggato: reindirizziamo alla form di accesso (o Login)
            showAlert("Booking", "You must be logged in to manage the notice board.");
            try {
                Parent signUpRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
                Stage stage = (Stage) manageNoticeBoardButton.getScene().getWindow();
                stage.setScene(new Scene(signUpRoot));
                stage.setTitle("Error in accessing the notice board");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            BookingSessionGraphicControllerColored bookingSessionGraphicControllerColored = new BookingSessionGraphicControllerColored();
            bookingSessionGraphicControllerColored.goToManageNoticeBoard(event);
        }
    }

    @FXML
    private void goToLeaveASharedReview(ActionEvent event) {
        // 1) Verifichiamo se l’utente è loggato (SessionManager.getLoggedUser() != null)
        if (SessionManager.getLoggedUser() == null) {
            // Utente NON loggato: reindirizziamo alla form di accesso (o Login)
            showAlert("Booking", "You must be logged in to manage the notice board.");
            try {
                goToLogin(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Parent signUpRoot = FXMLLoader.load(getClass().getResource("/fxml/LeaveASharedReview.fxml"));
                Stage stage = (Stage) leaveASharedReviewButton.getScene().getWindow();
                stage.setScene(new Scene(signUpRoot));
                stage.setTitle("Error in accessing the notice board");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {

        String chosenLocation = locationField.getText();
        String chosenSubject = subjectField.getText();

        // Combino la data in un'unica stringa "From X to Y" giusto per il label
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Costruzione dell'elemento Availability
        List<DayOfWeek> selectedDays = new ArrayList<>();
        if (mondayCheck.isSelected()) selectedDays.add(DayOfWeek.MONDAY);
        if (tuesdayCheck.isSelected()) selectedDays.add(DayOfWeek.TUESDAY);
        if (wednesdayCheck.isSelected()) selectedDays.add(DayOfWeek.WEDNESDAY);
        if (thursdayCheck.isSelected()) selectedDays.add(DayOfWeek.THURSDAY);
        if (fridayCheck.isSelected()) selectedDays.add(DayOfWeek.FRIDAY);
        if (saturdayCheck.isSelected()) selectedDays.add(DayOfWeek.SATURDAY);
        if (sundayCheck.isSelected()) selectedDays.add(DayOfWeek.SUNDAY);

        Availability availability = new Availability(startDate, endDate, selectedDays);

        // 2) Imposto i parametri nel BookingSessionGraphicControllerColored
        BookingSessionGraphicControllerColored.setSearchParameters(chosenLocation, chosenSubject, availability);

        // 3) Vado alla scena "BookingTutoringSession.fxml"
        goToBookingTutoringSession(event);
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

