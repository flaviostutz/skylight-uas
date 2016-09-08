package br.skylight.commons.dli.payload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.FocusType;
import br.skylight.commons.dli.enums.SetFocus;
import br.skylight.commons.dli.enums.SetZoom;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class PayloadSteeringCommand extends Message<PayloadSteeringCommand> implements MessageTargetedToStation {

	private BitmappedStation stationNumber = new BitmappedStation();//u4
	private float setCentrelineAzimuthAngle;
	private float setCentrelineElevationAngle;
	private SetZoom setZoom;//u1
	private float setHorizontalFieldOfView;
	private float setVerticalFieldOfView;
	private float horizontalSlewRate;
	private float verticalSlewRate;
	private double latitude;
	private double longitude;
	private double altitude;
	private AltitudeType altitudeType;//u1
	private SetFocus setFocus;//u1
	private FocusType focusType;//u1

	public PayloadSteeringCommand() {
		resetValues();
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		stationNumber.setData(readUnsignedInt(in));
		setCentrelineAzimuthAngle = in.readFloat();
		setCentrelineElevationAngle = in.readFloat();
		setZoom = SetZoom.values()[in.readUnsignedByte()];
		setHorizontalFieldOfView = in.readFloat();
		setVerticalFieldOfView = in.readFloat();
		horizontalSlewRate = in.readFloat();
		verticalSlewRate = in.readFloat();
		latitude = in.readDouble();
		longitude = in.readDouble();
		altitude = in.readDouble();
		altitudeType = AltitudeType.values()[in.readUnsignedByte()];
		setFocus = SetFocus.values()[in.readUnsignedByte()];
		focusType = FocusType.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt((int)stationNumber.getData());
		out.writeFloat(setCentrelineAzimuthAngle);
		out.writeFloat(setCentrelineElevationAngle);
		out.writeByte(setZoom.ordinal());
		out.writeFloat(setHorizontalFieldOfView);
		out.writeFloat(setVerticalFieldOfView);
		out.writeFloat(horizontalSlewRate);
		out.writeFloat(verticalSlewRate);
		out.writeDouble(latitude);
		out.writeDouble(longitude);
		out.writeDouble(altitude);
		out.writeByte(altitudeType.ordinal());
		out.writeByte(setFocus.ordinal());
		out.writeByte(focusType.ordinal());
	}

	@Override
	public void resetValues() {
		stationNumber.setData(0);
		setCentrelineAzimuthAngle = 0;
		setCentrelineElevationAngle = 0;
		setZoom = SetZoom.values()[0];
		setHorizontalFieldOfView = (float)Math.toRadians(55);
		setVerticalFieldOfView = (float)Math.toRadians(35);
		horizontalSlewRate = 0;
		verticalSlewRate = 0;
		latitude = 0;
		longitude = 0;
		altitude = 0;
		altitudeType = AltitudeType.values()[0];
		setFocus = SetFocus.values()[0];
		focusType = FocusType.values()[0];
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M200;
	}
	
	public BitmappedStation getStationNumber() {
		return stationNumber;
	}

	public void setStationNumber(BitmappedStation stationNumber) {
		this.stationNumber = stationNumber;
	}

	public float getSetCentrelineAzimuthAngle() {
		return setCentrelineAzimuthAngle;
	}

	public void setSetCentrelineAzimuthAngle(float setCentrelineAzimuthAngle) {
		this.setCentrelineAzimuthAngle = setCentrelineAzimuthAngle;
	}

	public float getSetCentrelineElevationAngle() {
		return setCentrelineElevationAngle;
	}

	public void setSetCentrelineElevationAngle(float setCentrelineElevationAngle) {
		this.setCentrelineElevationAngle = setCentrelineElevationAngle;
	}

	public SetZoom getSetZoom() {
		return setZoom;
	}

	public void setSetZoom(SetZoom setZoom) {
		this.setZoom = setZoom;
	}

	public float getSetHorizontalFieldOfView() {
		return setHorizontalFieldOfView;
	}

	public void setSetHorizontalFieldOfView(float setHorizontalFieldOfView) {
		this.setHorizontalFieldOfView = setHorizontalFieldOfView;
	}

	public float getSetVerticalFieldOfView() {
		return setVerticalFieldOfView;
	}

	public void setSetVerticalFieldOfView(float setVerticalFieldOfView) {
		this.setVerticalFieldOfView = setVerticalFieldOfView;
	}

	public float getHorizontalSlewRate() {
		return horizontalSlewRate;
	}

	public void setHorizontalSlewRate(float horizontalSlewRate) {
		this.horizontalSlewRate = horizontalSlewRate;
	}

	public float getVerticalSlewRate() {
		return verticalSlewRate;
	}

	public void setVerticalSlewRate(float verticalSlewRate) {
		this.verticalSlewRate = verticalSlewRate;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public AltitudeType getAltitudeType() {
		return altitudeType;
	}

	public void setAltitudeType(AltitudeType altitudeType) {
		this.altitudeType = altitudeType;
	}

	public SetFocus getSetFocus() {
		return setFocus;
	}

	public void setSetFocus(SetFocus setFocus) {
		this.setFocus = setFocus;
	}

	public FocusType getFocusType() {
		return focusType;
	}

	public void setFocusType(FocusType focusType) {
		this.focusType = focusType;
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
		PayloadSteeringCommand other = (PayloadSteeringCommand) obj;
		if (stationNumber != other.stationNumber)
			return false;
		return true;
	}

	@Override
	public BitmappedStation getTargetStations() {
		return stationNumber;
	}
	
}
