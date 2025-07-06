package logic.control.graphiccontrol.colored;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
import logic.bean.AccountBean;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import logic.bean.TutoringSessionBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.LoginController;
import logic.control.logiccontrol.ManageNoticeBoardController;
import logic.model.domain.state.TutoringSessionStatus;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageNoticeBoardGraphicControllerColored {

    private static final String ROLE_TUTOR   = "Tutor";
    private static final String ROLE_STUDENT = "Student";
    private static final String ERROR = "Error";

    private static final Logger LOGGER = Logger.getLogger(ManageNoticeBoardGraphicControllerColored.class.getName());

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

    // Riferimenti alla sessionTable
    @FXML
    private AnchorPane bookingSessionPane; // un contenitore definito in ManageNoticeBoard.fxml

    @FXML
    private TableView<TutoringSessionBean> sessionTable;

    @FXML
    private TableColumn<TutoringSessionBean, Void> modCancelActionColumn;

    // Campi FXML per la tabella daySessionTable
    @FXML
    private TableView<TutoringSessionBean> daySessionTable;

    @FXML
    private TableColumn<TutoringSessionBean, String> dayUserColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> daySubjectColumn;

    @FXML
    private TableColumn<TutoringSessionBean, String> dayTimeColumn;

    @FXML
    private Button requestModificationButton;

    @FXML
    private Button requestCancellationButton;

    // Campi FXML per il calendario custom
    @FXML private HBox monthNavigationBar;

    @FXML private Button prevMonthButton;

    @FXML private Button nextMonthButton;

    @FXML
    private Region spacer;

    @FXML private Label monthLabel;

    @FXML private GridPane calendarGrid;

    // Gestione del mese corrente nel calendario
    private YearMonth currentYearMonth;

    // Gestione della data selezionata
    private LocalDate selectedDate;

    // Riferimento al controller logico ManageNoticeBoardController
    private final LoginController loginController = new LoginController();
    private BookingSessionGraphicControllerColored bookingChildCtrl;
    // Riferimento al controller logico ManageNoticeBoardController
    private ManageNoticeBoardController manageController;

    private UUID sessionId;
    private UserBean userBean;

    /* Chiamare subito dopo il FXMLLoader in HomeGraphicControllerColored */
    public void initData(UUID sessionId, UserBean userBean) {
        this.sessionId = sessionId;
        this.userBean  = userBean;

        this.manageController = new ManageNoticeBoardController(sessionId);
        handleWelcomeArea(userBean); // può accedere ora a userBean

        String role = null;
        for (AccountBean account : userBean.getAccounts()) {
            String r = account.getRole();
            if (ROLE_STUDENT.equalsIgnoreCase(r) || ROLE_TUTOR.equalsIgnoreCase(r)) {
                role = r;
                break;
            }
        }

        // Ora che manageController esiste possiamo popolare il calendario
        currentYearMonth = YearMonth.now();
        populateCalendar(currentYearMonth);

        configureDayTable(role);
        loadBookingSessionPart();
    }


    @FXML
    public void initialize() {
        // Lasciato intenzionalmente vuoto, non abbiamo bisogno di inizializzazione per ManageNoticeBoard.fxml
    }

    // Helper per la fattorizzazione
    private void handleWelcomeArea(UserBean user) {
        boolean loggedIn = user != null;
        welcomeLabel.setVisible(loggedIn);
        logOutButton.setVisible(loggedIn);
        logInButton.setVisible(!loggedIn);
        signUpButton.setVisible(!loggedIn);

        if (loggedIn) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        }
    }

    // Configurazione colonne + row‑factory del daySessionTable
    private void configureDayTable(String role) {

        boolean isTutor = ROLE_TUTOR.equalsIgnoreCase(role);

        /* colonna User (studente e tutor) */
        dayUserColumn.setText(isTutor ? ROLE_STUDENT : ROLE_TUTOR);
        dayUserColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                manageController.getCounterpartLabel(
                        isTutor ? cd.getValue().getStudentId()
                                : cd.getValue().getTutorId())));

        /* colonna Subject */
        daySubjectColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getSubject()));

        /* colonna Time */
        dayTimeColumn.setCellValueFactory(cd -> {
            TutoringSessionBean s = cd.getValue();
            return new SimpleStringProperty(
                    (s.getStartTime() != null && s.getEndTime() != null)
                            ? s.getStartTime() + " - " + s.getEndTime()
                            : "");
        });

        /* row‑factory: doppio‑click apre profilo opposto */
        daySessionTable.setRowFactory(tv -> {
            TableRow<TutoringSessionBean> row = new TableRow<>();
            row.setOnMouseClicked(ev -> onRowDoubleClick(ev, row, isTutor));
            return row;
        });
    }

    private boolean hasAcceptedSessionsOn(LocalDate d) { return manageController.hasAcceptedSessionsOn(d); }

    private boolean hasWaitingSessionsOn (LocalDate d) { return manageController.hasWaitingSessionsOn (d); }

    private List<TutoringSessionBean> loadSessionsForLoggedUser(){  // ★
        return manageController.loadSessionsForLoggedUser();
    }

    // Helper estratto: riduce la complessità di configureDayTable
    private void onRowDoubleClick(MouseEvent ev,
                                  TableRow<TutoringSessionBean> row,
                                  boolean isTutor) {
        if (row.isEmpty() || ev.getButton() != MouseButton.PRIMARY || ev.getClickCount() != 2)
            return;

        TutoringSessionBean sel = row.getItem();
        if (sel == null) return;

        if (isTutor) {
            showStudentProfile(sel.getStudentId());
        } else {
            showTutorProfile(sel.getTutorId());
        }
    }

    // Carica il child FXML e aggiunge la colonna Action
    private void loadBookingSessionPart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookingSessionPart.fxml"));
            Parent childRoot  = loader.load();

            bookingChildCtrl = loader.getController();

            bookingChildCtrl.setParentController(this);
            bookingChildCtrl.initData(sessionId, userBean);

            bookingSessionPane.getChildren().add(childRoot);
            addActionColumnForModCancel(bookingChildCtrl.getModCancelActionColumn());
        } catch (IOException ex) {
            LOGGER.warning("Unable to load BookingSessionPart.fxml: " + ex.getMessage());
        }
    }

    // Metodo per implementare la logica del calendario
    private void populateCalendar(YearMonth yearMonth) {
        // Rimuoviamo i bottoni della vecchia generazione (tutte le row>0)
        calendarGrid.getChildren().removeIf(node -> {
            Integer rowIndex = GridPane.getRowIndex(node);
            return rowIndex != null && rowIndex > 0;
        });

        // Aggiorniamo la label mese
        monthLabel.setText(yearMonth.getMonth().toString() + " " + yearMonth.getYear());

        // Calcoliamo quanti giorni ha il mese
        LocalDate firstOfMonth = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // dayOfWeek: Lunedì=1... Domenica=7
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        // Se vogliamo Domenica=0, Lunedì=1, etc. => un piccolo adattamento, ad es.:
        // Domenica => col=0, Lunedì => col=1, ...
        int col = (startDayOfWeek % 7);  // Domenica(7) => 0, Lunedì(1) => 1, etc.

        // Partiamo da riga 1 (riga 0 è occupata dai nomi dei giorni)
        int row = 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day);

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefSize(45, 45); // Più grande
            dayButton.setStyle("-fx-font-size: 12px;");

            if (hasWaitingSessionsOn(date)) {
                // Giallo ocra (scegli un codice CSS a piacere)
                dayButton.setStyle("-fx-background-color: #FFD37F;");
            } else if (hasAcceptedSessionsOn(date)) {
                // Verde, come già facevi
                dayButton.setStyle("-fx-background-color: #a5ffa5;");
            } else {
                // Nessun sessione in attesa o accepted => colore di default
                dayButton.setStyle("-fx-font-size: 12px;");
            }

            // Al click, richiama handleDayClick(date)
            dayButton.setOnAction(e -> handleDayClick(date));

            // Aggiungiamo il bottone in col, row
            calendarGrid.add(dayButton, col, row);

            // Avanziamo colonna
            col++;
            // Se col > 6 => siamo oltre "Sat" => passiamo alla riga successiva
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    // Metodo per vuoi filtrare e mostrare nella sessionTable le sole sessioni di quella data:
    private void handleDayClick(LocalDate date) {

        this.selectedDate = date;

        List<TutoringSessionBean> allSessions = loadSessionsForLoggedUser();
        List<TutoringSessionBean> filtered  = new ArrayList<>();

        for (TutoringSessionBean s : allSessions) {
            if (s.getDate() != null
                    && s.getDate().equals(date)
                    && s.getStatus() == TutoringSessionStatus.ACCEPTED) {
                filtered.add(s);
            }
        }
        daySessionTable.setItems(FXCollections.observableArrayList(filtered));
    }

    public void refreshCalendarAndTable() {

        populateCalendar(currentYearMonth);
        refreshSessionTable();

        // Se esiste un giorno selezionato, ricarica le sessioni per quel giorno
        if (selectedDate != null) {
            handleDayClick(selectedDate);
        }
    }

    // Ricarica completamente la sessionTable dal DAO
    public void refreshSessionTable() {

        // 1) se la tabella non esiste ancora, esco
        if (bookingChildCtrl == null || bookingChildCtrl.getSessionTable() == null) return;

        // 2) recupero le sessioni per l’utente loggato
        List<TutoringSessionBean> list = manageController.loadSessionsForLoggedUser();

        // 3) imposto i dati sulla vera tabella (del file figlio)
        bookingChildCtrl.getSessionTable().setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleRequestModification() {
        TutoringSessionBean s = daySessionTable.getSelectionModel().getSelectedItem();
        if (s == null) {
            showAlert(ERROR, "No session selected!");
            return;
        }
        if (s.getStatus() != TutoringSessionStatus.ACCEPTED) {
            showAlert(ERROR, "You can only request a modification on an ACCEPTED session!");
            return;
        }

        // Esempio di un piccolo form con i nuovi orari e data
        // In modo semplificato, chiedo: new date, new startTime, new endTime, reason
        LocalDate newDate = showDateDialog(s.getDate());
        if (newDate == null) return;
        LocalTime newStart = showTimeDialog("New Start Time");
        if (newStart == null) return;
        LocalTime newEnd = showTimeDialog("New End Time");
        if (newEnd == null) return;

        TextInputDialog reasonDialog = new TextInputDialog("");
        reasonDialog.setTitle("Reason for Modification");
        reasonDialog.setHeaderText("Why do you want to modify this session?");
        Optional<String> reasonOpt = reasonDialog.showAndWait();

        if (reasonOpt.isPresent()) {
            manageController.requestModification(s, newDate, newStart, newEnd, reasonOpt.get());
            refreshCalendarAndTable();
        }
    }

    @FXML
    private void handleRequestCancellation() {
        TutoringSessionBean selected = daySessionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(ERROR, "No session selected");
            return;
        }
        if (selected.getStatus() != TutoringSessionStatus.ACCEPTED) {
            showAlert(ERROR, "You can only request a cancellation on an ACCEPTED session");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Request Cancellation");
        dialog.setHeaderText("Reason for cancellation");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            manageController.requestCancellation(selected, result.get());
            refreshCalendarAndTable();
        }
    }

    @SuppressWarnings("java:S1854")
    /* Sopprimo il falso positivo di SonarQube: le variabili locali come 'btn', 'acceptBtn' e 'refuseBtn'
    vengono effettivamente usate per configurare i pulsanti e aggiungerli alla cella,
    ma SonarQube segnala erroneamente che le assegnazioni sono inutili. */

    private void addActionColumnForModCancel(TableColumn<TutoringSessionBean, Void> modCancelActionColumn) {
        modCancelActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button acceptBtn = createButton(true);
            private final Button refuseBtn = createButton(false);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                updateActionCell(empty);
            }

            private Button createButton(boolean accepted) {
                Button btn = new Button(accepted ? "Accept" : "Refuse");
                btn.setOnAction(e -> handleAction(accepted));
                return btn;
            }

            private void updateActionCell(boolean empty) {
                if (empty || !canRespond()) {
                    setGraphic(null);
                    return;
                }

                TutoringSessionBean session = getSession();
                switch (session.getStatus()) {
                    case MOD_REQUESTED -> setupCell("Accept Modif", "Refuse Modif");
                    case CANCEL_REQUESTED -> setupCell("Accept Cancel", "Refuse Cancel");
                    default -> setGraphic(null);
                }
            }

            private boolean canRespond() {
                return manageController.canRespond(getSession().getSessionId());
            }

            private TutoringSessionBean getSession() {
                return getTableView().getItems().get(getIndex());
            }

            private void setupCell(String acceptText, String refuseText) {
                configureButtons(acceptText, refuseText);
                setGraphic(new HBox(5, acceptBtn, refuseBtn));
            }

            private void handleAction(boolean accepted) {
                TutoringSessionBean session = getSession();
                handleSessionAction(session, accepted);
                refreshCalendarAndTable();
            }

            private void configureButtons(String acceptText, String refuseText) {
                acceptBtn.setText(acceptText);
                refuseBtn.setText(refuseText);
            }
        });
    }

    @SuppressWarnings("java:S1144")

    /* Sopprimo il falso positivo: il metodo 'handleSessionAction' è effettivamente
    usato all'interno della classe anonima per gestire l'azione sui pulsanti */

    private void handleSessionAction(TutoringSessionBean session, boolean accepted) {
        if (session.getStatus() == TutoringSessionStatus.MOD_REQUESTED) {
            if (accepted) {
                manageController.acceptModification(session);
            } else {
                manageController.refuseModification(session);
            }
        } else if (session.getStatus() == TutoringSessionStatus.CANCEL_REQUESTED) {
            if (accepted) {
                manageController.acceptCancellation(session);
            } else {
                manageController.refuseCancellation(session);
            }
        }
    }


    // Metodo per mostrare il form necessario per specificare la nuova data
    @FXML
    private LocalDate showDateDialog(LocalDate defaultDate) {
        // Potresti usare un DatePicker in un dialog. Qui semplifico con un TextInputDialog
        TextInputDialog d = new TextInputDialog(defaultDate.toString());
        d.setTitle("Choose new Date");
        d.setHeaderText("Enter new date (yyyy-mm-dd)");
        Optional<String> result = d.showAndWait();
        if (result.isEmpty()) return null;
        try {
            return LocalDate.parse(result.get());
        } catch(Exception e) {
            showAlert(ERROR,"Invalid date format!");
            return null;
        }
    }

    // Metodo per mostrare il form necessario per specificare il nuovo orario
    @FXML
    private LocalTime showTimeDialog(String title) {
        TextInputDialog d = new TextInputDialog("10:00");
        d.setTitle(title);
        d.setHeaderText("Enter a time (HH:MM)");
        Optional<String> result = d.showAndWait();
        if (result.isEmpty()) return null;
        try {
            return LocalTime.parse(result.get());
        } catch(Exception e) {
            showAlert(ERROR,"Invalid time format!");
            return null;
        }
    }

    // Metodi per navigare tra i mesi
    @FXML
    private void prevMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        populateCalendar(currentYearMonth);
    }

    @FXML
    private void nextMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        populateCalendar(currentYearMonth);
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
    public void showStudentProfile(String studentAccountId) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Parent homeRoot = loader.load();

            HomeGraphicControllerColored homeGraphicControllerColored = loader.getController();
            homeGraphicControllerColored.initData(sessionId, userBean);

            // Imposto la scena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(homeRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setTitle("Home");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "An error occurred while loading the Home screen.", e);
        }
    }

    @FXML
    public void goToLeaveASharedReview(ActionEvent event) {

        // Verifichiamo se l’utente è loggato
        if (userBean == null) {
            showAlert("Booking", "You must be logged in to leave a shared review.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LeaveASharedReview.fxml"));
            Parent root = loader.load();

            LeaveASharedReviewGraphicControllerColored leaveASharedReviewGraphicControllerColored = loader.getController();
            leaveASharedReviewGraphicControllerColored.initData(sessionId, userBean);

            Stage stage = (Stage) leaveASharedReviewButton.getScene().getWindow();
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, bounds.getWidth(), bounds.getHeight()));
            stage.setTitle("Leave a Shared Review");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignUp(ActionEvent event) {
        SignUpGraphicControllerColored.showSignUpScene(event);
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        LoginGraphicControllerColored loginGraphicControllerColored = new LoginGraphicControllerColored();
        loginGraphicControllerColored.showLoginScene(event);
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
            // Logout tramite LoginController + ripulisco ApplicationContext
            if (sessionId != null) {
                loginController.logout(sessionId);
                sessionId = null;
                userBean  = null;
            }

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
