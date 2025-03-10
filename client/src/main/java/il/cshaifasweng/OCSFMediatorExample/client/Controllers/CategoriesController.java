package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Services.MenuByCatService;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class CategoriesController {

    private final SimpleClient client = SimpleClient.getClient();

    @FXML
    private AnchorPane titlePane;

    @FXML
    private AnchorPane pane;

    @FXML
    private Label titleLabel;

    @FXML
    private Button drinksBtn;

    @FXML
    private Button italianBtn;

    @FXML
    private Button meatBtn;

    @FXML
    private Button backBtn;

    @FXML
    void handleDrinksBtn(ActionEvent event) {
        try {
            client.sendToServer(new Message("fetchDrinks"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error handling drinks - CategoriesController", e);
        }
        MenuByCatService.setCurrentCategory("Drinks");
        ScreenManager.switchScreen("MenuByCategory");
    }

    @FXML
    void handleItalianBtn(ActionEvent event) {
        try {
            client.sendToServer(new Message("fetchItalian"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error handling Italian - CategoriesController", e);
        }
        MenuByCatService.setCurrentCategory("Italian");
        ScreenManager.switchScreen("MenuByCategory");
    }

    @FXML
    void HandleMeatBtn(ActionEvent event) {
        try {
            client.sendToServer(new Message("fetchMeat"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error handling Meat - CategoriesController", e);
        }
        MenuByCatService.setCurrentCategory("Meat");
        ScreenManager.switchScreen("MenuByCategory");
    }

    @FXML
    void handleBackBtn(ActionEvent event) {
        ScreenManager.switchScreen("Menu List");
    }

    @FXML
    void initialize() {
        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");
    }
}
