package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class MiscInfoMessage extends Message<MiscInfoMessage> {

	private double currentTargetLatitude;
	private double currentTargetLongitude;
	private float currentTargetAltitude;
	
	private float generatorVoltage;
	private float battery1Voltage;
	private float battery2Voltage;
	private short onboardTemperature;
	private short chtTemperature;
	private double linkLatencyTime;
	private int dataTerminalTransmitErrors;
	private boolean manualRCControl;
	private int numberOfHardwareResets;
	private int numberOfSkippedHardwareMessages;
	private int adtPacketsSentAPCounter;
	private int adtPacketsSentModemCounter;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2005;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		currentTargetLatitude = in.readDouble();
		currentTargetLongitude = in.readDouble();
		currentTargetAltitude = in.readFloat();
		generatorVoltage = in.readFloat();
		battery1Voltage = in.readFloat();
		battery2Voltage = in.readFloat();
		onboardTemperature = in.readShort();
		chtTemperature = in.readShort();
		linkLatencyTime = in.readDouble();
		dataTerminalTransmitErrors = in.readInt();
		manualRCControl = in.readBoolean();
		numberOfHardwareResets = in.readShort();
		numberOfSkippedHardwareMessages = in.readShort();
		adtPacketsSentAPCounter = in.readShort();
		adtPacketsSentModemCounter = in.readShort();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeDouble(currentTargetLatitude);
		out.writeDouble(currentTargetLongitude);
		out.writeFloat(currentTargetAltitude);
		out.writeFloat(generatorVoltage);
		out.writeFloat(battery1Voltage);
		out.writeFloat(battery2Voltage);
		out.writeShort(onboardTemperature);
		out.writeShort(chtTemperature);
		out.writeDouble(linkLatencyTime);
		out.writeInt(dataTerminalTransmitErrors);
		out.writeBoolean(manualRCControl);
		out.writeShort(numberOfHardwareResets);
		out.writeShort(numberOfSkippedHardwareMessages);
		out.writeShort(adtPacketsSentAPCounter);
		out.writeShort(adtPacketsSentModemCounter);
	}

	@Override
	public void resetValues() {
		currentTargetLatitude = 0;
		currentTargetLongitude = 0;
		currentTargetAltitude = 0;
		generatorVoltage = 0;
		battery1Voltage = 0;
		battery2Voltage = 0;
		onboardTemperature = 0;
		chtTemperature = 0;
		linkLatencyTime = 0;
		dataTerminalTransmitErrors = 0;
		manualRCControl = false;
		numberOfHardwareResets = 0;
		numberOfSkippedHardwareMessages = 0;
		adtPacketsSentAPCounter = 0;
		adtPacketsSentModemCounter = 0;
	}

	public void setDataTerminalTransmitErrors(int dataTerminalTransmitErrors) {
		this.dataTerminalTransmitErrors = dataTerminalTransmitErrors;
	}
	public int getDataTerminalTransmitErrors() {
		return dataTerminalTransmitErrors;
	}
	
	public void setManualRCControl(boolean manualRCControl) {
		this.manualRCControl = manualRCControl;
	}
	public boolean isManualRCControl() {
		return manualRCControl;
	}
	
	public float getCurrentTargetAltitude() {
		return currentTargetAltitude;
	}
	public double getCurrentTargetLatitude() {
		return currentTargetLatitude;
	}
	public double getCurrentTargetLongitude() {
		return currentTargetLongitude;
	}
	public void setCurrentTargetAltitude(float currentTargetAltitude) {
		this.currentTargetAltitude = currentTargetAltitude;
	}
	public void setCurrentTargetLatitude(double currentTargetLatitude) {
		this.currentTargetLatitude = currentTargetLatitude;
	}
	public void setCurrentTargetLongitude(double currentTargetLongitude) {
		this.currentTargetLongitude = currentTargetLongitude;
	}
	
	public void setBattery1Voltage(float battery1Voltage) {
		this.battery1Voltage = battery1Voltage;
	}
	public void setBattery2Voltage(float battery2Voltage) {
		this.battery2Voltage = battery2Voltage;
	}
	public float getBattery1Voltage() {
		return battery1Voltage;
	}
	public float getBattery2Voltage() {
		return battery2Voltage;
	}
	
	public void setGeneratorVoltage(float generatorVoltage) {
		this.generatorVoltage = generatorVoltage;
	}
	public float getGeneratorVoltage() {
		return generatorVoltage;
	}
	
	public void setLinkLatencyTime(double linkLatencyTime) {
		this.linkLatencyTime = linkLatencyTime;
	}
	public double getLinkLatencyTime() {
		return linkLatencyTime;
	}

	public short getOnboardTemperature() {
		return onboardTemperature;
	}

	public void setOnboardTemperature(short onboardTemperature) {
		this.onboardTemperature = onboardTemperature;
	}

	public short getChtTemperature() {
		return chtTemperature;
	}
	public void setChtTemperature(short chtTemperature) {
		this.chtTemperature = chtTemperature;
	}
	
	public int getNumberOfHardwareResets() {
		return numberOfHardwareResets;
	}
	public int getNumberOfSkippedHardwareMessages() {
		return numberOfSkippedHardwareMessages;
	}
	public void setNumberOfHardwareResets(int numberOfHardwareResets) {
		this.numberOfHardwareResets = numberOfHardwareResets;
	}
	public void setNumberOfSkippedHardwareMessages(
			int numberOfSkippedHardwareMessages) {
		this.numberOfSkippedHardwareMessages = numberOfSkippedHardwareMessages;
	}
	
	public int getAdtPacketsSentAPCounter() {
		return adtPacketsSentAPCounter;
	}
	public void setAdtPacketsSentAPCounter(int adtPacketsSentAPCounter) {
		this.adtPacketsSentAPCounter = adtPacketsSentAPCounter;
	}
	public int getAdtPacketsSentModemCounter() {
		return adtPacketsSentModemCounter;
	}
	public void setAdtPacketsSentModemCounter(int adtPacketsSentModemCounter) {
		this.adtPacketsSentModemCounter = adtPacketsSentModemCounter;
	}
	
}
