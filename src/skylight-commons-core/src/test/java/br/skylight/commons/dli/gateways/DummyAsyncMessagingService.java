package br.skylight.commons.dli.gateways;

import br.skylight.commons.dli.services.AsyncSenderMessagingService;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;

public class DummyAsyncMessagingService extends AsyncSenderMessagingService {

	private int receivedMessagesCount = 0;
	private Message lastReceivedMessage;

	public DummyAsyncMessagingService() {
		super.setName("Dummy");
		MessageListener ml = new MessageListener() {
			public void onMessageReceived(Message message) {
				receivedMessagesCount++;
				lastReceivedMessage = message;
				System.out.println("ON RECEIVED " + lastReceivedMessage);
			}
		};
		setMessageListener(ml);
	}

	public Message getLastReceivedMessage() {
		return lastReceivedMessage;
	}
	
	public int getReceivedMessagesCount() {
		return receivedMessagesCount;
	}
	
}
