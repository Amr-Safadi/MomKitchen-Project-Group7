package il.cshaifasweng.OCSFMediatorExample.client.Network;

import il.cshaifasweng.OCSFMediatorExample.client.Services.SecondaryService;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.scene.image.Image;
import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SimpleClient extends AbstractClient {
	
	private static SimpleClient client = null;
	public static int PORT = 3000;
	public static String HOST = "localhost";

	private SimpleClient(String host, int port) {
		super(host, port);
		System.out.println(host + ":" + port);

	}

	private static User user = null ; //to detrmine which role the current user

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (!(msg instanceof Message)) {
			System.out.println("Received an invalid message type.");
			return;
		}

		Message message = (Message) msg;

		switch (message.toString()) {
			case String msgStr when msgStr.startsWith("#MealImageResponse:"):
				String mealName = msgStr.split(":")[1];  // Extract the meal name
				byte[] imageData = (byte[]) message.getObject();


				// Create the image
				Image receivedImage = new Image(new ByteArrayInputStream(imageData));

				// 🔔 Let the UI know (publish to EventBus)
				EventBus.getDefault().post(new Message(new Object[]{mealName, receivedImage}, "receivedImage"));
				break;


			case "#PriceChangeApproved", "#PriceChangeRejected", "#PriceChangeRequestSent", "#PriceChangeRequestFailed", "Meal Updated Successfully",
                 "#MealAddedSuccessfully", "#UserValidated", "#ValidationFailed", "OrderCanceled",
                 "#MealAdditionFailed", "#MealDeleted", "#MealDeletionFailed", "#Update Complaints",
                 "#ReservationSuccess", "#ManagerHasNotifications", "#PriceRequestsList", "#UserReservations",
				 "#CancelReservationSuccess", "#OrdersReport", "#ReservationsReport", "#ComplaintsReport", "#ReserveTableFailed" , "CancellationSuccessWithFee":
				EventBus.getDefault().post(message);
				break;
            case "#OrderPlacedSuccessfully":
				System.out.println("Order placed successfully!");
				EventBus.getDefault().post(new Message("#OrderSuccess"));
				break;

			case "#OrderPlacementFailed":
				System.out.println("Order placement failed!");
				EventBus.getDefault().post(new Message("#OrderFailed"));
				break;

			case "Category Fetched":
				System.out.println(" simple client to  category fetched - handle message");
				EventBus.getDefault().post(message);
				break;

			case "#Update All Meals":
				System.out.println(" simple client to update meal - handle message");
				EventBus.getDefault().post(message);
				break;
			case "#Initialize Meals":
				System.out.println("Received ArrayList of meals");
				EventBus.getDefault().post(msg);
				break;

			case "#LoginSuccess":
				user = (User) message.getObject();
				System.out.println("Login successful: " + user.getEmail() + " | Role: " + user.getRole());
				EventBus.getDefault().post(message);  // Notify UI about login success
				break;



			case "#EmailNotFound":
				System.out.println("Login failed: Email not found.");
				EventBus.getDefault().post(message);
				break;

			case "#IncorrectPassword":
				System.out.println("Login failed: Incorrect password.");
				EventBus.getDefault().post(message);
				break;

			case "#AlreadyLoggedIn":
				EventBus.getDefault().post(new Message(null, "#AlreadyLoggedIn"));
				break;
			case "#ComplaintSubmissionSuccess":
				System.out.println("Complaint successfully stored!");
				EventBus.getDefault().post(new Message(null, "#ComplaintSubmissionSuccess"));
				break;
			case "#ComplaintList":
				System.out.println(" Received complaint list from server. - simple client");
				EventBus.getDefault().post(message);
				break;
			case "#ComplaintResolved":
				System.out.println(" Complaint successfully resolved.");
				EventBus.getDefault().post(message);
				break;
			case "#ResolvedComplaintList":
				System.out.println(" Received resolved complaints from server. - simple client");
				EventBus.getDefault().post(message);
				break;
			case "#BranchFetched":
				System.out.println("Received branch fetched message");
				EventBus.getDefault().post(message);
				break;

			case "#NoAvailability":
				System.out.println("Received no availability message");
				EventBus.getDefault().post(message);
				break;

			case "#TableReservedSuccess":
			case "#TableReservationCanceledSuccess":
				System.out.println("Table Reservation Message");
				EventBus.getDefault().post(message);
				break;


            default:
				System.out.println("Simple Client - Unknown message received: " + message.toString());
				break;
		}
	}



	public static User getUser() {
		return user;
	}
	public static void setUser(User user) {
		SimpleClient.user = user;
	}

	public static void deleteClient() {
		client = null;
	}
	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

	public static SimpleClient getClient(String host, int port) {
		if (client == null) {
			client = new SimpleClient(host, port);
		}
		return client;
	}

}
