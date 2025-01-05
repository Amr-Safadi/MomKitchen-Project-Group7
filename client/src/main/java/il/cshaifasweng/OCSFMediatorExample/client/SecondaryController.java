package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
    public void initializeListView (ArrayList<Meals> list) {
        this.mealsArrayList=list;
        System.out.println("meals are being initialized");

        while (mealsArrayList.size() > 0) {
            mealsList.getItems().add((mealsArrayList.remove(0).toString()));

        }

        System.out.println("mealsList Initialized - SecondaryController"); //debugging tool
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
