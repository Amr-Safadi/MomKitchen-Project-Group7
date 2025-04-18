package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AddMealController {

    @FXML private TextField mealNameField;
    @FXML private TextField mealPriceField;
    @FXML private TextArea mealIngredientsField;
    @FXML private ComboBox<Meals.Category> mealCategoryComboBox;
    @FXML private CheckBox isBranchMealCheckBox;
    @FXML private TextField preference1;
    @FXML private TextField preference2;
    @FXML private TextField preference3;
    @FXML private TextField preference4;
    @FXML private TextField preference5;
    @FXML private TextField preference6;
    @FXML private TextField preference7;
    @FXML private TextField preference8;
    @FXML private Button submitMealBtn;
    @FXML private Button backBtn;
    @FXML
    private Button imageBtn;

    @FXML
    private Label imageName;

    private File selectedImageFile;
    @FXML
    void handleSelectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Meal Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            imageName.setText("Chosen Image: " + file.getName());
            System.out.println("✅ Image selected: " + file.getAbsolutePath());
        }
    }


    @FXML private AnchorPane pane;

    @FXML
    public void initialize() {
        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        // Populate category dropdown using Enum values
        mealCategoryComboBox.setItems(FXCollections.observableArrayList(Meals.Category.values()));
    }

    @Subscribe
    public void onMessageReceived(Message message) {
        if (message.toString().equals("#MealAddedSuccessfully")) {
            EventBus.getDefault().unregister(this);
            Platform.runLater(() -> {
                showAlert("Success", "Meal added successfully!");
                ScreenManager.switchScreen("Menu List"); // Switch back to the main screen
            });
        }
        if (message.toString().equals("#MealAdditionFailed")) {
            showAlert("Failure", "Meal added failed.");
        }
    }
    @FXML
    void handleAddMeal() {
        String name = mealNameField.getText().trim();
        String priceText = mealPriceField.getText().trim();
        String ingredients = mealIngredientsField.getText().trim();
        Meals.Category category = mealCategoryComboBox.getValue();
        boolean isBranchMeal = isBranchMealCheckBox.isSelected();

        // Validate inputs
        if (name.isEmpty() || priceText.isEmpty() || ingredients.isEmpty() || category == null) {
            showAlert("Error", "All fields must be filled.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid price format.");
            return;
        }

        // Gather user-entered preferences as a formatted string
        String preferences = collectPreferences();

        // Create Meal object
        Meals newMeal = new Meals(name, ingredients, preferences, price, category, isBranchMeal);

        // Send the meal to the server
        try {
            SimpleClient.getClient().sendToServer(new Message(newMeal, "#AddMeal"));
            if (selectedImageFile != null) {
                try {
                    byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());
                    Object[] imagePayload = new Object[] { newMeal.getName(), imageBytes };
                    SimpleClient.getClient().sendToServer(new Message(imagePayload, "#UploadMealImage"));
                    System.out.println("📤 Image sent to server for: " + newMeal.getName());
                } catch (IOException e) {
                    System.out.println("❌ Failed to send image.");
                    e.printStackTrace();
                }
            }


        } catch (IOException e) {
            showAlert("Error", "Failed to send meal to the server.");
        }
    }

    private String collectPreferences() {
        List<String> preferences = new ArrayList<>();
        addIfNotEmpty(preference1, preferences);
        addIfNotEmpty(preference2, preferences);
        addIfNotEmpty(preference3, preferences);
        addIfNotEmpty(preference4, preferences);
        addIfNotEmpty(preference5, preferences);
        addIfNotEmpty(preference6, preferences);
        addIfNotEmpty(preference7, preferences);
        addIfNotEmpty(preference8, preferences);
        return String.join(",", preferences); // Convert list to a single string separated by commas
    }

    private void addIfNotEmpty(TextField textField, List<String> list) {
        String text = textField.getText().trim();
        if (!text.isEmpty()) {
            list.add(text);
        }
    }

    @FXML
    void handleBack() {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Primary"));

    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
