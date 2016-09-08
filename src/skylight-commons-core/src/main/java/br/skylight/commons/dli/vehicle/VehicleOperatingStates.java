package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.AltitudeCommandType;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.HeadingCommandType;
import br.skylight.commons.dli.enums.LandingGearState;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class VehicleOperatingStates extends Message<VehicleOperatingStates> {

	private float commandedAltitude;
	private AltitudeType altitudeType;//u1
	private float commandedHeading;
	private float commandedCourse;
	private float commandedTurnRate;
	private float commandedRollRate;
	private float commandedSpeed;
	private SpeedType speedType;//u1
	private int powerLevel;//u2
	private byte flapDeploymentAngle;//u1
	private byte speedBrakeDeploymentAngle;//u1
	private LandingGearState landingGearState;//u1
	private float currentPropulsionEnergyLevel;
	private float currentPropulsionEnergyUsageRate;
	private float commandedRoll;
	private AltitudeCommandType altitudeCommandType;//u1
	private HeadingCommandType headingCommandType;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M104;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		commandedAltitude = in.readFloat();
		altitudeType = AltitudeType.values()[in.readUnsignedByte()];
		commandedHeading = in.readFloat();
		commandedCourse = in.readFloat();
		commandedTurnRate = in.readFloat();
		commandedRollRate = in.readFloat();
		commandedSpeed = in.readFloat();
		speedType = SpeedType.values()[in.readUnsignedByte()];
		powerLevel = in.readUnsignedShort();
		flapDeploymentAngle = in.readByte();
		speedBrakeDeploymentAngle = in.readByte();
		landingGearState = LandingGearState.values()[in.readUnsignedByte()];
		currentPropulsionEnergyLevel = in.readFloat();
		currentPropulsionEnergyUsageRate = in.readFloat();
		commandedRoll = in.readFloat();
		altitudeCommandType = AltitudeCommandType.values()[in.readUnsignedByte()];
		headingCommandType = HeadingCommandType.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeFloat(commandedAltitude);
		out.writeByte(altitudeType.ordinal());
		out.writeFloat(commandedHeading);
		out.writeFloat(commandedCourse);
		out.writeFloat(commandedTurnRate);
		out.writeFloat(commandedRollRate);
		out.writeFloat(commandedSpeed);
		out.writeByte(speedType.ordinal());
		out.writeShort(powerLevel);
		out.writeByte(flapDeploymentAngle);
		out.writeByte(speedBrakeDeploymentAngle);
		out.writeByte(landingGearState.ordinal());
		out.writeFloat(currentPropulsionEnergyLevel);
		out.writeFloat(currentPropulsionEnergyUsageRate);
		out.writeFloat(commandedRoll);
		out.writeByte(altitudeCommandType.ordinal());
		out.writeByte(headingCommandType.ordinal());
	}

	@Override
	public void resetValues() {
		commandedAltitude = 0;
		altitudeType = AltitudeType.values()[0];
		commandedHeading = 0;
		commandedCourse = 0;
		commandedTurnRate = 0;
		commandedRollRate = 0;
		commandedSpeed = 0;
		speedType = SpeedType.values()[0];
		powerLevel = 0;
		flapDeploymentAngle = (byte)0;
		speedBrakeDeploymentAngle = (byte)0;
		landingGearState = LandingGearState.values()[0];
		currentPropulsionEnergyLevel = 0;
		currentPropulsionEnergyUsageRate = 0;
		commandedRoll = 0;
		altitudeCommandType = AltitudeCommandType.NO_VALID_ALTITUDE_COMMAND;
		headingCommandType = HeadingCommandType.NO_VALID_HEADING_COMMAND;
	}

	public float getCommandedAltitude() {
		return commandedAltitude;
	}

	public void setCommandedAltitude(float commandedAltitude) {
		this.commandedAltitude = commandedAltitude;
	}

	public AltitudeType getAltitudeType() {
		return altitudeType;
	}

	public void setAltitudeType(AltitudeType altitudeType) {
		this.altitudeType = altitudeType;
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

	public int getPowerLevel() {
		return powerLevel;
	}

	public void setPowerLevel(int powerLevel) {
		this.powerLevel = powerLevel;
	}

	public byte getFlapDeploymentAngle() {
		return flapDeploymentAngle;
	}

	public void setFlapDeploymentAngle(byte flapDeploymentAngle) {
		this.flapDeploymentAngle = flapDeploymentAngle;
	}

	public byte getSpeedBrakeDeploymentAngle() {
		return speedBrakeDeploymentAngle;
	}

	public void setSpeedBrakeDeploymentAngle(byte speedBrakeDeploymentAngle) {
		this.speedBrakeDeploymentAngle = speedBrakeDeploymentAngle;
	}

	public LandingGearState getLandingGearState() {
		return landingGearState;
	}

	public void setLandingGearState(LandingGearState landingGearState) {
		this.landingGearState = landingGearState;
	}

	public float getCurrentPropulsionEnergyLevel() {
		return currentPropulsionEnergyLevel;
	}

	public void setCurrentPropulsionEnergyLevel(float currentPropulsionEnergyLevel) {
		this.currentPropulsionEnergyLevel = currentPropulsionEnergyLevel;
	}

	public float getCurrentPropulsionEnergyUsageRate() {
		return currentPropulsionEnergyUsageRate;
	}

	public void setCurrentPropulsionEnergyUsageRate(float currentPropulsionEnergyUsageRate) {
		this.currentPropulsionEnergyUsageRate = currentPropulsionEnergyUsageRate;
	}

	public float getCommandedRoll() {
		return commandedRoll;
	}

	public void setCommandedRoll(float commandedRoll) {
		this.commandedRoll = commandedRoll;
	}

	public AltitudeCommandType getAltitudeCommandType() {
		return altitudeCommandType;
	}

	public void setAltitudeCommandType(AltitudeCommandType altitudeCommandType) {
		this.altitudeCommandType = altitudeCommandType;
	}

	public HeadingCommandType getHeadingCommandType() {
		return headingCommandType;
	}

	public void setHeadingCommandType(HeadingCommandType headingCommandType) {
		this.headingCommandType = headingCommandType;
	}
	
}
