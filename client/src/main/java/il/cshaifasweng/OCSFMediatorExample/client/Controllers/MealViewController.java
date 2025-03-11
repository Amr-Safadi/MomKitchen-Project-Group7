package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.CartSession;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.client.util.UIUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.io.IOException;

public class MealViewController {

    private SimpleClient client = SimpleClient.getClient();
    private Meals meal = new Meals();

    @FXML
    private Button cartBtn, addToCartBtn, btnEdit, btnDone, btnBack;
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
        User loggedInUser = UserSession.getUser();
        if (loggedInUser != null) {
            boolean isEditable = loggedInUser.getRole() == User.Role.DIETITIAN ||
                    loggedInUser.getRole() == User.Role.BRANCH_MANAGER ||
                    loggedInUser.getRole() == User.Role.GENERAL_MANAGER;
            btnEdit.setVisible(isEditable);
        } else {
            btnEdit.setVisible(false);
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
       // UIUtil.styleTextField(txtPrdctPrf);
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
        //txtPrdctPrf.setEditable(true);
        txtPrdctIng.setEditable(true);
        btnDone.setVisible(true);
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
}
