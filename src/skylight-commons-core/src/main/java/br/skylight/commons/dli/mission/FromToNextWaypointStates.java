package br.skylight.commons.dli.mission;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class FromToNextWaypointStates extends Message<FromToNextWaypointStates> {

	private AltitudeType altitudeType;//u1
	private SpeedType speedType;//u1
	private double fromWaypointLatitude;
	private double fromWaypointLongitude;
	private float fromWaypointAltitude;
	private double fromWaypointTime;
	private int fromWaypointNumber;//u2
	private double toWaypointLatitude;
	private double toWaypointLongitude;
	private float toWaypointAltitude;
	private float toWaypointSpeed;
	private double toWaypointTime;
	private int toWaypointNumber;//u2
	private double nextWaypointLatitude;
	private double nextWaypointLongitude;
	private float nextWaypointAltitude;
	private float nextWaypointSpeed;
	private double nextWaypointTime;
	private int nextWaypointNumber;//u2

	public FromToNextWaypointStates() {
		resetValues();
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		altitudeType = AltitudeType.values()[in.readUnsignedByte()];
		speedType = SpeedType.values()[in.readUnsignedByte()];
		fromWaypointLatitude = in.readDouble();
		fromWaypointLongitude = in.readDouble();
		fromWaypointAltitude = in.readFloat();
		fromWaypointTime = in.readDouble();
		fromWaypointNumber = in.readUnsignedShort();
		toWaypointLatitude = in.readDouble();
		toWaypointLongitude = in.readDouble();
		toWaypointAltitude = in.readFloat();
		toWaypointSpeed = in.readFloat();
		toWaypointTime = in.readDouble();
		toWaypointNumber = in.readUnsignedShort();
		nextWaypointLatitude = in.readDouble();
		nextWaypointLongitude = in.readDouble();
		nextWaypointAltitude = in.readFloat();
		nextWaypointSpeed = in.readFloat();
		nextWaypointTime = in.readDouble();
		nextWaypointNumber = in.readUnsignedShort();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(altitudeType.ordinal());
		out.writeByte(speedType.ordinal());
		out.writeDouble(fromWaypointLatitude);
		out.writeDouble(fromWaypointLongitude);
		out.writeFloat(fromWaypointAltitude);
		out.writeDouble(fromWaypointTime);
		out.writeShort(fromWaypointNumber);
		out.writeDouble(toWaypointLatitude);
		out.writeDouble(toWaypointLongitude);
		out.writeFloat(toWaypointAltitude);
		out.writeFloat(toWaypointSpeed);
		out.writeDouble(toWaypointTime);
		out.writeShort(toWaypointNumber);
		out.writeDouble(nextWaypointLatitude);
		out.writeDouble(nextWaypointLongitude);
		out.writeFloat(nextWaypointAltitude);
		out.writeFloat(nextWaypointSpeed);
		out.writeDouble(nextWaypointTime);
		out.writeShort(nextWaypointNumber);
	}

	@Override
	public void resetValues() {
		altitudeType = AltitudeType.values()[0];
		speedType = SpeedType.values()[0];
		fromWaypointLatitude = 0;
		fromWaypointLongitude = 0;
		fromWaypointAltitude = 0;
		fromWaypointTime = 0;
		fromWaypointNumber = 0;
		toWaypointLatitude = 0;
		toWaypointLongitude = 0;
		toWaypointAltitude = 0;
		toWaypointSpeed = 0;
		toWaypointTime = 0;
		toWaypointNumber = 0;
		nextWaypointLatitude = 0;
		nextWaypointLongitude = 0;
		nextWaypointAltitude = 0;
		nextWaypointSpeed = 0;
		nextWaypointTime = 0;
		nextWaypointNumber = 0;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M110;
	}

	public AltitudeType getAltitudeType() {
		return altitudeType;
	}

	public void setAltitudeType(AltitudeType altitudeType) {
		this.altitudeType = altitudeType;
	}

	public SpeedType getSpeedType() {
		return speedType;
	}

	public void setSpeedType(SpeedType speedType) {
		this.speedType = speedType;
	}

	public double getFromWaypointLatitude() {
		return fromWaypointLatitude;
	}

	public void setFromWaypointLatitude(double fromWaypointLatitude) {
		this.fromWaypointLatitude = fromWaypointLatitude;
	}

	public double getFromWaypointLongitude() {
		return fromWaypointLongitude;
	}

	public void setFromWaypointLongitude(double fromWaypointLongitude) {
		this.fromWaypointLongitude = fromWaypointLongitude;
	}

	public float getFromWaypointAltitude() {
		return fromWaypointAltitude;
	}

	public void setFromWaypointAltitude(float fromWaypointAltitude) {
		this.fromWaypointAltitude = fromWaypointAltitude;
	}

	public double getFromWaypointTime() {
		return fromWaypointTime;
	}

	public void setFromWaypointTime(double fromWaypointTime) {
		this.fromWaypointTime = fromWaypointTime;
	}

	public int getFromWaypointNumber() {
		return fromWaypointNumber;
	}

	public void setFromWaypointNumber(int fromWaypointNumber) {
		this.fromWaypointNumber = fromWaypointNumber;
	}

	public double getToWaypointLatitude() {
		return toWaypointLatitude;
	}

	public void setToWaypointLatitude(double toWaypointLatitude) {
		this.toWaypointLatitude = toWaypointLatitude;
	}

	public double getToWaypointLongitude() {
		return toWaypointLongitude;
	}

	public void setToWaypointLongitude(double toWaypointLongitude) {
		this.toWaypointLongitude = toWaypointLongitude;
	}

	public float getToWaypointAltitude() {
		return toWaypointAltitude;
	}

	public void setToWaypointAltitude(float toWaypointAltitude) {
		this.toWaypointAltitude = toWaypointAltitude;
	}

	public float getToWaypointSpeed() {
		return toWaypointSpeed;
	}

	public void setToWaypointSpeed(float toWaypointSpeed) {
		this.toWaypointSpeed = toWaypointSpeed;
	}

	public double getToWaypointTime() {
		return toWaypointTime;
	}

	public void setToWaypointTime(double toWaypointTime) {
		this.toWaypointTime = toWaypointTime;
	}

	public int getToWaypointNumber() {
		return toWaypointNumber;
	}

	public void setToWaypointNumber(int toWaypointNumber) {
		this.toWaypointNumber = toWaypointNumber;
	}

	public double getNextWaypointLatitude() {
		return nextWaypointLatitude;
	}

	public void setNextWaypointLatitude(double nextWaypointLatitude) {
		this.nextWaypointLatitude = nextWaypointLatitude;
	}

	public double getNextWaypointLongitude() {
		return nextWaypointLongitude;
	}

	public void setNextWaypointLongitude(double nextWaypointLongitude) {
		this.nextWaypointLongitude = nextWaypointLongitude;
	}

	public float getNextWaypointAltitude() {
		return nextWaypointAltitude;
	}

	public void setNextWaypointAltitude(float nextWaypointAltitude) {
		this.nextWaypointAltitude = nextWaypointAltitude;
	}

	public float getNextWaypointSpeed() {
		return nextWaypointSpeed;
	}

	public void setNextWaypointSpeed(float nextWaypointSpeed) {
		this.nextWaypointSpeed = nextWaypointSpeed;
	}

	public double getNextWaypointTime() {
		return nextWaypointTime;
	}

	public void setNextWaypointTime(double nextWaypointTime) {
		this.nextWaypointTime = nextWaypointTime;
	}

	public int getNextWaypointNumber() {
		return nextWaypointNumber;
	}

	public void setNextWaypointNumber(int nextWaypointNumber) {
		this.nextWaypointNumber = nextWaypointNumber;
	}

}
