package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Orders;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReportsController {

    private final SimpleClient client = SimpleClient.getClient();
    private final ObservableList<Orders> deliveryOrders = FXCollections.observableArrayList();
    private final ObservableList<Object[]> reservationsData = FXCollections.observableArrayList();
    private final ObservableList<Object[]> complaintsData = FXCollections.observableArrayList();

    @FXML private TableView<Orders> ordersTable;
    @FXML private TableColumn<Orders, Integer> orderIdColumn;
    @FXML private TableColumn<Orders, String> orderDateColumn;
    @FXML private TableColumn<Orders, String> orderTotalPriceColumn;

    @FXML private TableView<Object[]> reservationsTable;
    @FXML private TableColumn<Object[], String> reservationDateColumn;
    @FXML private TableColumn<Object[], String> reservationCountColumn;

    @FXML private BarChart<String, Number> complaintsChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Button exportCsvButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        if (UserSession.getUser() == null ||
                !(UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER)) {

            showAlert("Access Denied", "You do not have permission to access this page.");
            Platform.runLater(() -> ScreenManager.switchScreen("Primary"));
            return;
        }
        EventBus.getDefault().register(this);
        setupTables();
        fetchReports();
    }

    private void setupTables() {
        // ✅ Setup Delivery Orders Table
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderPlacedTime().toString()));
        orderTotalPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getTotalPrice())));

        // ✅ Setup Reservations Per Day Table
        reservationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString())); // Date
        reservationCountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString())); // Count
    }

    private void fetchReports() {
        try {
            client.sendToServer(new Message(null, "#FetchReports"));
        } catch (IOException e) {
            showAlert("Error", "Failed to fetch reports.");
        }
    }

    @Subscribe
    public void handleOrdersReport(Message message) {
        if ("#OrdersReport".equals(message.getText())) {
            List<Orders> orders = (List<Orders>) message.getObject();
            Platform.runLater(() -> {
                deliveryOrders.setAll(orders);
                ordersTable.getItems().setAll(orders);
                System.out.println("✅ Delivery orders loaded successfully!");
            });
        }
    }

    @Subscribe
    public void handleReservationsReport(Message message) {
        if ("#ReservationsReport".equals(message.getText())) {
            List<Object[]> reservations = (List<Object[]>) message.getObject();
            Platform.runLater(() -> {
                reservationsData.setAll(reservations);
                reservationsTable.getItems().setAll(reservations);
                System.out.println("✅ Reservations report loaded successfully!");
            });
        }
    }

    @Subscribe
    public void handleComplaintsReport(Message message) {
        if ("#ComplaintsReport".equals(message.getText())) {
            List<Object[]> complaints = (List<Object[]>) message.getObject();
            Platform.runLater(() -> {
                complaintsData.setAll(complaints);
                updateComplaintsChart();
                System.out.println("✅ Complaints histogram updated!");
            });
        }
    }

    private void updateComplaintsChart() {
        complaintsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Object[] entry : complaintsData) {
            String day = entry[0].toString();
            int count = Integer.parseInt(entry[1].toString());
            series.getData().add(new XYChart.Data<>(day, count));
        }
        complaintsChart.getData().add(series);
    }

    @FXML
    private void handleExportCsv() {
        try (FileWriter writer = new FileWriter("report.csv")) {
            // Export Orders
            writer.append("Order ID, Date, Total Price\n");
            for (Orders order : deliveryOrders) {
                writer.append(order.getId() + "," + order.getOrderPlacedTime() + "," + order.getTotalPrice() + "\n");
            }
            writer.append("\n");

            // Export Reservations
            writer.append("Date, Reservations Count\n");
            for (Object[] entry : reservationsData) {
                writer.append(entry[0].toString() + "," + entry[1].toString() + "\n");
            }
            writer.append("\n");

            // Export Complaints
            writer.append("Date, Complaints Count\n");
            for (Object[] entry : complaintsData) {
                writer.append(entry[0].toString() + "," + entry[1].toString() + "\n");
            }

            showAlert("Success", "Report exported successfully.");
        } catch (IOException e) {
            showAlert("Error", "Failed to export CSV.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToMain() {
        Platform.runLater(() -> ScreenManager.switchScreen("Primary"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onClose() {
        EventBus.getDefault().unregister(this);
    }
}
