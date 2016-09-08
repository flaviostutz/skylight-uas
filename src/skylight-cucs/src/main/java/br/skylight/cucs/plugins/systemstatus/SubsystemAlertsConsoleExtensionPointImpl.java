package br.skylight.cucs.plugins.systemstatus;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
								useDependenciesFrom=SubsystemAlertsView.class)
public class SubsystemAlertsConsoleExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View createView() {
		SubsystemAlertsConsoleView dv = new SubsystemAlertsConsoleView(this);
		pluginManager.manageObject(dv);
		return dv;
	}
	
	@Override
	public String getMenuLabel() {
		return "Alerts console";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
