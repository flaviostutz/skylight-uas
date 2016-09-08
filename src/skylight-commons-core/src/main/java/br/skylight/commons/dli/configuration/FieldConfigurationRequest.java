package br.skylight.commons.dli.configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.RequestType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class FieldConfigurationRequest extends Message<FieldConfigurationRequest> {

	private int vsmID;
	private int dataLinkID;
	private RequestType requestType;//u1
	private long requestedMessage;//u4
	private int requestedField;//u1
	private long stationNumber;//u4
	private int sensorSelect;//u1
	
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

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public long getRequestedMessage() {
		return requestedMessage;
	}

	public void setRequestedMessage(long requestedMessage) {
		this.requestedMessage = requestedMessage;
	}

	public int getRequestedField() {
		return requestedField;
	}

	public void setRequestedField(int requestedField) {
		this.requestedField = requestedField;
	}

	public long getStationNumber() {
		return stationNumber;
	}

	public void setStationNumber(long stationNumber) {
		this.stationNumber = stationNumber;
	}

	public int getSensorSelect() {
		return sensorSelect;
	}

	public void setSensorSelect(int sensorSelect) {
		this.sensorSelect = sensorSelect;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1200;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		requestType = RequestType.values()[in.readUnsignedByte()];
		requestedMessage = readUnsignedInt(in);
		requestedField = in.readUnsignedByte();
		stationNumber = readUnsignedInt(in);
		sensorSelect = in.readUnsignedByte();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeByte(requestType.ordinal());
		out.writeInt((int)requestedMessage);
		out.writeByte(requestedField);
		out.writeInt((int)stationNumber);
		out.writeByte(sensorSelect);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dataLinkID;
		result = prime * result + ((requestType == null) ? 0 : requestType.hashCode());
		result = prime * result + requestedField;
		result = prime * result + (int) (requestedMessage ^ (requestedMessage >>> 32));
		result = prime * result + sensorSelect;
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
		FieldConfigurationRequest other = (FieldConfigurationRequest) obj;
		if (dataLinkID != other.dataLinkID)
			return false;
		if (requestType == null) {
			if (other.requestType != null)
				return false;
		} else if (!requestType.equals(other.requestType))
			return false;
		if (requestedField != other.requestedField)
			return false;
		if (requestedMessage != other.requestedMessage)
			return false;
		if (sensorSelect != other.sensorSelect)
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
		requestType = null;
		requestedMessage = 0;
		requestedField = 0;
		stationNumber = 0;
		sensorSelect = 0;
	}
	
}
