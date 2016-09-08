package br.skylight.uav.plugins.control;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.SafetyAction;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.infra.SerializableState;

public class ControlMode implements SerializableState {

	private FlightPathControlMode mode = FlightPathControlMode.NO_MODE;
	private SafetyAction safetyAction = SafetyAction.DO_NOTHING;
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		mode = FlightPathControlMode.values()[in.readUnsignedByte()];
		safetyAction = SafetyAction.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeByte(mode.ordinal());
		out.writeByte(safetyAction.ordinal());
	}
	
	public FlightPathControlMode getMode() {
		return mode;
	}
	public void setMode(FlightPathControlMode mode) {
		this.mode = mode;
	}
	public SafetyAction getSafetyAction() {
		return safetyAction;
	}
	public void setSafetyAction(SafetyAction safetyAction) {
		this.safetyAction = safetyAction;
	}

}
