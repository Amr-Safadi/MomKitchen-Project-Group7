package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class TableMapController {

    @FXML
    private GridPane tableGrid;

    private Branch currentBranch; // The branch whose tables are being managed

    @FXML
    public void initialize() {
        // Optional: Check that only worker roles can access this page.
        // For example, if the user role is not a worker, you could redirect:
        // if (UserSession.getUser() == null || UserSession.getUser().getRole() == User.Role.CLIENT) { ... }

        // Fetch the current branch. This could be stored in SecondaryService (or similar)
        // For demonstration, assume SecondaryService holds the current branch object.
        currentBranch = SecondaryService.getBranchObj();

        // Register EventBus if needed for server responses.
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        populateTableGrid();
    }

    private void populateTableGrid() {
        List<RestaurantTable> tables = currentBranch.getTables();
        tableGrid.getChildren().clear();

        int col = 0;
        int row = 0;
        for (RestaurantTable table : tables) {
            VBox tableBox = createTableBox(table);
            tableGrid.add(tableBox, col, row);
            col++;
            if (col > 2) { // Adjust columns per row as needed
                col = 0;
                row++;
            }
        }
    }

    private VBox createTableBox(RestaurantTable table) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #f0f0f0;");

        Label tableLabel = new Label("Table " + table.getTableNumber() + " (" + table.getCapacity() + " ppl)");
        Label statusLabel = new Label(table.isReserved() ? "Reserved" : "Available");
        Button reserveBtn = new Button("Reserve");

        // If already reserved, disable the button
        reserveBtn.setDisable(table.isReserved());

        reserveBtn.setOnAction((ActionEvent e) -> {
            // Call method to reserve table. This could send a message to the server.
            reserveTable(table);
        });

        box.getChildren().addAll(tableLabel, statusLabel, reserveBtn);
        return box;
    }

    private void reserveTable(RestaurantTable table) {
        // For example, send a message to the server to update this table’s status
        try {
            // Create a message object with the table and a specific command, e.g., "#ReserveTable"
            // SimpleClient.getClient().sendToServer(new Message(table, "#ReserveTable"));
            // For demonstration, you could update the table locally:
            table.setReserved(true);
            populateTableGrid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Optionally listen for server confirmations (if using EventBus)
    @Subscribe
    public void onTableReserved(il.cshaifasweng.OCSFMediatorExample.entities.Message message) {
        // Optionally, update the UI upon receiving a confirmation.
        System.out.println("Received table reservation event: " + message);
    }


    @FXML
    void handleBack() {
        ScreenManager.switchScreen("Secondary"); // or the appropriate screen
    }
}
