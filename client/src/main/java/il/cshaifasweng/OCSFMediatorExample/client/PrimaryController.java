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

  //  public javafx.scene.image.ImageView branchHaifaIm;

    @FXML
    private ResourceBundle resources;

    @FXML
    private AnchorPane LOG;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private AnchorPane secondaryAnchorPane;

    @FXML
    private URL location;

    @FXML
    private Button HaifaBtn;

    @FXML
    private Button MajdalBtn;

    @FXML
    private Button MielyaBtn;

    @FXML
    private Button ArrabiBtn;

    @FXML
    private Button LOGINBtn;

    @FXML
    private ListView<?> branchListView;

    @FXML
    private Label branchesLabel;

    // Background image reference
    Image backgroundImage = new Image(String.valueOf(PrimaryController.class.getResource("/Images/NEWBACKGRND.jpg")));

    @FXML
    private void handleHaifaBtn() {
        SecondaryController.branch = "Haifa";
        switchScreen("Menu List");
    }

    @FXML
    private void handleMajdalBtn() {
        SecondaryController.branch = "Majdal Shams";
        switchScreen("Menu List");
    }

    @FXML
    private void handleMielyaBtn() {
        SecondaryController.branch = "Mielya";
        switchScreen("Menu List");
    }

    @FXML
    private void handleArrabiBtn() {
        SecondaryController.branch = "Arraba";
        switchScreen("Menu List");
    }

    @FXML
    private void handleLOGINBtn() {
        switchScreen("Login");
    }

    @FXML
    void initialize() {



        assert HaifaBtn != null : "fx:id=\"HaifaBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert MajdalBtn != null : "fx:id=\"MajdalBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert MielyaBtn != null : "fx:id=\"MielyaBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert ArrabiBtn != null : "fx:id=\"ArrabiBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchListView != null : "fx:id=\"branchListView\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchesLabel != null : "fx:id=\"branchesLabel\" was not injected: check your FXML file 'primary.fxml'.";
        assert LOGINBtn != null : "fx:id=\"LOGINBtn\" was not injected: check your FXML file 'primary.fxml'.";

        if (SimpleClient.getUser() != null) {
            LOGINBtn.setDisable(true);
            LOGINBtn.setVisible(false);
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
