package il.cshaifasweng.OCSFMediatorExample.client.Controllers;
import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;



public class ConnectionSetupController {

    @FXML
    private AnchorPane pane;

    @FXML
    private TextField hostField;

    @FXML
    private TextField portField;

    @FXML
    private Label errorLabel;

    @FXML
    void handleConnect(ActionEvent event) {
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();

        if (host.isEmpty() || portText.isEmpty()) {
            errorLabel.setText("Both host and port are required.");
            return;
        }

        try {
            int port = Integer.parseInt(portText);
            SimpleClient.HOST = host;
            SimpleClient.PORT = port;

            SimpleClient.getClient().openConnection();

            // ✅ Switch to your main screen here
            ScreenManager.switchScreen("Primary"); // or whatever your first screen is
        } catch (NumberFormatException e) {
            errorLabel.setText("Port must be a valid number.");
        } catch (Exception e) {
            errorLabel.setText("Failed to connect to server.");
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");
    }
}
