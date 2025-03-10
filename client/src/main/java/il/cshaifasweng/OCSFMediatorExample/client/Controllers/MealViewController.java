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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.io.IOException;

public class MealViewController {

    private SimpleClient client = SimpleClient.getClient();
    private Meals meal;

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
        UIUtil.styleTextField(txtPrdctPrf);
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
        txtPrdctPrf.setEditable(true);
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
        meal.setPreferences(txtPrdctPrf.getText());
        meal.setName(txtPrdctName.getText());
        String priceInput = txtPrdctPrice.getText();
        if (!UIUtil.isValidPrice(priceInput)) {
            showErrorAlert("Invalid Input", "Please enter a valid positive number for the price.");
            return;
        }
        meal.setPrice(Double.parseDouble(priceInput));
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
        if (meal != null) {
            txtPrdctName.setText(meal.getName());
            txtPrdctIng.setText(meal.getIngredients());
            txtPrdctPrf.setText(meal.getPreferences());
            txtPrdctPrice.setText(String.valueOf(meal.getPrice()));
            updateMealBackground();
        }
    }

    @FXML
    void btnAddToCartHandler(ActionEvent event) {
        if (meal != null) {
            CartSession.getCart().addMeal(meal);
        } else {
            showErrorAlert("Error", "No meal selected to add to cart.");
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
