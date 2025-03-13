package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.CartSession;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDateTime;

public class CheckOutController {

    @FXML
    private TextField addressTxt, cardTxt, mailTxt, nameTxt, phoneTxt;
    @FXML
    private Button backBtn, placeOrderBtn;
    @FXML
    private Label priceLabel;

    private final SimpleClient client = SimpleClient.getClient();

    @FXML
    void initialize() {
        priceLabel.setText("Total Price: $" + CartSession.getCart().getTotalPrice());
        EventBus.getDefault().register(this); // Register for event handling
    }

    @FXML
    void backHandler() {
        ScreenManager.switchScreen("Cart");
    }

    @FXML
    void handlePlaceOrderBtn() {
        if (CartSession.getCart().getMeals().isEmpty()) {
            showAlert("Error", "Your cart is empty. Please add items before placing an order.");
            return;
        }

        // Collect user input
        String name = nameTxt.getText();
        String address = addressTxt.getText();
        String phone = phoneTxt.getText();
        String email = mailTxt.getText();
        String creditCard = cardTxt.getText();
        LocalDateTime deliveryTime = LocalDateTime.now().plusHours(2); // Default delivery time in 2 hours

        // Validate input fields
        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty() || creditCard.isEmpty()) {
            showAlert("Error", "All fields are required. Please fill them in.");
            return;
        }

        // Create an Orders object
        Orders newOrder = new Orders(
                CartSession.getCart().getMeals(),
                name, address, phone, email, creditCard,
                CartSession.getCart().getTotalPrice(),
                deliveryTime
        );

        // Send order to server
        try {
            client.sendToServer(new Message(newOrder, "#PlaceOrder"));
            newOrder.printOrder();
           // showAlert("Processing Order", "Your order is being placed...");
        } catch (IOException e) {
            showAlert("Connection Error", "Failed to send order to the server. Please check your connection.");
        }
    }

    @Subscribe
    public void onOrderResponse(Message message) {
        Platform.runLater(() -> {
            switch (message.toString()) {
                case "#OrderSuccess":
                    showAlert("Order Success", "Your order has been placed successfully!");
                    CartSession.getCart().clearCart();
                    ScreenManager.switchScreen("Primary");
                    break;

                case "#OrderFailed":
                    showAlert("Order Failed", "There was an issue placing your order. Please try again.");
                    break;
            }
        });
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
