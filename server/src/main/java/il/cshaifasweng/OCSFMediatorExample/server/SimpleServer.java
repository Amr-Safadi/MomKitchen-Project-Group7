package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class SimpleServer extends AbstractServer {

	private static Session session;
	private static SessionFactory sessionFactory = getSessionFactory();

	private static SessionFactory getSessionFactory() throws
			HibernateException {
		Configuration configuration = new Configuration();

		// Add ALL of your entities here. You can also try adding a whole
		configuration.addAnnotatedClass(Meals.class);

		ServiceRegistry serviceRegistry = new
				StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties())
				.build();

		return configuration.buildSessionFactory(serviceRegistry);
	}


	public SimpleServer(int port) {
		super(port);
		try{
			session = sessionFactory.openSession();
			session.beginTransaction();
			populateInitialData(session);
			session.getTransaction().commit();
		} catch (Exception var5) {
			if (session != null && session.getTransaction().isActive()) {
				session.getTransaction().rollback();
			}
			var5.printStackTrace();
		}finally {
			if(session!=null){
				session.close();
			}
		}
		
	}
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		//Update Meal
		if (msg instanceof Meals) {
			Meals updatedMeal = (Meals) msg;
			updateMeal(updatedMeal);
			ArrayList<Meals> mealsArrayList = getAllMeals();
			// Notify the client of the successful update
			try {
				client.sendToClient(mealsArrayList);
				System.out.println("Meal updated.");
				// by sending the meals array again we force the client to update the the listview with the new meals
			} catch (IOException e) {
				System.out.println("Error updating the meal");
				e.printStackTrace();
			}
		} //Initialize Meals
		else if (msg.toString().startsWith("#Meals Request")) {
			try {
				ArrayList<Meals> mealsArrayList = getAllMeals();
				client.sendToClient(mealsArrayList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	// a function to give intial values to meals database
	private static void populateInitialData(Session session) {

		try {
			session.merge(new Meals(1,"Spaghetti Bolognese", "Pasta, Tomato Sauce, Ground Beef", "Spicy, Gluten-Free Option", 25.50));
			session.merge(new Meals(2,"Chicken Caesar Salad", "Lettuce, Chicken, Caesar Dressing", "Extra Dressing, No Croutons", 18.00));
			session.merge(new Meals(3,"Margherita Pizza", "Dough, Tomato Sauce, Mozzarella", "Extra Cheese, Thin Crust", 20.00));
			session.merge(new Meals(4,"Beef Burger", "Beef Patty, Lettuce, Tomato, Bun", "Well-Done, Gluten-Free Bun", 22.50));
			session.merge(new Meals(5,"Chocolate Cake", "Flour, Cocoa, Sugar, Eggs", "Extra Frosting, Vegan Option", 15.00));

			session.flush();

			System.out.println("Initial meals added to the database.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//a function that returns all the meals in the database
	public ArrayList<Meals> getAllMeals() {
		ArrayList<Meals> mealsArrayList = new ArrayList<>(); // Initialize a new ArrayList
		List<Meals> mealsList;

		try (Session session = getSessionFactory().openSession()) {
			// Begin a transaction
			Transaction transaction = session.beginTransaction();

			// Query to fetch all meals
			mealsList = session.createQuery("FROM Meals", Meals.class).list();
			System.out.println("1");
			// Add all meals to the ArrayList
			mealsArrayList.addAll(mealsList);
			System.out.println("2");
			// Commit the transaction
			transaction.commit();
			System.out.println("3");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mealsArrayList;
	}


	// a function that counts how many meals are there in the DB
	public long countMeals() {
		long count = 0;

		try (Session session = getSessionFactory().openSession()) {
			// Begin a transaction
			Transaction transaction = session.beginTransaction();

			// Perform a COUNT query
			count = session.createQuery("SELECT COUNT(m) FROM Meals m", Long.class).getSingleResult();

			// Commit the transaction
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	public void updateMeal(Meals updatedMeal) {
		try (Session session = getSessionFactory().openSession()) {
			// Start a transaction
			Transaction transaction = session.beginTransaction();

			// Fetch the meal from the database
			Meals existingMeal = session.get(Meals.class, updatedMeal.getId());

			if (existingMeal != null) {
				// Update fields only if they are not null (to allow partial updates)
				if (updatedMeal.getName() != null) {
					existingMeal.setName(updatedMeal.getName());
				}
				if (updatedMeal.getIngredients() != null) {
					existingMeal.setIngredients(updatedMeal.getIngredients());
				}
				if (updatedMeal.getPreferences() != null) {
					existingMeal.setPreferences(updatedMeal.getPreferences());
				}
				if (updatedMeal.getPrice() != 0) {  // Assuming 0 is not a valid price
					existingMeal.setPrice(updatedMeal.getPrice());
				}

				// Save changes
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
