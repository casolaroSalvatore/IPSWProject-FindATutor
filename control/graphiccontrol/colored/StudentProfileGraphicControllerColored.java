package logic.control.graphiccontrol.colored;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import logic.bean.AccountBean;
import logic.control.logiccontrol.StudentProfileController;
import java.time.Period;

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

    private StudentProfileController logic = new StudentProfileController();

    public void setStudentData(String studentAccountId) {

        AccountBean b = logic.loadStudentBean(studentAccountId);

        nameLabel.setText("Student: "+b.getName()+" "+b.getSurname());
        instituteLabel.setText("Institute: "+b.getInstitute());

        int age = (b.getBirthday()==null)? 0 :
                Period.between(b.getBirthday(), java.time.LocalDate.now()).getYears();
        ageLabel.setText("Age: "+age);

        if(b.getProfilePicturePath()!=null && !b.getProfilePicturePath().isBlank()
                && new java.io.File(b.getProfilePicturePath()).exists()){
            profilePictureView.setImage(
                    new Image(new java.io.File(b.getProfilePicturePath()).toURI().toString()));
        } else {
            profilePictureView.setImage(null);
        }

        profileCommentLabel.setText(
                (b.getProfileComment()==null)? "" : b.getProfileComment());
    }
}

