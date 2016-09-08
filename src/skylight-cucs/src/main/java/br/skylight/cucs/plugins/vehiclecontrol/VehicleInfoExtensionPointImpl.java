package br.skylight.cucs.plugins.vehiclecontrol;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class, 
								useDependenciesFrom=VehicleInfoView.class)
public class VehicleInfoExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View<VehicleControlState> createView() {
		VehicleInfoView cv = new VehicleInfoView(this);
		pluginManager.manageObject(cv);
		return cv;
	}

	@Override
	public String getMenuLabel() {
		return "Vehicle Info";
	}

	@Override
	public int getMenuOrder() {
		return 0;
	}
	
}
