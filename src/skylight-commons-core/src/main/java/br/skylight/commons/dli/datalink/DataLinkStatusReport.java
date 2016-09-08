package br.skylight.commons.dli.datalink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.enums.AntennaState;
import br.skylight.commons.dli.enums.CommunicationSecurityState;
import br.skylight.commons.dli.enums.DataLinkState;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.enums.LinkChannelPriorityState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class DataLinkStatusReport extends Message<DataLinkStatusReport> {

	@MessageField(number=4)
	public int dataLinkId;
	@MessageField(number=5)
	public DataTerminalType addressedTerminal = DataTerminalType.GDT;//u1
	@MessageField(number=6)
	public DataLinkState dataLinkState = DataLinkState.OFF;//u1
	@MessageField(number=7)
	public AntennaState antennaState = AntennaState.OMNI;//u1
	@MessageField(number=8)
	public int reportedChannel = 0;//u2
	@MessageField(number=9)
	public int reportedPrimaryHopPattern = 0;//u1
	@MessageField(number=10)
	public float reportedForwardLinkCarrierFreq = 0;
	@MessageField(number=11)
	public float reportedReturnLinkCarrierFreq = 0;
	@MessageField(number=12)
	public short downlinkStatus = 0;//u2
	@MessageField(number=13)
	public CommunicationSecurityState communicationSecurityState = CommunicationSecurityState.NOT_INSTALLED;//u1
	@MessageField(number=14)
	public LinkChannelPriorityState linkChannelPriorityState = LinkChannelPriorityState.PRIMARY;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M501;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		dataLinkId = in.readInt();
		addressedTerminal = DataTerminalType.values()[in.readUnsignedByte()];
		dataLinkState = DataLinkState.values()[in.readUnsignedByte()];
		antennaState = AntennaState.values()[in.readUnsignedByte()];
		reportedChannel = in.readUnsignedShort();
		reportedPrimaryHopPattern = in.readUnsignedByte();
		reportedForwardLinkCarrierFreq = in.readFloat();
		reportedReturnLinkCarrierFreq = in.readFloat();
		downlinkStatus = in.readShort();
		communicationSecurityState = CommunicationSecurityState.values()[in.readUnsignedByte()];
		linkChannelPriorityState = LinkChannelPriorityState.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(dataLinkId);
		out.writeByte(addressedTerminal.ordinal());
		out.writeByte(dataLinkState.ordinal());
		out.writeByte(antennaState.ordinal());
		out.writeShort(reportedChannel);
		out.writeByte(reportedPrimaryHopPattern);
		out.writeFloat(reportedForwardLinkCarrierFreq);
		out.writeFloat(reportedReturnLinkCarrierFreq);
		out.writeShort(downlinkStatus);
		out.writeByte(communicationSecurityState.ordinal());
		out.writeByte(linkChannelPriorityState.ordinal());
	}

	@Override
	public void resetValues() {
		dataLinkId = 0;
		addressedTerminal = DataTerminalType.GDT;
		dataLinkState = DataLinkState.OFF;
		antennaState = AntennaState.OMNI;
		reportedChannel = 0;
		reportedPrimaryHopPattern = 0;
		reportedForwardLinkCarrierFreq = 0;
		reportedReturnLinkCarrierFreq = 0;
		downlinkStatus = 0;
		communicationSecurityState = CommunicationSecurityState.NOT_INSTALLED;
		linkChannelPriorityState = LinkChannelPriorityState.PRIMARY;
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
		DataLinkStatusReport other = (DataLinkStatusReport) obj;
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

	public DataLinkState getDataLinkState() {
		return dataLinkState;
	}

	public void setDataLinkState(DataLinkState dataLinkState) {
		this.dataLinkState = dataLinkState;
	}

	public AntennaState getAntennaState() {
		return antennaState;
	}

	public void setAntennaState(AntennaState antennaState) {
		this.antennaState = antennaState;
	}

	public int getReportedChannel() {
		return reportedChannel;
	}

	public void setReportedChannel(int reportedChannel) {
		this.reportedChannel = reportedChannel;
	}

	public int getReportedPrimaryHopPattern() {
		return reportedPrimaryHopPattern;
	}

	public void setReportedPrimaryHopPattern(int reportedPrimaryHopPattern) {
		this.reportedPrimaryHopPattern = reportedPrimaryHopPattern;
	}

	public float getReportedForwardLinkCarrierFreq() {
		return reportedForwardLinkCarrierFreq;
	}

	public void setReportedForwardLinkCarrierFreq(float reportedForwardLinkCarrierFreq) {
		this.reportedForwardLinkCarrierFreq = reportedForwardLinkCarrierFreq;
	}

	public float getReportedReturnLinkCarrierFreq() {
		return reportedReturnLinkCarrierFreq;
	}

	public void setReportedReturnLinkCarrierFreq(float reportedReturnLinkCarrierFreq) {
		this.reportedReturnLinkCarrierFreq = reportedReturnLinkCarrierFreq;
	}

	public short getDownlinkStatus() {
		return downlinkStatus;
	}

	public void setDownlinkStatus(short downlinkStatus) {
		this.downlinkStatus = downlinkStatus;
	}

	public CommunicationSecurityState getCommunicationSecurityState() {
		return communicationSecurityState;
	}

	public void setCommunicationSecurityState(CommunicationSecurityState communicationSecurityState) {
		this.communicationSecurityState = communicationSecurityState;
	}

	public LinkChannelPriorityState getLinkChannelPriorityState() {
		return linkChannelPriorityState;
	}

	public void setLinkChannelPriorityState(LinkChannelPriorityState linkChannelPriorityState) {
		this.linkChannelPriorityState = linkChannelPriorityState;
	}
	
}
