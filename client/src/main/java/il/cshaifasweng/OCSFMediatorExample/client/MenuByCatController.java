package il.cshaifasweng.OCSFMediatorExample.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import static il.cshaifasweng.OCSFMediatorExample.client.App.switchScreen;


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

    public static ArrayList<Meals> mealsArrayList = new ArrayList<>();

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
        switchScreen("categories");
    }

    @FXML
    void handleCartBtn(ActionEvent event) {

    }

    @FXML
    void handleMenuBtn(MouseEvent event) {

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
