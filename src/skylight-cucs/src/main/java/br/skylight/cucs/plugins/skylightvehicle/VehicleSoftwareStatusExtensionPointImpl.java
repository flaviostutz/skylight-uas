package br.skylight.cucs.plugins.skylightvehicle;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
								useDependenciesFrom=VehicleSoftwareStatusView.class)
public class VehicleSoftwareStatusExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View createView() {
		VehicleSoftwareStatusView dv = new VehicleSoftwareStatusView(this);
		pluginManager.manageObject(dv);
		return dv;
	}
	
	@Override
	public String getMenuLabel() {
		return "Vehicle Software Status";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
