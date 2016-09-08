package br.skylight.cucs.plugins.skylightvehicle;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class)
public class ServoActuationViewExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View createView() {
		ServoActuationView v = new ServoActuationView(this);
		pluginManager.manageObject(v);
		return v;
	}

	@Override
	public String getMenuLabel() {
		return "Servo Actuation";
	}

	@Override
	public int getMenuOrder() {
		return 0;
	}

}
