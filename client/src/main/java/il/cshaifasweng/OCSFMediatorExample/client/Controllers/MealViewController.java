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
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MealViewController {

    private SimpleClient client = SimpleClient.getClient();
    private Meals meal = new Meals();
    private boolean movedMeal = false;

    @FXML private Button deleteMealBtn;

    @FXML
    private Button cartBtn, addToCartBtn, btnEdit, btnDone, btnBack, toggleMealTypeBtn, btnEditPrefs;;
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
    @FXML private Button btnChangeImage;

    private File selectedImageFile;

    @FXML
    void initialize() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        toggleMealTypeBtn.setVisible(false);
        deleteMealBtn.setVisible(false);
        btnEditPrefs.setVisible(false);
        btnEdit.setVisible(false);
        btnChangeImage.setVisible(false);
        User loggedInUser = UserSession.getUser();
        boolean isEditable = false;

        if (loggedInUser != null) {
            boolean isDietitian = loggedInUser.getRole() == User.Role.DIETITIAN;
            boolean isGM = loggedInUser.getRole() == User.Role.GENERAL_MANAGER;
            boolean isBranchManagerOfCurrent =
                    loggedInUser.getRole() == User.Role.BRANCH_MANAGER &&
                            loggedInUser.getBranch().equals(SecondaryService.getBranchObj().getName());

            isEditable = isDietitian || isGM || isBranchManagerOfCurrent;
            btnEdit.setVisible(isEditable);
        }



        Image bgImage = new Image(getClass().getResource("/Images/NEWBACKGRND.jpg").toExternalForm());
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


    @FXML
    void handleSelectNewImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Meal Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            System.out.println("✅ New image selected for: " + meal.getName());
        }
    }


    @FXML
    void handleEditPreferences(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(meal.getPreferences());
        dialog.setTitle("Edit Preferences");
        dialog.setHeaderText("Edit the meal's preferences (comma-separated)");
        dialog.setContentText("You can enter up to 8 preferences." +
                "\n  Example: Spicy, Vegan, Gluten-Free");

        dialog.showAndWait().ifPresent(input -> {
            String[] allPrefs = input.split(",");
            StringBuilder limitedPrefs = new StringBuilder();

            for (int i = 0; i < allPrefs.length && i < 8; i++) {
                if (limitedPrefs.length() > 0) {
                    limitedPrefs.append(", ");
                }
                limitedPrefs.append(allPrefs[i].trim());
            }

            String finalPrefs = limitedPrefs.toString();
            meal.setPreferences(finalPrefs);
            updatePreferenceCheckboxes(finalPrefs);
        });
    }

    private void updatePreferenceCheckboxes(String preferencesString) {
        String[] preferences = preferencesString == null || preferencesString.trim().isEmpty()
                ? new String[0]
                : preferencesString.split(",");

        CheckBox[] checkBoxes = {ingredint1, ingredint2, ingredint3, ingredint4,
                ingredint5, ingredint6, ingredint7, ingredint8};

        // Reset
        for (CheckBox cb : checkBoxes) {
            cb.setVisible(false);
            cb.setSelected(false);
            cb.setText("");
        }

        for (int i = 0; i < preferences.length && i < checkBoxes.length; i++) {
            checkBoxes[i].setText(preferences[i].trim());
            checkBoxes[i].setVisible(true);
            checkBoxes[i].setSelected(true);
        }
    }

    @Subscribe
    public void onMealUpdated(Message message) {
        if ("#Update All Meals".equals(message.toString())) {
            // Wait 100ms before trying to get updated meal
            new Thread(() -> {
                try {
                    Thread.sleep(200); // small delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    Meals updated = SecondaryController.receivedMeals.stream()
                            .filter(m -> m.getId() == (meal.getId()))
                            .findFirst()
                            .orElse(null);

                    if (updated != null) {
                        setMeal(updated);

                     //   showConfirmationAlert("Meal Updated", "This meal has been updated!");
                    }
                });
            }).start();
        }
    }


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
        if (message.toString().equals("#MealDeleted")) {
            // Unregister right away before UI update
            EventBus.getDefault().unregister(this);

            Platform.runLater(() -> {
                showConfirmationAlert("Success", "Meal has been successfully deleted.");
                ScreenManager.switchScreen("Menu List");
            });

        }  if (message.toString().equals("#MealDeletionFailed")) {
            Platform.runLater(() -> {
                showErrorAlert("Error", "Failed to delete the meal.");
            });
        }
    }


    @FXML
    void btnCartHandler(ActionEvent event) {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() -> ScreenManager.switchScreen("Cart"));
    }

    public void btnEditHandler(ActionEvent event) {
        txtPrdctPrice.setEditable(true);
        txtPrdctName.setEditable(true);
        txtPrdctIng.setEditable(true);
        btnDone.setVisible(true);
        toggleMealTypeBtn.setVisible(true);
        deleteMealBtn.setVisible(true);
        btnEdit.setVisible(false);
        addToCartBtn.setVisible(false);
        btnEditPrefs.setVisible(true);
        btnChangeImage.setVisible(true);
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

        // Basic field updates
        meal.setIngredients(txtPrdctIng.getText());
        meal.setName(txtPrdctName.getText());

        // Validate price input
        String priceInput = txtPrdctPrice.getText();
        if (!UIUtil.isValidPrice(priceInput)) {
            showErrorAlert("Invalid Input", "Please enter a valid positive number for the price.");
            return;
        }

        double oldPrice = meal.getPrice();
        double newPrice = Double.parseDouble(priceInput);

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

        meal.setPreferences(updatedPreferences.toString());

        User loggedInUser = UserSession.getUser();

        // 💥 Handle price change with manager approval
        if (oldPrice != newPrice && loggedInUser.getRole() == User.Role.DIETITIAN) {
            try {
                Object[] requestData = new Object[]{ meal, newPrice, loggedInUser };
                SimpleClient.getClient().sendToServer(new Message(requestData, "#RequestPriceChange"));
               // showConfirmationAlert("Request Sent", "Price change request sent to manager for approval.");
               // btnBackHandler(event);  // Go back after sending request
                return;  //  Don't continue with #Update Meal!
            } catch (IOException e) {
                showErrorAlert("Error", "Failed to send price change request.");
                return;
            }
        }

        // ✅ Manager or no price change: update directly
        meal.setPrice(newPrice);
        try {
            System.out.println("send to the server the meal " + meal.getName() + " with isBranch = " + meal.getisBranchMeal());
            client.sendToServer(new Message(meal, "#Update Meal"));
            if (selectedImageFile != null) {
                try {
                    byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());
                    Object[] imagePayload = new Object[] { meal.getName(), imageBytes };
                    SimpleClient.getClient().sendToServer(new Message(imagePayload, "#UploadMealImage"));
                    System.out.println("📤 New image sent to server for: " + meal.getName());
                    selectedImageFile = null;

                } catch (IOException e) {
                    System.out.println("❌ Failed to send updated image.");
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error sending the updated meal to server", e);
        }

    }

    public void btnBackHandler(ActionEvent event) {
        EventBus.getDefault().unregister(this);
        Platform.runLater(() ->ScreenManager.switchScreen("Menu List"));
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
        try {
            Message imageRequest = new Message(meal.getName(), "#RequestMealImage");
            SimpleClient.getClient().sendToServer(imageRequest);
        } catch (IOException e) {
            System.out.println("❌ Failed to request image for meal: " + meal.getName());
            e.printStackTrace();
        }

    }

    @Subscribe
    public void onImageMessageReceived(Message message) {
        if ("receivedImage".equals(message.toString())) {

            Object[] data = (Object[]) message.getObject();

            if (data.length != 2 || !(data[0] instanceof String) || !(data[1] instanceof Image)) {
                System.out.println(" Invalid image data received.");
                return;
            }

            String receivedMealName = (String) data[0];
            Image image = (Image) data[1];

            if (!receivedMealName.equals(meal.getName())) return; // Make sure it's the same meal

            AnchorPane imagePane = BackgroundUtil.createMealImagePaneFromImage(image);

            Platform.runLater(() -> {
                if (!gridMeal.getChildren().isEmpty()) {
                    gridMeal.getChildren().remove(0); // assumes image is always first
                }

                gridMeal.getChildren().addFirst(imagePane);
                gridMeal.setStyle("-fx-background-color: transparent;");
                gridMeal.setBackground(BackgroundUtil.createTransparentBackground());
                System.out.println(" Image displayed for " + receivedMealName);
            });

            System.out.println(" Image displayed for " + receivedMealName);
        }
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

        showConfirmationAlert("Success", "Meal Has been added to your cart.");
        EventBus.getDefault().unregister(this);
        ScreenManager.switchScreen("Menu List");
    }

    @Subscribe
    public void mealupdatesuccesfully (Message message) {
        if (message.toString().equals("Meal Updated Successfully"))
            showConfirmationAlert("Success", "Meal Has been updated successfully.");
    }

    @Subscribe
    public void onPriceChangeResponse(Message message) {
        if ("#PriceChangeRequestSent".equals(message.toString())) {
           showConfirmationAlert("Request Sent", "Your request to change the price was sent.");
            btnBackHandler(null); // Return to menu list
        } else if ("#PriceChangeRequestFailed".equals(message.toString())) {
            showErrorAlert("Error", "Could not send the request. Please try again.");
        }
    }

    private void showErrorAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }


    private void showConfirmationAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

}
