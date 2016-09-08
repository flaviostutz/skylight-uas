package br.skylight.cucs.plugins.controlmap2d;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
								useDependenciesFrom=ControlMap2DView.class)
public class ControlMap2DExtensionPointImpl implements ViewExtensionPoint<ControlMap2DState> {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View<ControlMap2DState> createView() {
		ControlMap2DView cm = new ControlMap2DView(this);
		pluginManager.manageObject(cm);
		return cm;
	}

	@Override
	public String getMenuLabel() {
		return "Control Map 2D";
	}

	@Override
	public int getMenuOrder() {
		return 99;
	}
	
}
