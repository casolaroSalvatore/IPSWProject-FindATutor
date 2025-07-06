package logic.control.graphiccontrol.colored;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import logic.bean.*;
import logic.control.logiccontrol.BookingTutoringSessionController;
import logic.control.logiccontrol.LoginController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import logic.exception.NoTutorFoundException;
import logic.model.domain.state.TutoringSessionStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BookingSessionGraphicControllerColored implements NavigableController {

    // Per evitare ripetizioni
    private static final String ROLE_TUTOR = "Tutor";
    private static final String ROLE_STUDENT = "Student";
    private static final String BOOKING_TITLE = "Booking";
    private final BookingTutoringSessionController bookingCtrl = new BookingTutoringSessionController();

    // FXML comuni: logOutButton, signUpButton, logInButton, welcomeLabel...
    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logOutButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Button logInButton;

    @FXML
    private Button manageNoticeBoardButton;

    @FXML
    private Button leaveASharedReviewButton;

    // *** FXML per scena "BookingTutoringSession.fxml"
    @FXML
    private Label subjectLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label staticInLabel;

    @FXML
    private ComboBox<String> orderComboBox;

    @FXML
    private CheckBox inPersonCheck;

    @FXML
    private CheckBox onlineCheck;

    @FXML
    private CheckBox groupCheck;

    @FXML
    private CheckBox weekendCheck;

    @FXML
    private CheckBox eveningsCheck;

    @FXML
    private CheckBox holidaysCheck;

    @FXML
    private CheckBox rating4Check;

    @FXML
    private CheckBox firstLessonFreeCheck;

    @FXML
    private TableView<TutorBean> tutorTable;

    @FXML
    private TableColumn<TutorBean, String> nameColumn;

    @FXML
    private TableColumn<TutorBean, Double> ratingColumn;

    @FXML
    private TableColumn<TutorBean, Double> hourlyRateColumn;

    @FXML
    private Button bookTutoringSessionButton;

    @FXML
    private TableView<DayBookingBean> dayTable;

    @FXML
    private TableColumn<DayBookingBean, Boolean> selectColumn;

    @FXML
    private TableColumn<DayBookingBean, LocalDate> dateBookingColumn;

    @FXML
    private TableColumn<DayBookingBean, String> startTimeColumn;

    @FXML
    private TableColumn<DayBookingBean, String> endTimeColumn;

    @FXML
    private TableColumn<DayBookingBean, String> commentBookingColumn;

    // FXML per scena "ManageNoticeBoard.fxml"
    // Campi FXML per la tabella sessionTable
    @FXML
    public TableView<TutoringSessionBean> sessionTable; // (Solo manage notice board)

    @FXML
    private TableColumn<TutoringSessionBean, String> studentColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> tutorColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> dateColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> timeColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> locationColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> subjectColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> commentColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> statusColumn;

    // Colonna gestita da questo controller, per PENDING
    @FXML
    private TableColumn<TutoringSessionBean, Void> bookingActionColumn;

    // Colonna gestita dal ManageNoticeBoardGraphicControllerColored
    @FXML
    private TableColumn<TutoringSessionBean, Void> modCancelActionColumn;

    private final LoginController loginCtrl = new LoginController();
    private UUID sessionId;
    private UserBean userBean;

    // Riferimento al ManageNoticeBoardGraphicControllerColored (servirà per permettere l'aggiornamento
    // del calendario al seguito di una prenotazione
    private BookingSessionParent parentController;

    // Variabili statiche per i parametri di ricerca (Booking scene)
    private static String chosenLocation;
    private static String chosenSubject;
    private static AvailabilityBean chosenAvailability;

    private ObservableList<DayBookingBean> dayBookings = FXCollections.observableArrayList();
    private Image fullStarImage;
    private Image emptyStarImage;

    // Serve per passare il riferimento al controller del ManageNoticeBoardGraphicControllerColored
    public void setParentController(BookingSessionParent parentController) {
        this.parentController = parentController;
    }

    // Chiamato dalla Home dopo il FXMLLoader
    @Override
    public void initData(UUID sid, UserBean userBean) {

        this.sessionId = sid;
        this.userBean = userBean;

        if (tutorTable != null) {
            initBookingScene(fullStarImage, emptyStarImage);
            tutorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            dayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        if (sessionTable != null) {
            initManageNoticeBoardScene();
        }
    }

    @FXML
    public void initialize() {
        // Caricamento delle risorse, indipendente dalla sessione
        fullStarImage = new Image(getClass().getResourceAsStream("/images/full_star.png"));
        emptyStarImage = new Image(getClass().getResourceAsStream("/images/empty_star.png"));
    }


    // Metodo invocato dalla Home, per salvare i parametri di ricerca
    public static void setSearchParameters(String location, String subject, AvailabilityBean availability) {
        chosenLocation = location;
        chosenSubject = subject;
        chosenAvailability = availability;
    }

    // Logica per la scena BookingTutoringSession.fxml
    private void initBookingScene(Image fullStarImage, Image emptyStarImage) {
        setupWelcomeLabel();
        setupFilterLabels();
        setupCheckboxListeners();
        setupTutorTable(fullStarImage, emptyStarImage);
        setupRowDoubleClick();
        setupDayBookingTable();
    }

    private void setupWelcomeLabel() {
        UserBean me = getLoggedUser();
        if (me != null) {
            welcomeLabel.setText("Welcome, " + me.getUsername() + "!");
            welcomeLabel.setVisible(true);
            logOutButton.setVisible(true);
            logInButton.setVisible(false);
            signUpButton.setVisible(false);
        } else {
            welcomeLabel.setVisible(false);
        }
    }


    private void setupFilterLabels() {
        if (chosenSubject != null && !chosenSubject.isBlank()) {
            subjectLabel.setText(chosenSubject);
            subjectLabel.setVisible(true);
            subjectLabel.setManaged(true);
        } else {
            subjectLabel.setVisible(false);
            subjectLabel.setManaged(false);
        }

        if (chosenLocation != null && !chosenLocation.isBlank()) {
            staticInLabel.setVisible(true);
            locationLabel.setText(chosenLocation);
            locationLabel.setVisible(true);
            locationLabel.setManaged(true);
        } else {
            locationLabel.setVisible(false);
            locationLabel.setManaged(false);
        }

        if (chosenAvailability != null && chosenAvailability.getStartDate() != null && chosenAvailability.getEndDate() != null) {
            timeLabel.setText("from " + chosenAvailability.getStartDate() + " to " + chosenAvailability.getEndDate());
            timeLabel.setVisible(true);
            timeLabel.setManaged(true);
        } else {
            timeLabel.setVisible(false);
            timeLabel.setManaged(false);
        }
    }

    private void setupCheckboxListeners() {
        inPersonCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateTutorTable());
        onlineCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateTutorTable());
        groupCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateTutorTable());
        rating4Check.selectedProperty().addListener((obs, oldVal, newVal) -> updateTutorTable());
        firstLessonFreeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateTutorTable());
    }

    private void setupTutorTable(Image fullStarImage, Image emptyStarImage) {
        setupCellValueFactories();
        setupRatingColumnWithStars(fullStarImage, emptyStarImage);
        updateTutorTable();

        tutorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            bookTutoringSessionButton.setDisable(newSelection == null);
            dayBookings.clear();
            if (newSelection != null) {
                populateDayBookingsForTutor(newSelection);
            }
        });
    }

    private void setupCellValueFactories() {
        nameColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getName() + " " +
                        cd.getValue().getSurname() + " (" +
                        cd.getValue().getAge() + ")"));

        ratingColumn.setCellValueFactory(cd ->
                new SimpleDoubleProperty(cd.getValue().getRating()).asObject());

        hourlyRateColumn.setCellValueFactory(cd ->
                new SimpleDoubleProperty(cd.getValue().getHourlyRate()).asObject());
    }

    @SuppressWarnings("java:S1854")
    /* Sopprimo i falsi positivi: la variabile 'star' viene usata per
       configurare e aggiungere l'ImageView a starsBox */
    private void setupRatingColumnWithStars(Image fullStarImage, Image emptyStarImage) {
        ratingColumn.setCellFactory(col -> new TableCell<>() {
            private final HBox starsBox = new HBox(2);

            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);

                if (empty || rating == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                starsBox.getChildren().clear();


                for (int i = 0; i < 5; i++) {
                    ImageView star = new ImageView(i < rating.intValue() ? fullStarImage : emptyStarImage);
                    star.setFitWidth(16);
                    star.setFitHeight(16);
                    starsBox.getChildren().add(star);
                }

                setText(null);
                setGraphic(starsBox);
            }
        });
    }


    /* updateTutorTable completamente delegato al controller applicativo */
    private void updateTutorTable() {

        // Costruisco l'oggetto criteria necessario per la chiamata di searchTutor
        TutorSearchCriteriaBean criteria = new TutorSearchCriteriaBean.Builder()
                .subject(chosenSubject)
                .location(chosenLocation)
                .availability(chosenAvailability)
                .inPerson(inPersonCheck.isSelected())
                .online(onlineCheck.isSelected())
                .group(groupCheck.isSelected())
                .rating4Plus(rating4Check.isSelected())
                .firstLessonFree(firstLessonFreeCheck.isSelected())
                .orderCriteria(orderComboBox.getValue())
                .build();

        try {
            List<TutorBean> list = bookingCtrl.searchTutors(criteria);
            tutorTable.setItems(FXCollections.observableArrayList(list));
        } catch (NoTutorFoundException ex) {
            tutorTable.setItems(FXCollections.observableArrayList());
            showAlert("No tutors matched your search.\nPlease try adjusting your filters and try again.", ex.getMessage());
        }
    }

    private void setupRowDoubleClick() {
        tutorTable.setRowFactory(tv -> {
            TableRow<TutorBean> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    TutorBean selected = row.getItem();
                    if (selected != null) {
                        showTutorProfile(selected.getAccountId());
                    }
                }
            });
            return row;
        });
    }

    private void setupDayBookingTable() {
        dayTable.setEditable(true);
        ObservableList<String> timeSlots = generateTimeSlots();

        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

        dateBookingColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        startTimeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(timeSlots));
        startTimeColumn.setOnEditCommit(e -> {
            DayBookingBean rowData = e.getRowValue();
            rowData.setStartTime(e.getNewValue());
        });

        endTimeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(timeSlots));
        endTimeColumn.setOnEditCommit(e -> {
            DayBookingBean rowData = e.getRowValue();
            rowData.setEndTime(e.getNewValue());
        });

        commentBookingColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        commentBookingColumn.setOnEditCommit(e -> {
            DayBookingBean rowData = e.getRowValue();
            rowData.setComment(e.getNewValue());
        });

        dayTable.setItems(dayBookings);
    }

    // Generazione degli orari disponibili
    private ObservableList<String> generateTimeSlots() {
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        LocalTime start = LocalTime.of(8, 0);  // inizio 8:00
        LocalTime end = LocalTime.of(20, 0); // fine 20:00
        while (!start.isAfter(end)) {
            timeSlots.add(start.toString());
            start = start.plusMinutes(30);
        }
        return timeSlots;
    }

    @FXML
    private void orderTutorTable() {
        // Recupero la lista dei tutor attualmente mostrati
        ObservableList<TutorBean> tutors = tutorTable.getItems();
        if (tutors == null || tutors.isEmpty()) {
            return; // Se la tabella è vuota, non faccio nulla
        }

        String orderKey = orderComboBox.getValue();
        switch (orderKey) {
            case "Hourly Rate (asc)" -> {
                tutors.sort(Comparator.comparingDouble(TutorBean::getHourlyRate));
            }
            case "Hourly Rate (desc)" -> {
                tutors.sort(Comparator.comparingDouble(TutorBean::getHourlyRate).reversed());
            }
            case "Rating (asc)" -> {
                tutors.sort(Comparator.comparingDouble(TutorBean::getRating));
            }
            case "Rating (desc)" -> {
                tutors.sort(Comparator.comparingDouble(TutorBean::getRating).reversed());
            }
            default -> {
                // Nessun ordinamento specifico selezionato
            }
        }

        // Aggiorno la tabella
        tutorTable.refresh();
    }

    private void populateDayBookingsForTutor(TutorBean tutor) {

        dayBookings.setAll(bookingCtrl.computeDayBookingsForTutor(tutor.getAccountId(), chosenAvailability));
        dayTable.setItems(dayBookings);
    }

    // Logica per la scena ManageNoticeBoard.fxml
    private void initManageNoticeBoardScene() {
        setupSessionRowFactory();
        setupSessionColumns();
        addBookingActionColumn();
        loadUserSessions();
    }

    private void setupSessionRowFactory() {
        if (sessionTable == null) {
            return;
        }

        sessionTable.setRowFactory(tv -> {
            TableRow<TutoringSessionBean> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (isDoubleClick(event, row)) {
                    TutoringSessionBean selected = row.getItem();
                    if (selected != null) {
                        String role = getLoggedUserRole();
                        if (ROLE_TUTOR.equalsIgnoreCase(role)) {
                            showStudentProfile(selected.getStudentId());
                        } else {
                            showTutorProfile(selected.getTutorId());
                        }
                    }
                }
            });

            return row;
        });
    }

    private boolean isDoubleClick(MouseEvent event, TableRow<?> row) {
        return !row.isEmpty()
                && event.getButton() == MouseButton.PRIMARY
                && event.getClickCount() == 2;
    }

    private String getLoggedUserRole() {
        UserBean me = getLoggedUser();
        if (me == null) return null;
        for (AccountBean ab : me.getAccounts()) {
            if (ROLE_STUDENT.equalsIgnoreCase(ab.getRole())
                    || ROLE_TUTOR.equalsIgnoreCase(ab.getRole()))
                return ab.getRole();
        }
        return null;
    }

    private void setupSessionColumns() {

        studentColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(bookingCtrl.counterpartLabel(cd.getValue().getStudentId())));
        tutorColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(bookingCtrl.counterpartLabel(cd.getValue().getTutorId())));

        dateColumn.setCellValueFactory(cd -> {
            TutoringSessionBean s = cd.getValue();
            if (s.getStatus() == TutoringSessionStatus.MOD_REQUESTED && s.getProposedDate() != null) {
                return new SimpleStringProperty(s.getProposedDate().toString());
            } else if (s.getDate() != null) {
                return new SimpleStringProperty(s.getDate().toString());
            } else {
                return new SimpleStringProperty("");
            }
        });

        timeColumn.setCellValueFactory(cd -> {
            TutoringSessionBean s = cd.getValue();
            if (s.getStatus() == TutoringSessionStatus.MOD_REQUESTED
                    && s.getProposedStartTime() != null && s.getProposedEndTime() != null) {
                return new SimpleStringProperty(
                        s.getProposedStartTime() + " - " + s.getProposedEndTime()
                );
            } else if (s.getStartTime() != null && s.getEndTime() != null) {
                return new SimpleStringProperty(
                        s.getStartTime() + " - " + s.getEndTime()
                );
            } else {
                return new SimpleStringProperty("");
            }
        });

        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadUserSessions() {
        UserBean me = getLoggedUser();
        if (me == null) return;

        String uid = null;
        String role = null;
        for (AccountBean ab : me.getAccounts()) {
            if (ROLE_STUDENT.equalsIgnoreCase(ab.getRole())
                    || ROLE_TUTOR.equalsIgnoreCase(ab.getRole())) {
                uid = ab.getAccountId();
                role = ab.getRole();
                break;
            }
        }
        if (uid == null || role == null)
            throw new IllegalStateException("No valid account (Student/Tutor) for logged user.");

        List<TutoringSessionBean> list =
                ROLE_TUTOR.equalsIgnoreCase(role)
                        ? bookingCtrl.loadAllSessionsForTutor(uid)
                        : bookingCtrl.loadAllSessionsForStudent(uid);

        studentColumn.setVisible(ROLE_TUTOR.equalsIgnoreCase(role));
        tutorColumn.setVisible(!ROLE_TUTOR.equalsIgnoreCase(role));

        sessionTable.setItems(FXCollections.observableArrayList(list));
    }

    // Aggiunge i pulsanti Accept/Refuse se l'utente è un Tutor
    private void addBookingActionColumn() {

        if (!isLogged()) {
            hideBookingActionColumn();
            return;
        }

        if (isUserTutor()) {
            setupBookingActionButtons();
        } else {
            hideBookingActionColumn();
        }
    }

    private void hideBookingActionColumn() {
        bookingActionColumn.setVisible(false);
    }

    private boolean isUserTutor() {
        UserBean me = getLoggedUser();
        if (me == null) return false;

        for (AccountBean account : me.getAccounts()) {
            String role = account.getRole();
            if (ROLE_TUTOR.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("java:S1854")
    /* Sopprimo i falsi positivi: la variabile 'btn'
    viene configurata e restituita, l'assegnazione è necessaria e la
    variabile 'btn' viene configurata e restituita, l'assegnazione è necessaria */

    private void setupBookingActionButtons() {
        bookingActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button acceptBtn = createAcceptButton();
            private final Button refuseBtn = createRefuseButton();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                if (getTableView().getItems().get(getIndex()).getStatus() == TutoringSessionStatus.PENDING) {
                    setGraphic(new HBox(5, acceptBtn, refuseBtn));
                } else {
                    setGraphic(null);
                }
            }

            private Button createAcceptButton() {
                Button btn = new Button("Accept");
                btn.setOnAction(e -> handleAccept(getIndex()));
                return btn;
            }

            private Button createRefuseButton() {
                Button btn = new Button("Refuse");
                btn.setOnAction(e -> handleRefuse(getIndex()));
                return btn;
            }
        });
    }

    @SuppressWarnings("java:S1144") // Il metodo è usato in setupBookingActionButtons
    private void handleAccept(int index) {
        if (userConfirmed(
                "Confirm Booking's approval",
                "Are you sure you want to accept the booking?",
                "Click OK to accept.")) {
            TutoringSessionBean session = bookingActionColumn.getTableView().getItems().get(index);
            new BookingTutoringSessionController(sessionId).acceptSession(session.getSessionId());

            bookingActionColumn.getTableView().getItems().remove(session);

            if (parentController != null) {
                parentController.refreshCalendarAndTable();
            }
        }
    }

    @SuppressWarnings("java:S1144") // Il metodo è usato in setupBookingActionButtons
    private void handleRefuse(int index) {
        if (userConfirmed(
                "Confirm Booking's refusal",
                "Are you sure you want to refuse the booking?",
                "Click OK to refuse.")) {
            TutoringSessionBean session = bookingActionColumn.getTableView().getItems().get(index);
            new BookingTutoringSessionController(sessionId).refuseSession(session.getSessionId());
            bookingActionColumn.getTableView().getItems().remove(session);
            if (parentController != null) {
                parentController.refreshCalendarAndTable();
            }
        }
    }

    private boolean userConfirmed(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Metodo getter, così che il ManageNoticeBoardGraphicControllerColored
    // possa configurare "modCancelActionColumn"
    public TableColumn<TutoringSessionBean, Void> getModCancelActionColumn() {
        return modCancelActionColumn;
    }

    public TableView<TutoringSessionBean> getSessionTable() {
        return sessionTable;
    }

    @FXML
    private void handleBookSession(ActionEvent event) {

        TutorBean tutor = tutorTable.getSelectionModel().getSelectedItem();
        if (tutor == null) {
            return;
        }

        if (!ensureUserLoggedIn(event)) {           // esce se non loggato
            return;
        }

        int booked = bookSelectedRows(tutor);

        if (booked > 0) {
            showAlert("Booking Confirmation",
                    "Successfully booked " + booked + " session(s).\nPlease wait for the tutor's confirmation.");
        } else {
            showAlert("No Bookings", "No rows selected or no valid time provided.");
        }
    }

    private int bookSelectedRows(TutorBean tutor) {
        int count = 0;
        String studentId = getLoggedAccountId();
        if (studentId == null) {
            throw new IllegalStateException("Logged user has no Student account.");
        }

        for (DayBookingBean row : dayTable.getItems()) {
            if (!row.isSelected() || row.missingTimes()) continue;

            try {
                TutoringSessionBean bean = bookingCtrl.buildBookingBean(
                        tutor, row, studentId,
                        chosenLocation, chosenSubject);
                bookingCtrl.bookSession(bean);
                count++;
            } catch (DateTimeParseException ex) {
                showAlert("Booking error", "Invalid time ...");
            }
        }
        return count;
    }

    // Helper centrale per recuperare l'utente loggato
    private UserBean getLoggedUser() {
        return (sessionId == null) ? null : loginCtrl.getLoggedUser(sessionId);
    }

    private String getLoggedAccountId() {
        UserBean ub = getLoggedUser();
        if (ub == null) return null;
        for (AccountBean ab : ub.getAccounts()) {
            if (ROLE_STUDENT.equalsIgnoreCase(ab.getRole())
                    || ROLE_TUTOR.equalsIgnoreCase(ab.getRole()))
                return ab.getAccountId();
        }
        return null;
    }

    private boolean isLogged() {
        return getLoggedUser() != null;
    }


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

    @FXML
    private boolean ensureUserLoggedIn(ActionEvent event) {

        if (isLogged()) return true;

        showAlert(BOOKING_TITLE, "You must be logged in to book a session.");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign Up");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @FXML
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
    public void goToLogin(ActionEvent event) {
        SceneNavigator.navigate("/fxml/Login.fxml", (Node) event.getSource(), sessionId, userBean, "Login");
    }

    @FXML
    private void goToSignUp(ActionEvent event) {
        SignUpGraphicControllerColored.showSignUpScene(event);
    }

    @FXML
    private void goToManageNoticeBoard(ActionEvent event) {
        if (userBean == null) {
            showAlert(BOOKING_TITLE, "You must be logged in to manage the notice board.");
            goToLogin(event);
            return;
        }
        SceneNavigator.navigate("/fxml/ManageNoticeBoard.fxml", (Node) event.getSource(), sessionId, userBean, "Manage Notice Board");
    }

    @FXML
    private void goToLeaveASharedReview(ActionEvent event) {
        if (userBean == null) {
            showAlert(BOOKING_TITLE, "You must be logged in to leave a shared review.");
            goToLogin(event);
            return;
        }
        SceneNavigator.navigate("/fxml/LeaveASharedReview.fxml", (Node) event.getSource(), sessionId, userBean, "Leave a Shared Review");
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
            if (sessionId != null) {
                loginCtrl.logout(sessionId);
            }

            sessionId = null;

            welcomeLabel.setVisible(false);
            logOutButton.setVisible(false);
            logInButton.setVisible(true);
            signUpButton.setVisible(true);

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