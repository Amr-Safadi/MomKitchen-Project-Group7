package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.ContactRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class ContactUsController {
    private SimpleClient client = SimpleClient.getClient();

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> branchComboBox;

    @FXML
    private TextArea complaintField;
    @FXML
    private TextField emailField;

    @FXML
    void initialize() {
        // Set branches in ComboBox
        branchComboBox.setItems(FXCollections.observableArrayList("Haifa", "Acre", "Netanya", "Tel Aviv"));

        // Register to listen for events
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @FXML
    void handleSubmit() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String branch = branchComboBox.getValue();
        String complaint = complaintField.getText().trim();
        if (name.isEmpty() || email.isEmpty() || branch == null || complaint.isEmpty()) {
            showAlert("Error", "All fields must be filled.");
            return;
        }

        try {
            client.sendToServer(new Message(new ContactRequest(name, email, branch, complaint), "#Update Complaint"));
        } catch (IOException e) {
            showAlert("Error", "Failed to send the complaint.");
            e.printStackTrace();
            return;
        }
    }

    @Subscribe
    public void onComplaintSuccess(Message message) {
        if (message.toString().equals("#ComplaintSubmissionSuccess")) {
            EventBus.getDefault().unregister(this);
            Platform.runLater(() -> {
                showAlert("Success", "Your complaint has been submitted! ");
                ScreenManager.switchScreen("Primary");
            });
        }
    }



    @FXML
    void handleBack() {
        EventBus.getDefault().unregister(this);
        // Switch back to the main screen
        Platform.runLater(() ->  ScreenManager.switchScreen("Primary"));

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
