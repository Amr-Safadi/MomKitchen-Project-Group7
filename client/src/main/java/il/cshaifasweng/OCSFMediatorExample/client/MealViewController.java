package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.CartSession;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private AnchorPane AnchorPane;

    @FXML
    void initialize() {
        User loggedInUser = UserSession.getUser();

        if (loggedInUser != null) {
            System.out.println(loggedInUser.getRole());
            boolean isEditable = loggedInUser.getRole() == User.Role.DIETITIAN
                    || loggedInUser.getRole() == User.Role.BRANCH_MANAGER
                    || loggedInUser.getRole() == User.Role.GENERAL_MANAGER;
            btnEdit.setVisible(isEditable);
        } else {
            btnEdit.setVisible(false);
        }

        // Set main background
        Image backgroundImage = new Image(String.valueOf(PrimaryController.class.getResource("/Images/NEWBACKGRND.jpg")));
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, false)
        );
        AnchorPane.setBackground(new Background(background));

        // Make text fields bolder and remove transparency
        txtPrdctName.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-text-fill: black;");
        txtPrdctPrice.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-text-fill: black;");
        txtPrdctIng.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-text-fill: black;");
        txtPrdctPrf.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-text-fill: black;");
    }
    /**
     * Method moved outside initialize()
     **/
    private void updateMealBackground() {
        if (meal == null) return;

        // Map of meal names to images
        Map<String, String> mealImages = new HashMap<>();
        mealImages.put("Pizza", "/Images/pizza.jpg");
        mealImages.put("Burger", "/Images/burger.jpg");
        mealImages.put("Pasta", "/Images/pasta.jpg");
        mealImages.put("Mineral Water", "/Images/water.jpg");
        mealImages.put("Diet Coke", "/Images/coke.jpg");
        mealImages.put("Orange juice", "/Images/juice.jpg");
        mealImages.put("Fillet Steak", "/Images/filletSteak.jpg");
        mealImages.put("Chicken Wings", "/Images/chickenWings.jpg");
        mealImages.put("cheese Ravioli", "/Images/ravioli.jpg");
        mealImages.put("Sezar Salad", "/Images/seafood.jpg");

        // Get the meal image path
        String imagePath = mealImages.get(meal.getName());
        if (imagePath == null) return;

        // Load the meal image
        Image mealImage = new Image(getClass().getResource(imagePath).toExternalForm());

        // Create an ImageView with the loaded image
        ImageView imageView = new ImageView(mealImage);

        // Set the desired opacity for the image (e.g., 0.3 means 30% opacity)
        imageView.setOpacity(0.3); // Adjust this value as needed for transparency

        // Resize the imageView to fixed 600x600 size
        imageView.setFitWidth(600); // Set the fixed width
        imageView.setFitHeight(600); // Set the fixed height
        imageView.setPreserveRatio(true); // Keep aspect ratio intact

        // Create an AnchorPane to layer the image
        AnchorPane imagePane = new AnchorPane();
        imagePane.getChildren().add(imageView);

        // Set the image position using AnchorPane
        AnchorPane.setTopAnchor(imageView, 330.0);  // Move it less down (adjust to 250)
        AnchorPane.setLeftAnchor(imageView, -50.0);  // Move it less to the left (adjust to -50)

        // Add the imagePane to the GridPane (Ensure it's behind other content)
        gridMeal.getChildren().addFirst(imagePane);

        // Make the GridPane background transparent by setting the style of the grid to transparent
        gridMeal.setStyle("-fx-background-color: transparent;");

        // Optional: Apply a semi-transparent overlay to gridMeal for additional effects (optional)
        // The overlay will still be visible, but the background will remain transparent.
        BackgroundFill transparentFill = new BackgroundFill(
                Color.rgb(255, 255, 255, 0.0), // 0% transparent white (making it fully transparent)
                CornerRadii.EMPTY,
                javafx.geometry.Insets.EMPTY
        );
        gridMeal.setBackground(new Background(new BackgroundFill[]{transparentFill}));
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
        if (!isValidPrice(priceInput)) {
            showErrorAlert("Invalid Input", "Please enter a valid positive number for the price.");
            return;
        }

        meal.setPrice(Double.parseDouble(priceInput));

        try {
            client.sendToServer(new Message(meal, "#Update Meal"));
        } catch (IOException e) {
            System.out.println("Error sending the updated meal to server - MealViewController");
            throw new RuntimeException(e);
        }

        btnBackHandler(event);
    }

    public void btnBackHandler(ActionEvent event) {
        ScreenManager.switchScreen("Menu List");
    }

    public void setMeal(Meals meal) {
        this.meal = meal;
        if (meal != null) {
            this.txtPrdctName.setText(meal.getName());
            this.txtPrdctIng.setText(meal.getIngredients());
            this.txtPrdctPrf.setText(meal.getPreferences());
            this.txtPrdctPrice.setText(String.valueOf(meal.getPrice()));

            // Update background when setting a new meal
            updateMealBackground();
        }
    }

    public void printMeal() {
        if (meal != null) {
            System.out.println(meal.getName() + " " + meal.getIngredients() + " " + meal.getPreferences());
        } else {
            System.out.println("Meal is null");
        }
    }

    private boolean isValidPrice(String priceInput) {
        try {
            double price = Double.parseDouble(priceInput);
            return price > 0;
        } catch (NumberFormatException e) {
            return false;
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
