package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.io.IOException;

public class CategoriesController {

    SimpleClient client = SimpleClient.getClient();

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
            System.out.print("error handle drinks - categories controller");
            throw new RuntimeException(e);
        }
        MenuByCatController.currentCategory = "Drinks";
        ScreenManager.switchScreen("MenuByCategory");
    }

    @FXML
    void handleItalianBtn(ActionEvent event) {
        try {
            client.sendToServer(new Message("fetchItalian"));
        } catch (IOException e) {
            System.out.print("error handle Italian - categories controller");
            throw new RuntimeException(e);
        }
        MenuByCatController.currentCategory = "Italian";
        ScreenManager.switchScreen("MenuByCategory");
    }

    @FXML
    void HandleMeatBtn(ActionEvent event) {
        try {
            client.sendToServer(new Message("fetchMeat"));
        } catch (IOException e) {
            System.out.print("error handle Meat - categories controller");
            throw new RuntimeException(e);
        }
        MenuByCatController.currentCategory = "Meat";
        ScreenManager.switchScreen("MenuByCategory");
    }
    @FXML
    void handleBackBtn(ActionEvent event) {
        ScreenManager.switchScreen("Menu List");
    }

    Image backgroundImage = new Image(String.valueOf(PrimaryController.class.getResource("/Images/NEWBACKGRND.jpg")));

@FXML
    void initialize() {
       // EventBus.getDefault().register(this);
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

