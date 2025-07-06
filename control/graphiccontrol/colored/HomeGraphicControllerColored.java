package logic.control.graphiccontrol.colored;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import logic.bean.AccountBean;
import logic.bean.AvailabilityBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.HomeController;
import logic.control.logiccontrol.LeaveASharedReviewController;
import logic.control.logiccontrol.ManageNoticeBoardController;
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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class HomeGraphicControllerColored extends Application {

    private static final String ROLE_STUDENT = "Student";
    private static final String ROLE_TUTOR = "Tutor";
    private static final Logger LOGGER = Logger.getLogger(HomeGraphicControllerColored.class.getName());

    @FXML
    private AnchorPane root;

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

    // Distanza tra lo spigolo del pulsante e il centro del cerchietto
    private static final double BADGE_SHIFT_X = 3;   // verso destra
    private static final double BADGE_SHIFT_Y = 5;   // verso il basso

    private final HomeController homeController = new HomeController();
    private UUID sessionId;
    private UserBean userBean;

    public void initData(UUID sessionId, UserBean userBean) {
        this.sessionId = sessionId;
        this.userBean  = userBean;
        showLoggedInUI(userBean);
    }

    @FXML
    public void initialize() {

        showLoggedOutUI();

        // Comando necessario per posizionare dinamicamente i pulsanti e la label
        Platform.runLater(() -> {
            updateBadgePosition(manageNoticeBoardButton, redCircle1, redDotLabel1);
            updateBadgePosition(leaveASharedReviewButton, redCircle2, redDotLabel2);
        });

        addRepositionListeners(manageNoticeBoardButton, redCircle1, redDotLabel1);
        addRepositionListeners(leaveASharedReviewButton, redCircle2, redDotLabel2);
    }

    // Aggancia alcuni listener alle proprietà che cambiano quando la finestra si ridimensiona
    private void addRepositionListeners(Button source, Circle badge, Label text) {
        ChangeListener<Number> l = (obs, o, n) -> updateBadgePosition(source, badge, text);

        Stream.of(source.layoutXProperty(), source.layoutYProperty(),
                        source.widthProperty(),  source.heightProperty(),
                        root.widthProperty(),    root.heightProperty())
                .forEach(p -> p.addListener(l));
    }

    // Calcola l’angolo in alto a destra del pulsante (in coordinate root) */
    private void updateBadgePosition(Button source, Circle badge, Label text) {
        Bounds btnScene = source.localToScene(source.getBoundsInLocal());
        Point2D inRoot   = root.sceneToLocal(btnScene.getMaxX(), btnScene.getMinY());

        // Sposta il cerchio: appena fuori dallo spigolo alto‑destro
        double cx = inRoot.getX() + BADGE_SHIFT_X - badge.getRadius();
        double cy = inRoot.getY() + BADGE_SHIFT_Y - badge.getRadius();

        badge.setLayoutX(cx);
        badge.setLayoutY(cy);

        // Centra il numero dentro il cerchio
        text.setLayoutX(cx - text.getWidth()  / 2);
        text.setLayoutY(cy - text.getHeight() / 2);
    }

    // Helper privati per una migliore fattorizzazione
    private void showLoggedOutUI() {
        welcomeLabel.setVisible(false);
        logOutButton.setVisible(false);
        logInButton.setVisible(true);
        signUpButton.setVisible(true);
        redDotLabel1.setVisible(false);
        redCircle1.setVisible(false);
        redDotLabel2.setVisible(false);
        redCircle2.setVisible(false);
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

        System.out.println("DEBUG - In HomeGraphicController, account disponibili:");
        for (AccountBean ab : user.getAccounts()) {
            System.out.println(" - Role: " + ab.getRole());
        }

        for (AccountBean account : user.getAccounts()) {
            String accRole = account.getRole();
            if (ROLE_STUDENT.equalsIgnoreCase(accRole) || ROLE_TUTOR.equalsIgnoreCase(accRole)) {
                accountId = account.getAccountId();
                role = ROLE_STUDENT.equalsIgnoreCase(accRole) ? ROLE_STUDENT : ROLE_TUTOR;
                break;
            }
        }

        if (accountId == null || role == null) {
            throw new IllegalStateException("No valid account (Student or Tutor) found for logged user.");
        }

        // Pallino rosso “Notice Board”
        ManageNoticeBoardController mCtrl = new ManageNoticeBoardController(sessionId);
        toggleRedDot(mCtrl.countNewRequests(accountId, role), redDotLabel1, redCircle1);

        // Pallino rosso “Shared Reviews”
        LeaveASharedReviewController rCtrl = new LeaveASharedReviewController();
        int pending = ROLE_STUDENT.equalsIgnoreCase(role)
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

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // logout tramite HomeController
            if (sessionId != null) {
                homeController.logout(sessionId);
                sessionId = null;
                userBean  = null;
            }

            showLoggedOutUI();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent startRoot = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(startRoot, screenBounds.getWidth(), screenBounds.getHeight());
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
        LoginGraphicControllerColored loginGraphicControllerColored = new LoginGraphicControllerColored();
        loginGraphicControllerColored.showLoginScene(event);
    }

    @FXML
    private void goToSignUp(ActionEvent event) {
        SignUpGraphicControllerColored.showSignUpScene(event);
    }

    @FXML
    private void goToBookingTutoringSession(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookingTutoringSession.fxml"));
            Parent bookingRoot = loader.load();

            BookingSessionGraphicControllerColored bookingSessionGraphicControllerColored = loader.getController();
            bookingSessionGraphicControllerColored.initData(sessionId, userBean);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(bookingRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Book a Tutoring Session");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "An error occurred while loading the Tutor List screen.", e);
        }
    }

    @FXML
    private void goToManageNoticeBoard(ActionEvent event) {

        if (userBean == null) {
            showAlert("Booking", "You must be logged in to manage the notice board.");
            goToLogin(event);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManageNoticeBoard.fxml"));
            Parent noticeBoardRoot  = loader.load();
            ManageNoticeBoardGraphicControllerColored manageNoticeBoardGraphicControllerColored = loader.getController();
            manageNoticeBoardGraphicControllerColored.initData(sessionId, userBean);
            Rectangle2D sb  = Screen.getPrimary().getVisualBounds();
            Scene scene  = new Scene(noticeBoardRoot, sb.getWidth(), sb.getHeight());
            Stage stage  = (Stage) manageNoticeBoardButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Manage Notice Board");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLeaveASharedReview(ActionEvent event) {

        if (userBean == null) {
            showAlert("Booking", "You must be logged in to leave a shared review.");
            goToLogin(event);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LeaveASharedReview.fxml"));
            Parent reviewRoot = loader.load();

            LeaveASharedReviewGraphicControllerColored leaveASharedReviewGraphicControllerColored = loader.getController();
            leaveASharedReviewGraphicControllerColored.initData(sessionId, userBean);

            Rectangle2D sb = Screen.getPrimary().getVisualBounds();
            Scene scene  = new Scene(reviewRoot, sb.getWidth(), sb.getHeight());
            Stage stage = (Stage) leaveASharedReviewButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Leave a Shared Review");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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

        AvailabilityBean availabilityBean = new AvailabilityBean();

        availabilityBean.setStartDate(startDate);
        availabilityBean.setEndDate(endDate);
        availabilityBean.setDays(selectedDays);
        try {
            availabilityBean.checkSyntax();
        } catch (IllegalArgumentException ex) {
            showAlert("Search error", ex.getMessage());
            return;
        }

        // Imposto i parametri nel BookingSessionGraphicControllerColored
        BookingSessionGraphicControllerColored.setSearchParameters(chosenLocation, chosenSubject, availabilityBean);

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
