package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.CartSession;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.client.util.UIUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class MealViewController {

    private SimpleClient client = SimpleClient.getClient();
    private Meals meal = new Meals();
    private boolean movedMeal = false;

    @FXML private Button deleteMealBtn;
    @FXML
    private Button cartBtn, addToCartBtn, btnEdit, btnDone, btnBack, toggleMealTypeBtn;
    @FXML
    private GridPane gridMeal;
    @FXML
    private Label lblPrdctName, lblPrdctPrice, lblPrdctIng, lblPrdctPrf;
    @FXML
    private TextField txtPrdctName, txtPrdctPrice, txtPrdctIng, txtPrdctPrf;
    @FXML
    private AnchorPane rootPane;

    @FXML
    private CheckBox ingredint1,ingredint2,ingredint3,ingredint4,ingredint5,ingredint6,ingredint7,ingredint8;

    @FXML
    void initialize() {
        EventBus.getDefault().register(this);

        toggleMealTypeBtn.setVisible(false);
        deleteMealBtn.setVisible(false);

        User loggedInUser = UserSession.getUser();
        boolean isEditable = false;
        if (loggedInUser != null) {
            isEditable = loggedInUser.getRole() == User.Role.DIETITIAN ||
                    loggedInUser.getRole() == User.Role.BRANCH_MANAGER ||
                    loggedInUser.getRole() == User.Role.GENERAL_MANAGER;
            btnEdit.setVisible(isEditable);
        }

        Image bgImage = new Image(getClass().getResource("/images/NEWBACKGRND.jpg").toExternalForm());
        BackgroundImage background = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, false)
        );
        rootPane.setBackground(new Background(background));

        UIUtil.styleTextField(txtPrdctName);
        UIUtil.styleTextField(txtPrdctPrice);
        UIUtil.styleTextField(txtPrdctIng);

    }

    /*
    @Subscribe
    public void onUpdateMeal(Message message) {
        Platform.runLater(() -> {
            if (message.toString().equals("#Update All Meals")) {
                System.out.println("lol");
                Meals updatedMeal = findUpdatedMeal(meal.getId());

                if (updatedMeal != null) {
                    setMeal(updatedMeal); // Refresh UI with updated meal details
                    showConfirmationAlert("Meal Updated", "The meal details have been refreshed.");
                } else {
                    showErrorAlert("Error", "Failed to find updated meal details.");
                }
            }
        });
    }

    private Meals findUpdatedMeal(int mealId) {
        for (Meals m : SecondaryService.getMealsList()) {
            if (m.getId() == mealId) {
                return m;
            }
        }
        return null; // If not found, return null
    }
*/

    @FXML
    void handleDeleteMeal() {
        if (meal == null) {
            showErrorAlert("Error", "Please select a meal to delete.");
            return;
        }

        // Show confirmation alert
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Meal");
        confirmation.setHeaderText("Are you sure you want to delete this meal?");
        confirmation.setContentText("Meal: " + meal.getName());


        // If user confirms deletion
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    SimpleClient.getClient().sendToServer(new Message(meal, "#DeleteMeal"));
                } catch (IOException e) {
                    showErrorAlert("Error", "Failed to send delete request.");
                    e.printStackTrace();
                }
            }
        });
    }

    @Subscribe
    public void onMealDeletionResponse(Message message) {
        Platform.runLater(() -> {
            if (message.toString().equals("#MealDeleted")) {
                showConfirmationAlert("Success", "Meal has been successfully deleted.");
                ScreenManager.switchScreen("Menu List"); // Go back to menu after deletion
            }
            if (message.toString().equals("#MealDeletionFailed"))
            {
                showErrorAlert("Error", "Failed to delete the meal.");
            }
        });
    }

    private void updateMealBackground() {
        if (meal == null) return;
        AnchorPane imagePane = BackgroundUtil.createMealImagePane(meal);
        if (imagePane != null) {
            gridMeal.getChildren().addFirst(imagePane);
            gridMeal.setStyle("-fx-background-color: transparent;");
            gridMeal.setBackground(BackgroundUtil.createTransparentBackground());
        }
    }

    @FXML
    void btnCartHandler(ActionEvent event) {
        ScreenManager.switchScreen("Cart");
    }

    public void btnEditHandler(ActionEvent event) {
        txtPrdctPrice.setEditable(true);
        txtPrdctName.setEditable(true);
        txtPrdctIng.setEditable(true);
        btnDone.setVisible(true);
        toggleMealTypeBtn.setVisible(true);
        deleteMealBtn.setVisible(true);
    }
    @FXML
    void handleToggleMealType() {
        if (meal == null) {
            showErrorAlert("Error", "No meal selected.");
            return;
        }

        // If it's becoming a Branch Meal, we need the branch ID
        Integer branchId = null;
        if (!meal.getisBranchMeal()) {
            branchId = SecondaryService.getBranchObj().getId();
        }

        // Create a request object (or just send an array with both values)
        Message request = new Message(new Object[]{meal.getId(), branchId}, "#ToggleMealType");

        try {
            client.sendToServer(request);

            if (meal.getisBranchMeal())
            {
                showConfirmationAlert("Meal Type Change" , meal.getName() + " type has been changed to a Network Meal");
            }
            else {
                showConfirmationAlert("Meal Type Change", meal.getName() +" type has been changed to a Branch Meal");
            }
        } catch (IOException e) {
            showErrorAlert("Error", "Failed to send meal update request.");
        }
    }




    @FXML
    public void btnDoneHandler(ActionEvent event) {
        if (meal == null) {
            showErrorAlert("Error", "Meal data is missing.");
            return;
        }

        meal.setIngredients(txtPrdctIng.getText());
        meal.setName(txtPrdctName.getText());

        // Validate price input
        String priceInput = txtPrdctPrice.getText();
        if (!UIUtil.isValidPrice(priceInput)) {
            showErrorAlert("Invalid Input", "Please enter a valid positive number for the price.");
            return;
        }
        meal.setPrice(Double.parseDouble(priceInput));

        // Store selected preferences before saving
        StringBuilder updatedPreferences = new StringBuilder();
        CheckBox[] checkBoxes = {ingredint1, ingredint2, ingredint3, ingredint4,
                ingredint5, ingredint6, ingredint7, ingredint8};

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isVisible() && checkBox.isSelected()) {
                if (updatedPreferences.length() > 0) {
                    updatedPreferences.append(", ");
                }
                updatedPreferences.append(checkBox.getText());
            }
        }

        // Update the meal's preferences
        meal.setPreferences(updatedPreferences.toString());
        // Send updated meal to server
        try {
            System.out.println("send to the server the meal" + meal.getName() + "with the isbranch = " + meal.getisBranchMeal());
            client.sendToServer(new Message(meal, "#Update Meal"));
        } catch (IOException e) {
            throw new RuntimeException("Error sending the updated meal to server", e);
        }

        btnBackHandler(event);
    }

    public void btnBackHandler(ActionEvent event) {
        ScreenManager.switchScreen("Menu List");
    }

    public void setMeal(Meals meal) {
        this.meal = meal;
        this.txtPrdctName.setText(meal.getName());
        this.txtPrdctIng.setText(meal.getIngredients());
        this.txtPrdctPrice.setText(String.valueOf(meal.getPrice()));

        // Get preferences from the meal object (comma-separated)
        String preferencesString = meal.getPreferences();
        String[] preferences = preferencesString == null || preferencesString.trim().isEmpty() ? new String[0] : preferencesString.split(",");

        // Store all checkboxes in an array
        CheckBox[] checkBoxes = {ingredint1, ingredint2, ingredint3, ingredint4,
                ingredint5, ingredint6, ingredint7, ingredint8};

        // Hide all checkboxes initially
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setVisible(false);
            checkBox.setSelected(false);
        }

        // **NEW FIX** → Only show checkboxes if preferences exist
        if (preferences.length > 0) {
            for (int i = 0; i < preferences.length && i < checkBoxes.length; i++) {
                checkBoxes[i].setText(preferences[i].trim());
                checkBoxes[i].setVisible(true);
                checkBoxes[i].setSelected(true); // By default, all preferences are checked
            }
        }
        updateMealBackground();
    }

    @FXML
    void btnAddToCartHandler(ActionEvent event) {
        if (meal == null) {
            showErrorAlert("Error", "No meal selected to add to cart.");
            return;
        }

        // Store selected preferences
        StringBuilder updatedPreferences = new StringBuilder();
        CheckBox[] checkBoxes = {ingredint1, ingredint2, ingredint3, ingredint4,
                ingredint5, ingredint6, ingredint7, ingredint8};

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isVisible() && checkBox.isSelected()) {
                if (updatedPreferences.length() > 0) {
                    updatedPreferences.append(", ");
                }
                updatedPreferences.append(checkBox.getText());
            }
        }

        // Update the meal's preferences before adding to cart
        meal.setPreferences(updatedPreferences.toString());

        System.out.println("Added " +meal.getName()+ " with the preference" + meal.getPreferences());
        // Add meal to cart
        CartSession.getCart().addMeal(meal);
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void showConfirmationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
