package br.skylight.simulation.flightgear;

import java.util.logging.Logger;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugins.streamchannel.StreamChannelPlugin;
import br.skylight.simulation.payload.SimulatedPayloadPlugin;
import br.skylight.uav.plugins.control.ControlPlugin;
import br.skylight.uav.plugins.payload.PayloadPlugin;
import br.skylight.uav.plugins.storage.UAVStoragePlugin;
import br.skylight.uav.plugins.tcptunnel.TCPTunnelPlugin;

public class FlightGearUAV {

	private static final Logger logger = Logger.getLogger(FlightGearUAV.class.getName());

	private static PluginManager pluginManager;
	
	public static void main(String[] args) throws Exception {
		startup();
	}

	public static void startup() {
		logger.info(">>Starting FlightGear UAV systems...");
		pluginManager = PluginManager.getInstance("FlightGearUAV");
		pluginManager.registerPluginInstance(Plugin.class, new ControlPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new FlightGearGatewaysPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new PayloadPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new SimulatedPayloadPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new UAVStoragePlugin());
		pluginManager.registerPluginInstance(Plugin.class, new StreamChannelPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new TCPTunnelPlugin());
//		pluginManager.registerPluginInstance(Plugin.class, new MonitorPlugin());
		pluginManager.setStartupReadClasspathJars(true);
		pluginManager.setStartupReadClasspathDirs(true);
		pluginManager.setStartupJarsFileNameSufix("plugins");
		pluginManager.startupPlugins();
		logger.info(">>FlightGear UAV systems started");
	}
	
	public static void shutdown() {
		pluginManager.shutdownPlugins();
	}

}
