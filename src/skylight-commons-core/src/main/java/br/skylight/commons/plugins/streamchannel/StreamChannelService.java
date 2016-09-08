package br.skylight.commons.plugins.streamchannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.StreamChannelCommand;
import br.skylight.commons.dli.skylight.StreamChannelData;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=StreamChannelService.class)
public class StreamChannelService extends Worker implements MessageListener {

	private static final Logger logger = Logger.getLogger(StreamChannelService.class.getName());
	private Map<Integer,StreamChannelOperator> operators = new HashMap<Integer,StreamChannelOperator>();

	@ServiceInjection
	public MessagingService messagingService;
	
	@ExtensionPointsInjection
	public List<StreamChannelOperator> streamChannelOperators;
	
	@Override
	public void onActivate() {
		messagingService.setMessageListener(MessageType.M2014, this);
		messagingService.setMessageListener(MessageType.M2015, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		//M2014
		if(message instanceof StreamChannelData) {
			StreamChannelData m = (StreamChannelData)message;
			sendMessageToStreamChannelOperator(m.getChannelNumber(), m);
		//M2015
		} else if(message instanceof StreamChannelCommand) {
			StreamChannelCommand m = (StreamChannelCommand)message;
			sendMessageToStreamChannelOperator(m.getChannelNumber(), m);
		} else {
			logger.fine("Ignoring unknown message type for payload operators. type=" + message.getMessageType());
		}
	}
	
	private void sendMessageToStreamChannelOperator(int channelNumber, Message m) {
		StreamChannelOperator po = null;
		for (StreamChannelOperator so : streamChannelOperators) {
			if(so.getChannelNumber()==channelNumber) {
				po = so;
				break;
			}
		}
		if(po!=null) {
			po.onMessageReceived(m);
		} else {
			logger.fine("Ignoring message for an unconfigured stream channel. channelNumber=" + channelNumber);
		}
	}

	public void registerStreamChannelOperator(StreamChannelOperator streamChannelOperator) {
		operators.put(streamChannelOperator.getChannelNumber(), streamChannelOperator);
	}

	public void unregisterStreamChannelOperator(int channelNumber) {
		operators.remove(channelNumber);
	}
	
}
