package br.skylight.commons.dli.messagetypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class GenericInformationRequestMessage extends Message<GenericInformationRequestMessage> {

	private int vsmID;
	private int dataLinkID;
	private long stationNumber;//u4
	private MessageType requestedMessageType;//u4

	public int getVsmID() {
		return vsmID;
	}

	public void setVsmID(int vsmID) {
		this.vsmID = vsmID;
	}

	public int getDataLinkID() {
		return dataLinkID;
	}

	public void setDataLinkID(int dataLinkID) {
		this.dataLinkID = dataLinkID;
	}

	public long getStationNumber() {
		return stationNumber;
	}

	public void setStationNumber(long stationNumber) {
		this.stationNumber = stationNumber;
	}

	public void setRequestedMessageType(MessageType requestedMessageType) {
		this.requestedMessageType = requestedMessageType;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1403;
	}

	public MessageType getRequestedMessageType() {
		return requestedMessageType;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		stationNumber = readUnsignedInt(in);
		requestedMessageType = MessageType.values()[(int)readUnsignedInt(in)];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeInt((int)stationNumber);
		out.writeInt((int)requestedMessageType.ordinal());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dataLinkID;
		result = prime * result + (int) (requestedMessageType.ordinal() ^ (requestedMessageType.ordinal() >>> 32));
		result = prime * result + (int) (stationNumber ^ (stationNumber >>> 32));
		result = prime * result + vsmID;
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
		GenericInformationRequestMessage other = (GenericInformationRequestMessage) obj;
		if (dataLinkID != other.dataLinkID)
			return false;
		if (requestedMessageType != other.requestedMessageType)
			return false;
		if (stationNumber != other.stationNumber)
			return false;
		if (vsmID != other.vsmID)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		vsmID = 0;
		dataLinkID = 0;
		stationNumber = 0;
		requestedMessageType = MessageType.values()[0];
	}

}
