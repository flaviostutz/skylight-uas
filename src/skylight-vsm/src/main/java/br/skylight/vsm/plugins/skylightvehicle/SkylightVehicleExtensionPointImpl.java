package br.skylight.vsm.plugins.skylightvehicle;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.uav.plugins.onboardintegration.OnboardVehicleIdService;
import br.skylight.vsm.VSMVehicle;
import br.skylight.vsm.plugins.core.VSMVehicleExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=VSMVehicleExtensionPoint.class)
public class SkylightVehicleExtensionPointImpl implements VSMVehicleExtensionPoint {

	@Override
	public Class<? extends VSMVehicle> getVSMVehicleClass() {
		return SkylightVehicle.class;
	}

	@Override
	public int getVehicleId() {
		return OnboardVehicleIdService.VEHICLE_ID.getVehicleID();
	}

}
