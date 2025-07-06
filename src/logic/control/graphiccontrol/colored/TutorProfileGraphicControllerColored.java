package logic.control.graphiccontrol.colored;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import logic.bean.SharedReviewBean;
import logic.bean.TutorBean;
import logic.control.logiccontrol.TutorProfileController;
import java.io.File;
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
    private TableView<SharedReviewBean> reviewsTable;
    @FXML
    private TableColumn<SharedReviewBean, String> studentNameColumn;
    @FXML
    private TableColumn<SharedReviewBean, String> starsColumn;
    @FXML
    private TableColumn<SharedReviewBean, String> reviewTitleColumn;
    @FXML
    private TableColumn<SharedReviewBean, String> reviewCommentColumn;

    private TutorProfileController logic = new TutorProfileController();

    public void setTutorData(String tutorAccountId) {

        /* 1. Bean con i dati anagrafici */
        TutorBean t = logic.loadTutorBean(tutorAccountId);

        nameLabel    .setText("Tutor: " + t.getName() + " " + t.getSurname());
        titleLabel   .setText("Educational Title: " + t.getEducationalTitle());
        locationLabel.setText("Location: " + t.getLocation());
        ratingLabel  .setText("Rating: " + t.getRating());

        if (t.getProfilePicturePath() != null && !t.getProfilePicturePath().isBlank()
                && new File(t.getProfilePicturePath()).exists()) {
            profilePictureView.setImage(
                    new Image(new File(t.getProfilePicturePath()).toURI().toString()));
        } else {
            profilePictureView.setImage(null);
        }
        profileCommentLabel.setText(
                t.getProfileComment() == null ? "" : t.getProfileComment());

        /* 2.Recensioni già mappate in Bean*/
        List<SharedReviewBean> completed = logic.loadCompletedReviews(tutorAccountId);

        studentNameColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCounterpartyInfo()));

        starsColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().getStudentStars())));

        reviewTitleColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getStudentTitle()));

        reviewCommentColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getStudentComment()));

        reviewsTable.setItems(FXCollections.observableArrayList(completed));
    }
}
