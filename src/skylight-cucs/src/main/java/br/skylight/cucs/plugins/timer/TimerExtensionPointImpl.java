package br.skylight.cucs.plugins.timer;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class)
public class TimerExtensionPointImpl implements ViewExtensionPoint<TimerState> {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View<TimerState> createView() {
		TimerView tv = new TimerView(this);
		pluginManager.manageObject(tv);
		return tv;
	}
	@Override
	public String getMenuLabel() {
		return "Timer";
	}
	@Override
	public int getMenuOrder() {
		return 99;
	}
	
}
