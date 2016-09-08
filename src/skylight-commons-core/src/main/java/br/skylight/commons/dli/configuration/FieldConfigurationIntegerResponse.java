package br.skylight.commons.dli.configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.FieldSupported;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class FieldConfigurationIntegerResponse extends Message<FieldConfigurationIntegerResponse> {

	private int vsmID;
	private int dataLinkID;
	private long stationNumber;//u4
	private long requestedMessage;//u4
	private int requestedField;//u1
	private FieldSupported fieldSupported;//u1
	private int maxValue;
	private int minValue;
	private int maxDisplayValue;
	private int minDisplayValue;
	private int minimumDisplayResolution;
	private int highCautionLimit;
	private int highWarningLimit;
	private int lowCautionLimit;
	private int lowWarningLimit;
	private String helpText;//c80
	private Subsystem subsystemID;//u1
	
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

	public FieldSupported getFieldSupported() {
		return fieldSupported;
	}

	public void setFieldSupported(FieldSupported fieldSupported) {
		this.fieldSupported = fieldSupported;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public int getMaxDisplayValue() {
		return maxDisplayValue;
	}

	public void setMaxDisplayValue(int maxDisplayValue) {
		this.maxDisplayValue = maxDisplayValue;
	}

	public int getMinDisplayValue() {
		return minDisplayValue;
	}

	public void setMinDisplayValue(int minDisplayValue) {
		this.minDisplayValue = minDisplayValue;
	}

	public int getMinimumDisplayResolution() {
		return minimumDisplayResolution;
	}

	public void setMinimumDisplayResolution(int minimumDisplayResolution) {
		this.minimumDisplayResolution = minimumDisplayResolution;
	}

	public int getHighCautionLimit() {
		return highCautionLimit;
	}

	public void setHighCautionLimit(int highCautionLimit) {
		this.highCautionLimit = highCautionLimit;
	}

	public int getHighWarningLimit() {
		return highWarningLimit;
	}

	public void setHighWarningLimit(int highWarningLimit) {
		this.highWarningLimit = highWarningLimit;
	}

	public int getLowCautionLimit() {
		return lowCautionLimit;
	}

	public void setLowCautionLimit(int lowCautionLimit) {
		this.lowCautionLimit = lowCautionLimit;
	}

	public int getLowWarningLimit() {
		return lowWarningLimit;
	}

	public void setLowWarningLimit(int lowWarningLimit) {
		this.lowWarningLimit = lowWarningLimit;
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public Subsystem getSubsystemID() {
		return subsystemID;
	}

	public void setSubsystemID(Subsystem subsystemID) {
		this.subsystemID = subsystemID;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		stationNumber = readUnsignedInt(in);
		requestedMessage = readUnsignedInt(in);
		requestedField = in.readUnsignedByte();
		fieldSupported = FieldSupported.values()[in.readUnsignedByte()];
		maxValue = in.readInt();
		minValue = in.readInt();
		maxDisplayValue = in.readInt();
		minDisplayValue = in.readInt();
		minimumDisplayResolution = in.readInt();
		highCautionLimit = in.readInt();
		highWarningLimit = in.readInt();
		lowCautionLimit = in.readInt();
		lowWarningLimit = in.readInt();
		helpText = readNullTerminatedString(in);
		subsystemID = Subsystem.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeInt((int)stationNumber);
		out.writeInt((int)requestedMessage);
		out.writeByte(requestedField);
		out.writeByte(fieldSupported.ordinal());
		out.writeInt(maxValue);
		out.writeInt(minValue);
		out.writeInt(maxDisplayValue);
		out.writeInt(minDisplayValue);
		out.writeInt(minimumDisplayResolution);
		out.writeInt(highCautionLimit);
		out.writeInt(highWarningLimit);
		out.writeInt(lowCautionLimit);
		out.writeInt(lowWarningLimit);
		writeNullTerminatedString(out, helpText);
		out.writeByte(subsystemID.ordinal());
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1300;
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
		FieldConfigurationIntegerResponse other = (FieldConfigurationIntegerResponse) obj;
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
		fieldSupported = null;
		maxValue = 0;
		minValue = 0;
		maxDisplayValue = 0;
		minDisplayValue = 0;
		minimumDisplayResolution = 0;
		highCautionLimit = 0;
		highWarningLimit = 0;
		lowCautionLimit = 0;
		lowWarningLimit = 0;
		helpText = "";
		subsystemID = null;
	}
	
}
