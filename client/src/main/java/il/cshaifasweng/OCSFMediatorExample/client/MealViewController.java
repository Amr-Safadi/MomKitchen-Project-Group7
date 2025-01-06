package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class MealViewController {

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

   @FXML
    void initialize() {
    }

    public void btnEditHandler (ActionEvent event) {
        printMeal();

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
            client.sendToServer(meal);
        } catch (IOException e) {
            System.out.println("Error sending the updated meal to server - MealViewController");
            throw new RuntimeException(e);
        }

        btnBackHandler(event);
    }

    public void btnBackHandler (ActionEvent event) {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Secondary.fxml"));
        Parent secondaryRoot = null;
        try {
            secondaryRoot = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get the current stage from the button
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Set the new scene
        Scene scene = new Scene(secondaryRoot);
        stage.setScene(scene);
        stage.show();
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
