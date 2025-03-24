package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.CartSession;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
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
    private Button addMealBtn;

    @FXML
    private Button backBtn;

    @FXML
    private Button searchBtn;

    @FXML
    private Button cartBtn;

    @FXML
    private Button manageTablesBtn;

    @FXML
    private Label mealsLabel;

    @FXML
    private Label specialLabel;

    @FXML
    private Label hoursLabel;

    @FXML
    private ListView<String> mealsList;

    @FXML
    private ListView<String> specialsList;

    @FXML
    private Button reservationBtn;

   public static ArrayList<Meals> receivedMeals;
   public static ArrayList<Meals> generalMeals;
   public static ArrayList<Meals> specialMeals;

    @Subscribe
    public void onMealsInitialized(Message msg) {
        if ("#Initialize Meals".equals(msg.toString())) {
            Platform.runLater(() -> {
                System.out.println("Initializing meals for branch: " + SecondaryService.getBranch());
                 receivedMeals = (ArrayList<Meals>) msg.getObject();
                 generalMeals = new ArrayList<>();
                 specialMeals = new ArrayList<>();

                for (Meals meal : receivedMeals) {
                    if (meal.getisBranchMeal() == false)
                        generalMeals.add(meal);
                    else
                        specialMeals.add(meal);
                }
                SecondaryService.setMealsList(generalMeals);
                mealsList.getItems().clear();
                for (Meals meal : SecondaryService.getMealsList()) {
                    mealsList.getItems().add(meal.getName() + " - $" + meal.getPrice());
                }
                SecondaryService.setMealsList(specialMeals);
                specialsList.getItems().clear();
                for (Meals meal : SecondaryService.getMealsList()) {
                    specialsList.getItems().add(meal.getName() + " - $" + meal.getPrice());
                }
                SecondaryService.setMealsList(receivedMeals);
            });
            User loggedInUser = UserSession.getUser();
            if (loggedInUser != null) {
                if (
                        loggedInUser.getRole() == User.Role.DIETITIAN ||
                                loggedInUser.getRole() == User.Role.BRANCH_MANAGER ||
                                loggedInUser.getRole() == User.Role.GENERAL_MANAGER) {
                    addMealBtn.setVisible(true);
                } else {
                    addMealBtn.setVisible(false);
                }
            } else {
                addMealBtn.setVisible(false);
            }
        }
    }

    @FXML
    public void handleAddMeal(ActionEvent event) {
        Platform.runLater(() -> ScreenManager.switchScreen("AddMeal"));
    }
    @FXML
    public void handleSearchBtn(ActionEvent event) {
        Platform.runLater(() -> ScreenManager.switchScreen("categories"));
    }

    @FXML
    public void handleMenuDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedMealInfo = mealsList.getSelectionModel().getSelectedItem();
            String selectedSpecialInfo = specialsList.getSelectionModel().getSelectedItem();
            if (selectedMealInfo != null) {
                String selectedMealName = selectedMealInfo.split(" - ")[0];
                openMealView(selectedMealName);
            }
            if (selectedSpecialInfo != null) {
                String selectedSpecialName = selectedSpecialInfo.split(" - ")[0];
                openMealView(selectedSpecialName);
            }
        }
    }

    @FXML
    public void handleReservation(ActionEvent event) {
        Platform.runLater(() -> ScreenManager.switchScreen("Reservation"));
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
    public void onBranchFetched(Message msg) {
        if ("#BranchFetched".equals(msg.toString())) {
            Branch branch = (Branch) msg.getObject();
            SecondaryService.setBranchObj(branch);
            System.out.println("Fetched branch: " + branch.getName());
            System.out.println("Branch open time: " + branch.getOpenHour());
            System.out.println("Branch close time: " + branch.getCloseHour());
            System.out.println("location: " + branch.getLocation());
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
        Platform.runLater(() -> ScreenManager.switchScreen("Cart"));
    }

    @FXML
    void handleBackBtn(ActionEvent event) {
        Platform.runLater(() ->   ScreenManager.switchScreen("Primary"));
        CartSession.clearCart();
        System.out.println("Cart cleared after navigating back from the branch.");
    }

    @FXML
    void handleManageTables() {
        Platform.runLater(() -> ScreenManager.switchScreen("TableMap"));
    }

    @FXML
    void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        try {
            SimpleClient.getClient().sendToServer(new Message(SecondaryService.getBranch(), "#BranchRequest"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            client.sendToServer(new Message(SecondaryService.getBranch(), "#Meals Request"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mealsLabel.setText( mealsLabel.getText() + SecondaryService.getBranch());
        specialLabel.setText(SecondaryService.getBranch() + "'s specials");
        switch (SecondaryService.getBranch()){
            case "Haifa": hoursLabel.setText("Openning hours: " + "8:00 - 23:00"); break;
            case "Acre": hoursLabel.setText("Openning hours: " + "9:00 - 21:00"); break;
            case "Tel-Aviv": hoursLabel.setText("Openning hours: " + "10:00 - 22:00"); break;
            case "Netanya": hoursLabel.setText("Openning hours: " + "10:00 - 21:00"); break;
        }
        if (UserSession.getUser() != null) {
            switch (UserSession.getUser().getRole()) {
                case BRANCH_MANAGER:
                case GENERAL_MANAGER:
                case DIETITIAN:
                case SERVICE_EMPLOYEE:
                    manageTablesBtn.setVisible(true);
                    break;
                default:
                    manageTablesBtn.setVisible(false);
                    break;
            }
        } else {
            manageTablesBtn.setVisible(false);
        }

        mealsLabel.setText(SecondaryService.getBranch() + "'s " + mealsLabel.getText());

        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");
    }
}
