package br.skylight.cucs.plugins.vehicleconfiguration;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.vehiclecontrol.VehicleInfoView;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class, 
								useDependenciesFrom=VehicleInfoView.class)
public class VehicleConfigurationExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View createView() {
		VehicleConfigurationView cv = new VehicleConfigurationView(this);
		pluginManager.manageObject(cv);
		return cv;
	}

	@Override
	public String getMenuLabel() {
		return "Vehicle Configuration";
	}

	@Override
	public int getMenuOrder() {
		return 0;
	}
	
}
