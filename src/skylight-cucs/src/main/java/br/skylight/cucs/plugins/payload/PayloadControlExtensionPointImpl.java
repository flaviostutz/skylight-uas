package br.skylight.cucs.plugins.payload;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class,
							useDependenciesFrom=PayloadControlView.class)
public class PayloadControlExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;

	@Override
	public View createView() {
		PayloadControlView ah = new PayloadControlView(this);
		pluginManager.manageObject(ah);
		return ah;
	}
	
	@Override
	public String getMenuLabel() {
		return "Payload Control";
	}

	@Override
	public int getMenuOrder() {
		return 22;
	}
	
}
