package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.ModeState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class ModePreferenceCommand extends Message<ModePreferenceCommand> {

	private ModeState altitudeMode = ModeState.CONFIGURATION;//u1
	private ModeState speedMode = ModeState.CONFIGURATION;//u1
	private ModeState courseHeadingMode = ModeState.CONFIGURATION;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M48;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		altitudeMode = ModeState.values()[in.readUnsignedByte()];
		speedMode = ModeState.values()[in.readUnsignedByte()];
		courseHeadingMode = ModeState.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(altitudeMode.ordinal());
		out.writeByte(speedMode.ordinal());
		out.writeByte(courseHeadingMode.ordinal());
	}

	@Override
	public void resetValues() {
		altitudeMode = ModeState.CONFIGURATION;
		speedMode = ModeState.CONFIGURATION;
		courseHeadingMode = ModeState.CONFIGURATION;
	}

	public ModeState getAltitudeMode() {
		return altitudeMode;
	}

	public void setAltitudeMode(ModeState altitudeMode) {
		this.altitudeMode = altitudeMode;
	}

	public ModeState getSpeedMode() {
		return speedMode;
	}

	public void setSpeedMode(ModeState speedMode) {
		this.speedMode = speedMode;
	}

	public ModeState getCourseHeadingMode() {
		return courseHeadingMode;
	}

	public void setCourseHeadingMode(ModeState courseHeadingMode) {
		this.courseHeadingMode = courseHeadingMode;
	}

}
