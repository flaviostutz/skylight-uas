package br.skylight.commons.dli.subsystemstatus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.enums.SubsystemState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class SubsystemStatusReport extends Message<SubsystemStatusReport> {

	@MessageField(number=4)
	public Subsystem subsystemID;//u1
	@MessageField(number=5)
	public SubsystemState subsystemState;//u1
	@MessageField(number=6)
	public int subsystemStateReportReference;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M1101;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		subsystemID = Subsystem.values()[in.readUnsignedByte()];
		subsystemState = SubsystemState.values()[in.readUnsignedByte()];
		subsystemStateReportReference = in.readInt();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(subsystemID.ordinal());
		out.writeByte(subsystemState.ordinal());
		out.writeInt(subsystemStateReportReference);
	}

	@Override
	public void resetValues() {
		subsystemID = null;
		subsystemState = SubsystemState.NO_STATUS;
		subsystemStateReportReference = 0;
	}

	public Subsystem getSubsystemID() {
		return subsystemID;
	}

	public void setSubsystemID(Subsystem subsystemID) {
		this.subsystemID = subsystemID;
	}

	public SubsystemState getSubsystemState() {
		return subsystemState;
	}

	public void setSubsystemState(SubsystemState subsystemState) {
		this.subsystemState = subsystemState;
	}

	public int getSubsystemStateReportReference() {
		return subsystemStateReportReference;
	}

	public void setSubsystemStateReportReference(int subsystemStateReportReference) {
		this.subsystemStateReportReference = subsystemStateReportReference;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((subsystemID == null) ? 0 : subsystemID.hashCode());
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
		SubsystemStatusReport other = (SubsystemStatusReport) obj;
		if (subsystemID == null) {
			if (other.subsystemID != null)
				return false;
		} else if (!subsystemID.equals(other.subsystemID))
			return false;
		return true;
	}

}
