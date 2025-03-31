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
    @FXML private ComboBox<String> branchFilterComboBox;
    @FXML private Label branchFilterLabel;

    private String selectedBranch = null;


    @FXML
    public void initialize() {
        if (UserSession.getUser() == null ||
                !(UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER)) {

            showAlert("Access Denied", "You do not have permission to access this page.");
            Platform.runLater(() -> ScreenManager.switchScreen("Primary"));
            return;
        }

        if (UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER) {
            branchFilterLabel.setVisible(true);
            branchFilterComboBox.setVisible(true);
            branchFilterComboBox.setItems(FXCollections.observableArrayList("All", "Haifa", "Tel-Aviv", "Acre", "Netanya"));
            branchFilterComboBox.getSelectionModel().selectFirst(); // Default to All
          //  branchFilterComboBox.setOnAction(e -> applyBranchFilter());
            selectedBranch = "All";
        } else {
            branchFilterLabel.setVisible(false);
            branchFilterComboBox.setVisible(false);
            selectedBranch = UserSession.getUser().getBranch(); // BRANCH_MANAGER sees only his branch
        }
        branchFilterComboBox.setOnAction(e -> handleBranchComboBoxChange());
        EventBus.getDefault().register(this);
        setupTables();
        fetchReports();
    }

    @FXML
    private void handleBranchComboBoxChange() {
        if (UserSession.getUser().getRole() != User.Role.GENERAL_MANAGER) return;

        String selected = branchFilterComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // 🧠 Filter Orders
        List<Orders> filteredOrders = selected.equals("All") ? orders :
                orders.stream().filter(o -> o.getBranchName().equalsIgnoreCase(selected)).toList();
        deliveryOrders.setAll(filteredOrders);
        ordersTable.getItems().setAll(filteredOrders);

        // 📅 Filter Reservations
        List<Object[]> filteredReservations = selected.equals("All") ? allReservations :
                allReservations.stream().filter(r -> r[2].toString().equalsIgnoreCase(selected)).toList();
        reservationsData.setAll(filteredReservations);
        reservationsTable.getItems().setAll(filteredReservations);

        // 🚨 Filter Complaints
        List<Object[]> filteredComplaints = selected.equals("All") ? complaints :
                complaints.stream().filter(c -> c[2].toString().equalsIgnoreCase(selected)).toList();
        complaintsData.setAll(filteredComplaints);
        updateComplaintsChart();
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

    List<Orders> orders ;

    @Subscribe
    public void handleOrdersReport(Message message) {
        if ("#OrdersReport".equals(message.getText())) {
            orders = (List<Orders>) message.getObject();
            List<Orders> filtered = filterOrdersByUserBranch(orders);

            Platform.runLater(() -> {
                deliveryOrders.setAll(filtered);
                ordersTable.getItems().setAll(filtered);
                System.out.println("✅ Filtered delivery orders loaded successfully!");
            });
        }
    }

    private List<Orders> filterOrdersByUserBranch(List<Orders> allOrders) {
        User user = UserSession.getUser();
        if (user.getRole() == User.Role.GENERAL_MANAGER) {
            return allOrders; // Show everything
        } else if (user.getRole() == User.Role.BRANCH_MANAGER) {
            String userBranch = user.getBranch();
            return allOrders.stream()
                    .filter(order -> order.getBranchName().equalsIgnoreCase(userBranch))
                    .toList();
        }
        return List.of(); // No access fallback
    }


    List<Object[]> allReservations;
    @Subscribe
    public void handleReservationsReport(Message message) {
        if ("#ReservationsReport".equals(message.getText())) {
            allReservations = (List<Object[]>) message.getObject();
            List<Object[]> filtered = filterReservationsByUserBranch(allReservations);
            Platform.runLater(() -> {
                reservationsData.setAll(filtered);
                reservationsTable.getItems().setAll(filtered);
                System.out.println("✅ Reservations report loaded successfully!");
            });
        }
    }

    private List<Object[]> filterReservationsByUserBranch(List<Object[]> all) {
        User user = UserSession.getUser();

        if (user.getRole() == User.Role.GENERAL_MANAGER) {
            return all; // Show all
        } else if (user.getRole() == User.Role.BRANCH_MANAGER) {
            String userBranch = user.getBranch();
            return all.stream()
                    .filter(entry -> entry[2].toString().equalsIgnoreCase(userBranch)) // Assuming index 2 = branch
                    .toList();
        }

        return List.of(); // No access
    }


    List<Object[]> complaints ;
    @Subscribe
    public void handleComplaintsReport(Message message) {
        if ("#ComplaintsReport".equals(message.getText())) {
             complaints = (List<Object[]>) message.getObject();
            List<Object[]> filtered = filterComplaintsByUserBranch(complaints);
            Platform.runLater(() -> {
                complaintsData.setAll(filtered);
                updateComplaintsChart();
                System.out.println("✅ Complaints histogram updated!");
            });
        }
    }

    private List<Object[]> filterComplaintsByUserBranch(List<Object[]> allComplaints) {
        User user = UserSession.getUser();

        if (user.getRole() == User.Role.GENERAL_MANAGER) {
            return allComplaints; // Show everything
        } else if (user.getRole() == User.Role.BRANCH_MANAGER) {
            String userBranch = user.getBranch();
            return allComplaints.stream()
                    .filter(entry -> entry[2].toString().equalsIgnoreCase(userBranch)) // Assuming index 2 = branch
                    .toList();
        }

        return List.of(); // No access fallback
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
