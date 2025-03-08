package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.greenrobot.eventbus.Subscribe;


import java.io.IOException;

import static il.cshaifasweng.OCSFMediatorExample.client.App.switchScreen;

public class MealViewController {

    Image backgroundImage = new Image(String.valueOf(PrimaryController.class.getResource("/Images/NEWBACKGRND.jpg")));
    SimpleClient client = SimpleClient.getClient();

    private Meals meal;


    @FXML
    private GridPane gridMeal;
    @FXML private Label lblPrdctName;
    @FXML private Label lblPrdctPrice;
    @FXML private Label lblPrdctIng;
    @FXML private Label lblPrdctPrf;
    @FXML private TextField txtPrdctName;
    @FXML private TextField txtPrdctPrice;
    @FXML private TextField txtPrdctIng;
    @FXML private TextField txtPrdctPrf;
    @FXML private Button btnEdit;
    @FXML private Button btnDone;
    @FXML private Button btnBack;
    @FXML private AnchorPane AnchorPane;

   @FXML
    void initialize() {

       // Get the logged-in user
       User loggedInUser = UserSession.getUser();

       if (loggedInUser != null) {
           // Check if the user is NOT a dietitian or branch manager
           if (loggedInUser.getRole() != User.Role.DIETITIAN && loggedInUser.getRole() != User.Role.BRANCH_MANAGER && loggedInUser.getRole() != User.Role.GENERAL_MANAGER) {
               btnEdit.setVisible(false); // Disable the button
           }
       } else {
           btnEdit.setVisible(true); // If no user is logged in, disable it
       }

       // Create and set the background image at the root StackPane level
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
       AnchorPane.setBackground(new Background(background));
    }

    public void btnEditHandler (ActionEvent event) {


        txtPrdctPrice.setEditable(true);
        //txtPrdctName.setEditable(true);
        //txtPrdctPrf.setEditable(true);
       // txtPrdctIng.setEditable(true);

        btnDone.setVisible(true);
    }
    @FXML
    public void btnDoneHandler (ActionEvent event) {
       // meal.setIngredients(txtPrdctIng.getText());
        //meal.setPreferences(txtPrdctPrf.getText());
        //meal.setName(txtPrdctName.getText());

        String priceInput = txtPrdctPrice.getText();

        if (!isValidPrice(priceInput)) {
            // Show an error message if the input is invalid
            showErrorAlert("Invalid Input", "Please enter a valid positive number for the price.");
            return;
        }

        meal.setPrice(Double.parseDouble(priceInput));

        try {
            client.sendToServer(new Message(meal , "#Update Meal"));
        } catch (IOException e) {
            System.out.println("Error sending the updated meal to server - MealViewController");
            throw new RuntimeException(e);
        }

        btnBackHandler(event);
    }

    public void btnBackHandler (ActionEvent event) {
        switchScreen("Menu List");
    }

    public void setMeal(Meals meal) {
        this.meal = meal;


        this.txtPrdctName.setText(meal.getName());
        this.txtPrdctIng.setText(meal.getIngredients());
        this.txtPrdctPrf.setText(meal.getPreferences());
        this.txtPrdctPrice.setText(String.valueOf(meal.getPrice()));
    }

    public void printMeal() {
        System.out.println(meal.getName() + " " + meal.getIngredients() + " " + meal.getPreferences());
    }

    private boolean isValidPrice(String priceInput) {
        try {
            double price = Double.parseDouble(priceInput);
            // Check if the price is positive
            return price > 0;
        } catch (NumberFormatException e) {
            // Input is not a valid number
            return false;
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
