package br.skylight.commons.dli.configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class FieldConfigurationEnumeratedResponse extends Message<FieldConfigurationEnumeratedResponse> {

	private int vsmID;
	private int dataLinkID;
	private long stationNumber;//u4
	private long requestedMessage;//u4
	private int requestedField;//u1
	private int fieldSupported;//u1
	private int enumerationCount;//u1
	private int enumerationIndex;//u1
	private String enumerationText;//c16
	private String helpText;//c80

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

	public int getFieldSupported() {
		return fieldSupported;
	}

	public void setFieldSupported(int fieldSupported) {
		this.fieldSupported = fieldSupported;
	}

	public int getEnumerationCount() {
		return enumerationCount;
	}

	public void setEnumerationCount(int enumerationCount) {
		this.enumerationCount = enumerationCount;
	}

	public int getEnumerationIndex() {
		return enumerationIndex;
	}

	public void setEnumerationIndex(int enumerationIndex) {
		this.enumerationIndex = enumerationIndex;
	}

	public String getEnumerationText() {
		return enumerationText;
	}

	public void setEnumerationText(String enumerationText) {
		this.enumerationText = enumerationText;
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1302;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		stationNumber = readUnsignedInt(in);
		requestedMessage = readUnsignedInt(in);
		requestedField = in.readUnsignedByte();
		fieldSupported = in.readUnsignedByte();
		enumerationCount = in.readUnsignedByte();
		enumerationIndex = in.readUnsignedByte();
		enumerationText = readNullTerminatedString(in);
		helpText = readNullTerminatedString(in);
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeInt((int)stationNumber);
		out.writeInt((int)requestedMessage);
		out.writeByte(requestedField);
		out.writeByte(fieldSupported);
		out.writeByte(enumerationCount);
		out.writeByte(enumerationIndex);
		writeNullTerminatedString(out, enumerationText);
		writeNullTerminatedString(out, helpText);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dataLinkID;
		result = prime * result + requestedField;
		result = prime * result + (int) (requestedMessage ^ (requestedMessage >>> 32));
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
		FieldConfigurationEnumeratedResponse other = (FieldConfigurationEnumeratedResponse) obj;
		if (dataLinkID != other.dataLinkID)
			return false;
		if (requestedField != other.requestedField)
			return false;
		if (requestedMessage != other.requestedMessage)
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
		requestedMessage = 0;
		requestedField = 0;
		fieldSupported = 0;
		enumerationCount = 0;
		enumerationIndex = 0;
		enumerationText = "";
		helpText = "";
	}

	
}
