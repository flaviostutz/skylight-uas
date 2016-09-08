package br.skylight.commons.dli.payload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.AddressedSensor;
import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.EOCameraStatus;
import br.skylight.commons.dli.enums.ImageOutput;
import br.skylight.commons.dli.enums.SetEOIRPointingMode;
import br.skylight.commons.dli.enums.SetIRPolarity;
import br.skylight.commons.dli.enums.SystemOperatingMode;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class EOIRLaserPayloadCommand extends Message<EOIRLaserPayloadCommand> implements MessageTargetedToStation {

	private BitmappedStation stationNumber = new BitmappedStation();//u4
	private AddressedSensor addressedSensor = new AddressedSensor();//u1
	private SystemOperatingMode systemOperatingMode;//u1
	private EOCameraStatus setEOSensorMode;//u1
	private SetIRPolarity setIRPolarity;//u1
	private ImageOutput imageOutput;//u1
	private SetEOIRPointingMode setEOIRPointingMode;//u1
	private int fireLaserPointer;//u1
	private int selectLaserRangefinderPulse;//u1
	private int setLaserDesignatorCode;//u2
	private int initiateLaserDesignator;//u1
	private int preplanMode;//u1

	public EOIRLaserPayloadCommand() {
		resetValues();
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M201;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		stationNumber.setData(readUnsignedInt(in));
		addressedSensor.setData(in.readUnsignedByte());
		systemOperatingMode = SystemOperatingMode.values()[in.readUnsignedByte()];
		setEOSensorMode = EOCameraStatus.values()[in.readUnsignedByte()];
		setIRPolarity = SetIRPolarity.values()[in.readUnsignedByte()];
		imageOutput = ImageOutput.values()[in.readUnsignedByte()];
		setEOIRPointingMode = SetEOIRPointingMode.values()[in.readUnsignedByte()];
		fireLaserPointer = in.readUnsignedByte();
		selectLaserRangefinderPulse = in.readUnsignedByte();
		setLaserDesignatorCode = in.readUnsignedShort();
		initiateLaserDesignator = in.readUnsignedByte();
		preplanMode = in.readUnsignedByte();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		stationNumber.writeState(out);
		out.writeByte((int)addressedSensor.getData());
		out.writeByte(systemOperatingMode.ordinal());
		out.writeByte(setEOSensorMode.ordinal());
		out.writeByte(setIRPolarity.ordinal());
		out.writeByte(imageOutput.ordinal());
		out.writeByte(setEOIRPointingMode.ordinal());
		out.writeByte(fireLaserPointer);
		out.writeByte(selectLaserRangefinderPulse);
		out.writeShort(setLaserDesignatorCode);
		out.writeByte(initiateLaserDesignator);
		out.writeByte(preplanMode);
	}

	@Override
	public void resetValues() {
		stationNumber.setData(0);
		addressedSensor.setData(0);
		systemOperatingMode = SystemOperatingMode.OFF;
		setEOSensorMode = EOCameraStatus.COLOUR_MODE;
		setIRPolarity = SetIRPolarity.WHITE_HOT;
		imageOutput = ImageOutput.BOTH;
		setEOIRPointingMode = SetEOIRPointingMode.NO_VALUE;
		fireLaserPointer = (byte)0;
		selectLaserRangefinderPulse = (byte)0;
		setLaserDesignatorCode = 0;
		initiateLaserDesignator = (byte)0;
		preplanMode = (byte)0;
	}
	
	public BitmappedStation getStationNumber() {
		return stationNumber;
	}

	public AddressedSensor getAddressedSensor() {
		return addressedSensor;
	}

	public void setAddressedSensor(AddressedSensor addressedSensor) {
		this.addressedSensor = addressedSensor;
	}

	public SystemOperatingMode getSystemOperatingMode() {
		return systemOperatingMode;
	}

	public void setSystemOperatingMode(SystemOperatingMode systemOperatingMode) {
		this.systemOperatingMode = systemOperatingMode;
	}

	public EOCameraStatus getSetEOSensorMode() {
		return setEOSensorMode;
	}

	public void setSetEOSensorMode(EOCameraStatus setEOSensorMode) {
		this.setEOSensorMode = setEOSensorMode;
	}

	public SetIRPolarity getSetIRPolarity() {
		return setIRPolarity;
	}

	public void setSetIRPolarity(SetIRPolarity setIRPolarity) {
		this.setIRPolarity = setIRPolarity;
	}

	public ImageOutput getImageOutput() {
		return imageOutput;
	}

	public void setImageOutput(ImageOutput imageOutput) {
		this.imageOutput = imageOutput;
	}

	public SetEOIRPointingMode getSetEOIRPointingMode() {
		return setEOIRPointingMode;
	}

	public void setSetEOIRPointingMode(SetEOIRPointingMode setEOIRPointingMode) {
		this.setEOIRPointingMode = setEOIRPointingMode;
	}

	public int getFireLaserPointer() {
		return fireLaserPointer;
	}

	public void setFireLaserPointer(int fireLaserPointer) {
		this.fireLaserPointer = fireLaserPointer;
	}

	public int getSelectLaserRangefinderPulse() {
		return selectLaserRangefinderPulse;
	}

	public void setSelectLaserRangefinderPulse(int selectLaserRangefinderPulse) {
		this.selectLaserRangefinderPulse = selectLaserRangefinderPulse;
	}

	public int getSetLaserDesignatorCode() {
		return setLaserDesignatorCode;
	}

	public void setSetLaserDesignatorCode(int setLaserDesignatorCode) {
		this.setLaserDesignatorCode = setLaserDesignatorCode;
	}

	public int getInitiateLaserDesignator() {
		return initiateLaserDesignator;
	}

	public void setInitiateLaserDesignator(int initiateLaserDesignator) {
		this.initiateLaserDesignator = initiateLaserDesignator;
	}

	public int getPreplanMode() {
		return preplanMode;
	}

	public void setPreplanMode(int preplanMode) {
		this.preplanMode = preplanMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (stationNumber.getData() ^ (stationNumber.getData() >>> 32));
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
		EOIRLaserPayloadCommand other = (EOIRLaserPayloadCommand) obj;
		if (stationNumber != other.stationNumber)
			return false;
		return true;
	}

	@Override
	public BitmappedStation getTargetStations() {
		return stationNumber;
	}

}
