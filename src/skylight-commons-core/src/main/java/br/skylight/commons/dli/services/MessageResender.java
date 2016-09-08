package br.skylight.commons.dli.services;

import br.skylight.commons.infra.ThreadWorker;

public class MessageResender extends ThreadWorker {

	private MessagingService messagingService;
	
	public MessageResender(MessagingService messagingService) {
		//TODO evaluate max frequency by tests (try to make this as low as possible)
		super(3);
		this.messagingService = messagingService;
	}

	public void startToResendUntilAcknowlegedByReceiver(Message message) {
		//configure the other part to ack this message type
		
		//resend configuration until getting ack for the ack configuration
		
	}
	
	protected void resendMessageUntilAcknowledgeReceived(Message message, long timeout) {
		
	}
	
	@Override
	public void step() throws Exception {
	}
	
}
