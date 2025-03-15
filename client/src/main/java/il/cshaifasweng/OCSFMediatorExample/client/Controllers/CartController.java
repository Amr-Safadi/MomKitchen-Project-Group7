package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Sessions.CartSession;
import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
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
        ScreenManager.switchScreen("Menu List");
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

        // Layout for options
        VBox optionsBox = new VBox(10,
                new Label("Order Type:"), deliveryOption, pickupOption,
                new Label("Payment Method:"), cashOption, cardOption);
        dialog.getDialogPane().setContent(optionsBox);

        // Add OK & Cancel buttons
        ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

        // Handle result
        dialog.setResultConverter(button -> {
            if (button == confirmButton) {
                String orderType = deliveryOption.isSelected() ? "Delivery" : "Pickup";
                String paymentMethod = cashOption.isSelected() ? "Cash" : "Card";
                return new String[]{orderType, paymentMethod};
            }
            return null;
        });

        // Show the dialog and process the response
        dialog.showAndWait().ifPresent(result -> {
            // Pass order type and payment method to checkout screen
            CheckOutController.setOrderPreferences(result[0], result[1]);
            ScreenManager.switchScreen("check out");
        });
    }

}
