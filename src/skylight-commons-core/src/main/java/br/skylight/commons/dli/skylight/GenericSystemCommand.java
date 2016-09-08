package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class GenericSystemCommand extends Message<GenericSystemCommand> {

	private CommandType commandType;
	private double commandValue1;
	private double commandValue2;
	
	public GenericSystemCommand() {
		resetValues();
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2017;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		commandType = CommandType.values()[in.readUnsignedByte()];
		commandValue1 = in.readDouble();
		commandValue2 = in.readDouble();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(commandType.ordinal());
		out.writeDouble(commandValue1);
		out.writeDouble(commandValue2);
	}

	@Override
	public void resetValues() {
		commandType = CommandType.NONE;
		commandValue1 = 0;
		commandValue2 = 0;
	}

	public CommandType getCommandType() {
		return commandType;
	}

	public void setCommandType(CommandType commandType) {
		this.commandType = commandType;
	}

	public double getCommandValue1() {
		return commandValue1;
	}

	public void setCommandValue1(double commandValue1) {
		this.commandValue1 = commandValue1;
	}

	public double getCommandValue2() {
		return commandValue2;
	}

	public void setCommandValue2(double commandValue2) {
		this.commandValue2 = commandValue2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + commandType.ordinal();
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
		GenericSystemCommand other = (GenericSystemCommand) obj;
		if (commandType != other.commandType)
			return false;
		return true;
	}
	
}
