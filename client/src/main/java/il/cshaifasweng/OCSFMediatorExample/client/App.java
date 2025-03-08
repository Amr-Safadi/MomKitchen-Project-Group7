package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private SimpleClient client;
    private static Stage appStage;



    @Override
    public void start(Stage stage) throws IOException {
        appStage = stage;
    	// EventBus.getDefault().register(this);
    	client = SimpleClient.getClient();
    	client.openConnection();

        System.out.println("client connected");

        scene = new Scene(loadFXML("primary"), 600, 600);
        setWindowTitle("MomKitchen");

        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    public static void setWindowTitle(String title) {
        appStage.setTitle(title);
    }
    public static void setContent(String pageName) throws IOException {
        Parent root = loadFXML(pageName);
        scene = new Scene(root);
        appStage.setScene(scene);
        appStage.show();
    }
    public static void switchScreen (String screenName) {
        switch (screenName) {
            case "Cart":
                Platform.runLater(() -> {
                    setWindowTitle("Cart");
                    try {
                        setContent("Cart");
                    } catch (IOException e) {
                        System.out.println("Error loading screen - app - switchScreen function - Cart");
                        e.printStackTrace();
                    }
                });
                break;

            case "Menu List":
                Platform.runLater(() -> {
                    setWindowTitle("Menu List");
                    try {
                        setContent("secondary");
                    } catch (IOException e) {
                        System.out.println("Error loading screen - app - switchScreen function - Menu List");
                        e.printStackTrace();
                    }
                });
                break;

            case "Meal View":
                Platform.runLater(() -> {
                    setWindowTitle("Meal View");
                    try {
                        setContent("MealView");
                    } catch (IOException e) {
                        System.out.println("Error loading screen - app - switchScreen function - Meal View");
                        e.printStackTrace();
                    }
                });
                break;

            case "Primary":
                Platform.runLater(() -> {
                    setWindowTitle("MomKitchen");
                    try {
                        setContent("primary");
                    } catch (IOException e) {
                        System.out.println("Error loading screen - app - switchScreen function - Primary");
                        e.printStackTrace();
                    }
                });
                break;
            case "Login":
                Platform.runLater(() -> {
                    setWindowTitle("Login");
                    try {
                        setContent("Login");
                    } catch (IOException e) {
                        System.out.println("Error loading screen - app - switchScreen function - Login");
                        e.printStackTrace();
                    }
                });
                break;
        }
    }


    @Override
    public void stop() throws Exception {
        if (UserSession.getUser() != null) {
            System.out.println("User " + UserSession.getUser().getEmail() + " disconnected.");
            UserSession.logout();
        }
        if (client != null && client.isConnected()) {
            try {
                if (EventBus.getDefault().isRegistered(this)) {
                    EventBus.getDefault().unregister(this);
                }
                client.closeConnection(); // Properly close connection
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.stop(); // Ensure JavaFX exits correctly
    }


    /*@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
    	// EventBus.getDefault().unregister(this);
		super.stop();
	}*/


	public static void main(String[] args) {
        launch();
    }

}

