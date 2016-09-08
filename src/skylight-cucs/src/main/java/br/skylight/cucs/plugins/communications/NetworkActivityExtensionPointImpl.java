package br.skylight.cucs.plugins.communications;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class)
public class NetworkActivityExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View createView() {
		View v = new NetworkActivityView(this);
		pluginManager.manageObject(v);
		return v;
	}

	@Override
	public String getMenuLabel() {
		return "Network Activity";
	}

	@Override
	public int getMenuOrder() {
		return 99;
	}

}
