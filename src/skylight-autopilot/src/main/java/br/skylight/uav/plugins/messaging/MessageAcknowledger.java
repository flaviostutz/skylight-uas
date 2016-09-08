package br.skylight.uav.plugins.messaging;

import java.util.logging.Logger;

import br.skylight.commons.dli.messagetypes.MessageAcknowledgeConfiguration;
import br.skylight.commons.dli.messagetypes.MessageAcknowledgement;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.storage.RepositoryService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=MessageAcknowledger.class)
public class MessageAcknowledger extends Worker implements MessageListener {

	private static final Logger logger = Logger.getLogger(MessageAcknowledger.class.getName());

	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public RepositoryService repositoryService;
	
	public MessageAcknowledger() {
	}

	@Override
	public void onActivate() throws Exception {
		messagingService.setMessageListener(MessageType.M1401, this);
		messagingService.setMessageListener(new MessageListener() {
			@Override
			public void onMessageReceived(Message message) {
				//ACKNOWLEDGE RECEIVED MESSAGE
				if(repositoryService.getMessagingConfiguration().getMessagesForAcknowledgement().contains(message.getMessageType().getNumber())) {
					MessageAcknowledgement m = messagingService.resolveMessageForSending(MessageAcknowledgement.class);
					m.setOriginalMessageInstanceID(message.getMessageInstanceId());
					m.setOriginalMessageTimestamp(message.getTimeStamp());
					m.setOriginalMessageType(message.getMessageType().getNumber());
					messagingService.sendMessage(m);
				}
			}
		});
	}

	@Override
	public void onMessageReceived(Message message) {
		//M1401 Message Acknowledge Configuration
		if(message instanceof MessageAcknowledgeConfiguration) {
			MessageAcknowledgeConfiguration m = (MessageAcknowledgeConfiguration)message;
			if(!repositoryService.getMessagingConfiguration().getMessagesForAcknowledgement().contains(m.getAcknowledgeMessageType().getNumber())) {
				repositoryService.getMessagingConfiguration().getMessagesForAcknowledgement().add(m.getAcknowledgeMessageType().getNumber());
			}
			repositoryService.setMessagingConfiguration(repositoryService.getMessagingConfiguration());
		
		} else {
			logger.warning("Unsupported message: " + message.getMessageType());
		}
	}
	
}
