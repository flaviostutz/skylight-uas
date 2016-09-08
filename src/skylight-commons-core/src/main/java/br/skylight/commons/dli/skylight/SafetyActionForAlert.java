package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.Alert;
import br.skylight.commons.SafetyAction;
import br.skylight.commons.infra.SerializableState;

public class SafetyActionForAlert implements SerializableState {

	private Alert alert;
	private SafetyAction safetyAction;
	
	public SafetyActionForAlert() {
	}
	
	public SafetyActionForAlert(Alert alert, SafetyAction safetyAction) {
		this.alert = alert;
		this.safetyAction = safetyAction;
	}
	
	public Alert getAlert() {
		return alert;
	}
	public SafetyAction getSafetyAction() {
		return safetyAction;
	}
	public void setAlert(Alert alert) {
		this.alert = alert;
	}
	public void setSafetyAction(SafetyAction safetyAction) {
		this.safetyAction = safetyAction;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		alert = Alert.values()[in.readUnsignedByte()];
		safetyAction = SafetyAction.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeByte(alert.ordinal());
		out.writeByte(safetyAction.ordinal());
	}
	
}
