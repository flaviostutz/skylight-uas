package br.skylight.vsm.plugins.xplanevehicle;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.simulation.xplane.XPlaneVehicleIdService;
import br.skylight.vsm.VSMVehicle;
import br.skylight.vsm.plugins.core.VSMVehicleExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=VSMVehicleExtensionPoint.class)
public class XPlaneVehicleExtensionPointImpl implements VSMVehicleExtensionPoint {

	@Override
	public Class<? extends VSMVehicle> getVSMVehicleClass() {
		return XPlaneVehicle.class;
	}

	@Override
	public int getVehicleId() {
		return XPlaneVehicleIdService.VEHICLE_ID.getVehicleID();
	}

}
