package br.skylight.commons.dli.subsystemstatus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class SubsystemStatusRequest extends Message<SubsystemStatusRequest> {

	@MessageField(number=4)
	public Subsystem subsystem;//u4

	public void setSubsystem(Subsystem subsystem) {
		this.subsystem = subsystem;
	}
	public Subsystem getSubsystem() {
		return subsystem;
	}
	public MessageType getMessageType() {
		return MessageType.M1000;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		subsystem = Subsystem.values()[(int)readUnsignedInt(in)];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt((int)subsystem.ordinal());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (subsystem.ordinal() ^ (subsystem.ordinal() >>> 32));
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
		SubsystemStatusRequest other = (SubsystemStatusRequest) obj;
		if (subsystem != other.subsystem)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		subsystem = Subsystem.values()[0];
	}

}
