package br.skylight.vsm.plugins.flightgearvehicle;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.simulation.flightgear.FlightGearVehicleIdService;
import br.skylight.vsm.VSMVehicle;
import br.skylight.vsm.plugins.core.VSMVehicleExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=VSMVehicleExtensionPoint.class)
public class FlightGearVehicleExtensionPointImpl implements VSMVehicleExtensionPoint {

	@Override
	public Class<? extends VSMVehicle> getVSMVehicleClass() {
		return FlightGearVehicle.class;
	}

	@Override
	public int getVehicleId() {
		return FlightGearVehicleIdService.VEHICLE_ID.getVehicleID();
	}

}
