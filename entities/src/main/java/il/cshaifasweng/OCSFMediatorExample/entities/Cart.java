package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {
    private List<Meals> meals; // List to store meals
    private double totalPrice;

    public Cart() {
        this.meals = new ArrayList<>();
        this.totalPrice = 0;
    }

    // Add a meal to the cart
    public void addMeal(Meals meal) {
        meals.add(meal);
        totalPrice += meal.getPrice();
    }

    // Remove a meal from the cart
    public void removeMeal(Meals meal) {
        if (meals.remove(meal)) { // Removes the first occurrence
            totalPrice -= meal.getPrice();
        }
    }

    // Get all meals in the cart
    public List<Meals> getMeals() {
        return meals;
    }

    // Get total price of the cart
    public double getTotalPrice() {
        return totalPrice;
    }

    // Clear the cart
    public void clearCart() {
        meals.clear();
        totalPrice = 0;
    }

    // Get cart size (number of items)
    public int getCartSize() {
        return meals.size();
    }
}
