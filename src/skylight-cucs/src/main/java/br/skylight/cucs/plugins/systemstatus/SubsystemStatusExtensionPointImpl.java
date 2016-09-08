package br.skylight.cucs.plugins.systemstatus;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
								useDependenciesFrom=SubsystemStatusView.class)
public class SubsystemStatusExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View createView() {
		SubsystemStatusView dv = new SubsystemStatusView(this);
		pluginManager.manageObject(dv);
		return dv;
	}
	
	@Override
	public String getMenuLabel() {
		return "Subsystem Status";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
