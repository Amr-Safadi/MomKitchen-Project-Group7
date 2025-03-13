package il.cshaifasweng.OCSFMediatorExample.initializers;

import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import org.hibernate.Session;

import java.time.LocalTime;
import java.util.ArrayList;

public class DataInitializer {
    public static void populateInitialData(Session session) {
        try {
            Branch branch1 = new Branch("Haifa", "Hanamal 16", LocalTime.of(8, 0), LocalTime.of(23, 0));
            Branch branch2 = new Branch("Acre", "Main St. 13", LocalTime.of(9, 0), LocalTime.of(21, 0));
            Branch branch3 = new Branch("Tel-Aviv", "Rothschild st. 11", LocalTime.of(10, 0), LocalTime.of(22, 0));
            Branch branch4 = new Branch("Netanya", "Hagefen st. 14", LocalTime.of(10, 0), LocalTime.of(21, 0));

            session.saveOrUpdate(branch1);
            session.saveOrUpdate(branch2);
            session.saveOrUpdate(branch3);
            session.saveOrUpdate(branch4);

            Meals meal1 = new Meals("Pizza", "Cheese, Tomato, Bail, Dough",
                    "Olives, Mushrooms, Peppers, Extra Cheese, Onions, Pepperoni, Basil, Oregano", 10.99, Meals.Category.ITALIAN,false);
            Meals meal2 = new Meals("Burger", "Beef, Lettuce, Tomato, BBQ sauce",
                    "Cheese, Lettuce, Tomato, Pickles, Onions, Bacon, Ketchup, Mayo", 89.00, Meals.Category.MEAT,false);
            Meals meal3 = new Meals("Pasta", "Tomato Sauce, Parmesan",
                    "Parmesan, Basil, Extra Sauce, Mushrooms, Garlic, Chili Flakes", 65.00, Meals.Category.ITALIAN,false);
            Meals meal4 = new Meals("Mineral Water", "", "", 10.00, Meals.Category.DRINKS,false);
            Meals meal5 = new Meals("Diet Coke", "", "", 13.00, Meals.Category.DRINKS,false);
            Meals meal6 = new Meals("Orange juice", "made of fresh oranges", "", 17.00, Meals.Category.DRINKS,false);
            Meals meal7 = new Meals("Fillet Steak", "350 gr steak, french fries on the side",
                    "Garlic Butter, Mushrooms, Grilled Onions, Herb Butter", 120.00, Meals.Category.MEAT,true);
            Meals meal8 = new Meals("Chicken Wings", "200 gr, with rice on the side",
                    "BBQ Sauce, Spicy Sauce, Ranch Dip, Honey Mustard, Extra Crispy, Lemon Pepper", 119.00, Meals.Category.MEAT,true);
            Meals meal9 = new Meals("cheese Ravioli", "Cream, Mushrooms, Parmesan",
                    "No Mushrooms", 119.00, Meals.Category.ITALIAN,true);
            Meals meal10 = new Meals("Sezar Salad", "Lettuce, Chicken slices, Sezar Sauce, Parmesan",
                    "No Cheese", 56.00, Meals.Category.ITALIAN,true);

            session.saveOrUpdate(meal1);
            session.saveOrUpdate(meal2);
            session.saveOrUpdate(meal3);
            session.saveOrUpdate(meal4);
            session.saveOrUpdate(meal5);
            session.saveOrUpdate(meal6);
            session.saveOrUpdate(meal7);
            session.saveOrUpdate(meal8);
            session.saveOrUpdate(meal9);
            session.saveOrUpdate(meal10);

            branch1.getMeals().add(meal1);
            branch1.getMeals().add(meal2);
            branch1.getMeals().add(meal3);
            branch1.getMeals().add(meal4);
            branch1.getMeals().add(meal5);
            branch1.getMeals().add(meal6);
            branch1.getMeals().add(meal7);

            branch2.getMeals().add(meal1);
            branch2.getMeals().add(meal2);
            branch2.getMeals().add(meal3);
            branch2.getMeals().add(meal4);
            branch2.getMeals().add(meal5);
            branch2.getMeals().add(meal6);
            branch2.getMeals().add(meal8);

            branch3.getMeals().add(meal1);
            branch3.getMeals().add(meal2);
            branch3.getMeals().add(meal3);
            branch3.getMeals().add(meal4);
            branch3.getMeals().add(meal5);
            branch3.getMeals().add(meal6);
            branch3.getMeals().add(meal9);

            branch4.getMeals().add(meal1);
            branch4.getMeals().add(meal2);
            branch4.getMeals().add(meal3);
            branch4.getMeals().add(meal4);
            branch4.getMeals().add(meal5);
            branch4.getMeals().add(meal6);
            branch4.getMeals().add(meal10);

            meal1.getBranches().add(branch1);
            meal1.getBranches().add(branch2);
            meal1.getBranches().add(branch3);
            meal1.getBranches().add(branch4);

            meal2.getBranches().add(branch1);
            meal2.getBranches().add(branch2);
            meal2.getBranches().add(branch3);
            meal2.getBranches().add(branch4);

            meal3.getBranches().add(branch1);
            meal3.getBranches().add(branch2);
            meal3.getBranches().add(branch3);
            meal3.getBranches().add(branch4);

            meal4.getBranches().add(branch1);
            meal4.getBranches().add(branch2);
            meal4.getBranches().add(branch3);
            meal4.getBranches().add(branch4);

            meal5.getBranches().add(branch1);
            meal5.getBranches().add(branch2);
            meal5.getBranches().add(branch3);
            meal5.getBranches().add(branch4);

            meal6.getBranches().add(branch1);
            meal6.getBranches().add(branch2);
            meal6.getBranches().add(branch3);
            meal6.getBranches().add(branch4);

            meal7.getBranches().add(branch1);
            meal8.getBranches().add(branch2);
            meal9.getBranches().add(branch3);
            meal10.getBranches().add(branch4);

            session.saveOrUpdate(branch1);
            session.saveOrUpdate(branch2);
            session.saveOrUpdate(branch3);
            session.saveOrUpdate(branch4);

            session.saveOrUpdate(meal1);
            session.saveOrUpdate(meal2);
            session.saveOrUpdate(meal3);
            session.saveOrUpdate(meal4);
            session.saveOrUpdate(meal5);
            session.saveOrUpdate(meal6);
            session.saveOrUpdate(meal7);
            session.saveOrUpdate(meal8);
            session.saveOrUpdate(meal9);
            session.saveOrUpdate(meal10);

            System.out.println("Initial meals and branches added to the database.");
            System.out.println("branch1 meals are: ");
            for (Meals meal : branch1.getMeals()) {
                System.out.println(meal.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
