package br.skylight.cucs.plugins.vehiclecontrol;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class)
public class VehicleControlExtensionPointImpl implements ViewExtensionPoint<VehicleControlState> {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View<VehicleControlState> createView() {
		VehicleControlView cv = new VehicleControlView(this);
		pluginManager.manageObject(cv);
		return cv;
	}

	@Override
	public String getMenuLabel() {
		return "Vehicle Control";
	}

	@Override
	public int getMenuOrder() {
		return 0;
	}
	
}
