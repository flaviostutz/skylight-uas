package br.skylight.commons.dli.payload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.AddressedSensor;
import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.DesignatorStatus;
import br.skylight.commons.dli.enums.EOCameraStatus;
import br.skylight.commons.dli.enums.FireLaserStatus;
import br.skylight.commons.dli.enums.IRPolarityStatus;
import br.skylight.commons.dli.enums.ImageOutputState;
import br.skylight.commons.dli.enums.ImagePositionValidity;
import br.skylight.commons.dli.enums.LaserPulse;
import br.skylight.commons.dli.enums.PointingModeState;
import br.skylight.commons.dli.enums.PreplanMode;
import br.skylight.commons.dli.enums.SystemOperatingModeState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class EOIRLaserOperatingState extends Message<EOIRLaserOperatingState> implements MessageTargetedToStation {

	private BitmappedStation stationNumber = new BitmappedStation();//u4
	private AddressedSensor addressedSensor = new AddressedSensor();//u1
	private SystemOperatingModeState systemOperatingModeState;//u1
	private EOCameraStatus eoCameraStatus;//u1
	private IRPolarityStatus irPolarityStatus;//u1
	private ImageOutputState imageOutputState;//u1
	private float actualCentrelineElevationAngle;
	private float actualVerticalFieldOfView;
	private float actualCentrelineAzimuthAngle;
	private float actualHorizontalFieldOfView;
	private float actualSensorRotationAngle;
	private ImagePositionValidity imagePosition;//u1
	private double latitudeOfImageCentre;
	private double longitudeOfImageCentre;
	private float altitudeWGS84;
	private PointingModeState pointModeState;//u1
	private PreplanMode preplanMode;//u1
	private float reportedRange;
	private FireLaserStatus fireLaserPointerRangeFinderStatus;//u1
	private LaserPulse selectedLaserRangefinderPulse;//u1
	private int laserDesignatorCode;//u2
	private DesignatorStatus laserDesignatorStatus;//u1

	@Override
	public MessageType getMessageType() {
		return MessageType.M302;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		stationNumber.setData(readUnsignedInt(in));
		addressedSensor.setData(in.readUnsignedByte());
		systemOperatingModeState = SystemOperatingModeState.values()[in.readUnsignedByte()];
		eoCameraStatus = EOCameraStatus.values()[in.readUnsignedByte()];
		irPolarityStatus = IRPolarityStatus.values()[in.readUnsignedByte()];
		imageOutputState = ImageOutputState.values()[in.readUnsignedByte()];
		actualCentrelineElevationAngle = in.readFloat();
		actualVerticalFieldOfView = in.readFloat();
		actualCentrelineAzimuthAngle = in.readFloat();
		actualHorizontalFieldOfView = in.readFloat();
		actualSensorRotationAngle = in.readFloat();
		imagePosition = ImagePositionValidity.values()[in.readUnsignedByte()];
		latitudeOfImageCentre = in.readDouble();
		longitudeOfImageCentre = in.readDouble();
		altitudeWGS84 = in.readFloat();
		pointModeState = PointingModeState.values()[in.readUnsignedByte()];
		preplanMode = PreplanMode.values()[in.readUnsignedByte()];
		reportedRange = in.readFloat();
		fireLaserPointerRangeFinderStatus = FireLaserStatus.values()[in.readUnsignedByte()];
		selectedLaserRangefinderPulse = LaserPulse.values()[in.readUnsignedByte()];
		laserDesignatorCode = in.readUnsignedShort();
		laserDesignatorStatus = DesignatorStatus.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		stationNumber.writeState(out);
		out.writeByte((int)addressedSensor.getData());
		out.writeByte(systemOperatingModeState.ordinal());
		out.writeByte(eoCameraStatus.ordinal());
		out.writeByte(irPolarityStatus.ordinal());
		out.writeByte(imageOutputState.ordinal());
		out.writeFloat(actualCentrelineElevationAngle);
		out.writeFloat(actualVerticalFieldOfView);
		out.writeFloat(actualCentrelineAzimuthAngle);
		out.writeFloat(actualHorizontalFieldOfView);
		out.writeFloat(actualSensorRotationAngle);
		out.writeByte(imagePosition.ordinal());
		out.writeDouble(latitudeOfImageCentre);
		out.writeDouble(longitudeOfImageCentre);
		out.writeFloat(altitudeWGS84);
		out.writeByte(pointModeState.ordinal());
		out.writeByte(preplanMode.ordinal());
		out.writeFloat(reportedRange);
		out.writeByte(fireLaserPointerRangeFinderStatus.ordinal());
		out.writeByte(selectedLaserRangefinderPulse.ordinal());
		out.writeShort(laserDesignatorCode);
		out.writeByte(laserDesignatorStatus.ordinal());
	}

	@Override
	public void resetValues() {
		stationNumber.setData(0);
		addressedSensor.setData(0);
		systemOperatingModeState = SystemOperatingModeState.values()[0];
		eoCameraStatus = EOCameraStatus.values()[0];
		irPolarityStatus = IRPolarityStatus.values()[0];
		imageOutputState = ImageOutputState.values()[0];
		actualCentrelineElevationAngle = 0;
		actualVerticalFieldOfView = 0;
		actualCentrelineAzimuthAngle = 0;
		actualHorizontalFieldOfView = 0;
		actualSensorRotationAngle = 0;
		imagePosition = ImagePositionValidity.values()[0];
		latitudeOfImageCentre = 0;
		longitudeOfImageCentre = 0;
		altitudeWGS84 = 0;
		pointModeState = PointingModeState.values()[0];
		preplanMode = PreplanMode.values()[0];
		reportedRange = 0;
		fireLaserPointerRangeFinderStatus = FireLaserStatus.values()[0];
		selectedLaserRangefinderPulse = LaserPulse.values()[0];
		laserDesignatorCode = 0;
		laserDesignatorStatus = DesignatorStatus.values()[0];
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

	public SystemOperatingModeState getSystemOperatingModeState() {
		return systemOperatingModeState;
	}

	public void setSystemOperatingModeState(SystemOperatingModeState systemOperatingModeState) {
		this.systemOperatingModeState = systemOperatingModeState;
	}

	public EOCameraStatus getEoCameraStatus() {
		return eoCameraStatus;
	}

	public void setEoCameraStatus(EOCameraStatus eoCameraStatus) {
		this.eoCameraStatus = eoCameraStatus;
	}

	public IRPolarityStatus getIrPolarityStatus() {
		return irPolarityStatus;
	}

	public void setIrPolarityStatus(IRPolarityStatus irPolarityStatus) {
		this.irPolarityStatus = irPolarityStatus;
	}

	public ImageOutputState getImageOutputState() {
		return imageOutputState;
	}

	public void setImageOutputState(ImageOutputState imageOutputState) {
		this.imageOutputState = imageOutputState;
	}

	public float getActualCentrelineElevationAngle() {
		return actualCentrelineElevationAngle;
	}

	public void setActualCentrelineElevationAngle(float actualCentrelineElevationAngle) {
		this.actualCentrelineElevationAngle = actualCentrelineElevationAngle;
	}

	public float getActualVerticalFieldOfView() {
		return actualVerticalFieldOfView;
	}

	public void setActualVerticalFieldOfView(float actualVerticalFieldOfView) {
		this.actualVerticalFieldOfView = actualVerticalFieldOfView;
	}

	public float getActualCentrelineAzimuthAngle() {
		return actualCentrelineAzimuthAngle;
	}

	public void setActualCentrelineAzimuthAngle(float actualCentrelineAzimuthAngle) {
		this.actualCentrelineAzimuthAngle = actualCentrelineAzimuthAngle;
	}

	public float getActualHorizontalFieldOfView() {
		return actualHorizontalFieldOfView;
	}

	public void setActualHorizontalFieldOfView(float actualHorizontalFieldOfView) {
		this.actualHorizontalFieldOfView = actualHorizontalFieldOfView;
	}

	public float getActualSensorRotationAngle() {
		return actualSensorRotationAngle;
	}

	public void setActualSensorRotationAngle(float actualSensorRotationAngle) {
		this.actualSensorRotationAngle = actualSensorRotationAngle;
	}

	public ImagePositionValidity getImagePosition() {
		return imagePosition;
	}

	public void setImagePosition(ImagePositionValidity imagePosition) {
		this.imagePosition = imagePosition;
	}

	public double getLatitudeOfImageCentre() {
		return latitudeOfImageCentre;
	}

	public void setLatitudeOfImageCentre(double latitudeOfImageCentre) {
		this.latitudeOfImageCentre = latitudeOfImageCentre;
	}

	public double getLongitudeOfImageCentre() {
		return longitudeOfImageCentre;
	}

	public void setLongitudeOfImageCentre(double longitudeOfImageCentre) {
		this.longitudeOfImageCentre = longitudeOfImageCentre;
	}

	public float getAltitudeWGS84() {
		return altitudeWGS84;
	}

	public void setAltitudeWGS84(float altitudeWGS84) {
		this.altitudeWGS84 = altitudeWGS84;
	}

	public PointingModeState getPointModeState() {
		return pointModeState;
	}

	public void setPointModeState(PointingModeState pointModeState) {
		this.pointModeState = pointModeState;
	}

	public PreplanMode getPreplanMode() {
		return preplanMode;
	}

	public void setPreplanMode(PreplanMode preplanMode) {
		this.preplanMode = preplanMode;
	}

	public float getReportedRange() {
		return reportedRange;
	}

	public void setReportedRange(float reportedRange) {
		this.reportedRange = reportedRange;
	}

	public FireLaserStatus getFireLaserPointerRangeFinderStatus() {
		return fireLaserPointerRangeFinderStatus;
	}

	public void setFireLaserPointerRangeFinderStatus(FireLaserStatus fireLaserPointerRangeFinderStatus) {
		this.fireLaserPointerRangeFinderStatus = fireLaserPointerRangeFinderStatus;
	}

	public LaserPulse getSelectedLaserRangefinderPulse() {
		return selectedLaserRangefinderPulse;
	}

	public void setSelectedLaserRangefinderPulse(LaserPulse selectedLaserRangefinderPulse) {
		this.selectedLaserRangefinderPulse = selectedLaserRangefinderPulse;
	}

	public int getLaserDesignatorCode() {
		return laserDesignatorCode;
	}

	public void setLaserDesignatorCode(int laserDesignatorCode) {
		this.laserDesignatorCode = laserDesignatorCode;
	}

	public DesignatorStatus getLaserDesignatorStatus() {
		return laserDesignatorStatus;
	}

	public void setLaserDesignatorStatus(DesignatorStatus laserDesignatorStatus) {
		this.laserDesignatorStatus = laserDesignatorStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((addressedSensor == null) ? 0 : addressedSensor.hashCode());
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
		EOIRLaserOperatingState other = (EOIRLaserOperatingState) obj;
		if (addressedSensor == null) {
			if (other.addressedSensor != null)
				return false;
		} else if (!addressedSensor.equals(other.addressedSensor))
			return false;
		if (stationNumber != other.stationNumber)
			return false;
		return true;
	}

	@Override
	public BitmappedStation getTargetStations() {
		return stationNumber;
	}
	
}