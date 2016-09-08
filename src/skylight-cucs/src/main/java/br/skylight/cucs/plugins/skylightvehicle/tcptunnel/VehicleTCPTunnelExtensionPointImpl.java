package br.skylight.cucs.plugins.skylightvehicle.tcptunnel;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=ViewExtensionPoint.class)
public class VehicleTCPTunnelExtensionPointImpl implements ViewExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public View createView() {
		VehicleTCPTunnelView v = new VehicleTCPTunnelView(this);
		pluginManager.manageObject(v);
		return v;
	}

	@Override
	public String getMenuLabel() {
		return "Vehicle TCP Tunnel";
	}

	@Override
	public int getMenuOrder() {
		return 0;
	}

}
