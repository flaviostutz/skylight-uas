package br.skylight.uav.plugins.payload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.payload.MessageTargetedToStation;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.services.ScheduledMessageReporter;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.messaging.MessageScheduler;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=PayloadService.class)
public class PayloadService extends ThreadWorker implements MessageListener, ScheduledMessageReporter {

	private static final Logger logger = Logger.getLogger(PayloadService.class.getName());
	private Map<Integer,PayloadOperatorExtensionPoint> operators = new HashMap<Integer,PayloadOperatorExtensionPoint>();
	private BitmappedStation availableStations = new BitmappedStation();

	public PayloadService() {
		super(15);
	}
	
	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public MessageScheduler messageScheduler;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	@ExtensionPointsInjection
	public List<PayloadOperatorExtensionPoint> payloadOperators;
	
	@Override
	public void onActivate() {
		//route messages related to payload
		messagingService.setMessageListener(MessageType.M200, this);
		messagingService.setMessageListener(MessageType.M201, this);
		messagingService.setMessageListener(MessageType.M206, this);
		
		messageScheduler.setMessageReporter(MessageType.M300, this);
		messageScheduler.setMessageReporter(MessageType.M301, this);
		messageScheduler.setMessageReporter(MessageType.M302, this);
		messageScheduler.setMessageReporter(MessageType.M308, this);
		
		pluginManager.executeAfterStartup(new Runnable() {
			public void run() {
				for (PayloadOperatorExtensionPoint pep : payloadOperators) {
					operators.put(pep.getPayload().getUniqueStationNumber(), pep);
					availableStations.addStation(pep.getPayload().getUniqueStationNumber());
				}
			}			
		});
	}

	@Override
	public void onMessageReceived(Message message) {
		if(message instanceof MessageTargetedToStation) {
			MessageTargetedToStation m = (MessageTargetedToStation)message;
			for (int sn : m.getTargetStations().getStations()) {
				sendMessageToPayloadOperator(sn, message);
			}
		} else {
			logger.info("Ignoring unsupported message type for payload operators. type=" + message.getMessageType());
		}
	}

	public void sendMessageToPayloadOperator(int stationNumber, Message message) {
		PayloadOperatorExtensionPoint po = operators.get(stationNumber);
		if(po!=null) {
			po.onPayloadMessageReceived(message);
		} else {
			logger.info("Ignoring message for an unconfigured station number. station=" + stationNumber);
		}
	}

	@Override
	public boolean prepareScheduledMessage(Message message) {
		//ask scheduled message for all payload operators. each one can send one message instance or return false if to indicate that it won't send any message
		for (PayloadOperatorExtensionPoint po : operators.values()) {
			Message m = messagingService.resolveMessageForSending(message.getMessageType().getImplementation());
			if(po.prepareScheduledPayloadMessage(m)) {
				//prepare targeted stations *later* because may be the payload operator changes this during preparation
				if(m instanceof MessageTargetedToStation) {
					MessageTargetedToStation tm = (MessageTargetedToStation)m;
					tm.getTargetStations().setUniqueStationNumber(po.getPayload().getUniqueStationNumber());
				}
				messagingService.sendMessage(m);
			} else {
				messagingService.returnUnsentMessageToPool(m);
			}
		}
		return false;
	}

	@Override
	public void step() throws Exception {
		for (PayloadOperatorExtensionPoint pep : payloadOperators) {
			pep.step();
		}
	}
	
	public BitmappedStation getAvailablePayloadStations() {
		return availableStations;
	}
	
}
