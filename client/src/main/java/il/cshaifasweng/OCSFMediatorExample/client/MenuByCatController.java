package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class MenuByCatController{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button backBtn;

    @FXML
    private Button cartBtn;

    @FXML
    private Label mealsLabel;

    @FXML
    private ListView<String> mealsList;

    @FXML
    private AnchorPane pane;


    SimpleClient client = SimpleClient.getClient();
    public static ArrayList<Meals> mealsArrayList = new ArrayList<>();
    public static String currentCategory;

    @Subscribe
    public void initializedMeals(Message message){
        System.out.println("initializedMeals - MenuByCatController");
        if (message.toString().equals("Category Fetched")) {
            mealsList.getItems().clear();
            mealsArrayList.clear();

            mealsArrayList.addAll((ArrayList<Meals>) message.getObject());

            if (mealsArrayList.isEmpty()) {
                System.out.println("No meals found for this category.");
            } else {
                for (Meals meal : mealsArrayList) {
                    mealsList.getItems().add(meal.getName() + " - $" + meal.getPrice());
                }
            }

            System.out.println("mealsList Updated - MenuByCatController");
        }
    }
    Image backgroundImage = new Image(String.valueOf(PrimaryController.class.getResource("/Images/NEWBACKGRND.jpg")));


    @FXML
    void handleBackBtn(ActionEvent event) {
        ScreenManager.switchScreen("categories");
    }

    @FXML
    void handleCartBtn(ActionEvent event) {
        ScreenManager.switchScreen("Cart");
    }


    public void handleMenuBtn(MouseEvent event) {
        if (event.getClickCount() == 2) { // Double-click to open the new screen
            String selectedMealInfo = mealsList.getSelectionModel().getSelectedItem(); // Get the selected item

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
            MealViewController controller = loader.getController();
            controller.setMeal(meal);
            Stage currentStage = (Stage) mealsList.getScene().getWindow();
            // Update the existing scene's root instead of creating a new Scene
            currentStage.getScene().setRoot(root);
            currentStage.setTitle("Edit Meal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //once a meal is updated we forced all the branch to re fetch their meals to make sure everything is up to date
    @Subscribe
    public void updateMeals(Message msg) {
        if (msg.toString().equals("#Update All Meals")) {
            try {
                System.out.println("fetch" + currentCategory + " , , , menubycatcaontroler");
                client.sendToServer(new Message("fetch" + currentCategory)); //**//
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        assert backBtn != null : "fx:id=\"backBtn\" was not injected: check your FXML file 'MenuByCategory.fxml'.";
        assert cartBtn != null : "fx:id=\"cartBtn\" was not injected: check your FXML file 'MenuByCategory.fxml'.";
        assert mealsLabel != null : "fx:id=\"mealsLabel\" was not injected: check your FXML file 'MenuByCategory.fxml'.";
        assert mealsList != null : "fx:id=\"mealsList\" was not injected: check your FXML file 'MenuByCategory.fxml'.";
        assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'MenuByCategory.fxml'.";

        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                        BackgroundSize.AUTO,
                        BackgroundSize.AUTO,
                        true,
                        true,
                        true,
                        false
                )
        );

        pane.setBackground(new Background(background));
    }

}
