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

    private String name;       // Customer Name
    private String address;    // Delivery Address
    private String phoneNumber;
    private String email;
    private String creditCard;
    private double totalPrice;
    private String status; // PENDING, COMPLETED, CANCELED
    private LocalDateTime orderPlacedTime;  // Automatically filled
    private LocalDateTime deliveryTime;

    public Orders() {}

    public Orders(List<Meals> meals, String name, String address, String phoneNumber, String email, String creditCard, double totalPrice, LocalDateTime deliveryTime) {
        this.meals = meals;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.creditCard = creditCard;
        this.totalPrice = totalPrice;
        this.status = "PENDING";
        this.orderPlacedTime = LocalDateTime.now(); // Automatically set order time
        this.deliveryTime = deliveryTime;
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
    public void printOrder() {
        System.out.println("==== Order Receipt ====");
        System.out.println("Order ID: " + this.id);
        System.out.println("Name: " + this.name);
        System.out.println("Address: " + this.address);
        System.out.println("Phone: " + this.phoneNumber);
        System.out.println("Email: " + this.email);
        System.out.println("Placed On: " + this.orderPlacedTime);
        System.out.println("Delivery Time: " + this.deliveryTime);
        System.out.println("\nItems Ordered:");

        for (Meals meal : this.meals) {
            System.out.println(" - " + meal.getName() + " ($" + meal.getPrice() + ")");
        }

        System.out.println("\nTotal Price: $" + this.totalPrice);
        System.out.println("=======================");
    }


    public void setStatus(String status) { this.status = status; }
}
