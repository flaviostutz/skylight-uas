package br.skylight.cucs.plugins.loiterdirector;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
							useDependenciesFrom=LoiterDirectorView.class)
public class LoiterDirectorExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;

	@Override
	public View createView() {
		LoiterDirectorView ah = new LoiterDirectorView(this);
		pluginManager.manageObject(ah);
		return ah;
	}
	
	@Override
	public String getMenuLabel() {
		return "Loiter Director";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
