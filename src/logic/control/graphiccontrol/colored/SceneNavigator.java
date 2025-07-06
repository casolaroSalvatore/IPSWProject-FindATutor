package logic.control.graphiccontrol.colored;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import logic.bean.UserBean;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneNavigator {

    private static final Logger LOGGER = Logger.getLogger(SceneNavigator.class.getName());

    private SceneNavigator() {}

    public static void navigate(String fxmlPath, Node sourceNode, UUID sessionId, UserBean userBean, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof HomeGraphicControllerColored homeCtrl) {
                homeCtrl.initData(sessionId, userBean);
            } else if (controller instanceof ManageNoticeBoardGraphicControllerColored noticeCtrl) {
                noticeCtrl.initData(sessionId, userBean);
            } else if (controller instanceof LeaveASharedReviewGraphicControllerColored reviewCtrl) {
                reviewCtrl.initData(sessionId, userBean);
            }

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Error while navigating to " + fxmlPath);
        }
    }
}

