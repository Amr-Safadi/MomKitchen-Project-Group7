package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "orders")
public class Orders implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToMany
    private List<Meals> meals;

    private String branchName;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String creditCard;
    private double totalPrice;
    private String status; // PENDING, COMPLETED, CANCELED
    private LocalDateTime orderPlacedTime;
    private LocalDateTime deliveryTime;

    // New Fields
    private String orderType;  // "Delivery" or "Pickup"
    private String paymentMethod; // "Cash" or "Card"

    public Orders() {}

    public Orders(List<Meals> meals, String name, String address, String phoneNumber,
                  String email, String creditCard, double totalPrice,
                  LocalDateTime deliveryTime, String orderType, String paymentMethod , String branchName) {
        this.meals = meals;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.creditCard = creditCard;
        this.totalPrice = totalPrice;
        this.status = "PENDING";
        this.orderPlacedTime = LocalDateTime.now();
        this.deliveryTime = deliveryTime;
        this.orderType = orderType;
        this.paymentMethod = paymentMethod;
        this.branchName = branchName;
    }

    public int getId() { return id; }
    public List<Meals> getMeals() { return meals; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getCreditCard() { return creditCard; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public LocalDateTime getOrderPlacedTime() { return orderPlacedTime; }
    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public String getOrderType() { return orderType; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Order ID: " + this.getId() +
                " | Type: " + this.getOrderType() +
                " | Payment: " + this.getPaymentMethod() +
                " | Placed: " + this.getOrderPlacedTime().toLocalDate() +
                " at " + this.getOrderPlacedTime().toLocalTime() +
                " by " + this.getName();
    }
}
