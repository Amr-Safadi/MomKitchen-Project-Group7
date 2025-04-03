package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.client.util.BackgroundUtil;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
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
import javafx.scene.layout.AnchorPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.util.TreeMap;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsController {
    private final SimpleClient client = SimpleClient.getClient();
    private final ObservableList<Orders> deliveryOrders = FXCollections.observableArrayList();
    private final ObservableList<Object[]> reservationsData = FXCollections.observableArrayList();
    private final ObservableList<Object[]> complaintsData = FXCollections.observableArrayList();

    @FXML private TableView<Orders> ordersTable;
    @FXML private TableColumn<Orders, Integer> orderIdColumn;
    @FXML private TableColumn<Orders, String> orderDateColumn;
    @FXML private TableColumn<Orders, String> orderTotalPriceColumn;
    @FXML private AnchorPane rootPane;
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
    @FXML private ScrollPane scrollPane;
    private String selectedBranch = null;
    private List<Orders> orders;
    private List<Reservation> allReservations;
    private List<ContactRequest> complaints;

    @FXML
    public void initialize() {
        BackgroundUtil.setPaneBackground(rootPane, "/Images/NEWBACKGRND.jpg");

        if (UserSession.getUser() == null ||
                !(UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER)) {
            showAlert("Access Denied", "You do not have permission to access this page.");
            EventBus.getDefault().unregister(this);
            Platform.runLater(() -> ScreenManager.switchScreen("Primary"));
            return;
        }

        if (UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER) {
            branchFilterLabel.setVisible(true);
            branchFilterComboBox.setVisible(true);
            branchFilterComboBox.setItems(FXCollections.observableArrayList("All", "Haifa", "Tel-Aviv", "Acre", "Netanya"));
            branchFilterComboBox.getSelectionModel().selectFirst();
            selectedBranch = "All";
        } else {
            branchFilterLabel.setVisible(false);
            branchFilterComboBox.setVisible(false);
            selectedBranch = UserSession.getUser().getBranch();
        }
        branchFilterComboBox.setOnAction(e -> handleBranchComboBoxChange());
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);


        complaintsChart.setAnimated(false);

        ordersTable.setRowFactory(tv -> {
            TableRow<Orders> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Orders selectedOrder = row.getItem();
                    showOrderDetails(selectedOrder);
                }
            });
            return row;
        });

        reservationsTable.setRowFactory(tv -> {
            TableRow<Object[]> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Object[] entry = row.getItem();
                    showReservationDetails(entry);
                }
            });
            return row;
        });

        setupTables();
        fetchReports();
    }

    private void showReservationDetails(Object[] entry) {
        // entry[0] = date, entry[1] = count
        String date = entry[0].toString();
        String count = entry[1].toString();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservation Details");
        alert.setHeaderText("Reservations on " + date);
        alert.setContentText("Total reservations: " + count);
        alert.showAndWait();
    }

    private void showOrderDetails(Orders order) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Details for Order #" + order.getId());

        String content = String.format("""
        Date: %s
        Total Price: %.2f
        Branch: %s
        Type: %s
        Email: %s
        Phone: %s
        """,
                order.getOrderPlacedTime(),
                order.getTotalPrice(),
                order.getBranchName(),
                order.getOrderType(),
                order.getEmail(),
                order.getPhoneNumber()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupTables() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderPlacedTime().toString()));
        orderTotalPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getTotalPrice())));

        reservationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
        reservationCountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));
    }

    private void fetchReports() {
        try {
            client.sendToServer(new Message(null, "#FetchReports"));
        } catch (IOException e) {
            showAlert("Error", "Failed to fetch reports.");
        }
    }

    @FXML
    private void handleBranchComboBoxChange() {
        if (UserSession.getUser().getRole() != User.Role.GENERAL_MANAGER) return;
        selectedBranch = branchFilterComboBox.getSelectionModel().getSelectedItem();

        // Filter and update all UI sections
        List<Orders> filteredOrders = filterOrdersByUserBranch(orders);
        deliveryOrders.setAll(filteredOrders);
        ordersTable.setItems(deliveryOrders);

        List<Reservation> filteredReservations = filterReservationsByBranch(allReservations);
        List<Object[]> groupedReservations = groupReservationsByDate(filteredReservations);
        reservationsData.setAll(groupedReservations);
        reservationsTable.setItems(reservationsData);

        List<ContactRequest> filteredComplaints = filterComplaintsByBranch(complaints);
        List<Object[]> groupedComplaints = groupComplaintsByDate(filteredComplaints);
        complaintsData.setAll(groupedComplaints);
        updateComplaintsChart();
    }

    private List<Orders> filterOrdersByUserBranch(List<Orders> allOrders) {
        if (UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER) {
            String branch = UserSession.getUser().getBranch();
            return allOrders.stream().filter(o -> o.getBranchName().equalsIgnoreCase(branch)).toList();
        } else if (!"All".equalsIgnoreCase(selectedBranch)) {
            return allOrders.stream().filter(o -> o.getBranchName().equalsIgnoreCase(selectedBranch)).toList();
        }
        return allOrders;
    }

    @Subscribe
    public void handleOrdersReport(Message message) {
        if ("#OrdersReport".equals(message.getText())) {
            orders = (List<Orders>) message.getObject();

            if (UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER) {
                Platform.runLater(() -> handleBranchComboBoxChange());
            } else if (UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER) {
                List<Orders> filteredOrders = filterOrdersByUserBranch(orders);
                deliveryOrders.setAll(filteredOrders);
                ordersTable.setItems(deliveryOrders);
            }
        }
    }

    @Subscribe
    public void handleReservationsReport(Message message) {
        if ("#ReservationsReport".equals(message.getText())) {
            allReservations = (List<Reservation>) message.getObject();

            if (UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER) {
                Platform.runLater(() -> handleBranchComboBoxChange());
            } else if (UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER) {
                List<Reservation> filteredReservations = filterReservationsByBranch(allReservations);
                List<Object[]> groupedReservations = groupReservationsByDate(filteredReservations);
                reservationsData.setAll(groupedReservations);
                reservationsTable.setItems(reservationsData);
            }
        }
    }

    @Subscribe
    public void handleComplaintsReport(Message message) {
        if ("#ComplaintsReport".equals(message.getText())) {
            complaints = (List<ContactRequest>) message.getObject();

            if (UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER) {
                Platform.runLater(() -> handleBranchComboBoxChange());
            } else if (UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER) {
                List<ContactRequest> filteredComplaints = filterComplaintsByBranch(complaints);
                List<Object[]> groupedComplaints = groupComplaintsByDate(filteredComplaints);
                complaintsData.setAll(groupedComplaints);
                Platform.runLater(() -> updateComplaintsChart());
            }
        }
    }

    private List<Reservation> filterReservationsByBranch(List<Reservation> all) {
        if (UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER) {
            String branch = UserSession.getUser().getBranch();
            return all.stream().filter(r -> r.getBranch().getName().equalsIgnoreCase(branch)).toList();
        } else if (!"All".equalsIgnoreCase(selectedBranch)) {
            return all.stream().filter(r -> r.getBranch().getName().equalsIgnoreCase(selectedBranch)).toList();
        }
        return all;
    }

    private List<ContactRequest> filterComplaintsByBranch(List<ContactRequest> all) {
        if (UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER) {
            String branch = UserSession.getUser().getBranch();
            return all.stream().filter(c -> c.getBranch().equalsIgnoreCase(branch)).toList();
        } else if (!"All".equalsIgnoreCase(selectedBranch)) {
            return all.stream().filter(c -> c.getBranch().equalsIgnoreCase(selectedBranch)).toList();
        }
        return all;
    }

    private List<Object[]> groupReservationsByDate(List<Reservation> list) {
        return list.stream()
                .collect(Collectors.groupingBy(r -> r.getDate(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new Object[]{e.getKey().toString(), e.getValue().toString()})
                .toList();
    }

    private List<Object[]> groupComplaintsByDate(List<ContactRequest> list) {
        return list.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getSubmittedAt().toLocalDate(),
                        TreeMap::new, // Using TreeMap to keep dates in order
                        Collectors.counting()))
                .entrySet().stream()
                .map(e -> new Object[]{e.getKey().toString(), e.getValue().toString()})
                .collect(Collectors.toList());
    }

    private void updateComplaintsChart() {
        complaintsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Object[] entry : complaintsData) {
            series.getData().add(new XYChart.Data<>(entry[0].toString(), Integer.parseInt(entry[1].toString())));
        }
        complaintsChart.getData().add(series);
    }

    @FXML
    private void handleBackToMain() {
        EventBus.getDefault().unregister(this);
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
