package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.CartSession;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

public class CartController {

    @FXML
    private Button backBtn;

    @FXML
    private Button removeBtn;

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
        }
    }
}
