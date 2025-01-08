package il.cshaifasweng.OCSFMediatorExample.client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import static il.cshaifasweng.OCSFMediatorExample.client.App.switchScreen;

public class PrimaryController {

    public javafx.scene.image.ImageView branchHaifaIm;

    @FXML
    private ResourceBundle resources;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private AnchorPane secondaryAnchorPane;

    @FXML
    private URL location;

    @FXML
    private Button branchBtn;

    @FXML
    private ListView<?> branchListView;

    @FXML
    private Label branchesLabel;

    // Background image reference
    Image backgroundImage = new Image(String.valueOf(PrimaryController.class.getResource("/Images/background.jpg")));

    @FXML
    private void handleBranchBtn() {
        switchScreen("Menu List");
    }

    @FXML
    void initialize() {
        assert branchBtn != null : "fx:id=\"branchBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchListView != null : "fx:id=\"branchListView\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchesLabel != null : "fx:id=\"branchesLabel\" was not injected: check your FXML file 'primary.fxml'.";

        try {// Update window title
            Platform.runLater(() -> {
                Stage currentStage = (Stage) branchesLabel.getScene().getWindow();
                currentStage.setTitle("MomKitchen");
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error setting primary title");
        }

        // Create and set the background image
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                        BackgroundSize.AUTO,
                        BackgroundSize.AUTO,
                        true,
                        true,
                        true,
                        false
                )
        );
        //mainAnchorPane.setBackground(new Background(background));
        secondaryAnchorPane.setBackground(new Background(background));
    }
}
