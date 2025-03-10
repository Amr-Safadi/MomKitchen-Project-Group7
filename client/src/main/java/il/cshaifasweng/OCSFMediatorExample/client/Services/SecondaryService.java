package il.cshaifasweng.OCSFMediatorExample.client.Services;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import java.util.ArrayList;
import java.util.List;

public class SecondaryService {

    private static String branch = "";
    private static final List<Meals> mealsList = new ArrayList<>();

    private SecondaryService() {
    }

    public static void setBranch(String branchName) {
        branch = branchName;
    }

    public static String getBranch() {
        return branch;
    }

    public static List<Meals> getMealsList() {
        return mealsList;
    }

    public static void setMealsList(List<Meals> newMeals) {
        mealsList.clear();
        if (newMeals != null) {
            mealsList.addAll(newMeals);
        }
    }
}
