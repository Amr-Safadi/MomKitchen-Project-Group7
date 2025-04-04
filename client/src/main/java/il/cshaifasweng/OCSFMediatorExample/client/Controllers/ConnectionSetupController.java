package il.cshaifasweng.OCSFMediatorExample.client.Controllers;
import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.greenrobot.eventbus.EventBus;


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

            System.out.println("host " + host);
            System.out.println("port " + port);
            SimpleClient.getClient(host,port).openConnection();

        EventBus.getDefault().unregister(this);
            ScreenManager.switchScreen("Primary"); // or whatever your first screen is
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            errorLabel.setText("Port must be a valid number.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            errorLabel.setText("Failed to connect to server.");
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        BackgroundUtil.setPaneBackground(pane, "/Images/NEWBACKGRND.jpg");
    }
}
