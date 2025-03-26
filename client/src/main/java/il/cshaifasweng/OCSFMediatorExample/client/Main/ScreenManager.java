package il.cshaifasweng.OCSFMediatorExample.client.Main;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScreenManager {
    private static Stage primaryStage;
    private static Scene scene;
    private static final Map<String, String> screens = new HashMap<>();

    static {
        screens.put("Primary", "primary.fxml");
        screens.put("Login", "Login.fxml");
        screens.put("Cart", "Cart.fxml");
        screens.put("Meal View", "MealView.fxml");
        screens.put("Menu List", "secondary.fxml");
        screens.put("MenuByCategory", "MenuByCategory.fxml");
        screens.put("categories", "categories.fxml");
        screens.put("check out", "CheckOut.fxml");
        screens.put("Reservation", "Reservation.fxml");
        screens.put("CancelReservationSearch", "CancelReservationSearch.fxml");
        screens.put("CancelReservationList", "CancelReservationList.fxml");
        screens.put("Contact Us", "ContactUs.fxml");
        screens.put("TableMap", "TableMap.fxml");
        screens.put("Validate User", "ValidateUser.fxml");
        screens.put("Cancel Order", "CancelOrder.fxml");
        screens.put("Manage Complaints", "ComplaintManage.fxml");
        screens.put("AddMeal", "AddMeal.fxml");
        screens.put("Reports", "Reports.fxml");
        screens.put("ConnectionSetup", "ConnectionSetup.fxml");

    }

    public static void init(Stage stage) throws IOException {
        primaryStage = stage;
        Parent root = loadFXML(screens.get("ConnectionSetup"));
        scene = new Scene(root, 600, 600);
        setTitle("MomKitchen");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static Parent loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(ScreenManager.class.getResource("/il/cshaifasweng/OCSFMediatorExample/client/" + fxmlFile));
        return loader.load();
    }

    public static void switchScreen(String screenName) {
        String fxmlFile = screens.get(screenName);
        if (fxmlFile == null) {
            System.err.println("Screen not found: " + screenName);
            return;
        }
        Platform.runLater(() -> {
            try {
                Parent root = loadFXML(fxmlFile);
                scene.setRoot(root);
                setTitle(screenName);
            } catch (IOException e) {
                System.err.println("Error loading screen: " + screenName);
                e.printStackTrace();
            }
        });
    }

    private static void setTitle(String title) {
        primaryStage.setTitle(title);
    }
}
