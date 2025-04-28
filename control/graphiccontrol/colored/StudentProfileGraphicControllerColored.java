package logic.control.graphiccontrol.colored;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;
import logic.model.domain.Account;
import logic.model.domain.Student;

import java.io.File;

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

    private String studentAccountId;

    public void setStudentData(String studentAccountId) {
        this.studentAccountId = studentAccountId;
        loadStudentInfo();
    }

    private void loadStudentInfo() {
        AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();
        Account acc = accountDAO.load(studentAccountId);
        if (!(acc instanceof Student student)) {
            nameLabel.setText("Error: not a student account");
            return;
        }

        String fullName = student.getName() + " " + student.getSurname();
        nameLabel.setText("Student: " + fullName);
        instituteLabel.setText("Institute: " + student.getInstitute());
        ageLabel.setText("Age: " + student.getAge());

        if (student.getProfilePicturePath() != null && !student.getProfilePicturePath().isEmpty()) {
            File file = new File(student.getProfilePicturePath());
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                profilePictureView.setImage(img);
            } else {
                // Se il file non esiste, puoi mettere un’immagine di default o lasciare vuoto
                profilePictureView.setImage(null);
            }
        } else {
            profilePictureView.setImage(null);
        }

        // Commento profilo
        if (student.getProfileComment() != null && !student.getProfileComment().isEmpty()) {
            profileCommentLabel.setText(student.getProfileComment());
        } else {
            profileCommentLabel.setText("");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}

