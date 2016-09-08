package br.skylight.cucs.plugins.dataviewer;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
							useDependenciesFrom=Vehicle3DView.class)
public class Vehicle3DExtensionPointImpl implements ViewExtensionPoint<DataViewerState> {

	@ServiceInjection
	public PluginManager pluginManager;

	@Override
	public View<DataViewerState> createView() {
		Vehicle3DView dv = new Vehicle3DView(this);
		pluginManager.manageObject(dv);
		return dv;
	}
	
	@Override
	public String getMenuLabel() {
		return "Vehicle 3D View";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
