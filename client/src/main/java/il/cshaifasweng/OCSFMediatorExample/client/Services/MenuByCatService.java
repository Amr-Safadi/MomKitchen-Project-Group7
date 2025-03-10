package il.cshaifasweng.OCSFMediatorExample.client.Services;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import java.util.ArrayList;
import java.util.List;

public class MenuByCatService {

    private static String currentCategory;
    private static final List<Meals> mealsList = new ArrayList<>();

    private MenuByCatService() {
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    public static String getCurrentCategory() {
        return currentCategory;
    }

    public static List<Meals> getMealsList() {
        return mealsList;
    }

    @SuppressWarnings("unchecked")
    public static void setMealsList(List<Meals> newMeals) {
        mealsList.clear();
        if (newMeals != null) {
            mealsList.addAll(newMeals);
        }
    }
}
