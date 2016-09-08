package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.EngineStatus;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class EngineCommand extends Message<EngineCommand> {

	private int engineNumber;
	private EngineStatus engineCommand;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M45;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		engineNumber = in.readInt();
		engineCommand = EngineStatus.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(engineNumber);
		out.writeByte(engineCommand.ordinal());
	}

	@Override
	public void resetValues() {
		engineNumber = 0;
		engineCommand = EngineStatus.values()[0];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + engineNumber;
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
		EngineCommand other = (EngineCommand) obj;
		if (engineNumber != other.engineNumber)
			return false;
		return true;
	}

	public int getEngineNumber() {
		return engineNumber;
	}

	public void setEngineNumber(int engineNumber) {
		this.engineNumber = engineNumber;
	}
	
	public EngineStatus getEngineCommand() {
		return engineCommand;
	}
	public void setEngineCommand(EngineStatus engineCommand) {
		this.engineCommand = engineCommand;
	}

}
