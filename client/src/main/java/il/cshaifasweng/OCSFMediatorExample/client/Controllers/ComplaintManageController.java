package il.cshaifasweng.OCSFMediatorExample.client.Controllers;

import il.cshaifasweng.OCSFMediatorExample.client.Main.ScreenManager;
import il.cshaifasweng.OCSFMediatorExample.client.Network.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.Sessions.UserSession;
import il.cshaifasweng.OCSFMediatorExample.entities.ContactRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ComplaintManageController {

    private final SimpleClient client = SimpleClient.getClient();
    private final ObservableList<ContactRequest> unresolvedComplaints = FXCollections.observableArrayList();
    private final ObservableList<ContactRequest> resolvedComplaints = FXCollections.observableArrayList();

    @FXML
    private TableView<ContactRequest> unresolvedTable;

    @FXML
    private TableColumn<ContactRequest, Integer> idColumn;
    @FXML
    private TableColumn<ContactRequest, String> nameColumn;
    @FXML
    private TableColumn<ContactRequest, String> branchColumn;
    @FXML
    private TableColumn<ContactRequest, String> complaintColumn;
    @FXML
    private TableColumn<ContactRequest, String> submittedColumn;

    @FXML
    private TableView<ContactRequest> resolvedTable;

    @FXML
    private TableColumn<ContactRequest, Integer> resolvedIdColumn;
    @FXML
    private TableColumn<ContactRequest, String> resolvedNameColumn;
    @FXML
    private TableColumn<ContactRequest, String> resolvedBranchColumn;
    @FXML
    private TableColumn<ContactRequest, String> resolutionColumn;
    @FXML
    private TableColumn<ContactRequest, String> refundColumn;
    @FXML
    private TableColumn<ContactRequest, String> handledColumn;

    @FXML
    private TextArea resolutionField;

    @FXML
    private CheckBox refundCheckbox;

    @FXML
    private Button resolveButton;
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button viewResolvedButton;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        if (UserSession.getUser() == null ||
                !(UserSession.getUser().getRole() == User.Role.BRANCH_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.GENERAL_MANAGER ||
                        UserSession.getUser().getRole() == User.Role.SERVICE_EMPLOYEE)) {

            showAlert("Access Denied", "You do not have permission to access this page.");
            ScreenManager.switchScreen("Primary");  // Redirect unauthorized users
            return;
        }
        EventBus.getDefault().register(this);
        setupTables();
        fetchComplaints();
        rootPane.setStyle("-fx-background-image: url('/Images/NEWBACKGRND.jpg'); -fx-background-size: cover;");

    }

    private void setupTables() {
        // ✅ Unresolved complaints table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        branchColumn.setCellValueFactory(new PropertyValueFactory<>("branch"));
        complaintColumn.setCellValueFactory(new PropertyValueFactory<>("complaint"));
        submittedColumn.setCellFactory(column -> new TableCell<ContactRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle(""); // Reset style
                } else {
                    setText(item);
                    ContactRequest complaint = getTableView().getItems().get(getIndex());
                    if (complaint != null && complaint.getSubmittedAt() != null) {
                        long hoursElapsed = java.time.Duration.between(complaint.getSubmittedAt(), LocalDateTime.now()).toHours();

                        if (hoursElapsed >= 20) {
                            setStyle("-fx-background-color: red; -fx-text-fill: white;"); // 🔥 Set RED background
                        } else {
                            setStyle(""); // Reset style for non-expired complaints
                        }
                    }
                }
            }
        });
        submittedColumn.setCellValueFactory(cellData -> {
            LocalDateTime submittedAt = cellData.getValue().getSubmittedAt();
            return new SimpleStringProperty((submittedAt != null) ? submittedAt.toString() : "N/A");
        });




        // ✅ Resolved complaints table
        resolvedIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        resolvedNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        resolvedBranchColumn.setCellValueFactory(new PropertyValueFactory<>("branch"));
        resolutionColumn.setCellValueFactory(new PropertyValueFactory<>("resolutionScript"));
        handledColumn.setCellValueFactory(cellData -> {
            LocalDateTime handledAt = cellData.getValue().getHandledAt();
            return new SimpleStringProperty((handledAt != null) ? handledAt.toString() : "N/A");
        });

        // ✅ **Refund Status Column**
        refundColumn.setCellValueFactory(cellData -> {
            boolean refund = cellData.getValue().isRefundIssued();
            return new SimpleStringProperty(refund ? "Yes" : "No");
        });

        // ✅ Load complaints immediately
        fetchComplaints();
    }



    private void fetchComplaints() {
        try {
            client.sendToServer(new Message(null, "#FetchComplaints"));
        } catch (IOException e) {
            showAlert("Error", "Failed to fetch complaints.");
            e.printStackTrace();
        }
    }



    // Receive Complaints from Server
    @Subscribe
    public void onComplaintsReceived(Message message) {
        if (message.toString().equals("#ComplaintList")) {
            List<ContactRequest> complaints = (List<ContactRequest>) message.getObject();
            Platform.runLater(() -> {
                unresolvedComplaints.setAll(
                        complaints.stream().filter(c -> !c.isHandled()).toList());
                resolvedComplaints.setAll(
                        complaints.stream().filter(ContactRequest::isHandled).toList());
            });
        }
    }
    @Subscribe
    public void handleComplaintList(Message message) {
        if (message.toString().equals("#ComplaintList")) {
            List<ContactRequest> complaints = (List<ContactRequest>) message.getObject();
            Platform.runLater(() -> {
                unresolvedComplaints.setAll(complaints.stream().filter(c -> !c.isHandled()).toList());
                resolvedComplaints.setAll(complaints.stream().filter(ContactRequest::isHandled).toList());

                unresolvedTable.setItems(unresolvedComplaints);
                resolvedTable.setItems(resolvedComplaints);
            });
        }
    }



    @FXML
    private void handleResolveComplaint() {
        ContactRequest selectedComplaint = unresolvedTable.getSelectionModel().getSelectedItem();

        if (selectedComplaint == null) {
            showAlert("Error", "Please select a complaint to resolve.");
            return;
        }

        String resolutionText = resolutionField.getText().trim();
        boolean refundIssued = refundCheckbox.isSelected();

        if (resolutionText.isEmpty()) {
            showAlert("Error", "Resolution text cannot be empty.");
            return;
        }

        // Update the complaint object
        selectedComplaint.setHandled(true);
        selectedComplaint.setResolutionScript(resolutionText);
        selectedComplaint.setRefundIssued(refundIssued);
        selectedComplaint.setHandledAt(LocalDateTime.now());

        // Send updated complaint to the server
        try {
            client.sendToServer(new Message(selectedComplaint, "#ResolveComplaint"));
        } catch (IOException e) {
            showAlert("Error", "Failed to resolve the complaint.");
            e.printStackTrace();
            return;
        }

        // ✅ **Update UI Immediately**
        Platform.runLater(() -> {
            unresolvedComplaints.remove(selectedComplaint);
            resolvedComplaints.add(selectedComplaint);
            resolutionField.clear();
            refundCheckbox.setSelected(false);
            showAlert("Success", "Complaint resolved successfully!");
        });
    }
    @FXML
    private void handleExportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Complaints as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("complaints.csv");

        // Show save dialog
        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            exportComplaintsToCSV(file);
        }
    }
    private void exportComplaintsToCSV(java.io.File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.append("ID,Customer Name,Branch,Complaint,Time Submitted,Resolved,Resolution,Time Handled,Refund Issued\n");

            // Write unresolved complaints
            for (ContactRequest complaint : unresolvedTable.getItems()) {
                writer.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        complaint.getId(),
                        complaint.getName(),
                        complaint.getBranch(),
                        complaint.getComplaint(),
                        (complaint.getSubmittedAt() != null) ? complaint.getSubmittedAt().toString() : "N/A",
                        complaint.isHandled() ? "Yes" : "No",
                        complaint.getResolutionScript() != null ? complaint.getResolutionScript() : "N/A",
                        (complaint.getHandledAt() != null) ? complaint.getHandledAt().toString() : "N/A",
                        complaint.isRefundIssued() ? "Yes" : "No"
                ));
            }

            // Write resolved complaints
            for (ContactRequest complaint : resolvedTable.getItems()) {
                writer.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        complaint.getId(),
                        complaint.getName(),
                        complaint.getBranch(),
                        complaint.getComplaint(),
                        (complaint.getSubmittedAt() != null) ? complaint.getSubmittedAt().toString() : "N/A",
                        complaint.isHandled() ? "Yes" : "No",
                        complaint.getResolutionScript() != null ? complaint.getResolutionScript() : "N/A",
                        (complaint.getHandledAt() != null) ? complaint.getHandledAt().toString() : "N/A",
                        complaint.isRefundIssued() ? "Yes" : "No"
                ));
            }

            showAlert("Success", "Complaints exported successfully!");
        } catch (IOException e) {
            showAlert("Error", "Failed to export complaints.");
            e.printStackTrace();
        }
    }



    // Receive Complaint Resolution Confirmation
    @Subscribe
    public void onComplaintResolved(Message message) {
        if (message.toString().equals("#ComplaintResolved")) {
            ContactRequest resolvedComplaint = (ContactRequest) message.getObject();
            Platform.runLater(() -> {
                unresolvedComplaints.remove(resolvedComplaint);
                resolvedComplaints.add(resolvedComplaint);
                resolutionField.clear();
                refundCheckbox.setSelected(false);
                showAlert("Success", "Complaint resolved successfully!");
            });
        }
    }
    @Subscribe
    public void onResolvedComplaintList(Message message) {
        if (message.toString().equals("#ResolvedComplaintList")) {
            List<ContactRequest> resolvedList = (List<ContactRequest>) message.getObject();

            Platform.runLater(() -> {
                resolvedComplaints.setAll(resolvedList);  // ✅ Ensure resolved complaints load immediately
            });
        }
    }



    // Show Resolved Complaints
    @FXML
    private void showResolvedComplaints() {
        resolvedTable.setVisible(!resolvedTable.isVisible());
    }

    // Navigate Back to Main Screen
    @FXML
    private void handleBackToMain() {
        ScreenManager.switchScreen("Primary");
    }

    // Show Alert Helper Method
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Unregister EventBus on Close
    public void onClose() {
        EventBus.getDefault().unregister(this);
    }
}
