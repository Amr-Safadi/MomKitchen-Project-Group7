package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import static il.cshaifasweng.OCSFMediatorExample.client.App.switchScreen;


public class LoginController {

    @FXML
    private TextField emailInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private Button loginButton;

    @FXML
    private Button backBtn;


    @FXML
    void handleBackBtn(ActionEvent event) {
        switchScreen("Primary");
    }
    /**
     * Handles the login process when the login button is clicked.
     */
    @FXML
    private void handleLogin() {
        String email = emailInput.getText();
        String password = passwordInput.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Email and password cannot be empty.");
            return;
        }

        try {
            // Sending login request to the server
            Message loginMessage = new Message(new User(email, password, null), "#LoginRequest");
            SimpleClient.getClient().sendToServer(loginMessage);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Connection Error", "Could not connect to the server.");
        }
    }

    @Subscribe
    public void onLoginResponse(Message message) {
        Platform.runLater(() -> {
            switch (message.toString()) {
                case "#LoginSuccess":
                    User loggedInUser = (User) message.getObject();
                    System.out.println("Login successful: " + loggedInUser.getEmail() + " | Role: " + loggedInUser.getRole());
                    UserSession.setUser(loggedInUser);
                    switchScreen("Primary");
                    break;

                case "#EmailNotFound":
                    showAlert("Login Failed", "Email not found. Please check and try again.");
                    break;

                case "#IncorrectPassword":
                    showAlert("Login Failed", "Incorrect password. Please try again.");
                    break;

                case "#AlreadyLoggedIn":
                    showAlert("Login Failed", "This account is already logged in from another device.");
                default:
                    System.out.println("Unknown response received: " + message.toString());
                    break;
            }
        });
    }

    /**
     * Displays an alert dialog with the given title and message.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    void initialize() {
        EventBus.getDefault().register(this);

      /*  // Create and set the background image at the root StackPane level
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

        // Ensure the background is applied to the entire scene
      /*  if (borderPane.getParent() instanceof StackPane) {
            ((StackPane) borderPane.getParent()).setBackground(new Background(background));
        } else {
            System.out.println("BorderPane is not wrapped in a StackPane.");
        } */
    }
}
