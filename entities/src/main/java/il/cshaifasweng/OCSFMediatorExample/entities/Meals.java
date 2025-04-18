package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Meals.Category category;

    @Column(nullable = false)
    private Boolean isBranchMeal = false;

    @ManyToMany(mappedBy = "meals")
    private List<Branch> branches = new ArrayList<>();


    public Meals() {}

    public Meals(String name, String ingredients, String preferences, double price, Category category,boolean isBranchMeal) {
        this.name = name;
        this.ingredients = ingredients;
        this.preferences = preferences;
        this.price = price;
        this.category = category;
        this.isBranchMeal = isBranchMeal;

    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Enum<Category> getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public boolean getisBranchMeal() { return isBranchMeal; }
    public void setBranchMeal(boolean isBranchMeal) { this.isBranchMeal = isBranchMeal; }

    public List<Branch> getBranches() {
        if (branches == null) {
            branches = new ArrayList<>();
        }
        return branches;
    }
    public void setBranches(List<Branch> branches) { this.branches = branches; }

    public enum Category {
        DRINKS,
        ITALIAN,
        MEAT
    }
}
