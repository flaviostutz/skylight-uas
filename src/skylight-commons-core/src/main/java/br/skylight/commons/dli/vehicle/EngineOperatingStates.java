package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.EnginePartStatus;
import br.skylight.commons.dli.enums.EngineStatus;
import br.skylight.commons.dli.enums.ReportedEngineCommand;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class EngineOperatingStates extends Message<EngineOperatingStates> {

	private int engineNumber;
	private EngineStatus engineStatus;//u1
	private ReportedEngineCommand reportedEngineCommand;//u1
	private float enginePowerSetting;
	private float engineSpeed;
	private EnginePartStatus engineSpeedStatus;//u1
	private EnginePartStatus outputPowerStatus;//u1
	private EnginePartStatus engineBodyTemperatureStatus;//u1
	private EnginePartStatus exhaustGasTemperatureStatus;//u1
	private EnginePartStatus coolantTemperatureStatus;//u1
	private EnginePartStatus lubricantPressureStatus;//u1
	private EnginePartStatus lubricantTemperatureStatus;//u1
	private EnginePartStatus fireDetectionSensorStatus;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M105;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		engineNumber = in.readInt();
		engineStatus = EngineStatus.values()[in.readUnsignedByte()];
		reportedEngineCommand = ReportedEngineCommand.values()[in.readUnsignedByte()];
		enginePowerSetting = in.readFloat();
		engineSpeed = in.readFloat();
		engineSpeedStatus = EnginePartStatus.values()[in.readUnsignedByte()];
		outputPowerStatus = EnginePartStatus.values()[in.readUnsignedByte()];
		engineBodyTemperatureStatus = EnginePartStatus.values()[in.readUnsignedByte()];
		exhaustGasTemperatureStatus = EnginePartStatus.values()[in.readUnsignedByte()];
		coolantTemperatureStatus = EnginePartStatus.values()[in.readUnsignedByte()];
		lubricantPressureStatus = EnginePartStatus.values()[in.readUnsignedByte()];
		lubricantTemperatureStatus = EnginePartStatus.values()[in.readUnsignedByte()];
		fireDetectionSensorStatus = EnginePartStatus.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(engineNumber);
		out.writeByte(engineStatus.ordinal());
		out.writeByte(reportedEngineCommand.ordinal());
		out.writeFloat(enginePowerSetting);
		out.writeFloat(engineSpeed);
		out.writeByte(engineSpeedStatus.ordinal());
		out.writeByte(outputPowerStatus.ordinal());
		out.writeByte(engineBodyTemperatureStatus.ordinal());
		out.writeByte(exhaustGasTemperatureStatus.ordinal());
		out.writeByte(coolantTemperatureStatus.ordinal());
		out.writeByte(lubricantPressureStatus.ordinal());
		out.writeByte(lubricantTemperatureStatus.ordinal());
		out.writeByte(fireDetectionSensorStatus.ordinal());
	}

	@Override
	public void resetValues() {
		engineNumber = 0;
		engineStatus = EngineStatus.values()[0];
		reportedEngineCommand = ReportedEngineCommand.values()[0];
		enginePowerSetting = 0;
		engineSpeed = 0;
		engineSpeedStatus = EnginePartStatus.values()[0];
		outputPowerStatus = EnginePartStatus.values()[0];
		engineBodyTemperatureStatus = EnginePartStatus.values()[0];
		exhaustGasTemperatureStatus = EnginePartStatus.values()[0];
		coolantTemperatureStatus = EnginePartStatus.values()[0];
		lubricantPressureStatus = EnginePartStatus.values()[0];
		lubricantTemperatureStatus = EnginePartStatus.values()[0];
		fireDetectionSensorStatus = EnginePartStatus.values()[0];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + engineNumber;
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
		EngineOperatingStates other = (EngineOperatingStates) obj;
		if (engineNumber != other.engineNumber)
			return false;
		return true;
	}

	public int getEngineNumber() {
		return engineNumber;
	}

	public void setEngineNumber(int engineNumber) {
		this.engineNumber = engineNumber;
	}

	public EngineStatus getEngineStatus() {
		return engineStatus;
	}

	public void setEngineStatus(EngineStatus engineStatus) {
		this.engineStatus = engineStatus;
	}

	public ReportedEngineCommand getReportedEngineCommand() {
		return reportedEngineCommand;
	}

	public void setReportedEngineCommand(ReportedEngineCommand reportedEngineCommand) {
		this.reportedEngineCommand = reportedEngineCommand;
	}

	public float getEnginePowerSetting() {
		return enginePowerSetting;
	}

	public void setEnginePowerSetting(float enginePowerSetting) {
		this.enginePowerSetting = enginePowerSetting;
	}

	public float getEngineSpeed() {
		return engineSpeed;
	}

	public void setEngineSpeed(float engineSpeed) {
		this.engineSpeed = engineSpeed;
	}

	public EnginePartStatus getEngineSpeedStatus() {
		return engineSpeedStatus;
	}

	public void setEngineSpeedStatus(EnginePartStatus engineSpeedStatus) {
		this.engineSpeedStatus = engineSpeedStatus;
	}

	public EnginePartStatus getOutputPowerStatus() {
		return outputPowerStatus;
	}

	public void setOutputPowerStatus(EnginePartStatus outputPowerStatus) {
		this.outputPowerStatus = outputPowerStatus;
	}

	public EnginePartStatus getEngineBodyTemperatureStatus() {
		return engineBodyTemperatureStatus;
	}

	public void setEngineBodyTemperatureStatus(EnginePartStatus engineBodyTemperatureStatus) {
		this.engineBodyTemperatureStatus = engineBodyTemperatureStatus;
	}

	public EnginePartStatus getExhaustGasTemperatureStatus() {
		return exhaustGasTemperatureStatus;
	}

	public void setExhaustGasTemperatureStatus(EnginePartStatus exhaustGasTemperatureStatus) {
		this.exhaustGasTemperatureStatus = exhaustGasTemperatureStatus;
	}

	public EnginePartStatus getCoolantTemperatureStatus() {
		return coolantTemperatureStatus;
	}

	public void setCoolantTemperatureStatus(EnginePartStatus coolantTemperatureStatus) {
		this.coolantTemperatureStatus = coolantTemperatureStatus;
	}

	public EnginePartStatus getLubricantPressureStatus() {
		return lubricantPressureStatus;
	}

	public void setLubricantPressureStatus(EnginePartStatus lubricantPressureStatus) {
		this.lubricantPressureStatus = lubricantPressureStatus;
	}

	public EnginePartStatus getLubricantTemperatureStatus() {
		return lubricantTemperatureStatus;
	}

	public void setLubricantTemperatureStatus(EnginePartStatus lubricantTemperatureStatus) {
		this.lubricantTemperatureStatus = lubricantTemperatureStatus;
	}

	public EnginePartStatus getFireDetectionSensorStatus() {
		return fireDetectionSensorStatus;
	}

	public void setFireDetectionSensorStatus(EnginePartStatus fireDetectionSensorStatus) {
		this.fireDetectionSensorStatus = fireDetectionSensorStatus;
	}
	
}
