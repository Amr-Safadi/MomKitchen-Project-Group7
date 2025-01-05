package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Meals")
public class Meals implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(columnDefinition = "TEXT")
    private String preferences;

    @Column(nullable = false)
    private double price;

    public Meals(int id ,String name, String ingredients, String preferences, double price) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.preferences = preferences;
        this.price = price;
    }

    public Meals(String name, String ingredients, String preferences, double price) {
        this.name = name;
        this.ingredients = ingredients;
        this.preferences = preferences;
        this.price = price;
    }

    public Meals() {
        this.name = "";
        this.ingredients = "";
        this.preferences = "";
        this.price = 0;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return name + " - $" + price;
    }

}
