package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;

import java.util.ArrayList;

public class SimpleClient extends AbstractClient {
	
	private static SimpleClient client = null;

	private SimpleClient(String host, int port) {
		super(host, port);
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
				EventBus.getDefault().post(new Message(null, "#ShowLoginError: Email not found."));
				break;

			case "#IncorrectPassword":
				System.out.println("Login failed: Incorrect password.");
				EventBus.getDefault().post(new Message(null, "#ShowLoginError: Incorrect password."));
				break;

			case "#AlreadyLoggedIn":
				EventBus.getDefault().post(new Message(null, "#AlreadyLoggedIn"));
				break;


			default:
				System.out.println("Unknown message received: " + message.toString());
				break;
		}
	}


	public static User getUser() {
		return user;
	}
	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

}
