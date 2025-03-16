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
    private String email;
    private String branch;
    private String complaint;
    private boolean handled;
    private String resolutionScript;
    private boolean refundIssued;
    private LocalDateTime submittedAt;
    private LocalDateTime handledAt;
    private double refundAmount = 0.0;



    public ContactRequest() {
        this.submittedAt = LocalDateTime.now();
        this.handled = false;
    }


    public ContactRequest(String name, String email, String branch, String complaint) {
        this.name = name;
        this.email = email;
        this.branch = branch;
        this.complaint = complaint;
        this.handled = false;
        this.submittedAt = LocalDateTime.now();
    }


    public ContactRequest(int id, boolean handled, String resolutionScript, boolean refundIssued) {
        this.id = id;
        this.handled = handled;
        this.resolutionScript = resolutionScript;
        this.refundIssued = refundIssued;
        this.handledAt = LocalDateTime.now();
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getBranch() { return branch; }
    public String getComplaint() { return complaint; }
    public boolean isHandled() { return handled; }
    public String getResolutionScript() { return resolutionScript; }
    public boolean isRefundIssued() { return refundIssued; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public double getRefundAmount() {return refundAmount;}

    public void setHandled(boolean handled) { this.handled = handled; }
    public void setResolutionScript(String resolutionScript) { this.resolutionScript = resolutionScript; }
    public void setRefundIssued(boolean refundIssued) { this.refundIssued = refundIssued; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    public void setRefundAmount(double refundAmount) {this.refundAmount = refundAmount;}
}
