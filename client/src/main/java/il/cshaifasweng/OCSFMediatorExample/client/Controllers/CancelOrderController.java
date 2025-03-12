package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class CancelOrderController {

    @FXML private ListView<String> ordersListView;
    @FXML private Label orderStatusLabel;
    @FXML private Label refundLabel;
    @FXML private Button cancelOrderBtn;
    @FXML private Button backBtn;

    public static List<Orders> Orders; // List of user's orders
    private Orders selectedOrder; // The currently selected order

    @FXML
    public void initialize() {
        ordersListView.getItems().clear();

        if (Orders != null) {
            for (Orders order : Orders) {
                ordersListView.getItems().add(
                        "Order ID: " + order.getId() +
                                " | Placed: " + order.getOrderPlacedTime().toLocalDate() +
                                " at " + order.getOrderPlacedTime().toLocalTime()
                );
            }
        }
    }

    @FXML
    void selectOrder(MouseEvent event) {
            if (event.getClickCount() == 2) { // Double-click
                int selectedIndex = ordersListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex != -1) {
                    selectedOrder = Orders.get(selectedIndex); // Update selected order
                    showOrderDetailsPopup(); // Show pop-up
                }
            }

        displayOrderDetails();
        /*// Handle selection change: Update UI
        ordersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int selectedIndex = ordersListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                selectedOrder = Orders.get(selectedIndex);

            }
        });*/

    }
    private void showOrderDetailsPopup() {
        if (selectedOrder == null) return;

        Alert orderDetailsAlert = new Alert(Alert.AlertType.INFORMATION);
        orderDetailsAlert.setTitle("Order Details");
        orderDetailsAlert.setHeaderText("Details for Order ID: " + selectedOrder.getId());

        StringBuilder details = new StringBuilder();
        details.append("Placed On: ").append(selectedOrder.getOrderPlacedTime()).append("\n");
        details.append("Total Price: $").append(selectedOrder.getTotalPrice()).append("\n");
        details.append("Status: ").append(selectedOrder.getStatus()).append("\n");
        details.append("Delivery/Pickup Time: ").append(selectedOrder.getDeliveryTime()).append("\n");
        details.append("Meals Ordered:\n");

        for (Meals meal : selectedOrder.getMeals()) {
            details.append("- ").append(meal.getName()).append(" ($").append(meal.getPrice()).append(")\n");
        }

        orderDetailsAlert.setContentText(details.toString());
        orderDetailsAlert.showAndWait();
    }

    private void displayOrderDetails() {
        if (selectedOrder == null) return;

        orderStatusLabel.setText("Order Status: " + selectedOrder.getStatus());

        Duration duration = Duration.between(LocalDateTime.now(), selectedOrder.getDeliveryTime());
        long hoursLeft = duration.toHours();

        if (hoursLeft >= 3) {
            refundLabel.setText("Cancellation is free.");
        } else if (hoursLeft >= 1) {
            refundLabel.setText("50% of the order price will be charged.");
        } else {
            refundLabel.setText("No refund available.");
            cancelOrderBtn.setDisable(true);
        }
    }

    @FXML
    void handleCancelOrder() {
        if (selectedOrder == null) {
            showAlert("Error", "Please select an order to cancel.");
            return;
        }

        try {
            SimpleClient.getClient().sendToServer(new Message(selectedOrder, "#CancelOrder"));
        } catch (IOException e) {
            showAlert("Error", "Failed to send cancellation request.");
        }
    }

    @Subscribe
    public void onOrderCancellationResponse(Message message) {
        Platform.runLater(() -> {
            if (message.toString().equals("#OrderCanceled")) {
                showAlert("Success", "Order has been successfully canceled.");
                int selectedIndex = ordersListView.getSelectionModel().getSelectedIndex();
                ordersListView.getItems().remove(selectedIndex);
                Orders.remove(selectedIndex);
            } else {
                showAlert("Error", "Failed to cancel the order.");
            }
        });
    }

    @FXML
    void handleBack() {
        ScreenManager.switchScreen("Primary");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
