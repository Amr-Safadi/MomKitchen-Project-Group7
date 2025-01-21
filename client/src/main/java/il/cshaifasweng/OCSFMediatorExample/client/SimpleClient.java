package il.cshaifasweng.OCSFMediatorExample.client;

import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;

import java.util.ArrayList;

public class SimpleClient extends AbstractClient {
	
	private static SimpleClient client = null;

	private SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {

		//sending meals to listview
		if (msg.toString().equals("#Initialize Meals")) {
			System.out.println("Received ArrayList message");
			EventBus.getDefault().post(msg);
		}

	}
	
	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
			//client = new SimpleClient("192.168.137.1", 3000);
		}
		return client;
	}

}
