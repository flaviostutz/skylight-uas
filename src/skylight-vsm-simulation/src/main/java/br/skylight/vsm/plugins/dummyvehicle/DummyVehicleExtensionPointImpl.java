package br.skylight.vsm.plugins.dummyvehicle;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.simulation.dummy.DummyVehicleIdService;
import br.skylight.vsm.VSMVehicle;
import br.skylight.vsm.plugins.core.VSMVehicleExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=VSMVehicleExtensionPoint.class)
public class DummyVehicleExtensionPointImpl implements VSMVehicleExtensionPoint {

	@Override
	public Class<? extends VSMVehicle> getVSMVehicleClass() {
		return DummyVehicle.class;
	}

	@Override
	public int getVehicleId() {
		return DummyVehicleIdService.VEHICLE_ID.getVehicleID();
	}

}
