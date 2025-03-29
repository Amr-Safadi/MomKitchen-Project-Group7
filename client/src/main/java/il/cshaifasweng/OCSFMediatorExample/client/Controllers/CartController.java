package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.CartSession;
import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class CartController {

    @FXML
    private Button checkOutBtn;

    @FXML
    private Button backBtn;

    @FXML
    private Label priceLabel;

    @FXML
    private Button removeBtn;

    @FXML
    private AnchorPane pane;

    @FXML
    private ListView<Meals> cartView;

    @FXML
    void initialize() {
        configureListView();
        loadCartItems();
    }

    private void configureListView() {
        cartView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        cartView.setCellFactory(listView -> new ListCell<Meals>() {
            @Override
            protected void updateItem(Meals meal, boolean empty) {
                super.updateItem(meal, empty);
                if (empty || meal == null) {
                    setText(null);
                } else {
                    setText(meal.getName() + " - $" + meal.getPrice());
                }
            }
        });
    }

    private void loadCartItems() {
        cartView.getItems().clear();
        cartView.getItems().addAll(CartSession.getCart().getMeals());


        priceLabel.setText("$" + CartSession.getCart().getTotalPrice());
    }

    @FXML
    void backHandler() {
        Platform.runLater(() -> ScreenManager.switchScreen("Menu List"));
    }

    @FXML
    void removeHandler() {
        Meals selectedMeal = cartView.getSelectionModel().getSelectedItem();
        if (selectedMeal != null) {
            CartSession.getCart().removeMeal(selectedMeal);
            cartView.getItems().remove(selectedMeal);
            priceLabel.setText("$" + CartSession.getCart().getTotalPrice());
        }
    }

    @FXML
    void checkOutHandler(ActionEvent event) {
        // Create the selection dialog
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Order Preferences");
        dialog.setHeaderText("Select your order type and payment method");

        // Buttons for Delivery or Pickup
        ToggleGroup deliveryGroup = new ToggleGroup();
        RadioButton deliveryOption = new RadioButton("Delivery");
        deliveryOption.setToggleGroup(deliveryGroup);
        deliveryOption.setSelected(true); // Default

        RadioButton pickupOption = new RadioButton("Pickup");
        pickupOption.setToggleGroup(deliveryGroup);

        // Buttons for Cash or Card Payment
        ToggleGroup paymentGroup = new ToggleGroup();
        RadioButton cashOption = new RadioButton("Cash");
        cashOption.setToggleGroup(paymentGroup);
        cashOption.setSelected(true); // Default

        RadioButton cardOption = new RadioButton("Card");
        cardOption.setToggleGroup(paymentGroup);

        // Time input
        Label timeLabel = new Label("Preferred Delivery Time (HH:MM):");
        TextField timeInputtxt = new TextField();
        timeInputtxt.setPromptText("e.g. 14:30");


        // Layout for options
        VBox optionsBox = new VBox(10,
                new Label("Order Type:"), deliveryOption, pickupOption,
                new Label("Payment Method:"), cashOption, cardOption,
                timeLabel, timeInputtxt);

        dialog.getDialogPane().setContent(optionsBox);

        // Add OK & Cancel buttons
        ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

        // Handle result
        dialog.setResultConverter(button -> {
            if (button == confirmButton) {
                String orderType = deliveryOption.isSelected() ? "Delivery" : "Pickup";
                String paymentMethod = cashOption.isSelected() ? "Cash" : "Card";
                return new String[]{orderType, paymentMethod, timeInputtxt.getText()};

            }
            return null;
        });

        if(CartSession.getCart().getCartSize()>0)
        {
        dialog.showAndWait().ifPresent(result -> {
            String timeInput = result[2].trim();

            // Validate format using regex
            if (!timeInput.matches("^([01]\\d|2[0-3]):([0-5]\\d)$")) {
                showAlert("Invalid Time Format", "Please enter the time in HH:MM format (24-hour).");
                return;
            }

            // Validate time is not in the past
            try {
                String[] parts = timeInput.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.LocalDateTime chosenTime = now.withHour(hour).withMinute(minute);

                if (chosenTime.isBefore(now.plusMinutes(30))) {
                    showAlert("Invalid Time", "Please choose a time that is at least 30 minutes later from now.");
                    return;
                }

                java.time.LocalTime closingTime = SecondaryService.getBranchObj().getCloseHour();
                if (chosenTime.toLocalTime().isAfter(closingTime)) {
                    showAlert("Too Late", "We close at " + closingTime.toString() + " . Please choose an earlier delivery time.");
                    return;
                }

                CheckOutController.setOrderPreferences(result[0], result[1], timeInput);
                Platform.runLater(() -> ScreenManager.switchScreen("check out"));

            } catch (Exception e) {
                showAlert("Invalid Time", "There was an error reading the time. Please try again.");
            }
        });

    }
        else {
            showAlert("Cart is empty", "Please choose an order .");
        }
    }




    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
