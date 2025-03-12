package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.ContactRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
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
    void initialize() {
        // Set branches in ComboBox
        branchComboBox.setItems(FXCollections.observableArrayList("Haifa", "Acre", "Netanya", "Tel Aviv"));

        // Register to listen for events
        EventBus.getDefault().register(this);
    }

    @FXML
    void handleSubmit() {
        String name = nameField.getText().trim();
        String branch = branchComboBox.getValue();
        String complaint = complaintField.getText().trim();

        if (name.isEmpty() || branch == null || complaint.isEmpty()) {
            showAlert("Error", "All fields must be filled.");
            return;
        }

        try {
            client.sendToServer(new Message(new ContactRequest(name, branch, complaint), "#Update Complaint"));
            ScreenManager.switchScreen("Primary"); // ✅ Switch back to main screen
        } catch (IOException e) {
            showAlert("Error", "Failed to send the complaint.");
            e.printStackTrace();
            return;
        }
    }

    // ✅ Listen for the confirmation and switch the screen
    @Subscribe
    public void onComplaintSuccess(Message message) {
        if (message.toString().equals("#ComplaintSubmissionSuccess")) {
            showAlert("Success", "Your complaint has been submitted!");
            EventBus.getDefault().unregister(this);
            ScreenManager.switchScreen("Primary"); // ✅ Switch back to main screen
        }
    }

    @FXML
    void handleBack() {
        // Switch back to the main screen
        ScreenManager.switchScreen("Primary");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Unregister EventBus when closing the screen to avoid memory leaks
    public void onClose() {
        EventBus.getDefault().unregister(this);
    }
}
