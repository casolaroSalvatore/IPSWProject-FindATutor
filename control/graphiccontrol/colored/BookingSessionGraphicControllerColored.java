package logic.control.graphiccontrol.colored;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import logic.bean.*;
import logic.control.logiccontrol.BookingTutoringSessionController;
import logic.model.domain.*;
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
import logic.model.domain.state.TutoringSessionStatus;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BookingSessionGraphicControllerColored {

    // Per evitare ripetizioni
    private static final String ROLE_TUTOR = "Tutor";
    private final BookingTutoringSessionController bookingCtrl = new BookingTutoringSessionController();


    // *** FXML comuni: logOutButton, signUpButton, logInButton, welcomeLabel...
    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logOutButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Button logInButton;

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

    // Stelle relative alle valutazioni
    private Image fullStarImage;
    private Image emptyStarImage;

    // Riferimento al ManageNoticeBoardGraphicControllerColored (servirà per permettere l'aggiornamento
    // del calendario al seguito di una prenotazione
    private ManageNoticeBoardGraphicControllerColored parentController;

    // Variabili statiche per i parametri di ricerca (Booking scene)
    private static String chosenLocation;
    private static String chosenSubject;
    private static AvailabilityBean chosenAvailability;

    private ObservableList<DayBookingBean> dayBookings = FXCollections.observableArrayList();

    // Serve per passare il riferimento al controller del ManageNoticeBoardGraphicControllerColored
    public void setParentController(ManageNoticeBoardGraphicControllerColored parentController) {
        this.parentController = parentController;
    }

    public void initialize() {

        fullStarImage  = new Image(getClass().getResourceAsStream("/images/full_star.png"));
        emptyStarImage = new Image(getClass().getResourceAsStream("/images/empty_star.png"));
        if (tutorTable != null) {
            // Scena "BookingTutoringSession.fxml"
            initBookingScene();
            // Ci consente di impostare la grandezza delle colonne in base alla grandezza dello schermo
            tutorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            dayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        if (sessionTable != null) {
            // Scena "ManageNoticeBoard.fxml"
            initManageNoticeBoardScene();
        }
    }

    // Metodo invocato dalla Home, per salvare i parametri di ricerca
    public static void setSearchParameters(String location, String subject, AvailabilityBean availability) {
        chosenLocation  = location;
        chosenSubject   = subject;
        chosenAvailability = availability;

    }

    // Logica per la scena BookingTutoringSession.fxml
    private void initBookingScene() {
        setupWelcomeLabel();
        setupFilterLabels();
        setupCheckboxListeners();
        setupTutorTable();
        setupRowDoubleClick();
        setupDayBookingTable();
    }

    private void setupWelcomeLabel() {
        if (SessionManager.getLoggedUser() != null) {
            welcomeLabel.setText("Welcome, " + SessionManager.getLoggedUser().getUsername() + "!");
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

    private void setupTutorTable() {
        nameColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getName()+" "+cd.getValue().getSurname()
                                +" ("+cd.getValue().getAge()+")"));

        ratingColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getRating()).asObject());
        hourlyRateColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getHourlyRate()).asObject());

        ratingColumn.setCellFactory(col -> new TableCell<TutorBean,Double>() {
            private final HBox starsBox = new HBox(2);

            // CellFactory per disegnare le stesse
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    starsBox.getChildren().clear();
                    // Arrotondo per difetto
                    int fullStars = rating.intValue();
                    for (int i = 0; i < 5; i++) {
                        ImageView iv = new ImageView(i < fullStars ? fullStarImage : emptyStarImage);
                        iv.setFitWidth(16);
                        iv.setFitHeight(16);
                        starsBox.getChildren().add(iv);
                    }
                    setText(null);
                    setGraphic(starsBox);
                }
            }
        });

        updateTutorTable();

        tutorTable.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{
            bookTutoringSessionButton.setDisable(n==null);
            dayBookings.clear();
            if(n!=null) populateDayBookingsForTutor(n);
        });
    }

    /* --- ADD : updateTutorTable completamente delegato al controller applicativo */
    private void updateTutorTable() {
        List<TutorBean> list = bookingCtrl.searchTutors(
                chosenSubject, chosenLocation, chosenAvailability,
                inPersonCheck.isSelected(), onlineCheck.isSelected(), groupCheck.isSelected(),
                rating4Check.isSelected(), firstLessonFreeCheck.isSelected(),
                orderComboBox.getValue());

        tutorTable.setItems(FXCollections.observableArrayList(list));
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
        LocalTime end   = LocalTime.of(20, 0); // fine 20:00
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


    private boolean passesAllFilters(Tutor tutor) {
        return !(inPersonCheck.isSelected() && !tutor.offersInPerson())
                && !(onlineCheck.isSelected() && !tutor.offersOnline())
                && !(groupCheck.isSelected() && !tutor.offersGroup())
                && !(rating4Check.isSelected() && tutor.getRating() < 4.0)
                && !(firstLessonFreeCheck.isSelected() && !tutor.isFirstLessonFree());
    }

    private void populateDayBookingsForTutor(TutorBean tutor) {

        dayBookings.clear();

        Availability av = bookingCtrl.getTutorAvailability(tutor.getAccountId());
        if(av==null || av.getDaysOfWeek()==null || av.getDaysOfWeek().isEmpty()) return;

        LocalDate start = (chosenAvailability!=null && chosenAvailability.getStartDate()!=null)?
                chosenAvailability.getStartDate() : LocalDate.now();
        LocalDate end   = (chosenAvailability!=null && chosenAvailability.getEndDate()!=null)?
                chosenAvailability.getEndDate()   : LocalDate.now();

        List<DayOfWeek> userDays = (chosenAvailability!=null)?
                chosenAvailability.getDays() : List.of();

        for(LocalDate d = start; !d.isAfter(end); d=d.plusDays(1)){
            boolean okUser = userDays.isEmpty() || userDays.contains(d.getDayOfWeek());
            boolean okTut  = av.getDaysOfWeek().contains(d.getDayOfWeek());
            if(okUser && okTut) dayBookings.add(new DayBookingBean(d));
        }
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
        if (sessionTable != null) {
            sessionTable.setRowFactory(tv -> {
                TableRow<TutoringSessionBean> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (!row.isEmpty()
                            && event.getButton() == MouseButton.PRIMARY
                            && event.getClickCount() == 2) {
                        TutoringSessionBean selected = row.getItem();
                        if (selected == null) {
                            return;
                        }
                        // Decidi chi aprire in base al ruolo
                        String role = null;

                        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
                            if ("Student".equalsIgnoreCase(account.getRole())) {
                                role = "Student";
                                break;
                            } else if ("Tutor".equalsIgnoreCase(account.getRole())) {
                                role = "Tutor";
                                break;
                            }
                        }
                        if (ROLE_TUTOR.equalsIgnoreCase(role)) {
                            showStudentProfile(selected.getStudentId());
                        } else {
                            showTutorProfile(selected.getTutorId());
                        }
                    }
                });
                return row;
            });
        }
    }

    private void setupSessionColumns() {

        studentColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(bookingCtrl.counterpartLabel(cd.getValue().getStudentId())));
        tutorColumn  .setCellValueFactory(cd ->
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
        if (SessionManager.getLoggedUser() == null) return;

        String uid = null;
        String role = null;

        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                uid = account.getAccountId();
                role = "Student";
                break;
            } else if ("Tutor".equalsIgnoreCase(account.getRole())) {
                uid = account.getAccountId();
                role = "Tutor";
                break;
            }
        }

        if (uid == null || role == null) {
            throw new IllegalStateException("No valid account (Student or Tutor) found for logged user.");
        }

        List<TutoringSessionBean> list;
        if (ROLE_TUTOR.equalsIgnoreCase(role)) {
            studentColumn.setVisible(true);
            tutorColumn.setVisible(false);
            list = bookingCtrl.loadAllSessionsForTutor(uid);
        } else {
            studentColumn.setVisible(false);
            tutorColumn.setVisible(true);
            list = bookingCtrl.loadAllSessionsForStudent(uid);
        }
        sessionTable.setItems(FXCollections.observableArrayList(list));
    }

    // Aggiunge i pulsanti Accept/Refuse se l'utente è un Tutor
    private void addBookingActionColumn() {
        if (SessionManager.getLoggedUser() != null) {

            boolean isTutor = false;

            for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
                if ("Tutor".equalsIgnoreCase(account.getRole())) {
                    isTutor = true;
                    break;
                }
            }

            if (isTutor) {
                bookingActionColumn.setCellFactory(col -> new TableCell<>() {
                    private final Button acceptBtn = new Button("Accept");
                    private final Button refuseBtn = new Button("Refuse");

                    {
                        acceptBtn.setOnAction(e -> handleAccept(getIndex()));
                        refuseBtn.setOnAction(e -> handleRefuse(getIndex()));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            if (getTableView().getItems().get(getIndex()).getStatus() == TutoringSessionStatus.PENDING) {
                                setGraphic(new HBox(5, acceptBtn, refuseBtn));
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                });
            } else {
                bookingActionColumn.setVisible(false);
            }

        } else {
            bookingActionColumn.setVisible(false);
        }
    }


    private void handleAccept(int index) {
        if (userConfirmed(
                "Confirm Booking's approval",
                "Are you sure you want to accept the booking?",
                "Click OK to accept.")) {
            TutoringSessionBean session = bookingActionColumn.getTableView().getItems().get(index);
            new BookingTutoringSessionController().acceptSession(session.getSessionId());
            bookingActionColumn.getTableView().getItems().remove(session);
            if (parentController != null) {
                parentController.refreshCalendarAndTable();
            }
        }
    }

    private void handleRefuse(int index) {
        if (userConfirmed(
                "Confirm Booking's refusal",
                "Are you sure you want to refuse the booking?",
                "Click OK to refuse.")) {
            TutoringSessionBean session = bookingActionColumn.getTableView().getItems().get(index);
            new BookingTutoringSessionController().refuseSession(session.getSessionId());
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

    /* Metodo getter, così che il ManageNoticeBoardGraphicControllerColored
    // possa ottenere "sessionTable"
    public TableView<TutoringSessionBean> getSessionTable() {
        return sessionTable;
    }

    private ObservableList<Tutor> loadTutorMatches(String subject, String location, Availability availability) {
        // 1) Ottengo la factory e l’AccountDAO
        AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();

        // 2) Carico TUTTI gli account con role="Tutor"
        List<Account> allTutorAccounts = accountDAO.loadAllAccountsOfType(ROLE_TUTOR);

        // 3) Converto in oggetti Tutor
        List<Tutor> allTutors = new ArrayList<>();
        for (Account acc : allTutorAccounts) {
            if (acc instanceof Tutor tutor) {
                allTutors.add(tutor);
            }
        }

        // Applico un semplice filtro su subject e location
        List<Tutor> filtered = new ArrayList<>();
        for (Tutor tutor : allTutors) {
            boolean matchLocality  = (location == null || location.isBlank())
                    || tutor.getLocation().equalsIgnoreCase(location);

            boolean matchAvailability = checkTutorAvailability(tutor, availability);

            boolean matchSubject = (subject == null || subject.isBlank())
                    || tutor.getSubject().equalsIgnoreCase(subject);

            if (matchLocality && matchAvailability && matchSubject) {
                filtered.add(tutor);
            }
        }

        /* System.out.println("Trovati: " + allTutorAccounts.size() + " tutor accounts.");
        for (Account a : allTutorAccounts) {
            System.out.println(" - " + a.getEmail() + " / " + a.getRole());
        }
        return FXCollections.observableArrayList(filtered);
    }

    private boolean checkTutorAvailability(Tutor tutor, Availability userReq) {

        // 1. Tutor privo di disponibilità --> subito false
        Availability tutorAvail = tutor.getAvailability();
        if (tutorAvail == null) {
            return false;
        }

        // 2. Controllo range di date (start / end)
        LocalDate reqStart = userReq.getStartDate();
        LocalDate reqEnd   = userReq.getEndDate();

        // Il tutor inizia troppo tardi
        if (reqStart != null && tutorAvail.getStartDate() != null
                && tutorAvail.getStartDate().isAfter(reqStart)) {
            return false;
        }

        // Il tutor finisce troppo presto
        if (reqEnd != null && tutorAvail.getEndDate() != null
                && tutorAvail.getEndDate().isBefore(reqEnd)) {
            return false;
        }

        // 3. Giorni della settimana
        List<DayOfWeek> requestedDays = userReq.getDaysOfWeek();
        if (requestedDays != null && !requestedDays.isEmpty()) {
            // basta 1 giorno in comune
            return requestedDays.stream()
                    .anyMatch(tutorAvail.getDaysOfWeek()::contains);
        }

        // Tutte le verifiche superate
        return true;
    } */


    @FXML
    private void handleBookSession(ActionEvent event) {

        TutorBean tutor = tutorTable.getSelectionModel().getSelectedItem();
        if (tutor == null) {
            return;
        }

        if (!ensureUserLoggedIn(event)) {           // esce se non loggato
            return;
        }

        BookingTutoringSessionController logic = new BookingTutoringSessionController();
        int booked = bookSelectedRows(tutor);

        if (booked > 0) {
            showAlert("Booking Confirmation",
                    "Successfully booked " + booked + " session(s).\nPlease wait for the tutor's confirmation.");
        } else {
            showAlert("No Bookings", "No rows selected or no valid time provided.");
        }
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
        if (SessionManager.getLoggedUser() != null) {
            return true;
        }
        showAlert("Booking", "You must be logged in to book a session.");
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

    private int bookSelectedRows(TutorBean tutor){
        int count = 0;

        String studentId = null;
        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                studentId = account.getAccountId();
                break;
            }
        }

        if (studentId == null) {
            throw new IllegalStateException("Logged user has no Student account.");
        }

        for( DayBookingBean row : dayTable.getItems()){
            if(!row.isSelected() || row.missingTimes()) continue;

            try{
                TutoringSessionBean bean = bookingCtrl.buildBookingBean(
                        tutor, row, studentId,
                        chosenLocation, chosenSubject);
                bookingCtrl.bookSession(bean);
                count++;
            }catch(DateTimeParseException ex){
                showAlert("Booking error","Invalid time ...");
            }
        }
        return count;
    }

    private TutoringSessionBean buildBean(Tutor tutor, DayBooking row) {
        TutoringSessionBean bean = new TutoringSessionBean();
        bean.setTutorId(tutor.getAccountId());

        String studentId = null;
        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                studentId = account.getAccountId();
                break;
            }
        }
        if (studentId == null) {
            throw new IllegalStateException("Logged user has no Student account.");
        }

        bean.setStudentId(studentId);
        bean.setDate(row.getDate());
        bean.setStartTime(LocalTime.parse(row.getStartTime()));
        bean.setEndTime(LocalTime.parse(row.getEndTime()));
        bean.setLocation(chosenLocation);
        bean.setSubject(chosenSubject);
        bean.setComment(row.getComment());
        return bean;
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
    public void goToLogin(ActionEvent event) {
        LoginGraphicControllerColored.showLoginScene(event);
    }

    @FXML
    private void goToSignUp(ActionEvent event) {
        SignUpGraphicControllerColored.showSignUpScene(event);
    }

    @FXML
    public void goToManageNoticeBoard(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(BookingSessionGraphicControllerColored.class.getResource("/fxml/ManageNoticeBoard.fxml"));
            Parent root = loader.load();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Manage Notice Board");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the Manage Notice Board screen.");
        }
    }


    @FXML
    public void showTutorListScene(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(BookingSessionGraphicControllerColored.class.getResource("/fxml/BookingTutoringSession.fxml"));
            Parent loginRoot = loader.load();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(loginRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Book a Tutoring Session");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred while loading the Tutoring Session screen.");
        }
    }

    @FXML
    private void goToLeaveASharedReview(ActionEvent event) {
        // 1) Verifichiamo se l’utente è loggato
        if (SessionManager.getLoggedUser() == null) {
            showAlert("Booking", "You must be logged in to manage the notice board.");
            try {
                Parent signUpRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
                Stage stage = (Stage) leaveASharedReviewButton.getScene().getWindow();
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                Scene scene = new Scene(signUpRoot, screenBounds.getWidth(), screenBounds.getHeight());
                stage.setTitle("Error in accessing the notice board");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("An error occurred while loading the Login screen for review.");
            }
        } else {
            try {
                Parent signUpRoot = FXMLLoader.load(getClass().getResource("/fxml/LeaveASharedReview.fxml"));
                Stage stage = (Stage) leaveASharedReviewButton.getScene().getWindow();
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                Scene scene = new Scene(signUpRoot, screenBounds.getWidth(), screenBounds.getHeight());
                stage.setTitle("Error in accessing the notice board");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("An error occurred while loading the Shared Review screen.");
            }
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