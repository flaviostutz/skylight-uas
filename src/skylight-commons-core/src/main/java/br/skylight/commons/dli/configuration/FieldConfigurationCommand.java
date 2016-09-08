package br.skylight.commons.dli.configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.FieldAvailability;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class FieldConfigurationCommand extends Message<FieldConfigurationCommand> {

	private int vsmID;
	private int dataLinkID;
	private long stationNumber;//u4
	private long reportedMessage;//u4
	private int reportedField;//u1
	private FieldAvailability fieldAvailable;//u1
	private int reportedEnumeratedIndex;//u1
	private int enumeratedIndexEnable;
	
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

	public long getReportedMessage() {
		return reportedMessage;
	}

	public void setReportedMessage(long reportedMessage) {
		this.reportedMessage = reportedMessage;
	}

	public int getReportedField() {
		return reportedField;
	}

	public void setReportedField(int reportedField) {
		this.reportedField = reportedField;
	}

	public FieldAvailability getFieldAvailable() {
		return fieldAvailable;
	}

	public void setFieldAvailable(FieldAvailability fieldAvailable) {
		this.fieldAvailable = fieldAvailable;
	}

	public int getReportedEnumeratedIndex() {
		return reportedEnumeratedIndex;
	}

	public void setReportedEnumeratedIndex(int reportedEnumeratedIndex) {
		this.reportedEnumeratedIndex = reportedEnumeratedIndex;
	}

	public int getEnumeratedIndexEnable() {
		return enumeratedIndexEnable;
	}

	public void setEnumeratedIndexEnable(int enumeratedIndexEnable) {
		this.enumeratedIndexEnable = enumeratedIndexEnable;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1303;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		stationNumber = readUnsignedInt(in);
		reportedMessage = readUnsignedInt(in);
		reportedField = in.readUnsignedByte();
		System.out.println(reportedField);
		fieldAvailable = FieldAvailability.values()[in.readUnsignedByte()];
		reportedEnumeratedIndex = in.readUnsignedByte();
		enumeratedIndexEnable = in.readInt();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeInt((int)stationNumber);
		out.writeInt((int)reportedMessage);
		out.writeByte(reportedField);
		out.writeByte(fieldAvailable.ordinal());
		out.writeByte(reportedEnumeratedIndex);
		out.writeInt(enumeratedIndexEnable);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dataLinkID;
		result = prime * result + reportedEnumeratedIndex;
		result = prime * result + reportedField;
		result = prime * result + (int) (reportedMessage ^ (reportedMessage >>> 32));
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
		FieldConfigurationCommand other = (FieldConfigurationCommand) obj;
		if (dataLinkID != other.dataLinkID)
			return false;
		if (reportedEnumeratedIndex != other.reportedEnumeratedIndex)
			return false;
		if (reportedField != other.reportedField)
			return false;
		if (reportedMessage != other.reportedMessage)
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
		reportedMessage = 0;
		reportedField = 0;
		fieldAvailable = null;
		reportedEnumeratedIndex = 0;
		enumeratedIndexEnable = 0;
	}
	
}
