package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
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
import java.util.List;
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
    private Button logOutBtn;

    private String loggedInUserBranch;

    @FXML
    private void handleCancelOrder() {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Validate User"));
    }

    @FXML
    private void handleHaifaBtn() {
        SecondaryService.setBranch("Haifa");
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));

    }

    @FXML
    private void handleAcreBtn() {
        SecondaryService.setBranch("Acre");
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));
    }

    @FXML
    private void handleTelavivBtn() {
        SecondaryService.setBranch("Tel-Aviv");
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));
    }

    @FXML
    private void handleNetanyaBtn() {
        SecondaryService.setBranch("Netanya");
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));
    }

    @FXML
    private void handleContactUsBtn() {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() ->  ScreenManager.switchScreen("Contact Us"));
    }

    @FXML
    private void handleLOGINBtn() {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() ->   ScreenManager.switchScreen("Login"));
    }
    @FXML
    private void handLogOutBtn() {
        try {
            // 📨 Notify server before logging out
            SimpleClient.getClient().sendToServer(new Message(null, "#LogoutRequest"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        UserSession.logout();  // Clears locally
        SimpleClient.setUser(null);
        // Reset UI
        complaintsButton.setVisible(false);
        reportsButton.setVisible(false);
        managerNotificationsBtn.setVisible(false);
        userRec.setVisible(false);
        LOGINBtn.setVisible(true);
        LOGINBtn.setDisable(false);
        logOutBtn.setVisible(false);
    }

    @FXML
    private void handleComplaints() {
        if (UserSession.getUser() != null &&
                (UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.SERVICE_EMPLOYEE)) {
            EventBus.getDefault().unregister(this);
            Platform.runLater(() ->  ScreenManager.switchScreen("Manage Complaints"));
        } else {
            showAlert("Access Denied", "You do not have permission to access this page.");
        }
    }
    @FXML
    private void handleReports() {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Reports"));
    }


    @Subscribe
    public void setNotifications (Message message) {
        if ("#PriceChangeRequestSent".equals(message.toString())) {
            try {
                SimpleClient.getClient().sendToServer(new Message("#CheckPendingNotifications"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

@Subscribe
public void onManagerNotificationStatus(Message message) {
    if (message.getObject() instanceof List<?>) {
        List<?> list = (List<?>) message.getObject();

        if (!list.isEmpty() && list.get(0) instanceof PriceChangeRequest) {
            List<PriceChangeRequest> requests = (List<PriceChangeRequest>) list;

            boolean hasRelevantNotifications = false;
            User currentUser = SimpleClient.getUser();

            for (PriceChangeRequest request : requests) {
                Meals meal = request.getMeal();
                if (meal == null || currentUser == null) continue;

                // ✅ General Manager sees all requests (branch and non-branch)
                if (currentUser.getRole() == User.Role.GENERAL_MANAGER) {
                    hasRelevantNotifications = true;
                    break;
                }

                // ✅ Branch Manager sees only if the meal is branch-specific and belongs to their branch
                if (currentUser.getRole() == User.Role.BRANCH_MANAGER && meal.getisBranchMeal()) {
                    if (meal.getBranches() != null) {
                        for (Branch b : meal.getBranches()) {
                            if (b.getName().equalsIgnoreCase(loggedInUserBranch)) {
                                hasRelevantNotifications = true;
                                break;
                            }
                        }
                        if (hasRelevantNotifications) break;
                    }
                }
            }

            // 🎯 Update the button style if any relevant requests exist
            if (hasRelevantNotifications) {
                Platform.runLater(() -> {
                    managerNotificationsBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    managerNotificationsBtn.setText("Notifications ");
                });
            } else {
                Platform.runLater(() -> {
                    managerNotificationsBtn.setStyle("");
                    managerNotificationsBtn.setText("Notifications");
                });
            }
        }
    } else if ("#ManagerClear".equals(message.toString())) {
        Platform.runLater(() -> {
            managerNotificationsBtn.setStyle("");
            managerNotificationsBtn.setText("Notifications");
        });
    }
}

    @FXML
    private void handleManagerNotifications() {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Notifications"));
    }



    @FXML
    void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        assert HaifaBtn != null : "fx:id=\"HaifaBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert AcreBtn != null : "fx:id=\"AcreBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert TelavivBtn != null : "fx:id=\"TelavivBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert NetanyaBtn != null : "fx:id=\"NetanyaBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchListView != null : "fx:id=\"branchListView\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchesLabel != null : "fx:id=\"branchesLabel\" was not injected: check your FXML file 'primary.fxml'.";
        assert LOGINBtn != null : "fx:id=\"LOGINBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert complaintsButton != null : "fx:id=\"complaintsButton\" was not injected: check your FXML file 'primary.fxml'.";

        User loggedInUser = SimpleClient.getUser();
        if(loggedInUser == null) {

            logOutBtn.setVisible(false);
            LOGINBtn.setVisible(true);
        }

        complaintsButton.setVisible(false);
        reportsButton.setVisible(false);
        managerNotificationsBtn.setVisible(false);
        userRec.setVisible(false);


        if (loggedInUser != null) {
            loggedInUserBranch = loggedInUser.getBranch();

            LOGINBtn.setDisable(true);
            LOGINBtn.setVisible(false);
            logOutBtn.setVisible(true);


            if(loggedInUser.getRole() == User.Role.SERVICE_EMPLOYEE) {
                complaintsButton.setVisible(true);
            }
            if ( loggedInUser.getRole() == User.Role.BRANCH_MANAGER ||
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
            userRec.setVisible(true);
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


