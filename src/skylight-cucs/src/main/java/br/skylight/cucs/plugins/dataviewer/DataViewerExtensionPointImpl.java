package br.skylight.cucs.plugins.dataviewer;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class)
public class DataViewerExtensionPointImpl implements ViewExtensionPoint<DataViewerState> {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View<DataViewerState> createView() {
		DataViewerView dv = new DataViewerView(this);
		pluginManager.manageObject(dv);
		return dv;
	}
	
	@Override
	public String getMenuLabel() {
		return "Data Viewer";
	}

	@Override
	public int getMenuOrder() {
		return 88;
	}
	
}
