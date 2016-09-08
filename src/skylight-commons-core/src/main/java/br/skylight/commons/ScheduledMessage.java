package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.infra.SerializableState;

public class ScheduledMessage implements SerializableState {

	private MessageType messageType;
	private float frequency;
	
	private long lastTimeoutTime;
	private long timeBetweenSteps;

	public ScheduledMessage() {
	}
	
	public ScheduledMessage(MessageType messageType) {
		this.messageType = messageType;
	}
	
	/**
	 * Returns true if frequency timeout and resets current time
	 * @return
	 */
	public boolean checkTimeout() {
		if(frequency>0 && (System.currentTimeMillis()-lastTimeoutTime)>timeBetweenSteps) {
			lastTimeoutTime = System.currentTimeMillis();
			return true;
		}
		return false;
	}
	
	public void setFrequency(float frequency) {
		this.frequency = frequency;
		this.timeBetweenSteps = (long)((1F/frequency)*1000F);
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	public float getFrequency() {
		return frequency;
	}
	public MessageType getMessageType() {
		return messageType;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		messageType = MessageType.values()[in.readInt()];
		setFrequency(in.readFloat());
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeInt(messageType.ordinal());
		out.writeFloat(frequency);
	}
	
}
