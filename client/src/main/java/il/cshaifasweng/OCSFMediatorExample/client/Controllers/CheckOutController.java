package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.CartSession;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDateTime;

public class CheckOutController {

    @FXML
    private AnchorPane pane;
    @FXML
    private TextField addressTxt, cardTxt, mailTxt, nameTxt, phoneTxt;
    @FXML
    private Button backBtn, placeOrderBtn;
    @FXML
    private Label priceLabel;
    @FXML
    private Label orderTypeLabel, paymentMethodLabel; // Add labels for the selections

    private static String orderType = "Delivery"; // Default
    private static String paymentMethod = "Cash"; // Default
    private static String preferredTime = "";

    // Setter method to update selections
    public static void setOrderPreferences(String type, String payment, String time) {
        orderType = type;
        paymentMethod = payment;
        preferredTime = time;
    }

    private final SimpleClient client = SimpleClient.getClient();

    @FXML
    void initialize() {
        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");
        priceLabel.setText(priceLabel.getText() + CartSession.getCart().getTotalPrice());

        // Update labels
       // orderTypeLabel.setText("Order Type: " + orderType);
       // paymentMethodLabel.setText("Payment Method: " + paymentMethod);

        // If pickup, disable address input
        if (orderType.equals("Pickup")) {
            addressTxt.setDisable(true);
            addressTxt.setText("Pickup from Branch");
        }

        // If cash, disable credit card input
        if (paymentMethod.equals("Cash")) {
            cardTxt.setDisable(true);
            cardTxt.setText("Paying in Cash");
        }
    }

    @FXML
    void backHandler() {
        Platform.runLater(() ->ScreenManager.switchScreen("Cart"));
    }
    @FXML
    void handlePlaceOrderBtn() {
        if (CartSession.getCart().getMeals().isEmpty()) {
            showAlert("Error", "Your cart is empty. Please add items before placing an order.");
            return;
        }

        // Collect user input
        String name = nameTxt.getText();
        String address = orderType.equals("Pickup") ? "Pickup" : addressTxt.getText();
        String phone = phoneTxt.getText();
        String email = mailTxt.getText();
        String creditCard = paymentMethod.equals("Cash") ? "Cash Payment" : cardTxt.getText();

        LocalDateTime deliveryTime;
        if (orderType.equals("Pickup")) {
            deliveryTime = LocalDateTime.now();
        } else {
            try {
                String[] timeParts = preferredTime.split(":");
                int hour = Integer.parseInt(timeParts[0].trim());
                int minute = Integer.parseInt(timeParts[1].trim());

                LocalDateTime now = LocalDateTime.now();
                deliveryTime = now.withHour(hour).withMinute(minute);
                if (deliveryTime.isBefore(now)) {
                    deliveryTime = deliveryTime.plusDays(1); // if time has passed, schedule for next day
                }
            } catch (Exception e) {
                showAlert("Invalid Time", "Please enter delivery time in HH:MM format.");
                return;
            }
        }


        // Validate input fields
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || (paymentMethod.equals("Card") && creditCard.isEmpty())) {
            showAlert("Error", "Please fill in all required fields.");
            return;
        }

        // Create an Orders object with the new fields
        Orders newOrder = new Orders(
                CartSession.getCart().getMeals(),
                name, address, phone, email, creditCard,
                CartSession.getCart().getTotalPrice(),
                deliveryTime,
                orderType, paymentMethod // Pass new fields
        );

        // Send order to server
        try {
            client.sendToServer(new Message(newOrder, "#PlaceOrder"));
        } catch (IOException e) {
            showAlert("Connection Error", "Failed to send order to the server. Please check your connection.");
        }

        Platform.runLater(() -> ScreenManager.switchScreen("Primary"));
        showAlert("Order Placed", "Your order has been placed successfully!");
        CartSession.getCart().clearCart();

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
