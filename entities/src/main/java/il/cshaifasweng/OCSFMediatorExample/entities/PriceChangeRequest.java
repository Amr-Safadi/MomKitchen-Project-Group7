package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "PriceChangeRequests")
public class PriceChangeRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Meals meal;

    private double requestedPrice;

    @ManyToOne
    private User requestedBy;

    private boolean approved = false;

    private boolean resolved = false;

    private LocalDateTime requestedAt;

    private LocalDateTime resolvedAt;

    public PriceChangeRequest() {}

    public PriceChangeRequest(Meals meal, double requestedPrice, User requestedBy) {
        this.meal = meal;
        this.requestedPrice = requestedPrice;
        this.requestedBy = requestedBy;
        this.requestedAt = LocalDateTime.now();
        this.approved = false;
        this.resolved = false;
    }

    public int getId() {
        return id;
    }

    public Meals getMeal() {
        return meal;
    }

    public void setMeal(Meals meal) {
        this.meal = meal;
    }

    public double getRequestedPrice() {
        return requestedPrice;
    }

    public void setRequestedPrice(double requestedPrice) {
        this.requestedPrice = requestedPrice;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
