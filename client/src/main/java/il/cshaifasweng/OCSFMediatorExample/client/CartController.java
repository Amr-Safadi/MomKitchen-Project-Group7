package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.CartSession;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import static il.cshaifasweng.OCSFMediatorExample.client.App.switchScreen;

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
        switchScreen("Menu List");
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
        switchScreen("Check out");
    }
}
