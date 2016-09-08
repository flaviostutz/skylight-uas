package br.skylight.cucs.plugins.vehicleconfiguration;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class)
public class MessagesConfigExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View createView() {
		View v = new MessagesConfigView(this);
		pluginManager.manageObject(v);
		return v;
	}

	@Override
	public String getMenuLabel() {
		return "Messages Configuration";
	}

	@Override
	public int getMenuOrder() {
		return 99;
	}

}
