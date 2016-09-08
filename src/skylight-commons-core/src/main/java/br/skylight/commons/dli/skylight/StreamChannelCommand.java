package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class StreamChannelCommand extends Message<StreamChannelCommand> {
	
	private int channelNumber;//u1
	private long commandNumber;
	private String commandText = "";
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2015;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		channelNumber = in.readUnsignedByte();
		commandNumber = in.readLong();
		commandText = in.readUTF();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(channelNumber);
		out.writeLong(commandNumber);
		out.writeUTF(commandText);
	}

	@Override
	public void resetValues() {
		channelNumber = 0;
		commandNumber = 0;
		commandText = "";
	}

	public int getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}

	public long getCommandNumber() {
		return commandNumber;
	}
	public String getCommandText() {
		return commandText;
	}
	public void setCommandNumber(long commandNumber) {
		this.commandNumber = commandNumber;
	}
	public void setCommandText(String commandText) {
		this.commandText = commandText;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + channelNumber;
		result = prime * result + (int) (commandNumber ^ (commandNumber >>> 32));
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
		StreamChannelCommand other = (StreamChannelCommand) obj;
		if (channelNumber != other.channelNumber)
			return false;
		if (commandNumber != other.commandNumber)
			return false;
		return true;
	}
	
}
