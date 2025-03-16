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
    private TableColumn<ContactRequest, String> emailColumn;

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
    private TableColumn<ContactRequest, String> resolvedEmailColumn;
    @FXML
    private TableColumn<ContactRequest, String> refundAmountColumn;
    @FXML private TableColumn<ContactRequest, String> resolvedComplaintColumn;
    @FXML private TableColumn<ContactRequest, String> resolvedSubmittedColumn;


    @FXML
    private TextArea resolutionField;


    @FXML
    private CheckBox refundCheckbox;
    @FXML
    private TextField refundAmountField;

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
            Platform.runLater(() ->  ScreenManager.switchScreen("Primary"));  // Redirect unauthorized users
            return;
        }
        EventBus.getDefault().register(this);
        setupTables();
        fetchComplaints();
        rootPane.setStyle("-fx-background-image: url('/Images/NEWBACKGRND.jpg'); -fx-background-size: cover;");

        unresolvedTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showComplaintDetails();
            }
        });

        resolvedTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showResolvedDetails();
            }
        });
    }


    private void setupTables() {
        // ✅ Unresolved complaints table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        branchColumn.setCellValueFactory(new PropertyValueFactory<>("branch"));
       // complaintColumn.setCellValueFactory(new PropertyValueFactory<>("complaint"));
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
        resolvedEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        resolvedBranchColumn.setCellValueFactory(new PropertyValueFactory<>("branch"));
        resolutionColumn.setCellValueFactory(new PropertyValueFactory<>("resolutionScript"));
        handledColumn.setCellValueFactory(cellData -> {
            LocalDateTime handledAt = cellData.getValue().getHandledAt();
            return new SimpleStringProperty((handledAt != null) ? handledAt.toString() : "N/A");
        });
        //resolvedComplaintColumn.setCellValueFactory(new PropertyValueFactory<>("complaint")); // ✅ Updated
        resolvedSubmittedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubmittedAt() != null ?
                        cellData.getValue().getSubmittedAt().toString() : "N/A")); // ✅ Updated

        // ✅ **Refund Status Column**
        refundColumn.setCellValueFactory(cellData -> {
            boolean refund = cellData.getValue().isRefundIssued();
            return new SimpleStringProperty(refund ? "Yes" : "No");
        });
        refundAmountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getRefundAmount())));

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
    @FXML
    private void showComplaintDetails() {
        ContactRequest selectedComplaint = unresolvedTable.getSelectionModel().getSelectedItem();

        if (selectedComplaint == null) {
            showAlert("Error", "Please select a complaint to view.");
            return;
        }

        String message = String.format(
                "Customer: %s\nBranch: %s\nEmail: %s\nComplaint:\n%s\n\nSubmitted At: %s",
                selectedComplaint.getName(),
                selectedComplaint.getBranch(),
                selectedComplaint.getEmail(),
                selectedComplaint.getComplaint(),
                selectedComplaint.getSubmittedAt() != null ? selectedComplaint.getSubmittedAt().toString() : "N/A"

        );

        showAlert("Complaint Details", message);
    }
    @FXML
    private void showResolvedDetails() {
        ContactRequest selectedComplaint = resolvedTable.getSelectionModel().getSelectedItem();

        if (selectedComplaint == null) {
            showAlert("Error", "Please select a complaint to view.");
            return;
        }

        String message = String.format(
                "Customer: %s\nBranch: %s\nEmail: %s\nComplaint:\n%s\n\nSubmitted At: %s \n\n Resolution Script: %s \n Refund Amount: %s",
                selectedComplaint.getName(),
                selectedComplaint.getBranch(),
                selectedComplaint.getEmail(),
                selectedComplaint.getComplaint(),
                selectedComplaint.getSubmittedAt() != null ? selectedComplaint.getSubmittedAt().toString() : "N/A" ,
                selectedComplaint.getResolutionScript() ,
           /**/     selectedComplaint.getRefundAmount()
        );

        showAlert("Complaint Details", message);
    }







    // Receive Complaints from Server
    @Subscribe
    public void onComplaintsReceived(Message message) {
        if (message.getText().equals("#ComplaintList")) {
            List<ContactRequest> complaints = (List<ContactRequest>) message.getObject();
            Platform.runLater(() -> {
                unresolvedComplaints.setAll(complaints.stream().filter(c -> !c.isHandled()).toList());
                resolvedComplaints.setAll(complaints.stream().filter(ContactRequest::isHandled).toList());
            });
        }
    }

    @Subscribe
    public void handleComplaintList(Message message) {
        if ("#ComplaintList".equals(message.getText())) {
            List<ContactRequest> complaints = (List<ContactRequest>) message.getObject();
            Platform.runLater(() -> {
                unresolvedTable.getItems().setAll(complaints);
                System.out.println("✅ Unresolved complaints loaded successfully!");
            });
        }
    }

    @Subscribe
    public void handleResolvedComplaintList(Message message) {
        if ("#ResolvedComplaintList".equals(message.getText())) {
            List<ContactRequest> resolvedComplaintsList = (List<ContactRequest>) message.getObject();
            Platform.runLater(() -> {
                resolvedTable.getItems().setAll(resolvedComplaintsList);
                System.out.println("✅ Resolved complaints loaded successfully!");
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

        if (refundIssued) {
            try {
                selectedComplaint.setRefundAmount(Double.parseDouble(refundAmountField.getText().trim()));
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid refund amount.");
                return;
            }
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

        // Remove from unresolved and add to resolved
        unresolvedComplaints.remove(selectedComplaint);
        resolvedComplaints.add(selectedComplaint);

        // Clear input fields
        resolutionField.clear();
        refundCheckbox.setSelected(false);
        refundAmountField.clear(); // ✅ Clear refund input field
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
        Platform.runLater(() ->ScreenManager.switchScreen("Primary"));
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
    @FXML


    private void handleResolvedComplaintDoubleClick() {
        ContactRequest selectedComplaint = resolvedTable.getSelectionModel().getSelectedItem();
        if (selectedComplaint == null) {
            showAlert("Error", "Please select a complaint to view.");
            return;
        }
        showComplaintPopup("Resolved Complaint Details", selectedComplaint);
    }

    private void showComplaintPopup(String title, ContactRequest complaint) {
        String message = String.format(
                "Customer: %s\nBranch: %s\nEmail: %s\nComplaint:\n%s\nSubmitted At: %s\nResolution: %s\nRefund: %s ($%.2f)",
                complaint.getName(), complaint.getBranch(), complaint.getEmail(),
                complaint.getComplaint(), complaint.getSubmittedAt(),
                complaint.getResolutionScript(), complaint.isRefundIssued() ? "Yes" : "No",
                complaint.getRefundAmount());
        showAlert(title, message);
    }
    @FXML
    private void handleComplaintDoubleClick() {
        ContactRequest selectedComplaint = unresolvedTable.getSelectionModel().getSelectedItem();
        if (selectedComplaint == null) {
            showAlert("Error", "Please select a complaint to view.");
            return;
        }
        showComplaintPopup("Complaint Details", selectedComplaint);
    }

    @FXML
    private void handleViewComplaintDetails() {
        ContactRequest selectedComplaint = unresolvedTable.getSelectionModel().getSelectedItem();

        if (selectedComplaint == null) {
            showAlert("Error", "Please select a complaint to view details.");
            return;
        }

        String message = String.format(
                "Customer: %s\nBranch: %s\nEmail: %s\nComplaint:\n%s\n\nSubmitted At: %s",
                selectedComplaint.getName(),
                selectedComplaint.getBranch(),
                selectedComplaint.getEmail(),
                selectedComplaint.getComplaint(),
                selectedComplaint.getSubmittedAt() != null ? selectedComplaint.getSubmittedAt().toString() : "N/A"
        );

        showAlert("Complaint Details", message);
    }

}