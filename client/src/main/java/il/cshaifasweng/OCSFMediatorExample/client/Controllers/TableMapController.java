package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class TableMapController {

    @FXML
    private GridPane tableGrid;

    private final Object branchObj = SecondaryService.getBranchObj();

    @FXML
    public void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        populateTableGrid();
    }

    private void populateTableGrid() {
        List<RestaurantTable> tables = SecondaryService.getBranchObj().getTables();
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
        box.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #f0f0f0;");

        Label tableLabel = new Label("Table " + table.getTableNumber() + " (" + table.getCapacity() + " ppl)");
        Label seatingLabel = new Label("Seating: " + table.getSeatingArea());
        Label statusLabel = new Label(table.isReserved() ? "Reserved" : "Available");

        Button toggleBtn = new Button();
        toggleBtn.setText(table.isReserved() ? "Cancel Reservation" : "Reserve");

        toggleBtn.setOnAction(e -> {
            if (!table.isReserved()) {
                reserveTable(table);
            } else {
                cancelTableReservation(table);
            }
        });

        box.getChildren().addAll(tableLabel, seatingLabel, statusLabel, toggleBtn);
        return box;
    }

    private void reserveTable(RestaurantTable table) {
        try {
            table.setReserved(true);
            SimpleClient.getClient().sendToServer(new Message(table, "#ReserveTable"));
            System.out.println("ReserveTable message sent for Table " + table.getTableNumber());
            Platform.runLater(() -> populateTableGrid());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void cancelTableReservation(RestaurantTable table) {
        try {
            table.setReserved(false);
            SimpleClient.getClient().sendToServer(new Message(table, "#CancelTableReservation"));
            System.out.println("CancelTableReservation message sent for Table " + table.getTableNumber());
            Platform.runLater(() -> populateTableGrid());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Subscribe
    public void onTableReservationUpdate(Message message) {
        String msg = message.toString();
        System.out.println("Received broadcast message: " + msg);
        if (msg.equals("#TableReservedSuccess") || msg.equals("#TableReservationCanceledSuccess")) {
            RestaurantTable updatedTable = (RestaurantTable) message.getObject();
            for (RestaurantTable t : SecondaryService.getBranchObj().getTables()) {
                if (t.getId() == updatedTable.getId()) {
                    t.setReserved(updatedTable.isReserved());
                    System.out.println("Updated table " + t.getTableNumber() + " reserved status to " + t.isReserved());
                    break;
                }
            }
            Platform.runLater(this::populateTableGrid);
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        ScreenManager.switchScreen("Menu List");
    }
}
