package il.cshaifasweng.OCSFMediatorExample.client.Main;

import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
      /*  client = SimpleClient.getClient();
        client.openConnection();*/
        System.out.println("client connected");
        ScreenManager.init(stage);
    }

    @Override
    public void stop() throws Exception {
        if (UserSession.getUser() != null) {
            System.out.println("User " + UserSession.getUser().getEmail() + " disconnected.");
            UserSession.logout();
        }
        if (SimpleClient.getClient() != null && SimpleClient.getClient().isConnected()) {
            try {
               SimpleClient.getClient().closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}
