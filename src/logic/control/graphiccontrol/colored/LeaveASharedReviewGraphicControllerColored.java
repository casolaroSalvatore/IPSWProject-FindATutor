package logic.control.graphiccontrol.colored;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.bean.SharedReviewBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.LeaveASharedReviewController;
import logic.model.domain.ReviewStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

public class LeaveASharedReviewGraphicControllerColored implements NavigableController {

    // *** FXML comuni: logInButton, signUpButton, welcomeLabel, logOutButton
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

    // Elementi FXML per gestire le recensioni

    // Pane per Studente
    @FXML
    private AnchorPane studentPane;

    @FXML
    private TableView<SharedReviewBean> studentTable;

    @FXML
    private TableColumn<SharedReviewBean, String> tutorInfoColumnStudent;

    @FXML
    private TableColumn<SharedReviewBean, String> tutorSubjectColumnStudent;

    @FXML
    private TableColumn<SharedReviewBean, String> tutorLocationColumnStudent;

    @FXML
    private TableColumn<SharedReviewBean, String> statusColumnStudent;

    @FXML
    private VBox studentReviewForm; // form di compilazione
    @FXML
    private TextField studentReviewTitle;
    @FXML
    private HBox starContainer;
    @FXML
    private ImageView star1;
    @FXML
    private ImageView star2;
    @FXML
    private ImageView star3;
    @FXML
    private ImageView star4;
    @FXML
    private ImageView star5;
    @FXML
    private TextArea studentReviewComment;

    @FXML
    private HBox studentCompletedBox; // box in cui visualizzo la review completata
    @FXML
    private TextField studentCompletedTitle;
    @FXML
    private HBox studentCompletedStarsBox;
    @FXML
    private TextArea studentCompletedComment;
    @FXML
    private TextField studentCompletedTutorTitle;
    @FXML
    private TextArea studentCompletedTutorComment;

    // Pane per Tutor
    @FXML
    private AnchorPane tutorPane;

    @FXML
    private TableView<SharedReviewBean> tutorTable;

    @FXML
    private TableColumn<SharedReviewBean, String> studentInfoColumnTutor;

    @FXML
    private TableColumn<SharedReviewBean, String> studentSubjectColumnTutor;

    @FXML
    private TableColumn<SharedReviewBean, String> studentLocationColumnTutor;

    @FXML
    private TableColumn<SharedReviewBean, String> statusColumnTutor;

    @FXML
    private VBox tutorReviewForm; // form di compilazione
    @FXML
    private TextField tutorReviewTitle;
    @FXML
    private TextArea tutorReviewComment;

    @FXML
    private HBox tutorCompletedBox; // box per mostrare i dati completati
    @FXML
    private TextField tutorCompletedTitle;
    @FXML
    private TextArea tutorCompletedComment;
    @FXML
    private TextField tutorCompletedStudentTitle;
    @FXML
    private HBox tutorCompletedStudentStarsBox;
    @FXML
    private TextArea tutorCompletedStudentComment;

    // Campi per salvare la review selezionata
    private SharedReviewBean selectedStudentReview;
    private SharedReviewBean selectedTutorReview;

    // Variabile per memorizzare stelle scelte
    private int selectedStars = 0;
    // Immagini delle stelle
    private Image emptyStar;
    private Image fullStar;

    private LeaveASharedReviewController leaveASharedReviewController;
    private UUID sessionId;
    private UserBean userBean;

    @FXML
    public void initialize() {
        // Solo caricamento delle immagini o risorse neutre
        emptyStar = new Image(getClass().getResourceAsStream("/images/empty_star.png"));
        fullStar = new Image(getClass().getResourceAsStream("/images/full_star.png"));
    }

    // Inizializza la sessione e la UI in base al ruolo utente, configura le tabelle e i badge
    @Override
    public void initData(UUID sid, UserBean user) {

        this.sessionId = sid;
        this.userBean = user;

        if (user == null) {
            showLoggedOutUI();
            return;
        }

        // Istanzio il controller logico con la sessione
        this.leaveASharedReviewController = new LeaveASharedReviewController(sid);


        showLoggedInUI(user);
        configureStudentTableColumns();
        configureTutorTableColumns();
        configureStudentRowFactory();
        configureTutorRowFactory();
        showPaneByRoleAndLoadData();
        initializeStarRating();
    }

