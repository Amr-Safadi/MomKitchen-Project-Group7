package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "contact_requests")
public class ContactRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String branch;
    private String complaint;
    private boolean handled;
    private String resolutionScript;
    private boolean refundIssued;
    private LocalDateTime submittedAt;
    private LocalDateTime handledAt;

    // Ensure submittedAt is always set when a complaint is created
    public ContactRequest() {
        this.submittedAt = LocalDateTime.now();  // Ensure default constructor sets it
        this.handled = false;
    }

    public ContactRequest(String name, String branch, String complaint) {
        this.name = name;
        this.branch = branch;
        this.complaint = complaint;
        this.handled = false;
        this.submittedAt = LocalDateTime.now(); // Ensure timestamp is set
    }

    public ContactRequest(String name, String branch, String complaint, boolean handled) {
        this.name = name;
        this.branch = branch;
        this.complaint = complaint;
        this.handled = handled;
        this.submittedAt = LocalDateTime.now(); // Ensure timestamp is set
    }


    // ✅ Constructor for resolving a complaint (handling date, resolution script, refund)
    public ContactRequest(int id, boolean handled, String resolutionScript, boolean refundIssued) {
        this.id = id;
        this.handled = handled;
        this.resolutionScript = resolutionScript;
        this.refundIssued = refundIssued;
        this.handledAt = LocalDateTime.now();
    }

    // ✅ Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBranch() { return branch; }
    public String getComplaint() { return complaint; }
    public boolean isHandled() { return handled; }
    public String getResolutionScript() { return resolutionScript; }
    public boolean isRefundIssued() { return refundIssued; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getHandledAt() { return handledAt; }

    // ✅ Setters
    public void setHandled(boolean handled) {
        this.handled = handled;
        if (handled) {
            this.handledAt = LocalDateTime.now();
        }
    }

    public void setResolutionScript(String resolutionScript) { this.resolutionScript = resolutionScript; }
    public void setRefundIssued(boolean refundIssued) { this.refundIssued = refundIssued; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
}
