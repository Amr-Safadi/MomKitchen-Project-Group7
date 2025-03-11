package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class SimpleServer extends AbstractServer {

	private static final ConcurrentHashMap<ConnectionToClient, String> onlineUsers = new ConcurrentHashMap<>();



	private static Session session;
	private static SessionFactory sessionFactory = getSessionFactory();

	private static SessionFactory getSessionFactory() throws
			HibernateException {
		Configuration configuration = new Configuration();

		// Add ALL of your entities here. You can also try adding a whole
		configuration.addAnnotatedClass(Meals.class);
		configuration.addAnnotatedClass(Branch.class);
		configuration.addAnnotatedClass(User.class);
		configuration.addAnnotatedClass(Reservation.class);

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
//			populateInitialData(session);
//			populateUsers(session);
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

	ArrayList<Meals> mealsArrayList ;
	ArrayList<Meals> mealsByCategories ;
	String branch ;


	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		if (!(msg instanceof Message)) {
			System.out.println("Received an invalid message type.");
			return;
		}

		Message message = (Message) msg;
		String msgStr = message.toString();

		switch (msgStr) {
			case "fetchDrinks":

				mealsByCategories = fetchMealByCategoriesAndBranch(branch,"Drinks");
                try {
                    client.sendToClient(new Message(mealsByCategories,"Category Fetched"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }break;
			case "fetchItalian":

				mealsByCategories = fetchMealByCategoriesAndBranch(branch,"Italian");
				try {
					client.sendToClient(new Message(mealsByCategories,"Category Fetched"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}break;
			case "fetchMeat":

				mealsByCategories = fetchMealByCategoriesAndBranch(branch,"Meat");
				try {
					client.sendToClient(new Message(mealsByCategories,"Category Fetched"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}break;
            case "#LoginRequest":
				User user = (User) message.getObject();
				handleUserLogin(user, client);
				break;

			case "#Update Meal":
				System.out.println("Handling meal update request.");
				Meals updatedMeal = (Meals) message.getObject();
				updateMeal(updatedMeal);
				try {
					sendToAllClients(new Message("#Update All Meals"));
					System.out.println("Meal updated and sent to clients.");
				} catch (Exception e) {
					System.out.println("Error updating the meal");
					e.printStackTrace();
				}
				break;

			case "#Meals Request":
				try {
					branch = (String) message.getObject();
					mealsArrayList = getMeals(branch);
					client.sendToClient(new Message(mealsArrayList, "#Initialize Meals"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case "#ReservationRequest":
				Reservation reservationRequest = (Reservation) message.getObject();
				boolean available = checkAvailability(reservationRequest);
				if (available) {
					saveReservation(reservationRequest);
					try {
						client.sendToClient(new Message(reservationRequest, "#ReservationSuccess"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					String alternatives = computeAlternativeTimes(reservationRequest);
					try {
						client.sendToClient(new Message(alternatives, "#NoAvailability"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;

			default:
				System.out.println("Unknown message received: " + msgStr);
				break;
		}
	}


	private static void populateUsers(Session session)
	{
		User Amr = new User("amr",passwordEncrypt("Amr123"), "Amr Safadi" , User.Role.GENERAL_MANAGER);
		User Marian = new User("marian",passwordEncrypt("Marian123"), "Marian Dahmoush" , User.Role.BRANCH_MANAGER);
		User Kanar = new User("kanar",passwordEncrypt("Kanar123"), "Kanar Arrabi" , User.Role.DIETITIAN);

		session.saveOrUpdate(Amr);
		session.saveOrUpdate(Marian);
		session.saveOrUpdate(Kanar);
		System.out.println("Users added to the database.");
	}

	private boolean checkAvailability(Reservation reservation) {
		LocalTime open = LocalTime.of(17, 0);
		LocalTime close = LocalTime.of(20, 0);
		LocalTime startAllowed = open.plusMinutes(15);
		LocalTime endAllowed = close.minusMinutes(60);
		LocalTime requested = reservation.getTime();
		return !requested.isBefore(startAllowed) && !requested.isAfter(endAllowed);
	}

	private void saveReservation(Reservation reservation) {
		try (Session session = getSessionFactory().openSession()) {
			Transaction tx = session.beginTransaction();

			if (reservation.getBranch() == null || reservation.getBranch().getId() == 0) {
				String branchName = reservation.getBranch() != null ? reservation.getBranch().getName() : "defaultBranch";
				Branch persistentBranch = session.createQuery("FROM Branch WHERE name = :branchName", Branch.class)
						.setParameter("branchName", branchName)
						.uniqueResult();
				if (persistentBranch == null) {
					throw new RuntimeException("Branch not found: " + branchName);
				}
				reservation.setBranch(persistentBranch);
			}

			session.save(reservation);
			tx.commit();
			System.out.println("Reservation saved: " + reservation.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private String computeAlternativeTimes(Reservation reservation) {
		LocalTime open = LocalTime.of(17, 0);
		LocalTime close = LocalTime.of(20, 0);
		LocalTime startAllowed = open.plusMinutes(15);
		LocalTime endAllowed = close.minusMinutes(60);
		List<String> alternatives = new ArrayList<>();
		LocalTime slot = startAllowed;
		while (!slot.isAfter(endAllowed)) {
			alternatives.add(slot.toString());
			slot = slot.plusMinutes(15);
		}
		return String.join(", ", alternatives);
	}

	// a function to give intial values to meals database
	private static void populateInitialData(Session session) {

		try {

			Branch branch1 = new Branch("Haifa", "Hanamal 16", LocalTime.of(8, 0), LocalTime.of(23, 0));
			Branch branch2 = new Branch("Acre", "Main St. 13", LocalTime.of(9, 0), LocalTime.of(21, 0));
			Branch branch3 = new Branch("Tel-Aviv", "Rothschild st. 11", LocalTime.of(10, 0), LocalTime.of(22, 0));
			Branch branch4 = new Branch("Netanya", "Hagefen st. 14", LocalTime.of(10, 0), LocalTime.of(21, 0));


			session.saveOrUpdate(branch1);
			session.saveOrUpdate(branch2);
			session.saveOrUpdate(branch3);
			session.saveOrUpdate(branch4);

			Meals meal1 = new Meals("Pizza", "Cheese, Tomato, Bail, Dough","Olives, Mushrooms, Peppers, Extra Cheese, Onions, Pepperoni, Basil, Oregano", 10.99, Meals.Category.ITALIAN);
			Meals meal2 = new Meals("Burger", "Beef, Lettuce, Tomato, BBQ sauce", " Cheese, Lettuce, Tomato, Pickles, Onions, Bacon, Ketchup, Mayo", 89.00, Meals.Category.MEAT);
			Meals meal3 = new Meals("Pasta", "Tomato Sauce, Parmesan", "Parmesan, Basil, Extra Sauce, Mushrooms, Garlic, Chili Flakes", 65.00, Meals.Category.ITALIAN);
			Meals meal4 = new Meals("Mineral Water", "", "", 10.00, Meals.Category.DRINKS);
			Meals meal5 = new Meals("Diet Coke", "", "", 13.00, Meals.Category.DRINKS);
			Meals meal6 = new Meals("Orange juice", "made of fresh oranges", "", 17.00, Meals.Category.DRINKS);
			Meals meal7 = new Meals("Fillet Steak", "350 gr steak, french fries on the side", "Garlic Butter, Mushrooms, Grilled Onions, Herb Butter", 120.00, Meals.Category.MEAT);
			Meals meal8 = new Meals("Chicken Wings", "200 gr, with rice on the side", "BBQ Sauce, Spicy Sauce, Ranch Dip, Honey Mustard, Extra Crispy, Lemon Pepper", 119.00, Meals.Category.MEAT);
			Meals meal9 = new Meals("cheese Ravioli", "Cream, Mushrooms, Parmesan", "No Mushrooms", 119.00, Meals.Category.ITALIAN);
			Meals meal10 = new Meals("Sezar Salad", "Lettuce, Chicken slices, Sezar Sauce, Parmesan", "No Cheese", 56.00, Meals.Category.ITALIAN);

			session.	saveOrUpdate(meal1);
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


			System.out.println("Initial meals added to the database.");
			System.out.println("branch1 meals are : \n");
			for (Meals meal : branch1.getMeals()) {
				System.out.println(meal.getName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String passwordEncrypt(String password) {
		String salt = "encrypt";
		try {
			String passwordWithSalt = password + salt;
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			byte[] hashBytes = digest.digest(passwordWithSalt.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hashBytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
	}

	//a function that returns all the meals in the database
	public ArrayList<Meals> getMeals(String branchName) {

		ArrayList<Meals> mealsArrayList = new ArrayList<>(); // Initialize a new ArrayList
		List<Meals> mealsList;

		try (Session session = getSessionFactory().openSession()) {
			// Begin a transaction
			Transaction transaction = session.beginTransaction();

			// Query to fetch all meals
			// Fetch meals only for the given branch
			mealsList = session.createQuery(
							"SELECT m FROM Meals m JOIN m.branches b WHERE b.name = :branchName", Meals.class)
					.setParameter("branchName", branchName)
					.getResultList();
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


	private void handleUserLogin(User user, ConnectionToClient client) {
		Transaction tx = null;
		User authenticatedUser = null;

		try (Session session = getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			authenticatedUser = session.createQuery("FROM User WHERE email = :email", User.class)
					.setParameter("email", user.getEmail())
					.uniqueResult();
			tx.commit();
		} catch (Exception e) {
			if (tx != null) tx.rollback();
			e.printStackTrace();
		}

		try {
			if (authenticatedUser == null) {
				client.sendToClient(new Message(null, "#EmailNotFound"));
				System.out.println("Login failed: Email not found -> " + user.getEmail());
			} else if (!authenticatedUser.getPassword().equals(passwordEncrypt(user.getPassword()))) {
				client.sendToClient(new Message(null, "#IncorrectPassword"));
				System.out.println("Login failed: Incorrect password for -> " + user.getEmail());
			} else if (onlineUsers.contains(authenticatedUser.getEmail())) {
				client.sendToClient(new Message(null, "#AlreadyLoggedIn"));
				System.out.println("Login failed: User already logged in -> " + user.getEmail());
			} else {
				onlineUsers.put(client, authenticatedUser.getEmail());
				client.sendToClient(new Message(authenticatedUser, "#LoginSuccess"));
				System.out.println("User logged in: " + authenticatedUser.getEmail() + " | Role: " + authenticatedUser.getRole());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public ArrayList<Meals> fetchMealByCategoriesAndBranch(String branchName, String category) {
		ArrayList<Meals> mealsByCategoryAndBranch = new ArrayList<>();
		List<Meals> mealsList;

		try (Session session = getSessionFactory().openSession()) {
			// Begin a transaction
			Transaction transaction = session.beginTransaction();

			// Convert category string to Enum safely
			Meals.Category categoryEnum;
			try {
				categoryEnum = Meals.Category.valueOf(category.toUpperCase()); // Convert string to Enum
			} catch (IllegalArgumentException e) {
				System.out.println("❌ Invalid category: " + category);
				return mealsByCategoryAndBranch; // Return empty list if the category is invalid
			}

			// Query to fetch meals by both category and branch
			mealsList = session.createQuery(
							"SELECT m FROM Meals m JOIN m.branches b " +
									"WHERE m.category = :category AND b.name = :branchName", Meals.class)
					.setParameter("category", categoryEnum)
					.setParameter("branchName", branchName)
					.getResultList();

			// Add all fetched meals to the ArrayList
			mealsByCategoryAndBranch.addAll(mealsList);

			// Commit the transaction
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mealsByCategoryAndBranch;
	}

	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
		String disconnectedEmail = onlineUsers.remove(client);

		if (disconnectedEmail != null) {
			System.out.println("✅ User disconnected: " + disconnectedEmail);
		} else {
			System.out.println("⚠️ Unknown user disconnected or already removed.");
		}

		System.out.println("Online users now are " + onlineUsers); // Debugging
	}
}
