package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.PriceChangeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.server.SimpleServer;
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

    public static boolean savePriceChangeRequest(Meals meal, double requestedPrice, User requestedBy, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Attach the meal and user from the database (to avoid detached issues)
            Meals dbMeal = session.get(Meals.class, meal.getId());
            User dbUser = session.get(User.class, requestedBy.getId());

            if (dbMeal == null || dbUser == null) {
                System.out.println("❌ Could not find meal or user for price change request.");
                return false;
            }

            PriceChangeRequest request = new PriceChangeRequest(dbMeal, requestedPrice, dbUser);

            session.saveOrUpdate(request);
            tx.commit();

            System.out.println("✅ Price change request saved.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<PriceChangeRequest> getUnresolvedRequests(SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT r FROM PriceChangeRequest r " +
                            "JOIN FETCH r.meal m " +
                            "LEFT JOIN FETCH m.branches " +
                            "WHERE r.resolved = false", PriceChangeRequest.class
            ).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static boolean approvePriceChangeRequest(PriceChangeRequest request, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            PriceChangeRequest dbRequest = session.get(PriceChangeRequest.class, request.getId());
            if (dbRequest == null || dbRequest.isResolved()) {
                System.out.println("❌ Request not found or already resolved.");
                return false;
            }

            Meals meal = dbRequest.getMeal();
            meal.setPrice(dbRequest.getRequestedPrice());

            dbRequest.setApproved(true);
            dbRequest.setResolved(true);
            dbRequest.setResolvedAt(java.time.LocalDateTime.now());

            session.update(meal);
            session.update(dbRequest);

            tx.commit();

            System.out.println("✅ Price change approved and applied.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void handleToggleMealType(int mealId, Integer branchId, SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // Fetch the meal
            Meals meal = session.get(Meals.class, mealId);
            if (meal == null) {
                System.out.println("❌ Meal not found!");
                return;
            }

            // Toggle meal type
            meal.setBranchMeal(!meal.getisBranchMeal());

            if (!meal.getisBranchMeal()) {
                // Convert to Network Meal: Assign to all branches
                List<Branch> allBranches = session.createQuery("FROM Branch", Branch.class).getResultList();

                // Remove meal from all branches to avoid duplicates
                for (Branch branch : meal.getBranches()) {
                    branch.getMeals().remove(meal);
                    session.merge(branch);
                }

                meal.getBranches().clear();
                meal.getBranches().addAll(allBranches);

                // Update each branch to include the meal
                for (Branch branch : allBranches) {
                    branch.getMeals().add(meal);
                    session.merge(branch);
                }

                System.out.println("✅ Meal converted to a Network Meal and added to all branches.");
            } else {
                // Convert to Branch-Specific Meal: Assign only to the provided branch
                if (branchId == null) {
                    System.out.println("❌ No branch selected for conversion!");
                    return;
                }

                Branch selectedBranch = session.get(Branch.class, branchId);
                if (selectedBranch == null) {
                    System.out.println("❌ Invalid branch ID: " + branchId);
                    return;
                }

                // Remove from all other branches
                for (Branch branch : meal.getBranches()) {
                    if (!branch.equals(selectedBranch)) {
                        branch.getMeals().remove(meal);
                        session.merge(branch);
                    }
                }

                meal.getBranches().clear();
                meal.getBranches().add(selectedBranch);

                System.out.println("✅ Meal converted to Branch-Specific and assigned to " + selectedBranch.getName());
            }

            session.merge(meal);
            transaction.commit();
            System.out.println("✅ Meal type updated successfully!");

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            System.out.println("❌ Error updating meal type.");
        } finally {
            session.close();
        }
    }
    public static boolean addMeal(Meals meal, String branchName, SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        try {
            // Save the meal to the database
            session.save(meal);

            // Find the branch by name
            List<Branch> branches;

            if (meal.getisBranchMeal()) {
                // If it's a branch meal, find the specific branch
                branches = session.createQuery(
                                "FROM Branch WHERE name = :branchName", Branch.class)
                        .setParameter("branchName", branchName)
                        .getResultList();
            } else {
                // If it's a network-wide meal, fetch ALL branches
                branches = session.createQuery("FROM Branch", Branch.class).getResultList();
            }

            for (Branch branch : branches) {
                branch.getMeals().add(meal);
                meal.getBranches().add(branch);
                session.update(branch);
            }

            transaction.commit();
            return true; // Success
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            return false; // Failure
        } finally {
            session.close();
        }
    }
    public static boolean deleteMeal(Meals meal, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Fetch meal from DB
            Meals mealFromDB = session.get(Meals.class, meal.getId());
            if (mealFromDB != null) {

                // ✅ Remove meal from all branches first
                for (Branch branch : mealFromDB.getBranches()) {
                    branch.getMeals().remove(mealFromDB);
                    session.update(branch); // Update each branch
                }

                // ✅ Remove all associations from branch_meal table
                mealFromDB.getBranches().clear();
                session.update(mealFromDB);

                // ✅ Delete meal after associations are removed
                session.delete(mealFromDB);
                tx.commit();
                System.out.println("✅ Meal deleted successfully: " + meal.getName());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}

