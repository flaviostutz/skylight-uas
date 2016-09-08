package br.skylight.cucs.plugins.vehicleconfiguration;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.skylightvehicle.preflight.AutoChecklistView;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
							useDependenciesFrom=AutoChecklistView.class)
public class VehicleCommandsExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;

	@Override
	public View createView() {
		VehicleCommandsView ah = new VehicleCommandsView(this);
		pluginManager.manageObject(ah);
		return ah;
	}
	
	@Override
	public String getMenuLabel() {
		return "Vehicle Commands";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
