package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
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

    private Branch currentBranch;

    @FXML
    public void initialize() {
        currentBranch = SecondaryService.getBranchObj();

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
            if (col > 2) {
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

        reserveBtn.setDisable(table.isReserved());

        reserveBtn.setOnAction((ActionEvent e) -> {
            reserveTable(table);
        });

        box.getChildren().addAll(tableLabel, statusLabel, reserveBtn);
        return box;
    }

    private void reserveTable(RestaurantTable table) {
        try {
            table.setReserved(true);
            populateTableGrid();
            SimpleClient.getClient().sendToServer(new Message(table, "#ReserveTable"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onTableReserved(il.cshaifasweng.OCSFMediatorExample.entities.Message message) {
        if (message.toString().equals("#TableReservedSuccess")) {
            RestaurantTable updatedTable = (RestaurantTable) message.getObject();
            for (RestaurantTable t : currentBranch.getTables()) {
                if (t.getId() == updatedTable.getId()) {
                    t.setReserved(updatedTable.isReserved());
                    break;
                }
            }
            populateTableGrid();
        }
    }



    @FXML
    void handleBack() {
        ScreenManager.switchScreen("Secondary");
    }
}
