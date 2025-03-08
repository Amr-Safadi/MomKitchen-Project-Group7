package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.CartSession;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import static il.cshaifasweng.OCSFMediatorExample.client.App.switchScreen;

public class CartController {

    @FXML
    private Button backBtn;

    @FXML
    private ListView<Meals> cartView; // Store actual Meals objects

    @FXML
    void initialize() {
        loadCartItems(); // Load cart items when the scene initializes
    }

    @FXML
    void backHandler() {
        switchScreen("Menu List");
    }

    // Method to load cart items into the ListView
    private void loadCartItems() {
        cartView.getItems().clear(); // Clear previous items
        cartView.getItems().addAll(CartSession.getCart().getMeals()); // Add Meals objects

        // Custom cell factory to display the name and price
        cartView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
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
}
