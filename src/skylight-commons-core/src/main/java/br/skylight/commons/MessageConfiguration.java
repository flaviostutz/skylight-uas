package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.messagetypes.MessageAcknowledgeConfiguration;
import br.skylight.commons.dli.messagetypes.ScheduleMessageUpdateCommand;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.infra.SerializableState;

public class MessageConfiguration implements SerializableState {

	private MessageType messageType;
	private float scheduledFrequency;
	private boolean acknowledgeReceipt;
	private boolean requestOnConnect;

	public MessageConfiguration() {
	}
	
	public MessageConfiguration(MessageType messageType) {
		this.messageType = messageType;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		messageType = MessageType.values()[in.readInt()];
		scheduledFrequency = in.readFloat();
		acknowledgeReceipt = in.readBoolean();
		requestOnConnect = in.readBoolean();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeInt(messageType.ordinal());
		out.writeFloat(scheduledFrequency);
		out.writeBoolean(acknowledgeReceipt);
		out.writeBoolean(requestOnConnect);
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public float getScheduledFrequency() {
		return scheduledFrequency;
	}

	public void setScheduledFrequency(float scheduledFrequency) {
		this.scheduledFrequency = scheduledFrequency;
	}

	public boolean isAcknowledgeReceipt() {
		return acknowledgeReceipt;
	}

	public void setAcknowledgeReceipt(boolean acknowledgeReceipt) {
		this.acknowledgeReceipt = acknowledgeReceipt;
	}

	public boolean isRequestOnConnect() {
		return requestOnConnect;
	}

	public void setRequestOnConnect(boolean requestOnConnect) {
		this.requestOnConnect = requestOnConnect;
	}

	public void sendConfigurationToVehicle(int vehicleId, MessagingService messagingService) {
		//SCHEDULED UPDATE FREQUENCY
//		System.out.println("Sending update config for " + getMessageType());
		ScheduleMessageUpdateCommand mu = messagingService.resolveMessageForSending(ScheduleMessageUpdateCommand.class);
		mu.setRequestedMessageType(getMessageType());
		mu.setVehicleID(vehicleId);
		mu.setFrequency(getScheduledFrequency());
		messagingService.sendMessage(mu);

		//ACKNOWLEDGE RECEIPT OPTION
		if(isAcknowledgeReceipt()) {
//			System.out.println("Sending acknowledge option for " + getMessageType());
			MessageAcknowledgeConfiguration ac = messagingService.resolveMessageForSending(MessageAcknowledgeConfiguration.class);
			ac.setAcknowledgeMessageType(getMessageType());
			ac.setVehicleID(vehicleId);
			messagingService.sendMessage(ac);
		} else {
			//TODO send cancel configuration message
		}
	}
	
}
