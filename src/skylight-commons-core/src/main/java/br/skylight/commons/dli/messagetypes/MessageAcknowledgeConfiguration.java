package br.skylight.commons.dli.messagetypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class MessageAcknowledgeConfiguration extends Message<MessageAcknowledgeConfiguration> {

	private int vsmID;
	private int dataLinkID;
	private MessageType acknowledgeMessageType;//u4
	
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

	public void setAcknowledgeMessageType(MessageType acknowledgeMessageType) {
		this.acknowledgeMessageType = acknowledgeMessageType;
	}
	public MessageType getAcknowledgeMessageType() {
		return acknowledgeMessageType;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1401;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		acknowledgeMessageType = MessageType.getMessageType(readUnsignedInt(in));
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeInt((int)acknowledgeMessageType.getNumber());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dataLinkID;
		result = prime * result + (int) (acknowledgeMessageType.getNumber() ^ (acknowledgeMessageType.getNumber() >>> 32));
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
		MessageAcknowledgeConfiguration other = (MessageAcknowledgeConfiguration) obj;
		if (dataLinkID != other.dataLinkID)
			return false;
		if (acknowledgeMessageType.getNumber() != other.acknowledgeMessageType.getNumber())
			return false;
		if (vsmID != other.vsmID)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		vsmID = 0;
		dataLinkID = 0;
		acknowledgeMessageType = MessageType.M1;
	}
	
}
