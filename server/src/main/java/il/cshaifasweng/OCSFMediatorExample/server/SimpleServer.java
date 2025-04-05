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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static il.cshaifasweng.OCSFMediatorExample.util.HibernateUtil.getSessionFactory;

public class SimpleServer extends AbstractServer {

	private static final ConcurrentHashMap<ConnectionToClient, String> onlineUsers = new ConcurrentHashMap<>();
	private static SessionFactory sessionFactory = getSessionFactory();
    private static Session session;
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private ArrayList<Meals> mealsArrayList;
	private ArrayList<Meals> mealsByCategories;
	private String branch;

	private static SimpleServer instance;

	public SimpleServer(int port) {
		super(port);
		instance = this;
		DataInitializer.initializeDatabaseIfEmpty(sessionFactory);
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

			case "#LogoutRequest":

				String email1 = onlineUsers.remove(client);
				if (email1 != null) {
					System.out.println("✅ User logged out: " + email1);
				} else {
					System.out.println("⚠️ Logout request received but user wasn't found in onlineUsers.");
				}
				break;

			case "#UploadMealImage":
				Object[] imagePayload = (Object[]) message.getObject();
				String imageMealName = (String) imagePayload[0];
				byte[] uploadedImageBytes = (byte[]) imagePayload[1];

				File uploadPath = new File("src/main/resources/Images/" + imageMealName + ".jpg");
				try {
					Files.write(uploadPath.toPath(), uploadedImageBytes);
					client.sendToClient(new Message("#ImageUploadSuccess:" + imageMealName));
					System.out.println("✅ Image uploaded successfully: " + imageMealName);
				} catch (IOException e) {
					e.printStackTrace();
                    try {
                        client.sendToClient(new Message("#ImageUploadFailed:" + imageMealName));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
				break;


			case "#RequestMealImage":
				String mealName = (String) message.getObject();
				File imageFile = new File("src/main/resources/Images/" + mealName + ".jpg");

				if (!imageFile.exists()) {
					System.out.println("❌ Image not found for meal: " + mealName);
					break;
				}

				try {
					byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
					client.sendToClient(new Message(imageBytes, "#MealImageResponse:" + mealName));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;


			case "#CheckPendingNotifications":
				List<PriceChangeRequest> pendingRequests = MealHandler.getUnresolvedRequests(sessionFactory);

				if (!pendingRequests.isEmpty()) {
					try {
						client.sendToClient(new Message(pendingRequests,"#ManagerHasNotifications"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;


			case "#RejectPriceChange":
				PriceChangeRequest rejectRequest = (PriceChangeRequest) message.getObject();

				try (Session session = sessionFactory.openSession()) {
					Transaction tx = session.beginTransaction();

					PriceChangeRequest dbRequest = session.get(PriceChangeRequest.class, rejectRequest.getId());

					if (dbRequest != null && !dbRequest.isResolved()) {
						dbRequest.setResolved(true);
						dbRequest.setApproved(false);
						dbRequest.setResolvedAt(java.time.LocalDateTime.now());

						session.update(dbRequest);
						tx.commit();

						client.sendToClient(new Message("#PriceChangeRejected"));
						sendToAllClients(new Message("#PriceChangeRequestSent"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;


			case "#ApprovePriceChange":
				PriceChangeRequest approvalRequest = (PriceChangeRequest) message.getObject();
				boolean approved = MealHandler.approvePriceChangeRequest(approvalRequest, sessionFactory);

				if (approved) {
					try {
						client.sendToClient(new Message("#PriceChangeApproved"));
						sendToAllClients(new Message("#Update All Meals")); // optional
						sendToAllClients(new Message("#PriceChangeRequestSent"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;

			case "#FetchPriceRequests":
				try {
					List<PriceChangeRequest> requests = MealHandler.getUnresolvedRequests(sessionFactory);
					client.sendToClient(new Message(requests, "#PriceRequestsList"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			case "#RequestPriceChange":
				Object[] requestData = (Object[]) message.getObject();
				Meals mealForPriceChange = (Meals) requestData[0];
				double newPrice = (double) requestData[1];
				User dietitian = (User) requestData[2];

				boolean requestSaved = MealHandler.savePriceChangeRequest(mealForPriceChange, newPrice, dietitian, sessionFactory);


				try {
					if (requestSaved) {
						sendToAllClients(new Message("#PriceChangeRequestSent"));
					} else {
						client.sendToClient(new Message("#PriceChangeRequestFailed"));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

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
					client.sendToClient(new Message("Meal Updated Successfully"));
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
					// Calculate the delay for scheduling the reservation confirmation
					LocalDateTime reservationTime = LocalDateTime.of(reservationRequest.getDate(), reservationRequest.getTime());
					LocalDateTime now = LocalDateTime.now();
					long delayMillis = Duration.between(now, reservationTime).toMillis()  ;  // Calculate delay in milliseconds

					try {
						client.sendToClient(new Message(allocatedTables, "#ReservationSuccess"));
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (delayMillis > 0) {
						// Schedule the task to send confirmation at the requested reservation time
						scheduler.schedule(() -> {
							for (RestaurantTable table : allocatedTables) {
								sendToAllClients(new Message(table, "#TableReservedSuccess"));
								TableHandler.scheduleAutoRelease(table.getId(), sessionFactory);
							}
						}, delayMillis, TimeUnit.MILLISECONDS);
						System.out.println("Scheduled reservation confirmation for table(s) at " + reservationTime);

					} else {
						// If reservation time is in the past, send confirmation immediately
						for (RestaurantTable table : allocatedTables) {
							sendToAllClients(new Message(table, "#TableReservedSuccess"));
							TableHandler.scheduleAutoRelease(table.getId(), sessionFactory);
						}
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
				RestaurantTable tableFromClient = (RestaurantTable) message.getObject();
				RestaurantTable updatedTable = TableHandler.reserveTable(tableFromClient, sessionFactory);
				if (updatedTable != null) {
					sendToAllClients(new Message(updatedTable, "#TableReservedSuccess"));
				} else {
					try {
						client.sendToClient(new Message(tableFromClient, "#ReserveTableFailed"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
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
			case "#FetchReports":
				try (Session session = getSessionFactory().openSession()) {
					List<Orders> deliveryOrders = session.createQuery(
							"FROM Orders WHERE orderType = 'Delivery'", Orders.class).getResultList();
					System.out.println("📤 Sending Orders: " + deliveryOrders.size());

					List<Reservation> allReservations = session.createQuery(
							"FROM Reservation", Reservation.class).getResultList();

					System.out.println("📤 Sending Reservations: " + allReservations.size());

					List<ContactRequest> allComplaints = session.createQuery(
							"FROM ContactRequest", ContactRequest.class).getResultList();
					System.out.println("📤 Sending Complaints: " + allComplaints.size());



					client.sendToClient(new Message(deliveryOrders, "#OrdersReport"));
					client.sendToClient(new Message(allReservations, "#ReservationsReport"));
					client.sendToClient(new Message(allComplaints, "#ComplaintsReport"));

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case "#Update Complaint":
				ContactRequest newComplaint = (ContactRequest) message.getObject();
				try (Session session = getSessionFactory().openSession()) {
					Transaction tx = session.beginTransaction();
					session.save(newComplaint);
					tx.commit();
					client.sendToClient(new Message(null, "#ComplaintSubmissionSuccess"));
					sendToAllClients(new Message( "#Update Complaints"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "#FetchComplaints":
				try (Session session = getSessionFactory().openSession()) {
					List<ContactRequest> unresolvedComplaints = session.createQuery(
							"FROM ContactRequest WHERE handled = false", ContactRequest.class).getResultList();

					List<ContactRequest> resolvedComplaints = session.createQuery(
							"FROM ContactRequest WHERE handled = true", ContactRequest.class).getResultList();

					client.sendToClient(new Message(unresolvedComplaints, "#ComplaintList"));
					client.sendToClient(new Message(resolvedComplaints, "#ResolvedComplaintList"));


					System.out.println("📤 Sent unresolved and resolved complaints to client.");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case "#ResolveComplaint":
				ContactRequest resolvedComplaint = (ContactRequest) message.getObject();
				try (Session session = getSessionFactory().openSession()) {
					Transaction tx = session.beginTransaction();

					ContactRequest complaintFromDB = session.get(ContactRequest.class, resolvedComplaint.getId());
					if (complaintFromDB != null) {
						complaintFromDB.setHandled(true);
						complaintFromDB.setResolutionScript(resolvedComplaint.getResolutionScript());
						complaintFromDB.setRefundIssued(resolvedComplaint.isRefundIssued());
						complaintFromDB.setRefundAmount(resolvedComplaint.getRefundAmount()); // ✅ Store refund amount
						complaintFromDB.setHandledAt(LocalDateTime.now());

						session.update(complaintFromDB);
						tx.commit();

						// Send updated complaints to clients
						List<ContactRequest> updatedUnresolved = session.createQuery(
								"FROM ContactRequest WHERE handled = false", ContactRequest.class).getResultList();
						List<ContactRequest> updatedResolved = session.createQuery(
								"FROM ContactRequest WHERE handled = true", ContactRequest.class).getResultList();

						sendToAllClients(new Message(updatedUnresolved, "#ComplaintList"));
						sendToAllClients(new Message(updatedResolved, "#ResolvedComplaintList"));
						ComplaintHandler.sendResolutionEmail(complaintFromDB);
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
