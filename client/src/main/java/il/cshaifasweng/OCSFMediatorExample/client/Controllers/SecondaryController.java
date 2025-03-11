package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.CartSession;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SecondaryController {

    private final SimpleClient client = SimpleClient.getClient();

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
    private Button cartBtn;

    @FXML
    private Label mealsLabel;

    @FXML
    private ListView<String> mealsList;

    @FXML
    private Button reservationBtn;

    @Subscribe
    public void onMealsInitialized(Message msg) {
        if ("#Initialize Meals".equals(msg.toString())) {
            Platform.runLater(() -> {
                System.out.println("Initializing meals for branch: " + SecondaryService.getBranch());
                ArrayList<Meals> receivedMeals = (ArrayList<Meals>) msg.getObject();
                SecondaryService.setMealsList(receivedMeals);

                mealsList.getItems().clear();
                for (Meals meal : SecondaryService.getMealsList()) {
                    mealsList.getItems().add(meal.getName() + " - $" + meal.getPrice());
                }
            });
        }
    }

    @FXML
    public void handleSearchBtn(ActionEvent event) {
        ScreenManager.switchScreen("categories");
    }

    @FXML
    public void handleMenuDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedMealInfo = mealsList.getSelectionModel().getSelectedItem();
            if (selectedMealInfo != null) {
                String selectedMealName = selectedMealInfo.split(" - ")[0];
                openMealView(selectedMealName);
            }
        }
    }

    @FXML
    public void handleReservation(ActionEvent event) {
        ScreenManager.switchScreen("Reservation");
    }

    private void openMealView(String mealName) {
        Meals foundMeal = SecondaryService.getMealsList().stream()
                .filter(meal -> meal.getName().equals(mealName))
                .findFirst()
                .orElse(null);

        if (foundMeal == null) {
            System.out.println("Meal not found: " + mealName);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/MealView.fxml"));
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

    @Subscribe
    public void onMealsUpdated(Message msg) {
        if ("#Update All Meals".equals(msg.toString())) {
            try {
                System.out.println("Re-fetching meals for branch: " + SecondaryService.getBranch());
                client.sendToServer(new Message(SecondaryService.getBranch(), "#Meals Request"));
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
    void handleBackBtn(ActionEvent event) {
        ScreenManager.switchScreen("Primary");
        CartSession.clearCart();
        System.out.println("Cart cleared after navigating back from the branch.");
    }

    @FXML
    void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        try {
            client.sendToServer(new Message(SecondaryService.getBranch(), "#Meals Request"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mealsLabel.setText(SecondaryService.getBranch() + "'s " + mealsLabel.getText());

        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");
    }
}
