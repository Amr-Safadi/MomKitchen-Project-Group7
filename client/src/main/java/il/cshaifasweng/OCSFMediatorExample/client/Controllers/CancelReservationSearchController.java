package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class CancelReservationSearchController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneField;

    @FXML
    public void initialize() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String fullName = fullNameField.getText();
        String phone = phoneField.getText();
        if (fullName.isEmpty() || phone.isEmpty()) {
            System.out.println("Please enter both full name and phone.");
            return;
        }
        try {
            SimpleClient.getClient().sendToServer(new Message(new String[]{fullName, phone}, "#FetchReservations"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onUserReservations(Message message) {
        if ("#UserReservations".equals(message.toString())) {
            CancelReservationListController.setReservationsList((List) message.getObject());
            EventBus.getDefault().unregister(this);
            ScreenManager.switchScreen("CancelReservationList");
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        ScreenManager.switchScreen("Reservation");
    }
}
