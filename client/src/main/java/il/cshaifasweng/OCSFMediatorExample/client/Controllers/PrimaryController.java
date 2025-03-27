package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PrimaryController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private Button managerNotificationsBtn;

    @FXML
    private URL location;

    @FXML
    private AnchorPane LOG;

    @FXML
    private Button cancelOrderBtn;

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
    private Button complaintsButton;

    @FXML
    private Button reportsButton;

    @FXML
    private ListView<?> branchListView;

    @FXML
    private Label branchesLabel;

    @FXML
    private void handleCancelOrder() {
        Platform.runLater(() -> ScreenManager.switchScreen("Validate User"));
    }

    @FXML
    private void handleHaifaBtn() {
        SecondaryService.setBranch("Haifa");
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));

    }

    @FXML
    private void handleAcreBtn() {
        SecondaryService.setBranch("Acre");
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));
    }

    @FXML
    private void handleTelavivBtn() {
        SecondaryService.setBranch("Tel-Aviv");
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));
    }

    @FXML
    private void handleNetanyaBtn() {
        SecondaryService.setBranch("Netanya");
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));
    }

    @FXML
    private void handleContactUsBtn() {
        Platform.runLater(() ->  ScreenManager.switchScreen("Contact Us"));
    }

    @FXML
    private void handleLOGINBtn() {
        Platform.runLater(() ->   ScreenManager.switchScreen("Login"));
    }

    @FXML
    private void handleComplaints() {
        if (UserSession.getUser() != null &&
                (UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.SERVICE_EMPLOYEE)) {
            Platform.runLater(() ->  ScreenManager.switchScreen("Manage Complaints"));
        } else {
            showAlert("Access Denied", "You do not have permission to access this page.");
        }
    }
    @FXML
    private void handleReports() {
        Platform.runLater(() -> ScreenManager.switchScreen("Reports"));
    }


    @Subscribe
    public void onManagerNotificationStatus(Message message) {
        if ("#ManagerHasNotifications".equals(message.toString())) {
            Platform.runLater(() -> {
                managerNotificationsBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                managerNotificationsBtn.setText("Notifications ❗");
            });
        } else if ("#ManagerClear".equals(message.toString())) {
            Platform.runLater(() -> {
                managerNotificationsBtn.setStyle(""); // default style
                managerNotificationsBtn.setText("Notifications");
            });
        }
    }

    @FXML
    private void handleManagerNotifications() {
        Platform.runLater(() -> ScreenManager.switchScreen("Notifications"));
    }



    @FXML
    void initialize() {
        EventBus.getDefault().register(this);
        assert HaifaBtn != null : "fx:id=\"HaifaBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert AcreBtn != null : "fx:id=\"AcreBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert TelavivBtn != null : "fx:id=\"TelavivBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert NetanyaBtn != null : "fx:id=\"NetanyaBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchListView != null : "fx:id=\"branchListView\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchesLabel != null : "fx:id=\"branchesLabel\" was not injected: check your FXML file 'primary.fxml'.";
        assert LOGINBtn != null : "fx:id=\"LOGINBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert complaintsButton != null : "fx:id=\"complaintsButton\" was not injected: check your FXML file 'primary.fxml'.";

        User loggedInUser = SimpleClient.getUser();
        complaintsButton.setVisible(false);
        reportsButton.setVisible(false);
        managerNotificationsBtn.setVisible(false);

        if (loggedInUser != null) {
            LOGINBtn.setDisable(true);
            LOGINBtn.setVisible(false);


            if (loggedInUser.getRole() == User.Role.BRANCH_MANAGER ||
                    loggedInUser.getRole() == User.Role.GENERAL_MANAGER ||
                    loggedInUser.getRole() == User.Role.SERVICE_EMPLOYEE) {
                complaintsButton.setVisible(true);
            }
            if (loggedInUser.getRole() == User.Role.BRANCH_MANAGER ||
                    loggedInUser.getRole() == User.Role.GENERAL_MANAGER) {
                reportsButton.setVisible(true);
                managerNotificationsBtn.setVisible(true); // Show the button

                try {
                    SimpleClient.getClient().sendToServer(new Message("#CheckPendingNotifications"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                userRec.setVisible(false);
                managerNotificationsBtn.setVisible(false);
            }
        }

        if (UserSession.getUser() != null) {
            userRec.setText("Welcome " + UserSession.getUser().getFullName());
        }

        BackgroundUtil.setPaneBackground(secondaryAnchorPane, "/Images/NEWBACKGRND.jpg");
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}


