package br.skylight.commons.dli.configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.FieldSupported;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class FieldConfigurationDoubleResponse extends Message<FieldConfigurationDoubleResponse> {

	private int vsmID;
	private int dataLinkID;
	private long stationNumber;//u4
	private long requestedMessage;//u4
	private int requestedField;//u1
	private FieldSupported fieldSupported;//u1
	private double maxValue;
	private double minValue;
	private double maxDisplayValue;
	private double minDisplayValue;
	private double minimumDisplayResolution;
	private double highCautionLimit;
	private double highWarningLimit;
	private double lowCautionLimit;
	private double lowWarningLimit;
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

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxDisplayValue() {
		return maxDisplayValue;
	}

	public void setMaxDisplayValue(double maxDisplayValue) {
		this.maxDisplayValue = maxDisplayValue;
	}

	public double getMinDisplayValue() {
		return minDisplayValue;
	}

	public void setMinDisplayValue(double minDisplayValue) {
		this.minDisplayValue = minDisplayValue;
	}

	public double getMinimumDisplayResolution() {
		return minimumDisplayResolution;
	}

	public void setMinimumDisplayResolution(double minimumDisplayResolution) {
		this.minimumDisplayResolution = minimumDisplayResolution;
	}

	public double getHighCautionLimit() {
		return highCautionLimit;
	}

	public void setHighCautionLimit(double highCautionLimit) {
		this.highCautionLimit = highCautionLimit;
	}

	public double getHighWarningLimit() {
		return highWarningLimit;
	}

	public void setHighWarningLimit(double highWarningLimit) {
		this.highWarningLimit = highWarningLimit;
	}

	public double getLowCautionLimit() {
		return lowCautionLimit;
	}

	public void setLowCautionLimit(double lowCautionLimit) {
		this.lowCautionLimit = lowCautionLimit;
	}

	public double getLowWarningLimit() {
		return lowWarningLimit;
	}

	public void setLowWarningLimit(double lowWarningLimit) {
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
		maxValue = in.readDouble();
		minValue = in.readDouble();
		maxDisplayValue = in.readDouble();
		minDisplayValue = in.readDouble();
		minimumDisplayResolution = in.readDouble();
		highCautionLimit = in.readDouble();
		highWarningLimit = in.readDouble();
		lowCautionLimit = in.readDouble();
		lowWarningLimit = in.readDouble();
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
		out.writeDouble(maxValue);
		out.writeDouble(minValue);
		out.writeDouble(maxDisplayValue);
		out.writeDouble(minDisplayValue);
		out.writeDouble(minimumDisplayResolution);
		out.writeDouble(highCautionLimit);
		out.writeDouble(highWarningLimit);
		out.writeDouble(lowCautionLimit);
		out.writeDouble(lowWarningLimit);
		writeNullTerminatedString(out, helpText);
		out.writeByte(subsystemID.ordinal());
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1301;
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
		FieldConfigurationDoubleResponse other = (FieldConfigurationDoubleResponse) obj;
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
