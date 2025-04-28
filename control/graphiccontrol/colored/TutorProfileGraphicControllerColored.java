package logic.control.graphiccontrol.colored;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.dao.SharedReviewDAO;
import logic.model.domain.Account;
import logic.model.domain.ReviewStatus;
import logic.model.domain.SharedReview;
import logic.model.domain.Tutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TutorProfileGraphicControllerColored {

    @FXML
    private Label nameLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private ImageView profilePictureView;
    @FXML
    private Label profileCommentLabel;

    @FXML
    private TableView<SharedReview> reviewsTable;
    @FXML
    private TableColumn<SharedReview, String> studentNameColumn;
    @FXML
    private TableColumn<SharedReview, String> starsColumn;
    @FXML
    private TableColumn<SharedReview, String> reviewTitleColumn;
    @FXML
    private TableColumn<SharedReview, String> reviewCommentColumn;

    private String tutorAccountId; // ad es. "alex@example.com_Tutor"

    public void setTutorData(String tutorAccountId) {
        this.tutorAccountId = tutorAccountId;
        loadTutorInfo();
        loadTutorReviews();
    }

    private void loadTutorInfo() {
        AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();
        Account acc = accountDAO.load(tutorAccountId);
        if (!(acc instanceof Tutor tutor)) {
            // Errore, l’account non è un tutor
            nameLabel.setText("Error: not a tutor account");
            return;
        }

        // Mostriamo i campi
        String fullName = tutor.getName() + " " + tutor.getSurname();
        nameLabel.setText("Tutor: " + fullName);

        titleLabel.setText("Educational Title: " + tutor.getEducationalTitle());
        locationLabel.setText("Location: " + tutor.getLocation());
        ratingLabel.setText("Rating: " + tutor.getRating());

        // Carichiamo l’immagine di profilo
        if (tutor.getProfilePicturePath() != null && !tutor.getProfilePicturePath().isEmpty()) {
            File file = new File(tutor.getProfilePicturePath());
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                profilePictureView.setImage(img);
            } else {
                profilePictureView.setImage(null);
            }
        } else {
            profilePictureView.setImage(null);
        }

        // Commento profilo
        if (tutor.getProfileComment() != null && !tutor.getProfileComment().isEmpty()) {
            profileCommentLabel.setText(tutor.getProfileComment());
        } else {
            profileCommentLabel.setText("");
        }
    }

    private void loadTutorReviews() {
        // Carichiamo tutte le SharedReview per questo tutor
        SharedReviewDAO reviewDAO = DaoFactory.getInstance().getSharedReviewDAO();
        List<SharedReview> all = reviewDAO.loadForTutor(tutorAccountId);

        // Filtra per status = COMPLETE
        List<SharedReview> completed = new ArrayList<>();
        for (SharedReview sr : all) {
            if (sr.getStatus() == ReviewStatus.COMPLETE) {
                completed.add(sr);
            }
        }

        // Configuriamo le colonne (se non già configurate in initialize())
        studentNameColumn.setCellValueFactory(cd -> {
            SharedReview sr = cd.getValue();
            // sr.getStudentId() => carichiamo l’account per stampare Nome Cognome
            Account acc = DaoFactory.getInstance().getAccountDAO().load(sr.getStudentId());
            if (acc != null) {
                return new SimpleStringProperty(acc.getName() + " " + acc.getSurname());
            }
            return new SimpleStringProperty(sr.getStudentId());
        });

        starsColumn.setCellValueFactory(cd -> {
            SharedReview sr = cd.getValue();
            return new SimpleStringProperty(String.valueOf(sr.getStudentStars()));
        });

        reviewTitleColumn.setCellValueFactory(cd -> {
            SharedReview sr = cd.getValue();
            return new SimpleStringProperty(sr.getStudentTitle());
        });

        reviewCommentColumn.setCellValueFactory(cd -> {
            SharedReview sr = cd.getValue();
            return new SimpleStringProperty(sr.getStudentComment());
        });

        reviewsTable.setItems(FXCollections.observableArrayList(completed));
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) reviewsTable.getScene().getWindow();
        stage.close();
    }
}
