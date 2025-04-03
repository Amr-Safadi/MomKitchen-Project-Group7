package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
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

import java.time.LocalDate;
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
        BackgroundUtil.setPaneBackground(reservationPane, "/Images/NEWBACKGRND.jpg");
        branchLabel.setText(SecondaryService.getBranch());

        List<String> timeSlots = computeTimeSlots(SecondaryService.getBranchObj().getOpenHour(), SecondaryService.getBranchObj().getCloseHour());
        timeComboBox.getItems().addAll(timeSlots);

        seatingToggleGroup = new ToggleGroup();
        indoorRadio.setToggleGroup(seatingToggleGroup);
        outdoorRadio.setToggleGroup(seatingToggleGroup);

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    private List<String> computeTimeSlots(LocalTime open, LocalTime close) {
        List<String> slots = new ArrayList<>();
        LocalTime start = open.plusMinutes(15);
        LocalTime end = close.minusMinutes(10);/****/ //come back and fix!!
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

       if (isReservationInputValid() == false) {
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
    public void onReservationResponse(Message message) {
        String msgText = message.toString();
        Platform.runLater(() -> {
            if ("#ReservationSuccess".equals(msgText)) {
                showInfoAlert("Reservation Success", "Your reservation was successful!");
                handleBack(null);
            } else if ("#NoAvailability".equals(msgText)) {
                String alternatives = (String) message.getObject();
                if (alternatives.isEmpty())
                {
                    showAlert("No Availability", "No reservation for this day try a different date");
                }
                else {
                    showAlert("No Availability", "No reservation available at requested time. Alternatives: " + alternatives);
                }
            }
        });
    }

    @FXML
    public void handleCancelReservation(ActionEvent event) {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("CancelReservationSearch"));
    }

    @FXML
    public void handleBack(ActionEvent event) {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));
    }

    private boolean isReservationInputValid() {
        String name = fullNameField.getText();
        String phone = phoneField.getText();
        String guestsText = guestsField.getText();
        LocalDate date = datePicker.getValue();
        String timeText = timeComboBox.getValue();
        Branch selectedBranch = SecondaryService.getBranchObj();

        if (name == null || name.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your name.");
            return false;
        }

        if (phone == null || !phone.matches("\\d{7,15}")) {
            showAlert("Validation Error", "Please enter a valid phone number (7–15 digits).");
            return false;
        }

        if (guestsText == null || guestsText.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter number of guests.");
            return false;
        }

        int people;
        try {
            people = Integer.parseInt(guestsText);
            if (people <= 0) {
                showAlert("Validation Error", "Number of people must be greater than zero.");
                return false;
            }
            if (people > 25)
            {
                showAlert("Validation Error" , "Maximum of 25 people at a time");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid number of guests.");
            return false;
        }

        if (date == null) {
            showAlert("Validation Error", "Please select a date.");
            return false;
        }

        if (date.isBefore(LocalDate.now())) {
            showAlert("Validation Error", "The reservation date cannot be in the past.");
            return false;
        }

        if (date.equals(LocalDate.now())) {
            if (LocalTime.parse(timeText).isBefore(LocalTime.now())) {
                showAlert("Validation Error", "The reservation date cannot be in the past.");
                return false;
            }
        }

        if (timeText == null || timeText.trim().isEmpty()) {
            showAlert("Validation Error", "Please select a time.");
            return false;
        }

        try {
            LocalTime.parse(timeText);
        } catch (Exception e) {
            showAlert("Validation Error", "Invalid time format.");
            return false;
        }

        if (selectedBranch == null) {
            showAlert("Validation Error", "Branch information is missing.");
            return false;
        }

        return true;
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
