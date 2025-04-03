package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Services.MenuByCatService;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MenuByCatController {

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

    private final SimpleClient client = SimpleClient.getClient();

    @Subscribe
    public void onMealsFetched(Message message) {
            if ("Category Fetched".equals(message.toString())) {
                Platform.runLater(() -> {
                    mealsList.getItems().clear();
                    mealsList.refresh();
                    MenuByCatService.setMealsList((ArrayList<Meals>) message.getObject());

                    if (MenuByCatService.getMealsList().isEmpty()) {
                        System.out.println("No meals found for this category.");
                    } else {
                        for (Meals meal : MenuByCatService.getMealsList()) {
                            mealsList.getItems().add(meal.getName() + " - $" + meal.getPrice());
                        }
                    }
                    System.out.println("Meals list updated for category: " + MenuByCatService.getCurrentCategory());
                });
            }

            mealsList.refresh();
    }

    @Subscribe
    public void onMealsUpdated(Message msg) {
        if ("#Update All Meals".equals(msg.toString())) {
            try {
                // Example: fetch the current category again
                client.sendToServer(new Message("fetch" + MenuByCatService.getCurrentCategory()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    void handleBackBtn(ActionEvent event) {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("categories"));
    }

    @FXML
    void handleCartBtn(ActionEvent event) {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() ->  ScreenManager.switchScreen("Cart"));
    }

    @FXML
    public void handleMenuBtn(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedMealInfo = mealsList.getSelectionModel().getSelectedItem();
            if (selectedMealInfo != null) {
                String selectedMealName = selectedMealInfo.split(" - ")[0];
                openMealView(selectedMealName);
            }
        }
    }

    private void openMealView(String mealName) {
        Meals foundMeal = MenuByCatService.getMealsList().stream()
                .filter(m -> m.getName().equals(mealName))
                .findFirst()
                .orElse(null);

        if (foundMeal == null) {
            System.out.println("Meal not found!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/MealView.fxml")
            );
            Parent root = loader.load();
            MealViewController controller = loader.getController();
            controller.setMeal(foundMeal);

            Stage currentStage = (Stage) mealsList.getScene().getWindow();
            currentStage.getScene().setRoot(root);
            currentStage.setTitle("Edit Meal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (MenuByCatService.getCurrentCategory() != null) {
            mealsLabel.setText(MenuByCatService.getCurrentCategory());
        }

        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");


            try {
                client.sendToServer(new Message("fetch" + MenuByCatService.getCurrentCategory()));
                System.out.println("Retrying meal fetch: " + MenuByCatService.getCurrentCategory());
            } catch (IOException e) {
                e.printStackTrace();
            }


    }

    private void updateMealsList() {
        mealsList.getItems().clear();
        for (Meals meal : MenuByCatService.getMealsList()) {
            mealsList.getItems().add(meal.getName() + " - $" + meal.getPrice());
        }
        mealsList.refresh();
    }
}
