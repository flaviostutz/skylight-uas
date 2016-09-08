package br.skylight.uav.plugins.messaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.ScheduledMessage;
import br.skylight.commons.dli.messagetypes.GenericInformationRequestMessage;
import br.skylight.commons.dli.messagetypes.ScheduleMessageUpdateCommand;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.services.ScheduledMessageReporter;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.storage.RepositoryService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=MessageScheduler.class)
public class MessageScheduler extends ThreadWorker implements MessageListener {

	private static final Logger logger = Logger.getLogger(MessageScheduler.class.getName());

	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public RepositoryService repositoryService;
	
	@ServiceInjection
	public PluginManager pluginManager;

	private Map<MessageType,ScheduledMessageReporter> messageReporters = new HashMap<MessageType,ScheduledMessageReporter>();
	
	public MessageScheduler() {
		super(60);
	}

	@Override
	public void onActivate() throws Exception {
		messagingService.setMessageListener(MessageType.M1403, this);
		messagingService.setMessageListener(MessageType.M1402, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		//M1402 find current message schedule and update its frequency
		if(message instanceof ScheduleMessageUpdateCommand) {
			ScheduleMessageUpdateCommand m = (ScheduleMessageUpdateCommand)message;
			boolean found = false;
			for (ScheduledMessage sm : repositoryService.getMessagingConfiguration().getScheduledMessages()) {
				if(sm.getMessageType().equals(m.getRequestedMessageType())) {
					sm.setFrequency(m.getFrequency());
					found = true;
				}
			}
			if(!found) {
				ScheduledMessage sm = new ScheduledMessage(m.getRequestedMessageType());
				sm.setFrequency(m.getFrequency());
				repositoryService.getMessagingConfiguration().getScheduledMessages().add(sm);
			}
			repositoryService.setMessagingConfiguration(repositoryService.getMessagingConfiguration());
			
		//M1403 - generic information request
		} else if(message instanceof GenericInformationRequestMessage) {
			GenericInformationRequestMessage m = (GenericInformationRequestMessage)message;
			logger.finer("Message "+ m.getRequestedMessageType() +" was requested by cucs");
			try {
				sendScheduledMessage(m.getRequestedMessageType());
			} catch (IOException e) {
				logger.throwing(null, null, e);
				e.printStackTrace();
			}
			
		} else {
			logger.warning("Unsupported message: " + message.getMessageType());
		}
	}
	
	@Override
	public void step() throws Exception {
		for (ScheduledMessage sm : repositoryService.getMessagingConfiguration().getScheduledMessages()) {
			if(sm.checkTimeout()) {
				try {
					sendScheduledMessage(sm.getMessageType());
				} catch (Exception e) {
					logger.warning("Problem generating scheduled message " + sm.getMessageType() + ". e=" + e.toString());
					logger.throwing(null, null, e);
					e.printStackTrace();
					if(pluginManager.isPluginsStarted()) {//avoid removing unregistered reporters during plugins startup
						logger.warning("Disabling schedule for " + sm.getMessageType() + ". e=" + e.toString());
						sm.setFrequency(0);
					}
				}
			}
		}
	}

	public void sendScheduledMessage(MessageType messageType) throws IOException {
		ScheduledMessageReporter mr = messageReporters.get(messageType);
		if(mr!=null) {
//			logger.info("Sending scheduled message " + messageType);
			Message m = messagingService.resolveMessageForSending(messageType.getImplementation());
			if(mr.prepareScheduledMessage(m)) {
				messagingService.sendMessage(m);
			}
		} else {
			throw new IOException("Message " + messageType + " won't be sent because there is no message reporter set");
		}
	}
	
	public void setMessageReporter(MessageType messageType, ScheduledMessageReporter messageReporter) {
		ScheduledMessageReporter mr = messageReporters.get(messageType);
		if(mr!=null) {
			logger.warning("'messageReporter' is already defined for this message type and will be replaced. type=" + this + "; existingMessageListener=" + mr.getClass().getName());
		}
		logger.finest("Setting " + messageReporter.getClass().getName() + " as listener for message type " + this);
		this.messageReporters.put(messageType, messageReporter);
	}
	
}
