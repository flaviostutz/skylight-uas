package br.skylight.cucs.plugins.vehiclecontrol;

import java.io.Serializable;

public class VehicleControlState implements Serializable {

	private int lastSelectedVehicleId;
	
	public int getLastSelectedVehicleId() {
		return lastSelectedVehicleId;
	}
	public void setLastSelectedVehicleId(int lastSelectedVehicleId) {
		this.lastSelectedVehicleId = lastSelectedVehicleId;
	}

}