    // Mostra la UI per utente disconnesso
    private void showLoggedOutUI() {
        welcomeLabel.setVisible(false);
        logInButton.setVisible(true);
        signUpButton.setVisible(true);
        logOutButton.setVisible(false);
        studentPane.setVisible(false);
        tutorPane.setVisible(false);
    }

    // Mostra la UI per utente loggato e configura pulsanti
    private void showLoggedInUI(UserBean user) {
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        welcomeLabel.setVisible(true);
        logOutButton.setVisible(true);
        logInButton.setVisible(false);
        signUpButton.setVisible(false);
    }

    // Configura le colonne della tabella dello studente
    private void configureStudentTableColumns() {

        tutorInfoColumnStudent.setCellValueFactory(cd ->               /// info già calcolata nel bean
                new SimpleStringProperty(cd.getValue().getCounterpartyInfo()));

        tutorSubjectColumnStudent.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTutorAccount().getSubject()));

        tutorLocationColumnStudent.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTutorAccount().getLocation()));

        statusColumnStudent.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getStatus().name()));
    }

    // Configura le colonne della tabella del tutor
    private void configureTutorTableColumns() {

        studentInfoColumnTutor.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCounterpartyInfo()));

        studentSubjectColumnTutor.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTutorAccount().getSubject()));

        studentLocationColumnTutor.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTutorAccount().getLocation()));

        statusColumnTutor.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getStatus().name()));
    }

    // Configura il doppio clic sulle righe della tabella studente per mostrare profilo tutor
    private void configureStudentRowFactory() {
        studentTable.setRowFactory(tv -> {
            TableRow<SharedReviewBean> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() &&
                        ev.getButton() == MouseButton.PRIMARY &&  // ⇐ controllo corretto sul tasto sinistro
                        ev.getClickCount() == 2) {
                    SharedReviewBean sr = row.getItem();
                    if (sr != null && sr.getCounterpartAccount() != null) {
                        showTutorProfile(sr.getCounterpartAccount().getAccountId());
                    }
                }
            });
            return row;
        });
    }

    // Configura il doppio clic sulle righe della tabella tutor per mostrare profilo studente
    private void configureTutorRowFactory() {
        tutorTable.setRowFactory(tv -> {
            TableRow<SharedReviewBean> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() &&
                        ev.getButton() == MouseButton.PRIMARY &&  // ⇐ controllo corretto sul tasto sinistro
                        ev.getClickCount() == 2) {
                    SharedReviewBean sr = row.getItem();
                    if (sr != null && sr.getCounterpartAccount() != null) {
                        showStudentProfile(sr.getCounterpartAccount().getAccountId());
                    }
                }
            });
            return row;
        });
    }

    // Mostra il pannello corretto in base al ruolo e carica i dati
    private void showPaneByRoleAndLoadData() {

        String role = leaveASharedReviewController.getLoggedRole(sessionId);
        String accountId = leaveASharedReviewController.getLoggedAccountId(sessionId);

        if (role == null || accountId == null) {
            throw new IllegalStateException("No valid account found for logged user.");
        }

        if ("Student".equalsIgnoreCase(role)) {
            studentPane.setVisible(true);
            studentPane.setManaged(true);
            tutorPane.setVisible(false);
            tutorPane.setManaged(false);
            loadTutorListAndBuildReviews(accountId);
        } else {
            tutorPane.setVisible(true);
            tutorPane.setManaged(true);
            studentPane.setVisible(false);
            studentPane.setManaged(false);
            loadStudentListAndBuildReviews(accountId);
        }
    }

    // Carica i tutor per uno studente e costruisce le review
    private void loadTutorListAndBuildReviews(String studentId) {
        var tutorIds = leaveASharedReviewController.findAllTutorsForStudent(studentId);
        List<SharedReviewBean> rows = new ArrayList<>();
        for (String tutorId : tutorIds) {
            SharedReviewBean sharedReviewBean = leaveASharedReviewController.findOrCreateSharedReviewBean(studentId, tutorId);
            rows.add(sharedReviewBean);
        }
        studentTable.setItems(FXCollections.observableArrayList(rows));
    }

    // Carica gli studenti per un tutor e costruisce le review
    private void loadStudentListAndBuildReviews(String tutorId) {
        var studentIds = leaveASharedReviewController.findAllStudentsForTutor(tutorId);
        List<SharedReviewBean> rows = new ArrayList<>();
        for (String studentId : studentIds) {
            SharedReviewBean sharedReviewBean = leaveASharedReviewController.findOrCreateSharedReviewBean(studentId, tutorId);
            rows.add(sharedReviewBean);
        }
        tutorTable.setItems(FXCollections.observableArrayList(rows));
    }

    // Gestisce il clic su una riga della tabella studente e aggiorna il form
    @FXML
    public void handleStudentTableClick(MouseEvent event) {
        SharedReviewBean sr = studentTable.getSelectionModel().getSelectedItem();
        if (sr == null) return;

        sr = leaveASharedReviewController.findOrCreateSharedReviewBean(sr.getStudentId(), sr.getTutorId());
        selectedStudentReview = sr;
        studentTable.refresh();

        // Se COMPLETA => nascondo form, mostro "studentCompletedBox"
        if (ReviewStatus.COMPLETE.equals(sr.getStatus())) {
            studentReviewForm.setVisible(false);
            studentReviewForm.setManaged(false);

            // Riempi i campi
            studentCompletedTitle.setText(sr.getStudentTitle());
            displayStars(studentCompletedStarsBox, sr.getStudentStars());
            studentCompletedComment.setText(sr.getStudentComment());

            studentCompletedTutorTitle.setText(sr.getTutorTitle());
            studentCompletedTutorComment.setText(sr.getTutorComment());

            studentCompletedBox.setVisible(true);
            studentCompletedBox.setManaged(true);
        } else {
            // NOT_STARTED o PENDING
            studentCompletedBox.setVisible(false);
            studentCompletedBox.setManaged(false);

            // Se lo studente non ha ancora inviato => form compilabile
            if (!sr.isStudentSubmitted()) {
                studentReviewForm.setVisible(true);
                studentReviewForm.setManaged(true);

                studentReviewTitle.setText(sr.getStudentTitle());
                updateStarDisplay();
                studentReviewComment.setText(sr.getStudentComment());
            } else {
                // Studente ha già inviato => disabiliti form
                studentReviewTitle.setDisable(true);
                starContainer.setDisable(true);
                studentReviewComment.setDisable(true);
            }
        }
    }

    // Gestisce il clic su una riga della tabella tutor e aggiorna il form
    @FXML
    public void handleTutorTableClick(MouseEvent event) {
        SharedReviewBean sr = tutorTable.getSelectionModel().getSelectedItem();
        if (sr == null) return;

        sr = leaveASharedReviewController.findOrCreateSharedReviewBean(sr.getStudentId(), sr.getTutorId());

        selectedTutorReview = sr;

        tutorTable.refresh();

        if (ReviewStatus.COMPLETE.equals(sr.getStatus())) {
            tutorReviewForm.setVisible(false);
            tutorReviewForm.setManaged(false);

            // Riempi i campi
            tutorCompletedTitle.setText(sr.getTutorTitle());
            tutorCompletedComment.setText(sr.getTutorComment());

            tutorCompletedStudentTitle.setText(sr.getStudentTitle());
            displayStars(tutorCompletedStudentStarsBox, sr.getStudentStars());
            tutorCompletedStudentComment.setText(sr.getStudentComment());

            tutorCompletedBox.setVisible(true);
            tutorCompletedBox.setManaged(true);
        } else {
            tutorCompletedBox.setVisible(false);
            tutorCompletedBox.setManaged(false);

            if (!sr.isTutorSubmitted()) {
                tutorReviewForm.setVisible(true);
                tutorReviewForm.setManaged(true);

                tutorReviewTitle.setText(sr.getTutorTitle());
                tutorReviewComment.setText(sr.getTutorComment());
            } else {
                tutorReviewTitle.setDisable(true);
                tutorReviewComment.setDisable(true);
            }
        }
    }


    // Gestisce l'invio della recensione lato studente
    @FXML
    public void handleStudentSubmit(ActionEvent event) {
        if (selectedStudentReview == null) {
            showAlert("No review selected", "Select a row first.");
            return;
        }
        try {
            int stars = (int) selectedStars;
            String title = studentReviewTitle.getText();
            String comment = studentReviewComment.getText();

            // Imposta i nuovi dati dentro il bean
            selectedStudentReview.setStudentStars(stars);
            selectedStudentReview.setStudentTitle(title);
            selectedStudentReview.setStudentComment(comment);
            selectedStudentReview.setSenderRole(SharedReviewBean.SenderRole.STUDENT); // importantissimo

            // Chiamo il nuovo metodo generico
            leaveASharedReviewController.submitReview(selectedStudentReview);

            studentTable.refresh();
            showAlert("Review sent", "Your student-side review has been submitted!");
        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Stars must be an integer.");
        }
    }

    // Inizializza le stelle cliccabili per lo studente
    private void initializeStarRating() {
        List<ImageView> stars = List.of(star1, star2, star3, star4, star5);

        for (int i = 0; i < 5; i++) {
            ImageView iv = stars.get(i);
            iv.setImage(emptyStar);
            iv.setPickOnBounds(true);
            final int index = i;
            iv.setOnMouseClicked(e -> {
                selectedStars = (index + 1);
                updateStarDisplay();
            });
        }
    }

    private void updateStarDisplay() {
        List<ImageView> stars = List.of(star1, star2, star3, star4, star5);
        for (int i = 0; i < 5; i++) {
            stars.get(i).setImage(i < selectedStars ? fullStar : emptyStar);
        }
    }

    private void displayStars(HBox box, int count) {
        box.getChildren().clear();
        for (int i = 0; i < 5; i++) {
            ImageView iv = new ImageView(i < count ? fullStar : emptyStar);
            iv.setFitWidth(24);
            iv.setFitHeight(24);
            box.getChildren().add(iv);
        }
    }

    // Gestisce l'invio della recensione lato tutor
    @FXML
    public void handleTutorSubmit(ActionEvent event) {
        if (selectedTutorReview == null) {
            showAlert("No review selected", "Select a row first.");
            return;
        }

        String title = tutorReviewTitle.getText();
        String comment = tutorReviewComment.getText();

        // Imposta i nuovi dati dentro il bean
        selectedTutorReview.setTutorTitle(title);
        selectedTutorReview.setTutorComment(comment);
        selectedTutorReview.setSenderRole(SharedReviewBean.SenderRole.TUTOR); // importantissimo

        // Chiamo il nuovo metodo generico
        leaveASharedReviewController.submitReview(selectedTutorReview);

        tutorTable.refresh();
        showAlert("Review sent", "Tutor-side review has been submitted!");
    }

    // Mostra il profilo del tutor selezionato
    @FXML
    private void showTutorProfile(String tutorAccountId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TutorProfile.fxml"));
            Parent root = loader.load();

            TutorProfileGraphicControllerColored controller = loader.getController();
            controller.setTutorData(tutorAccountId);

            Stage stage = new Stage();
            stage.setTitle("Tutor Profile");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mostra il profilo dello studente selezionato
    private void showStudentProfile(String studentAccountId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentProfile.fxml"));
            Parent root = loader.load();

            StudentProfileGraphicControllerColored controller = loader.getController();
            controller.setStudentData(studentAccountId);

            Stage stage = new Stage();
            stage.setTitle("Student Profile");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void goToHome(ActionEvent event) {
        SceneNavigator.navigate("/fxml/Home.fxml", (Node) event.getSource(), sessionId, userBean, "Home");
    }


    @FXML
    private void goToManageNoticeBoard(ActionEvent event) {
        SceneNavigator.navigate("/fxml/ManageNoticeBoard.fxml", (Node) event.getSource(), sessionId, userBean, "Manage Notice Board");
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
            leaveASharedReviewController.logout(sessionId);

            goToHome(event);

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
