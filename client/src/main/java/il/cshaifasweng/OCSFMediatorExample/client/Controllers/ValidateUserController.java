package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class ValidateUserController {

    private final SimpleClient client = SimpleClient.getClient();

    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML
    private Button validateBtn;
    @FXML
    private Button backBtn;

    @FXML
    void backHandler(ActionEvent event) {
        Platform.runLater(() -> ScreenManager.switchScreen("Primary"));
    }
    @FXML
    void validateUser() {
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (email.isEmpty() || phone.isEmpty()) {
            showAlert("Validation Error", "Please enter both email and phone.");
            return;
        }

        // Send validation request to the server
        try {
            client.sendToServer(new Message(new String[]{email, phone}, "#ValidateUser"));
        } catch (IOException e) {
            showAlert("Error", "Failed to send request.");
        }
    }
    @Subscribe
    public void onUserValidationResponse(Message message) {
        Platform.runLater(() -> {
            if (message.toString().equals("#ValidationFailed")) {
                showAlert("Error", "No orders found. Please check your email and phone.");
            } else if (message.toString().equals("#UserValidated")) {
                List<Orders> userOrders = (List<Orders>) message.getObject();
                if (userOrders.isEmpty()) {
                    showAlert("Error", "No orders found for this user.");
                    return;
                }

                // Move to the order cancellation screen
                Platform.runLater(() ->  ScreenManager.switchScreen("Cancel Order"));
                CancelOrderController.Orders = userOrders;
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    void initialize() {
        EventBus.getDefault().register(this);
        assert emailField != null : "fx:id=\"emailField\" was not injected: check your FXML file 'ValidateUser.fxml'.";
        assert phoneField != null : "fx:id=\"phoneField\" was not injected: check your FXML file 'ValidateUser.fxml'.";
        assert validateBtn != null : "fx:id=\"validateBtn\" was not injected: check your FXML file 'ValidateUser.fxml'.";

    }
}
