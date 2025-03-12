package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class ValidateUserController {

    private final SimpleClient client = SimpleClient.getClient();

    @FXML private TextField emailField;
    @FXML private TextField phoneField;

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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
