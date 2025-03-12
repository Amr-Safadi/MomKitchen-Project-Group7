package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationController {

    @FXML
    private AnchorPane reservationPane;
    @FXML
    private Label branchLabel;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> timeComboBox;
    @FXML
    private TextField guestsField;
    @FXML
    private RadioButton indoorRadio;
    @FXML
    private RadioButton outdoorRadio;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField creditCardField;

    private ToggleGroup seatingToggleGroup;

    @FXML
    public void initialize() {
        branchLabel.setText(SecondaryService.getBranch());

        List<String> timeSlots = computeTimeSlots(SecondaryService.getBranchObj().getOpenHour(), SecondaryService.getBranchObj().getCloseHour());
        timeComboBox.getItems().addAll(timeSlots);

        seatingToggleGroup = new ToggleGroup();
        indoorRadio.setToggleGroup(seatingToggleGroup);
        outdoorRadio.setToggleGroup(seatingToggleGroup);

        EventBus.getDefault().register(this);
    }

    private List<String> computeTimeSlots(LocalTime open, LocalTime close) {
        List<String> slots = new ArrayList<>();
        LocalTime start = open.plusMinutes(15);
        LocalTime end = close.minusMinutes(60);
        while (!start.isAfter(end)) {
            slots.add(start.toString());
            start = start.plusMinutes(15);
        }
        return slots;
    }

    @FXML
    public void handleReserve(ActionEvent event) {
        if (datePicker.getValue() == null || timeComboBox.getValue() == null ||
                guestsField.getText().isEmpty() || fullNameField.getText().isEmpty() ||
                phoneField.getText().isEmpty() || emailField.getText().isEmpty() ||
                creditCardField.getText().isEmpty() || seatingToggleGroup.getSelectedToggle() == null) {
            showAlert("Validation Error", "Please fill in all the fields.");
            return;
        }

        Reservation reservation = new Reservation();
        Branch persistentBranch = SecondaryService.getBranchObj();
        if (persistentBranch == null) {
            showAlert("Error", "Branch information not available.");
            return;
        }
        reservation.setBranch(persistentBranch);

        reservation.setDate(datePicker.getValue());
        reservation.setTime(LocalTime.parse(timeComboBox.getValue()));
        reservation.setGuests(Integer.parseInt(guestsField.getText()));
        RadioButton selectedSeating = (RadioButton) seatingToggleGroup.getSelectedToggle();
        reservation.setSeatingArea(selectedSeating.getText());
        reservation.setFullName(fullNameField.getText());
        reservation.setPhone(phoneField.getText());
        reservation.setEmail(emailField.getText());
        reservation.setCreditCard(creditCardField.getText());

        try {
            SimpleClient.getClient().sendToServer(new Message(reservation, "#ReservationRequest"));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send reservation request.");
        }
    }


    @Subscribe
    public void onReservationResponse(il.cshaifasweng.OCSFMediatorExample.entities.Message message) {
        String msgText = message.toString();
        Platform.runLater(() -> {
            if ("#ReservationSuccess".equals(msgText)) {
                showAlert("Reservation Success", "Your reservation was successful!");
            } else if ("#NoAvailability".equals(msgText)) {
                String alternatives = (String) message.getObject();
                showAlert("No Availability", "No reservation available at requested time. Alternatives: " + alternatives);
            }
        });
    }

    @FXML
    public void handleBack(ActionEvent event) {
        ScreenManager.switchScreen("Menu List");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
