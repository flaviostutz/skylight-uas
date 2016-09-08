package br.skylight.cucs.plugins.subscriber;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;

public class MessageRunner implements Runnable {

	private Message message;
	private MessageListener messageListener;
	
	@Override
	public void run() {
		messageListener.onMessageReceived(message);
	}
	
	public void setMessage(Message message) {
		this.message = message;
	}
	
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

}
