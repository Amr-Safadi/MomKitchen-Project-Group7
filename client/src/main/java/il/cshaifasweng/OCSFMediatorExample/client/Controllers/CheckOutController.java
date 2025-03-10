package il.cshaifasweng.OCSFMediatorExample.client.Controllers;
import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CheckOutController {

    @FXML
    private TextField addressTxt;

    @FXML
    private Button backBtn;

    @FXML
    private TextField cardTxt;

    @FXML
    private Button checkorderBtn;

    @FXML
    private TextField idTxt;

    @FXML
    private TextField mailTxt;

    @FXML
    private TextField nameTxt;

    @FXML
    private TextField phoneTxt;

    @FXML
    private Label priceLabel;

    @FXML
    void backHandler() {
        ScreenManager.switchScreen("Cart");
    }
}
