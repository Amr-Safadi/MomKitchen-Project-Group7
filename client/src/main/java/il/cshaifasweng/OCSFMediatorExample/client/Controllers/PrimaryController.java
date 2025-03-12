package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class PrimaryController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane LOG;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private AnchorPane secondaryAnchorPane;

    @FXML
    private Label userRec;

    @FXML
    private Button HaifaBtn;

    @FXML
    private Button AcreBtn;

    @FXML
    private Button TelavivBtn;

    @FXML
    private Button NetanyaBtn;

    @FXML
    private Button LOGINBtn;

    @FXML
    private Button contactUsBtn;


    @FXML
    private ListView<?> branchListView;

    @FXML
    private Label branchesLabel;

    @FXML
    private void handleHaifaBtn() {
        SecondaryService.setBranch("Haifa");
        ScreenManager.switchScreen("Menu List");
    }

    @FXML
    private void handleAcreBtn() {
        SecondaryService.setBranch("Acre");
        ScreenManager.switchScreen("Menu List");
    }

    @FXML
    private void handleTelavivBtn() {
        SecondaryService.setBranch("Tel-Aviv");
        ScreenManager.switchScreen("Menu List");
    }
    @FXML
    private void handleContactUsBtn() {
        ScreenManager.switchScreen("Contact Us");
    }


    @FXML
    private void handleNetanyaBtn() {
        SecondaryService.setBranch("Netanya");
        ScreenManager.switchScreen("Menu List");
    }

    @FXML
    private void handleLOGINBtn() {
        ScreenManager.switchScreen("Login");
    }

    @FXML
    void initialize() {
        assert HaifaBtn != null : "fx:id=\"HaifaBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert AcreBtn != null : "fx:id=\"AcreBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert TelavivBtn != null : "fx:id=\"TelavivBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert NetanyaBtn != null : "fx:id=\"NetanyaBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchListView != null : "fx:id=\"branchListView\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchesLabel != null : "fx:id=\"branchesLabel\" was not injected: check your FXML file 'primary.fxml'.";
        assert LOGINBtn != null : "fx:id=\"LOGINBtn\" was not injected: check your FXML file 'primary.fxml'.";

        if (SimpleClient.getUser() != null) {
            LOGINBtn.setDisable(true);
            LOGINBtn.setVisible(false);
        }

        if (UserSession.getUser() == null) {
            userRec.setVisible(false);
        } else {
            userRec.setText("Welcome " + UserSession.getUser().getFullName());
        }

        BackgroundUtil.setPaneBackground(secondaryAnchorPane, "/Images/NEWBACKGRND.jpg");
    }
}
