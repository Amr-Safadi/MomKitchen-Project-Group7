package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.handlers.*;
import il.cshaifasweng.OCSFMediatorExample.initializers.DataInitializer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleServer extends AbstractServer {

	private static final ConcurrentHashMap<ConnectionToClient, String> onlineUsers = new ConcurrentHashMap<>();
	private static SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

	ArrayList<Meals> mealsArrayList;
	ArrayList<Meals> mealsByCategories;
	String branch;

	public SimpleServer(int port) {
		super(port);
		Session session = null;
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();
			DataInitializer.populateInitialData(session);
			UserHandler.populateUsers(session);
			session.getTransaction().commit();
		} catch (Exception e) {
			if (session != null && session.getTransaction().isActive()) {
				session.getTransaction().rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

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
				mealsByCategories = MealHandler.fetchMealByCategoriesAndBranch(branch, "Drinks", sessionFactory);
				try {
					client.sendToClient(new Message(mealsByCategories, "Category Fetched"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				break;
			case "fetchItalian":
				mealsByCategories = MealHandler.fetchMealByCategoriesAndBranch(branch, "Italian", sessionFactory);
				try {
					client.sendToClient(new Message(mealsByCategories, "Category Fetched"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				break;
			case "fetchMeat":
				mealsByCategories = MealHandler.fetchMealByCategoriesAndBranch(branch, "Meat", sessionFactory);
				try {
					client.sendToClient(new Message(mealsByCategories, "Category Fetched"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				break;
			case "#LoginRequest":
				UserHandler.handleLogin((User) message.getObject(), client, sessionFactory, onlineUsers);
				break;
			case "#Update Meal":
				System.out.println("Handling meal update request.");
				Meals updatedMeal = (Meals) message.getObject();
				MealHandler.updateMeal(updatedMeal, sessionFactory);
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
					mealsArrayList = MealHandler.getMeals(branch, sessionFactory);
					client.sendToClient(new Message(mealsArrayList, "#Initialize Meals"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "#BranchRequest":
				String branchName = (String) message.getObject();
				Branch branchObj = BranchHandler.getBranchByName(branchName, sessionFactory);
				try {
					client.sendToClient(new Message(branchObj, "#BranchFetched"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "#ReservationRequest":
				Reservation reservationRequest = (Reservation) message.getObject();
				if (ReservationHandler.checkAvailability(reservationRequest, sessionFactory)) {
					ReservationHandler.saveReservation(reservationRequest, sessionFactory);
					try {
						client.sendToClient(new Message(reservationRequest, "#ReservationSuccess"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					String alternatives = ReservationHandler.computeAlternativeTimes(reservationRequest);
					try {
						client.sendToClient(new Message(alternatives, "#NoAvailability"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;

			case "#ReserveTable":
				RestaurantTable table = (RestaurantTable) message.getObject();
				TableHandler.reserveTable(table, sessionFactory);
				// Optionally send a confirmation message back to the client:
				try {
					client.sendToClient(new Message(table, "#TableReservedSuccess"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			case "#Update Complaint":
				System.out.println("Storing complaint");
				ContactRequest complaint = (ContactRequest) message.getObject();
				ComplaintHandler.saveComplaint(complaint, sessionFactory);
				try {
					client.sendToClient(new Message(null, "#ComplaintSubmissionSuccess"));
					System.out.println("Complaint stored and confirmation sent to client.");
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			default:
				System.out.println("Unknown message received: " + msgStr);
				break;
		}
	}

	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
		String disconnectedEmail = onlineUsers.remove(client);
		if (disconnectedEmail != null) {
			System.out.println("✅ User disconnected: " + disconnectedEmail);
		} else {
			System.out.println("⚠️ Unknown user disconnected or already removed.");
		}
		System.out.println("Online users now are " + onlineUsers);
	}
}
