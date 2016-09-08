package br.skylight.commons.dli.datalink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class DataLinkSetupMessage extends Message<DataLinkSetupMessage> {

	@MessageField(number=4)
	public int dataLinkId;
	@MessageField(number=5)
	public DataTerminalType addressedTerminal = DataTerminalType.GDT;//u1
	@MessageField(number=6)
	public int selectChannel = 0;//u2
	@MessageField(number=7)
	public int selectPrimaryHopPattern = 0;//u1
	@MessageField(number=8)
	public float selectForwardLinkCarrierFreq = 0;
	@MessageField(number=9)
	public float selectReturnLinkCarrierFreq = 0;
	@MessageField(number=10)
	public int setPnCode = 0;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M400;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		dataLinkId = in.readInt();
		addressedTerminal = DataTerminalType.values()[in.readUnsignedByte()];
		selectChannel = in.readUnsignedShort();
		selectPrimaryHopPattern = in.readUnsignedByte();
		selectForwardLinkCarrierFreq = in.readFloat();
		selectReturnLinkCarrierFreq = in.readFloat();
		setPnCode = in.readUnsignedByte();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(dataLinkId);
		out.writeByte(addressedTerminal.ordinal());
		out.writeShort(selectChannel);
		out.writeByte(selectPrimaryHopPattern);
		out.writeFloat(selectForwardLinkCarrierFreq);
		out.writeFloat(selectReturnLinkCarrierFreq);
		out.writeByte(setPnCode);
	}

	@Override
	public void resetValues() {
		dataLinkId = 0;
		addressedTerminal = DataTerminalType.GDT;
		selectChannel = 0;
		selectPrimaryHopPattern = 0;
		selectForwardLinkCarrierFreq = 0;
		selectReturnLinkCarrierFreq = 0;
		setPnCode = 0;
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
		DataLinkSetupMessage other = (DataLinkSetupMessage) obj;
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

	public int getSelectChannel() {
		return selectChannel;
	}

	public void setSelectChannel(int selectChannel) {
		this.selectChannel = selectChannel;
	}

	public int getSelectPrimaryHopPattern() {
		return selectPrimaryHopPattern;
	}

	public void setSelectPrimaryHopPattern(int selectPrimaryHopPattern) {
		this.selectPrimaryHopPattern = selectPrimaryHopPattern;
	}

	public float getSelectForwardLinkCarrierFreq() {
		return selectForwardLinkCarrierFreq;
	}

	public void setSelectForwardLinkCarrierFreq(float selectForwardLinkCarrierFreq) {
		this.selectForwardLinkCarrierFreq = selectForwardLinkCarrierFreq;
	}

	public float getSelectReturnLinkCarrierFreq() {
		return selectReturnLinkCarrierFreq;
	}

	public void setSelectReturnLinkCarrierFreq(float selectReturnLinkCarrierFreq) {
		this.selectReturnLinkCarrierFreq = selectReturnLinkCarrierFreq;
	}

	public int getSetPnCode() {
		return setPnCode;
	}

	public void setSetPnCode(int setPnCode) {
		this.setPnCode = setPnCode;
	}
	
}
