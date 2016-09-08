package br.skylight.cucs.plugins.flightdirector;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
							useDependenciesFrom=FlightDirectorView.class)
public class FlightDirectorExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;

	@Override
	public View createView() {
		FlightDirectorView ah = new FlightDirectorView(this);
		pluginManager.manageObject(ah);
		return ah;
	}
	
	@Override
	public String getMenuLabel() {
		return "Flight Director/Manual Override";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
