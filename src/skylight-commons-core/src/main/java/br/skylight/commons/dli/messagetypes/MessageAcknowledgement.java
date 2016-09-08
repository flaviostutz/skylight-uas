package br.skylight.commons.dli.messagetypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class MessageAcknowledgement extends Message<MessageAcknowledgement> {

	private int vsmID;
	private int dataLinkID;
	private double originalMessageTimestamp;
	private long originalMessageInstanceID;//u4
	private long originalMessageType;//u4
	
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

	public double getOriginalMessageTimestamp() {
		return originalMessageTimestamp;
	}

	public void setOriginalMessageTimestamp(double originalMessageTimestamp) {
		this.originalMessageTimestamp = originalMessageTimestamp;
	}

	public long getOriginalMessageInstanceID() {
		return originalMessageInstanceID;
	}

	public void setOriginalMessageInstanceID(long originalMessageInstanceID) {
		this.originalMessageInstanceID = originalMessageInstanceID;
	}

	public long getOriginalMessageType() {
		return originalMessageType;
	}

	public void setOriginalMessageType(long originalMessageType) {
		this.originalMessageType = originalMessageType;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1400;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		originalMessageTimestamp = in.readDouble();
		originalMessageInstanceID = readUnsignedInt(in);
		originalMessageType = readUnsignedInt(in);
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeDouble(originalMessageTimestamp);
		out.writeInt((int)originalMessageInstanceID);
		out.writeInt((int)originalMessageType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dataLinkID;
		result = prime * result + (int) (originalMessageInstanceID ^ (originalMessageInstanceID >>> 32));
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
		MessageAcknowledgement other = (MessageAcknowledgement) obj;
		if (dataLinkID != other.dataLinkID)
			return false;
		if (originalMessageInstanceID != other.originalMessageInstanceID)
			return false;
		if (vsmID != other.vsmID)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		vsmID = 0;
		dataLinkID = 0;
		originalMessageTimestamp = 0;
		originalMessageInstanceID = 0;
		originalMessageType = 0;
	}

}
