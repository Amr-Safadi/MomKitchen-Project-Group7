package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import il.cshaifasweng.OCSFMediatorExample.client.CartSession;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import javafx.scene.input.MouseEvent;

public class SecondaryController {

    SimpleClient client = SimpleClient.getClient();

    @FXML
    private BorderPane borderPane;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane pane;

    @FXML
    private Button backBtn;

    @FXML
    private Button searchBtn;

    @FXML
    private Label mealsLabel;

    @FXML
    private ListView<String> mealsList;

    Image backgroundImage = new Image(String.valueOf(SecondaryController.class.getResource("/Images/NEWBACKGRND.jpg")));

    public static ArrayList<Meals> mealsArrayList = new ArrayList<>();
    public static String branch = "" ;

    @Subscribe
    public void initializeListView(Message msg) {
        if (msg.toString().equals("#Initialize Meals")) { // Use .equals() for string comparison

            Platform.runLater(() -> {
                System.out.println("Meals are being initialized");
                mealsArrayList = (ArrayList<Meals>) msg.getObject(); // assign the list

                mealsList.getItems().clear(); // Clear the current items in the ListView
                for (Meals meal : mealsArrayList) {
                    mealsList.getItems().add(meal.getName() + " - $" + meal.getPrice());
                }

                System.out.println("mealsList Initialized - SecondaryController"); // Debugging tool
            });
        }
    }
    @FXML
    public void handleSearchBtn() {
        ScreenManager.switchScreen("categories");
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
                client.sendToServer(new Message(branch,"#Meals Request")); //**//
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @FXML
    void handleCartBtn(ActionEvent event) {
        ScreenManager.switchScreen("Cart");
    }

@FXML
   public void handleBackBtn() {
    ScreenManager.switchScreen("Primary");
        CartSession.clearCart();
    System.out.println("cart has been cleared after backing from the branch");
    }
    @FXML
    void initialize() {
        EventBus.getDefault().register(this);
        try {
            client.sendToServer(new Message(branch,"#Meals Request")); //**//
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert mealsLabel != null : "fx:id=\"mealsLabel\" was not injected: check your FXML file 'secondary.fxml'.";
        assert mealsList != null : "fx:id=\"mealsList\" was not injected: check your FXML file 'secondary.fxml'.";

        mealsLabel.setText(branch + "'s " + mealsLabel.getText());
        // Create and set the background image at the root StackPane level
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
