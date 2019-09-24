package br.skylight.simulation.dummy;

import java.util.logging.Logger;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.simulation.payload.SimulatedPayloadPlugin;
import br.skylight.uav.plugins.control.ControlPlugin;
import br.skylight.uav.plugins.payload.PayloadPlugin;
import br.skylight.uav.plugins.storage.UAVStoragePlugin;

public class DummyUAV {

	private static final Logger logger = Logger.getLogger(DummyUAV.class.getName());

	private static PluginManager pluginManager;
	
	public static void main(String[] args) throws Exception {
		startup();
	}

	public static void startup() {
		logger.info(">>Starting Dummy UAV systems...");
		pluginManager = PluginManager.getInstance("DummyUAV");
		pluginManager.registerPluginInstance(Plugin.class, new ControlPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new DummyGatewaysPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new PayloadPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new SimulatedPayloadPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new UAVStoragePlugin());
		pluginManager.setStartupReadClasspathJars(true);
		pluginManager.startupPlugins();
		logger.info(">>Dummy UAV systems started");
	}

	public static void shutdown() {
		pluginManager.shutdownPlugins();
	}
	
}
