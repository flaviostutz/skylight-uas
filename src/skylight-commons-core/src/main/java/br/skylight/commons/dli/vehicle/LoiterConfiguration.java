package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.LoiterDirection;
import br.skylight.commons.dli.enums.LoiterType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class LoiterConfiguration extends Message<LoiterConfiguration> {

	private LoiterType loiterType;//u1
	private float loiterRadius;
	private float loiterLength;
	private float loiterBearing;
	private LoiterDirection loiterDirection;//u1
	private float loiterAltitude;
	private AltitudeType altitudeType;//u1
	private float loiterSpeed;
	private SpeedType speedType;//u1

	//transient
	private double latitude;
	private double longitude;
	
	public LoiterConfiguration() {
		resetValues();
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		loiterType = LoiterType.values()[in.readUnsignedByte()];//u1
		loiterRadius = in.readFloat();
		loiterLength = in.readFloat();
		loiterBearing = in.readFloat();
		loiterDirection = LoiterDirection.values()[in.readUnsignedByte()];//u1
		loiterAltitude = in.readFloat();
		altitudeType = AltitudeType.values()[in.readUnsignedByte()];//u1
		loiterSpeed = in.readFloat();
		speedType = SpeedType.values()[in.readUnsignedByte()];//u1
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(loiterType.ordinal());//u1
		out.writeFloat(loiterRadius);
		out.writeFloat(loiterLength);
		out.writeFloat(loiterBearing);
		out.writeByte(loiterDirection.ordinal());//u1
		out.writeFloat(loiterAltitude);
		out.writeByte(altitudeType.ordinal());//u1
		out.writeFloat(loiterSpeed);
		out.writeByte(speedType.ordinal());//u1
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M41;
	}

	@Override
	public void resetValues() {
		loiterType = LoiterType.CIRCULAR;
		loiterRadius = 200;
		loiterLength = 0;
		loiterBearing = 0;
		loiterDirection = LoiterDirection.VEHICLE_DEPENDENT;
		loiterAltitude = 200;
		altitudeType = AltitudeType.AGL;
		loiterSpeed = 20;
		speedType = SpeedType.INDICATED_AIRSPEED;
	}

	public LoiterType getLoiterType() {
		return loiterType;
	}

	public void setLoiterType(LoiterType loiterType) {
		this.loiterType = loiterType;
	}

	public float getLoiterRadius() {
		return loiterRadius;
	}

	public void setLoiterRadius(float loiterRadius) {
		this.loiterRadius = loiterRadius;
	}

	public float getLoiterLength() {
		return loiterLength;
	}

	public void setLoiterLength(float loiterLength) {
		this.loiterLength = loiterLength;
	}

	public float getLoiterBearing() {
		return loiterBearing;
	}

	public void setLoiterBearing(float loiterBearing) {
		this.loiterBearing = loiterBearing;
	}

	public LoiterDirection getLoiterDirection() {
		return loiterDirection;
	}

	public void setLoiterDirection(LoiterDirection loiterDirection) {
		this.loiterDirection = loiterDirection;
	}

	public float getLoiterAltitude() {
		return loiterAltitude;
	}

	public void setLoiterAltitude(float loiterAltitude) {
		this.loiterAltitude = loiterAltitude;
	}

	public AltitudeType getAltitudeType() {
		return altitudeType;
	}

	public void setAltitudeType(AltitudeType altitudeType) {
		this.altitudeType = altitudeType;
	}

	public float getLoiterSpeed() {
		return loiterSpeed;
	}

	public void setLoiterSpeed(float loiterSpeed) {
		this.loiterSpeed = loiterSpeed;
	}

	public SpeedType getSpeedType() {
		return speedType;
	}

	public void setSpeedType(SpeedType speedType) {
		this.speedType = speedType;
	}
	
	/**
	 * This IS NOT from STANAG and is not serialized
	 */
	public double getLatitude() {
		return latitude;
	}
	/**
	 * This IS NOT from STANAG and is not serialized
	 * Should be used only for GUI internal events
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	/**
	 * This IS NOT from STANAG and is not serialized
	 * Should be used only for GUI internal events
	 */
	public double getLongitude() {
		return longitude;
	}
	/**
	 * This IS NOT from STANAG and is not serialized
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void copyParametersFrom(LoiterConfiguration m) {
		this.loiterType = m.loiterType;
		this.loiterRadius = m.loiterRadius;
		this.loiterLength = m.loiterLength;
		this.loiterBearing = m.loiterBearing;
		this.loiterDirection = m.loiterDirection;
		this.loiterAltitude = m.loiterAltitude;
		this.altitudeType = m.altitudeType;
		this.loiterSpeed = m.loiterSpeed;
		this.speedType = m.speedType;
		this.latitude = m.latitude;
		this.longitude = m.longitude;
	}
	
}
