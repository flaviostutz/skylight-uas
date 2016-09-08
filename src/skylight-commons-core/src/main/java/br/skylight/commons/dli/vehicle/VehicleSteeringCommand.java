package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.AltitudeCommandType;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.HeadingCommandType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class VehicleSteeringCommand extends Message<VehicleSteeringCommand> {

	private AltitudeCommandType altitudeCommandType;//u1
	private float commandedAltitude;
	private float commandedVerticalSpeed;
	private HeadingCommandType headingCommandType;//u1
	private float commandedHeading;
	private float commandedCourse;
	private float commandedTurnRate;
	private float commandedRollRate;
	private float commandedRoll;
	private float commandedSpeed;
	private SpeedType speedType;//u1
	private int commandedWaypointNumber;//u2
	private float altimeterSetting;
	private AltitudeType altitudeType;//u1
	private double loiterPositionLatitude;
	private double loiterPositionLongitude;

	public static final float STANDARD_MSL_PRESSURE = 101325;
	
	public VehicleSteeringCommand() {
		resetValues();
	}
	
	public AltitudeCommandType getAltitudeCommandType() {
		return altitudeCommandType;
	}

	public void setAltitudeCommandType(AltitudeCommandType altitudeCommandType) {
		this.altitudeCommandType = altitudeCommandType;
	}

	public float getCommandedAltitude() {
		return commandedAltitude;
	}

	public void setCommandedAltitude(float commandedAltitude) {
		this.commandedAltitude = commandedAltitude;
	}

	public float getCommandedVerticalSpeed() {
		return commandedVerticalSpeed;
	}

	public void setCommandedVerticalSpeed(float commandedVerticalSpeed) {
		this.commandedVerticalSpeed = commandedVerticalSpeed;
	}

	public HeadingCommandType getHeadingCommandType() {
		return headingCommandType;
	}

	public void setHeadingCommandType(HeadingCommandType headingCommandType) {
		this.headingCommandType = headingCommandType;
	}

	public float getCommandedHeading() {
		return commandedHeading;
	}

	public void setCommandedHeading(float commandedHeading) {
		this.commandedHeading = commandedHeading;
	}

	public float getCommandedCourse() {
		return commandedCourse;
	}

	public void setCommandedCourse(float commandedCourse) {
		this.commandedCourse = commandedCourse;
	}

	public float getCommandedTurnRate() {
		return commandedTurnRate;
	}

	public void setCommandedTurnRate(float commandedTurnRate) {
		this.commandedTurnRate = commandedTurnRate;
	}

	public float getCommandedRollRate() {
		return commandedRollRate;
	}

	public void setCommandedRollRate(float commandedRollRate) {
		this.commandedRollRate = commandedRollRate;
	}

	public float getCommandedRoll() {
		return commandedRoll;
	}

	public void setCommandedRoll(float commandedRoll) {
		this.commandedRoll = commandedRoll;
	}

	public float getCommandedSpeed() {
		return commandedSpeed;
	}

	public void setCommandedSpeed(float commandedSpeed) {
		this.commandedSpeed = commandedSpeed;
	}

	public SpeedType getSpeedType() {
		return speedType;
	}

	public void setSpeedType(SpeedType speedType) {
		this.speedType = speedType;
	}

	public int getCommandedWaypointNumber() {
		return commandedWaypointNumber;
	}

	public void setCommandedWaypointNumber(int commandedWaypointNumber) {
		this.commandedWaypointNumber = commandedWaypointNumber;
	}

	public float getAltimeterSetting() {
		return altimeterSetting;
	}

	public void setAltimeterSetting(float altimeterSetting) {
		this.altimeterSetting = altimeterSetting;
	}

	public AltitudeType getAltitudeType() {
		return altitudeType;
	}

	public void setAltitudeType(AltitudeType altitudeType) {
		this.altitudeType = altitudeType;
	}

	public double getLoiterPositionLatitude() {
		return loiterPositionLatitude;
	}

	public void setLoiterPositionLatitude(double loiterPositionLatitude) {
		this.loiterPositionLatitude = loiterPositionLatitude;
	}

	public double getLoiterPositionLongitude() {
		return loiterPositionLongitude;
	}

	public void setLoiterPositionLongitude(double loiterPositionLongitude) {
		this.loiterPositionLongitude = loiterPositionLongitude;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		altitudeCommandType = AltitudeCommandType.values()[in.readUnsignedByte()];//u1
		commandedAltitude = in.readFloat();
		commandedVerticalSpeed = in.readFloat();
		headingCommandType = HeadingCommandType.values()[in.readUnsignedByte()];//u1
		commandedHeading = in.readFloat();
		commandedCourse = in.readFloat();
		commandedTurnRate = in.readFloat();
		commandedRollRate = in.readFloat();
		commandedRoll = in.readFloat();
		commandedSpeed = in.readFloat();
		speedType = SpeedType.values()[in.readUnsignedByte()];//u1
		commandedWaypointNumber = in.readUnsignedShort();//u2
		altimeterSetting = in.readFloat();
		altitudeType = AltitudeType.values()[in.readUnsignedByte()];//u1
		loiterPositionLatitude = in.readDouble();
		loiterPositionLongitude = in.readDouble();
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(altitudeCommandType.ordinal());//u1
		out.writeFloat(commandedAltitude);
		out.writeFloat(commandedVerticalSpeed);
		out.writeByte(headingCommandType.ordinal());//u1
		out.writeFloat(commandedHeading);
		out.writeFloat(commandedCourse);
		out.writeFloat(commandedTurnRate);
		out.writeFloat(commandedRollRate);
		out.writeFloat(commandedRoll);
		out.writeFloat(commandedSpeed);
		out.writeByte(speedType.ordinal());//u1
		out.writeShort(commandedWaypointNumber);//u2
		out.writeFloat(altimeterSetting);
		out.writeByte(altitudeType.ordinal());//u1
		out.writeDouble(loiterPositionLatitude);
		out.writeDouble(loiterPositionLongitude);
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M43;
	}

	@Override
	public void resetValues() {
		altitudeCommandType = AltitudeCommandType.NO_VALID_ALTITUDE_COMMAND;
		commandedAltitude = 0;
		commandedVerticalSpeed = 0;
		headingCommandType = HeadingCommandType.NO_VALID_HEADING_COMMAND;
		commandedHeading = 0;
		commandedCourse = 0;
		commandedTurnRate = 0;
		commandedRollRate = 0;
		commandedRoll = 0;
		commandedSpeed = 0;
		speedType = SpeedType.GROUND_SPEED;
		commandedWaypointNumber = 0;
		altimeterSetting = STANDARD_MSL_PRESSURE;
		altitudeType = AltitudeType.AGL;
		loiterPositionLatitude = 0;
		loiterPositionLongitude = 0;
	}

}
