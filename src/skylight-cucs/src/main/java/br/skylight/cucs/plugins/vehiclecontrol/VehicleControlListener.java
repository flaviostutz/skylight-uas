package br.skylight.cucs.plugins.vehiclecontrol;

import java.util.Map;

import br.skylight.commons.Vehicle;

public interface VehicleControlListener {

	public void onVehiclesUpdated(Map<Integer, Vehicle> knownVehicles, Map<Integer, CUCS> knownCUCS);

}
