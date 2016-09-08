package br.skylight.commons.dli.datalink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.enums.AntennaMode;
import br.skylight.commons.dli.enums.CommunicationSecurityMode;
import br.skylight.commons.dli.enums.DataLinkState;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.enums.LinkChannelPriorityState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class DataLinkControlCommand extends Message<DataLinkControlCommand> {

	@MessageField(number=4)
	public int dataLinkId;
	@MessageField(number=5)
	public DataTerminalType addressedTerminal = DataTerminalType.GDT;//u1
	@MessageField(number=6)
	public DataLinkState setDataLinkState = DataLinkState.OFF;//u1
	@MessageField(number=7)
	public AntennaMode setAntennaMode = AntennaMode.AUTO;//u1
	@MessageField(number=8)
	public CommunicationSecurityMode communicationSecurityMode = CommunicationSecurityMode.NORMAL;//u1
	@MessageField(number=9)
	public LinkChannelPriorityState linkChannelPriority = LinkChannelPriorityState.PRIMARY;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M401;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		dataLinkId = in.readInt();
		addressedTerminal = DataTerminalType.values()[in.readUnsignedByte()];
		setDataLinkState = DataLinkState.values()[in.readUnsignedByte()];
		setAntennaMode = AntennaMode.values()[in.readUnsignedByte()];
		communicationSecurityMode = CommunicationSecurityMode.values()[in.readUnsignedByte()];
		linkChannelPriority = LinkChannelPriorityState.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(dataLinkId);
		out.writeByte(addressedTerminal.ordinal());
		out.writeByte(setDataLinkState.ordinal());
		out.writeByte(setAntennaMode.ordinal());
		out.writeByte(communicationSecurityMode.ordinal());
		out.writeByte(linkChannelPriority.ordinal());
	}

	@Override
	public void resetValues() {
		dataLinkId = 0;
		addressedTerminal = DataTerminalType.GDT;
		setDataLinkState = DataLinkState.OFF;
		setAntennaMode = AntennaMode.OMNI;
		communicationSecurityMode = CommunicationSecurityMode.NORMAL;
		linkChannelPriority = LinkChannelPriorityState.PRIMARY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((addressedTerminal == null) ? 0 : addressedTerminal.hashCode());
		result = prime * result + dataLinkId;
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
		DataLinkControlCommand other = (DataLinkControlCommand) obj;
		if (addressedTerminal == null) {
			if (other.addressedTerminal != null)
				return false;
		} else if (!addressedTerminal.equals(other.addressedTerminal))
			return false;
		if (dataLinkId != other.dataLinkId)
			return false;
		return true;
	}

	public int getDataLinkId() {
		return dataLinkId;
	}

	public void setDataLinkId(int dataLinkId) {
		this.dataLinkId = dataLinkId;
	}

	public DataTerminalType getAddressedTerminal() {
		return addressedTerminal;
	}

	public void setAddressedTerminal(DataTerminalType addressedTerminal) {
		this.addressedTerminal = addressedTerminal;
	}

	public DataLinkState getSetDataLinkState() {
		return setDataLinkState;
	}

	public void setSetDataLinkState(DataLinkState setDataLinkState) {
		this.setDataLinkState = setDataLinkState;
	}

	public AntennaMode getSetAntennaMode() {
		return setAntennaMode;
	}

	public void setSetAntennaMode(AntennaMode setAntennaMode) {
		this.setAntennaMode = setAntennaMode;
	}

	public CommunicationSecurityMode getCommunicationSecurityMode() {
		return communicationSecurityMode;
	}

	public void setCommunicationSecurityMode(CommunicationSecurityMode communicationSecurityMode) {
		this.communicationSecurityMode = communicationSecurityMode;
	}

	public LinkChannelPriorityState getLinkChannelPriority() {
		return linkChannelPriority;
	}

	public void setLinkChannelPriority(LinkChannelPriorityState linkChannelPriority) {
		this.linkChannelPriority = linkChannelPriority;
	}
	
}
