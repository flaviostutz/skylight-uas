package br.skylight.cucs.plugins.missionplan;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
							useDependenciesFrom=MissionPlanView.class)
public class MissionPlanExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;

	@Override
	public View createView() {
		MissionPlanView ah = new MissionPlanView(this);
		pluginManager.manageObject(ah);
		return ah;
	}
	
	@Override
	public String getMenuLabel() {
		return "Mission Plan";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
