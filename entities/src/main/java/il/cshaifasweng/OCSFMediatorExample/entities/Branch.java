package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Branches")
public class Branch implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalTime openHour;

    @Column(nullable = false)
    private LocalTime closeHour;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RestaurantTable> tables = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "Branch_Meal",
            joinColumns = @JoinColumn(name = "branch_id"),
            inverseJoinColumns = @JoinColumn(name = "meal_id")
    )
    private List<Meals> meals = new ArrayList<>();
    public Branch() {}

    public Branch(String name, String location, LocalTime openHour, LocalTime closeHour) {
        this.name = name;
        this.location = location;
        this.openHour = openHour;
        this.closeHour = closeHour;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalTime getOpenHour() { return openHour; }
    public void setOpenHour(LocalTime openHour) { this.openHour = openHour; }
    public LocalTime getCloseHour() { return closeHour; }
    public void setCloseHour(LocalTime closeHour) { this.closeHour = closeHour; }
    public List<Meals> getMeals() {
        if (meals == null) {
            meals = new ArrayList<>();
        }
        return meals;
    }
    public void setMeals(List<Meals> meals) { this.meals = meals; }
    public List<RestaurantTable> getTables() {
        return tables;
    }

    public void setTables(List<RestaurantTable> tables) {
        this.tables = tables;
    }
}
