package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.handlers.*;
import il.cshaifasweng.OCSFMediatorExample.initializers.DataInitializer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static il.cshaifasweng.OCSFMediatorExample.util.HibernateUtil.getSessionFactory;

public class SimpleServer extends AbstractServer {

	private static final ConcurrentHashMap<ConnectionToClient, String> onlineUsers = new ConcurrentHashMap<>();
	private static SessionFactory sessionFactory = getSessionFactory();
    private static Session session;

	private ArrayList<Meals> mealsArrayList;
	private ArrayList<Meals> mealsByCategories;
	private String branch;

	private static SimpleServer instance;

	public SimpleServer(int port) {
		super(port);
		instance = this;
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

	public static SimpleServer getInstance() {
		return instance;
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
			case "#DeleteMeal":
				Meals mealToDelete = (Meals) message.getObject();
				boolean success = MealHandler.deleteMeal(mealToDelete, sessionFactory);

				if (success) {
					try {
						client.sendToClient(new Message("#MealDeleted"));
						sendToAllClients(new Message("#Update All Meals"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						client.sendToClient(new Message("#MealDeletionFailed"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;

			case "#AddMeal":
				Meals newMeal = (Meals) message.getObject();
				 success = MealHandler.addMeal(newMeal, branch, sessionFactory);
				if (success) {
                    try {
						client.sendToClient(new Message("#MealAddedSuccessfully"));
						sendToAllClients(new Message("#Update All Meals"));
					} catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("✅ Meal added: " + newMeal.getName() + " to " + branch);
				} else {
                    try {
                        client.sendToClient(new Message("#MealAdditionFailed"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("❌ Failed to add meal.");
				}
				break;

			case "#ToggleMealType":
				Object[] mealData = (Object[]) message.getObject();
				int mealId = (int) mealData[0];
				Integer branchId = (mealData[1] != null) ? (Integer) mealData[1] : null;

				MealHandler.handleToggleMealType(mealId, branchId, sessionFactory);

				mealsArrayList = MealHandler.getMeals(branch, sessionFactory);
                try {
					sendToAllClients(new Message("#Update All Meals"));
                } catch (Exception e) {
					System.out.println("Error - simple server - toggle meal type");
                }

                break;

			case "#CancelOrder":
				CancelingHandler.cancelOrder((Orders) message.getObject(), sessionFactory);
				try {
					client.sendToClient(new Message("OrderCanceled"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				break;
			case "#ValidateUser": {
				String[] userDetails = (String[]) message.getObject();
				String email = userDetails[0];
				String phone = userDetails[1];

				List<Orders> userOrders = CancelingHandler.fetchOrders(email, phone, sessionFactory);

				System.out.println("fetched the following orders : ");
				for (Orders order : userOrders) {
					System.out.println(
							"Order ID: " + order.getId() +
									" | Placed: " + order.getOrderPlacedTime().toLocalDate() +
									" at " + order.getOrderPlacedTime().toLocalTime()
					);
				}
				if (!userOrders.isEmpty()) {
					try {
						client.sendToClient(new Message(userOrders, "#UserValidated"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					try {
						client.sendToClient(new Message(null, "#ValidationFailed"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			break;
			case "#PlaceOrder": {
				try {
					OrderHandler.placeOrder((Orders) message.getObject(), sessionFactory);
					Message response = new Message("#OrderPlacedSuccessfully");
					client.sendToClient(response);

				} catch (Exception e) {
					e.printStackTrace();
					try {
						client.sendToClient(new Message("#OrderPlacementFailed"));
						System.out.println("Order placement failed - server");
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
				}
			}
			break;
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
				List<RestaurantTable> allocatedTables = ReservationHandler.saveReservation(reservationRequest, sessionFactory);
				if (allocatedTables != null && !allocatedTables.isEmpty()) {
					for (RestaurantTable table : allocatedTables) {
						sendToAllClients(new Message(table, "#TableReservedSuccess"));
					}
					try {
						client.sendToClient(new Message(reservationRequest, "#ReservationSuccess"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						String alternatives = ReservationHandler.computeAlternativeTimes(reservationRequest, sessionFactory);
						client.sendToClient(new Message(alternatives, "#NoAvailability"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;

			case "#ReserveTable":
				RestaurantTable table = (RestaurantTable) message.getObject();
				TableHandler.reserveTable(table, sessionFactory);
				sendToAllClients(new Message(table, "#TableReservedSuccess"));
				break;

			case "#CancelTableReservation":
				RestaurantTable tableToCancel = (RestaurantTable) message.getObject();
				TableHandler.cancelTableReservation(tableToCancel, sessionFactory);
				sendToAllClients(new Message(tableToCancel, "#TableReservationCanceledSuccess"));
				break;

			case "#FetchReservations":
				String[] details = (String[]) message.getObject();
				String fullName = details[0];
				String phone = details[1];
				List<Reservation> userReservations = ReservationHandler.getReservationsByUser(fullName, phone, sessionFactory);
				try {
					client.sendToClient(new Message(userReservations, "#UserReservations"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			case "#CancelReservation":
				Reservation reservationToCancel = (Reservation) message.getObject();
				boolean feeApplied = ReservationHandler.cancelReservation(reservationToCancel, sessionFactory);
				String responseDetail = feeApplied ? "CancellationSuccessWithFee" : "CancellationSuccess";
				try {
					client.sendToClient(new Message(responseDetail, "#CancelReservationSuccess"));
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
			case "#FetchComplaints":
				try (Session session = getSessionFactory().openSession()) {
					List<ContactRequest> complaints = session.createQuery("FROM ContactRequest", ContactRequest.class).list();
					client.sendToClient(new Message(complaints, "#ComplaintList"));
					System.out.println("✅ Sent all complaints to client.");
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("❌ Error fetching complaints from database.");
				}
				break;


			case "#ResolveComplaint":
				ContactRequest resolvedComplaint = (ContactRequest) message.getObject();
				try (Session session = getSessionFactory().openSession()) {
					Transaction tx = session.beginTransaction();

					// Fetch the actual complaint from DB
					ContactRequest complaintFromDB = session.get(ContactRequest.class, resolvedComplaint.getId());
					if (complaintFromDB != null) {
						complaintFromDB.setHandled(true);
						complaintFromDB.setResolutionScript(resolvedComplaint.getResolutionScript());
						complaintFromDB.setRefundIssued(resolvedComplaint.isRefundIssued());
						complaintFromDB.setHandledAt(LocalDateTime.now());

						session.update(complaintFromDB);
						tx.commit();

						// Send updated list back to all clients
						List<ContactRequest> updatedUnresolved = session.createQuery(
								"FROM ContactRequest WHERE handled = false", ContactRequest.class).getResultList();
						List<ContactRequest> updatedResolved = session.createQuery(
								"FROM ContactRequest WHERE handled = true", ContactRequest.class).getResultList();

						sendToAllClients(new Message(updatedUnresolved, "#ComplaintList"));
						sendToAllClients(new Message(updatedResolved, "#ResolvedComplaintList"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;



			default:
				System.out.println("Unknown message received: " + msgStr);
				break;
		}
	}
	public void resolveComplaint(int complaintId, String resolutionScript, boolean refundIssued) {
		try (Session session = sessionFactory.openSession()) {
			Transaction tx = session.beginTransaction();
			ContactRequest complaint = session.get(ContactRequest.class, complaintId);

			if (complaint != null && !complaint.isHandled()) {
				complaint.setHandled(true);
				complaint.setHandledAt(LocalDateTime.now());
				complaint.setResolutionScript(resolutionScript);

				session.update(complaint);
				tx.commit();

				System.out.println("✅ Complaint #" + complaintId + " resolved.");
				System.out.println("📜 Resolution: " + resolutionScript);

				if (refundIssued) {
					System.out.println("💰 Refund has been issued to the customer.");
				}
			} else {
				System.out.println("⚠️ Complaint not found or already resolved.");
			}
		} catch (Exception e) {
			e.printStackTrace();
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
