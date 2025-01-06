package il.cshaifasweng.OCSFMediatorExample.client;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import javafx.scene.input.MouseEvent;


public class SecondaryController {

    SimpleClient client = SimpleClient.getClient();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label mealsLabel;

    @FXML
    private ListView<String> mealsList;

    public static ArrayList<Meals> mealsArrayList =new ArrayList<>();

    @Subscribe
    public void initializeListView(ArrayList<Meals> list) {
        Platform.runLater(() -> {
            mealsArrayList = list;
            System.out.println("Meals are being initialized");

            mealsList.getItems().clear(); // Clear the current items in the ListView
            for (Meals meal : mealsArrayList) {
                mealsList.getItems().add(meal.getName() + " - $" + meal.getPrice());
            }

            System.out.println("mealsList Initialized - SecondaryController"); // Debugging tool
        });
    }


    public void handleMenuBtn(MouseEvent event) {
        if (event.getClickCount() == 2) { // Double-click to open the new screen
            String selectedMealInfo = mealsList.getSelectionModel().getSelectedItem(); // Get the selected item (e.g., "Meal Name - $20.00")

            if (selectedMealInfo != null) {
                String selectedMealName = selectedMealInfo.split(" - ")[0]; // Extract the name before the dash
                searchAndSendMeal(selectedMealName);
            }
        }
    }

    private void searchAndSendMeal(String mealName) {
        Meals foundMeal = null;

        for (Meals meal : mealsArrayList) { // Search for the meal by name
            if (meal.getName().equals(mealName)) {
                foundMeal = meal;
                break;
            }
        }

        if (foundMeal != null) {
            openEditScreen(foundMeal); // Pass the meal to the next screen
        } else {
            System.out.println("Meal not found!");
        }
    }

    private void openEditScreen(Meals meal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MealView.fxml"));
            Parent root = loader.load();

            // Get the controller for the new screen
            MealViewController controller = loader.getController();

            // Pass the meal to the controller
            controller.setMeal(meal);

            // Get the current stage (the stage that shows the Secondary screen)
            Stage currentStage = (Stage) mealsList.getScene().getWindow();

            // Replace the scene of the current stage with the new scene
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Edit Meal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    @FXML
    void initialize() {
         EventBus.getDefault().register(this);
        try {
            client.sendToServer("#Meals Request");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert mealsLabel != null : "fx:id=\"mealsLabel\" was not injected: check your FXML file 'secondary.fxml'.";
        assert mealsList != null : "fx:id=\"mealsList\" was not injected: check your FXML file 'secondary.fxml'.";

    }

}
