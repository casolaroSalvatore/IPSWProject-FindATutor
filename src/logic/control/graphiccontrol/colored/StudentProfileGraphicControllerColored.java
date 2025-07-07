package logic.control.graphiccontrol.colored;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import logic.bean.AccountBean;
import logic.bean.SharedReviewBean;
import logic.control.logiccontrol.StudentProfileController;

import java.time.Period;
import java.util.List;

public class StudentProfileGraphicControllerColored {

    @FXML
    private Label nameLabel;
    @FXML
    private Label instituteLabel;
    @FXML
    private Label ageLabel;
    @FXML
    private ImageView profilePictureView;
    @FXML
    private Label profileCommentLabel;
    @FXML
    private TableView<SharedReviewBean> reviewsTable;
    @FXML
    private TableColumn<SharedReviewBean, String> tutorNameColumn;
    @FXML
    private TableColumn<SharedReviewBean, String> reviewTitleColumn;
    @FXML
    private TableColumn<SharedReviewBean, String> reviewCommentColumn;

    private StudentProfileController logic = new StudentProfileController();

    // Imposta i dati dello studente nei componenti della view (nome, istituto, età, immagine, commento)
    public void setStudentData(String studentAccountId) {

        AccountBean b = logic.loadStudentBean(studentAccountId);

        nameLabel.setText("Student: " + b.getName() + " " + b.getSurname());
        instituteLabel.setText("Institute: " + b.getInstitute());

        int age = (b.getBirthday() == null) ? 0 :
                Period.between(b.getBirthday(), java.time.LocalDate.now()).getYears();
        ageLabel.setText("Age: " + age);

        if (b.getProfilePicturePath() != null && !b.getProfilePicturePath().isBlank()
                && new java.io.File(b.getProfilePicturePath()).exists()) {
            profilePictureView.setImage(
                    new Image(new java.io.File(b.getProfilePicturePath()).toURI().toString()));
        } else {
            profilePictureView.setImage(null);
        }

        profileCommentLabel.setText(
                (b.getProfileComment() == null) ? "" : b.getProfileComment());

        // Carica e mostra le recensioni
        List<SharedReviewBean> reviews = logic.loadCompletedReviews(studentAccountId);
        reviewsTable.setItems(FXCollections.observableArrayList(reviews));

        tutorNameColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCounterpartyInfo()));
        reviewTitleColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTutorTitle()));
        reviewCommentColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTutorComment()));
    }
}




