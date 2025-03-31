package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class CancelReservationListController {
    @FXML private AnchorPane pane;
    @FXML
    private ListView<String> reservationList;

    private static List<Reservation> reservations;

    public static void setReservationsList(List<Reservation> resList) {
        reservations = resList;
    }

    @FXML
    public void initialize() {
        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        populateReservationList();
    }

    private void populateReservationList() {
        reservationList.getItems().clear();
        if (reservations != null) {
            for (Reservation r : reservations) {
                String item = "ID: " + r.getId() + " | " + r.getDate() + " " + r.getTime() +
                        " | " + r.getFullName() + " | " + r.getPhone();
                reservationList.getItems().add(item);
            }
        }
    }

    @FXML
    public void handleCancelSelected(ActionEvent event) {
        int selectedIndex = reservationList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showAlert("Error", "Please select a reservation to cancel.");
            return;
        }
        Reservation selectedReservation = reservations.get(selectedIndex);
        try {
            SimpleClient.getClient().sendToServer(new Message(selectedReservation, "#CancelReservation"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onCancelReservationResponse(Message message) {
        if ("#CancelReservationSuccess".equals(message.toString())) {
            Platform.runLater(() -> {
                String responseDetail = (String) message.getObject();
                String alertContent = "Reservation canceled successfully.";
                if ("CancellationSuccessWithFee".equals(responseDetail)) {
                    alertContent += " Note: Your card was billed 10 shekels for canceling within 1 hour of the reservation time.";
                }
               showAlert("Cancellation","Reservation canceled successfully.");

                EventBus.getDefault().unregister(this);

                Platform.runLater(() -> ScreenManager.switchScreen("Reservation"));
            });
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        Platform.runLater(() ->ScreenManager.switchScreen("CancelReservationSearch"));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
