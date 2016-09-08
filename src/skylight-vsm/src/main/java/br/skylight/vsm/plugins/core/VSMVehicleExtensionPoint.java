package br.skylight.vsm.plugins.core;

import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;
import br.skylight.vsm.VSMVehicle;

@ExtensionPointDefinition
public interface VSMVehicleExtensionPoint {

	public int getVehicleId();
	public Class<? extends VSMVehicle> getVSMVehicleClass();
	
}
