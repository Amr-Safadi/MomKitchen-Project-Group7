package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.PriceChangeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManagerNotificationsController {
    @FXML private AnchorPane pane;
    @FXML private TableView<PriceChangeRequest> requestTable;
    @FXML private TableColumn<PriceChangeRequest, String> mealNameCol;
    @FXML private TableColumn<PriceChangeRequest, Double> oldPriceCol;
    @FXML private TableColumn<PriceChangeRequest, Double> requestedPriceCol;
    @FXML private TableColumn<PriceChangeRequest, String> requestedByCol;
    @FXML private TableColumn<PriceChangeRequest, Void> actionCol;
    @FXML private ComboBox<String> branchFilterComboBox;
    @FXML private Label branchFilterLabel;

    private List<PriceChangeRequest> allRequests;

    @FXML
    public void initialize() {
        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");
        User user = UserSession.getUser();

        if (user == null || (user.getRole() != User.Role.BRANCH_MANAGER && user.getRole() != User.Role.GENERAL_MANAGER)) {
            showAlert("Access Denied", "You do not have permission to access this page.");
            EventBus.getDefault().unregister(this);
            ScreenManager.switchScreen("Primary");
            return;
        }

        if (user.getRole() == User.Role.GENERAL_MANAGER) {
            branchFilterLabel.setVisible(true);
            branchFilterComboBox.setVisible(true);
            branchFilterComboBox.setItems(FXCollections.observableArrayList("All", "Haifa", "Tel-Aviv", "Acre", "Netanya"));
            branchFilterComboBox.getSelectionModel().selectFirst();
            branchFilterComboBox.setOnAction(e -> filterNotificationsByBranch());
        } else {
            branchFilterLabel.setVisible(false);
            branchFilterComboBox.setVisible(false);
        }

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        mealNameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMeal().getName()));
        oldPriceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMeal().getPrice()).asObject());
        requestedPriceCol.setCellValueFactory(new PropertyValueFactory<>("requestedPrice"));
        requestedByCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRequestedBy().getFullName()));

        addActionButtons();

        try {
            SimpleClient.getClient().sendToServer(new Message(null, "#FetchPriceRequests"));
        } catch (IOException e) {
            showAlert("Error", "Failed to fetch price requests.");
        }
    }

    @Subscribe
    public void setNotifications(Message message) {
        if ("#PriceChangeRequestSent".equals(message.toString())) {
            try {
                SimpleClient.getClient().sendToServer(new Message(null, "#FetchPriceRequests"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addActionButtons() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final HBox actionBox = new HBox(10);
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");

            {
                approveBtn.setStyle("-fx-background-color: #8ad4a8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
                rejectBtn.setStyle("-fx-background-color: #e27d60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
                approveBtn.setOnAction(e -> {
                    PriceChangeRequest request = getTableView().getItems().get(getIndex());
                    handleApproval(request);
                });

                rejectBtn.setOnAction(e -> {
                    PriceChangeRequest request = getTableView().getItems().get(getIndex());
                    handleRejection(request);
                });

                actionBox.getChildren().addAll(approveBtn, rejectBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void handleRejection(PriceChangeRequest request) {
        try {
            SimpleClient.getClient().sendToServer(new Message(request, "#RejectPriceChange"));
        } catch (IOException e) {
            showAlert("Error", "Failed to send rejection.");
        }
    }

    private void handleApproval(PriceChangeRequest request) {
        try {
            SimpleClient.getClient().sendToServer(new Message(request, "#ApprovePriceChange"));
        } catch (IOException e) {
            showAlert("Error", "Failed to send approval.");
        }
    }

    @Subscribe
    public void onRequestsReceived(Message message) {
        if ("#PriceRequestsList".equals(message.toString())) {
            Platform.runLater(() -> {
                allRequests = (List<PriceChangeRequest>) message.getObject();
                filterNotificationsByBranch();
            });
        } else if ("#PriceChangeApproved".equals(message.toString())) {
            Platform.runLater(() -> {
                showAlert("Success", "Price change approved!");
                try {
                    SimpleClient.getClient().sendToServer(new Message(null, "#FetchPriceRequests"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if ("#PriceChangeRejected".equals(message.toString())) {
            Platform.runLater(() -> {
                showAlert("Rejected", "Request has been rejected.");
                try {
                    SimpleClient.getClient().sendToServer(new Message(null, "#FetchPriceRequests"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @FXML
    void handleBack() {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Primary"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void filterNotificationsByBranch() {
        String selected;
        if (UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER) {
            selected = branchFilterComboBox.getSelectionModel().getSelectedItem();
        } else {
            selected = UserSession.getUser().getBranch();
        }

        List<PriceChangeRequest> filtered = selected.equals("All") ? allRequests :
                allRequests.stream()
                        .filter(req -> req.getMeal().getBranches().stream()
                                .anyMatch(branch -> branch.getName().equalsIgnoreCase(selected)))
                        .toList();

        requestTable.setItems(FXCollections.observableArrayList(filtered));
    }

    public void onClose() {
        EventBus.getDefault().unregister(this);
    }
}
