package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.ModeState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class ModePreferenceReport extends Message<ModePreferenceReport> {

	private ModeState altitudeModeState;
	private ModeState speedModeState;
	private ModeState courseHeadingModeState;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M109;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		altitudeModeState = ModeState.values()[in.readUnsignedByte()];
		speedModeState = ModeState.values()[in.readUnsignedByte()];
		courseHeadingModeState = ModeState.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(altitudeModeState.ordinal());
		out.writeByte(speedModeState.ordinal());
		out.writeByte(courseHeadingModeState.ordinal());
	}

	@Override
	public void resetValues() {
		altitudeModeState = ModeState.values()[0];
		speedModeState = ModeState.values()[0];
		courseHeadingModeState = ModeState.values()[0];
	}

	public ModeState getAltitudeModeState() {
		return altitudeModeState;
	}

	public void setAltitudeModeState(ModeState altitudeModeState) {
		this.altitudeModeState = altitudeModeState;
	}

	public ModeState getSpeedModeState() {
		return speedModeState;
	}

	public void setSpeedModeState(ModeState speedModeState) {
		this.speedModeState = speedModeState;
	}

	public ModeState getCourseHeadingModeState() {
		return courseHeadingModeState;
	}

	public void setCourseHeadingModeState(ModeState courseHeadingModeState) {
		this.courseHeadingModeState = courseHeadingModeState;
	}

}
