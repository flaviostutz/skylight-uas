package br.skylight.uav.plugins.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.infra.SerializableState;

public class MiscStates implements SerializableState {

	private boolean safetyActionsArmed = false;
	private boolean armFailsafesAtStartup = false;
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		safetyActionsArmed = in.readBoolean();
		armFailsafesAtStartup = in.readBoolean();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeBoolean(safetyActionsArmed);
		out.writeBoolean(armFailsafesAtStartup);
	}
	
	public boolean isSafetyActionsArmed() {
		return safetyActionsArmed;
	}
	public void setSafetyActionsArmed(boolean safetyActionsArmed) {
		this.safetyActionsArmed = safetyActionsArmed;
	}
	
	public boolean isArmFailsafesAtStartup() {
		return armFailsafesAtStartup;
	}
	public void setArmFailsafesAtStartup(boolean armFailsafesAtStartup) {
		this.armFailsafesAtStartup = armFailsafesAtStartup;
	}

}
