package br.skylight.commons.dli.messagetypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class ScheduleMessageUpdateCommand extends Message<ScheduleMessageUpdateCommand> {

	private MessageType requestedMessageType;//u4
	private float frequency;
	
	public MessageType getRequestedMessageType() {
		return requestedMessageType;
	}

	public void setRequestedMessageType(MessageType requestedMessageType) {
		this.requestedMessageType = requestedMessageType;
	}

	public float getFrequency() {
		return frequency;
	}

	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1402;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		requestedMessageType = MessageType.getMessageType(readUnsignedInt(in));
		frequency = in.readFloat();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt((int)requestedMessageType.getNumber());
		out.writeFloat(frequency);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (requestedMessageType.getNumber() ^ (requestedMessageType.getNumber() >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScheduleMessageUpdateCommand other = (ScheduleMessageUpdateCommand) obj;
		if (requestedMessageType != other.requestedMessageType)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		requestedMessageType = MessageType.M1;
		frequency = 0;
	}
	
}
