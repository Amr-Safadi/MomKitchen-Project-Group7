package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.loader.plan.build.internal.returns.ScalarReturnImpl;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class MealHandler {
    private static Session session;

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
        String branchName = updatedMeal.getName();
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
                //if (updatedMeal.getisBranchMeal() != existingMeal.getisBranchMeal()) {
                    //updateMealPlacement(existingMeal, updatedMeal.getisBranchMeal(), branchName, session);
               //     existingMeal.setBranchMeal(updatedMeal.getisBranchMeal());
                //}
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
    /*public static void updateMealPlacement(Meals existingMeal,boolean moveToSpecial,String branchName,Session session) {
        Branch targetBranch = session.createQuery("FROM Branch WHERE name = :branchName", Branch.class)
                .setParameter("branchName", branchName)
                .uniqueResult();

        if (targetBranch == null) {
            System.out.println("Branch not found: " + branchName);
            return;
        }

        if (moveToSpecial) {
            // Remove meal from all branches first, then add only to the selected branch
            for (Branch branch : existingMeal.getBranches()) {
                branch.getMeals().remove(existingMeal);
                session.merge(branch);
            }
            existingMeal.getBranches().clear();
            existingMeal.getBranches().add(targetBranch);
            targetBranch.getMeals().add(existingMeal);
        } else {
            // Move meal back to general (all branches should have it)
            List<Branch> allBranches = session.createQuery("FROM Branch", Branch.class).getResultList();
            for (Branch branch : allBranches) {
                if (!branch.getMeals().contains(existingMeal)) {
                    branch.getMeals().add(existingMeal);
                }
            }
            existingMeal.getBranches().clear();
            existingMeal.getBranches().addAll(allBranches);
        }
        session.merge(existingMeal);
        session.merge(targetBranch);
    }
    public static void removeMealFromAllBranchesExceptOne(Meals meal, String exceptionBranchName, Session session) {
        Transaction transaction = session.beginTransaction();
        try {
            Branch exceptionBranch = session.createQuery(
                            "FROM Branch WHERE name = :branchName", Branch.class)
                    .setParameter("branchName", exceptionBranchName)
                    .uniqueResult();

            if (exceptionBranch == null) {
                System.out.println("Branch not found: " + exceptionBranchName);
                return;
            }

            List<Branch> allBranches = session.createQuery("FROM Branch", Branch.class).getResultList();

            for (Branch branch : allBranches) {
                if (!branch.equals(exceptionBranch) && branch.getMeals().contains(meal)) {
                    branch.getMeals().remove(meal);
                    session.merge(branch);
                }
            }

            Iterator<Branch> branchIterator = meal.getBranches().iterator();
            while (branchIterator.hasNext()) {
                Branch branch = branchIterator.next();
                if (!branch.equals(exceptionBranch)) {
                    branchIterator.remove();
                }
            }

            session.merge(meal);
            transaction.commit();
            System.out.println("Meal removed from all branches except: " + exceptionBranchName);
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public static void addMealToAllBranchesExceptOne(Meals meal, String exceptionBranchName,Session session) {
        Transaction transaction = session.beginTransaction();
        try {
            Branch exceptionBranch = session.createQuery(
                            "FROM Branch WHERE name = :branchName", Branch.class)
                    .setParameter("branchName", exceptionBranchName)
                    .uniqueResult();

            if (exceptionBranch == null) {
                System.out.println("Branch not found: " + exceptionBranchName);
                return;
            }

            List<Branch> allBranches = session.createQuery("FROM Branch", Branch.class).getResultList();

            for (Branch branch : allBranches) {
                if (!branch.equals(exceptionBranch) && !branch.getMeals().contains(meal)) {
                    branch.getMeals().add(meal);
                    session.merge(branch);
                }
            }

            for (Branch branch : allBranches) {
                if (!branch.equals(exceptionBranch) && !meal.getBranches().contains(branch)) {
                    meal.getBranches().add(branch);
                }
            }

            session.merge(meal);
            transaction.commit();
            System.out.println("Meal added to all branches except: " + exceptionBranchName);
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }/*

     */

