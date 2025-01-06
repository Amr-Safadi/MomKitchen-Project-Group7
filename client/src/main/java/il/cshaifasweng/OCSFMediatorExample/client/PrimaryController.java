package il.cshaifasweng.OCSFMediatorExample.client;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.swing.text.html.ImageView;

public class PrimaryController {

    public javafx.scene.image.ImageView branchHaifaIm;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button branchBtn;

    @FXML
    private ListView<?> branchListView;

    @FXML
    private Label branchesLabel;


    @FXML
    private void handleBranchBtn(MouseEvent actionEvent) {
        // Load the secondary screen FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Secondary.fxml"));
        Parent secondaryRoot = null;
        try {
            secondaryRoot = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get the current stage from the button
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

        // Set the new scene
        Scene scene = new Scene(secondaryRoot);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void initialize() {
        assert branchBtn != null : "fx:id=\"branchBtn\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchListView != null : "fx:id=\"branchListView\" was not injected: check your FXML file 'primary.fxml'.";
        assert branchesLabel != null : "fx:id=\"branchesLabel\" was not injected: check your FXML file 'primary.fxml'.";

    }

}
