package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class MealHandler {
    public static ArrayList<Meals> fetchMealByCategoriesAndBranch(String branchName, String category, SessionFactory sessionFactory) {
        ArrayList<Meals> mealsByCategoryAndBranch = new ArrayList<>();
        List<Meals> mealsList;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Meals.Category categoryEnum;
            try {
                categoryEnum = Meals.Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid category: " + category);
                return mealsByCategoryAndBranch;
            }
            mealsList = session.createQuery(
                            "SELECT m FROM Meals m JOIN m.branches b WHERE m.category = :category AND b.name = :branchName", Meals.class)
                    .setParameter("category", categoryEnum)
                    .setParameter("branchName", branchName)
                    .getResultList();
            mealsByCategoryAndBranch.addAll(mealsList);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mealsByCategoryAndBranch;
    }

    public static ArrayList<Meals> getMeals(String branchName, SessionFactory sessionFactory) {
        ArrayList<Meals> mealsArrayList = new ArrayList<>();
        List<Meals> mealsList;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            mealsList = session.createQuery(
                            "SELECT m FROM Meals m JOIN m.branches b WHERE b.name = :branchName", Meals.class)
                    .setParameter("branchName", branchName)
                    .getResultList();
            System.out.println("1");
            mealsArrayList.addAll(mealsList);
            System.out.println("2");
            transaction.commit();
            System.out.println("3");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mealsArrayList;
    }

    public static long countMeals(SessionFactory sessionFactory) {
        long count = 0;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            count = session.createQuery("SELECT COUNT(m) FROM Meals m", Long.class).getSingleResult();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static void updateMeal(Meals updatedMeal, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Meals existingMeal = session.get(Meals.class, updatedMeal.getId());
            if (existingMeal != null) {
                if (updatedMeal.getName() != null) {
                    existingMeal.setName(updatedMeal.getName());
                }
                if (updatedMeal.getIngredients() != null) {
                    existingMeal.setIngredients(updatedMeal.getIngredients());
                }
                if (updatedMeal.getPreferences() != null) {
                    existingMeal.setPreferences(updatedMeal.getPreferences());
                }
                if (updatedMeal.getPrice() != 0) {
                    existingMeal.setPrice(updatedMeal.getPrice());
                }
                session.merge(existingMeal);
                transaction.commit();
                System.out.println("Meal updated successfully.");
            } else {
                System.out.println("Meal not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
