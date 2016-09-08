package br.skylight.simulation.xplane;

import java.util.logging.Logger;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugins.streamchannel.StreamChannelPlugin;
import br.skylight.simulation.payload.SimulatedPayloadPlugin;
import br.skylight.uav.plugins.control.ControlPlugin;
import br.skylight.uav.plugins.payload.PayloadPlugin;
import br.skylight.uav.plugins.storage.UAVStoragePlugin;
import br.skylight.uav.plugins.tcptunnel.TCPTunnelPlugin;

public class XPlaneUAV {

	private static final Logger logger = Logger.getLogger(XPlaneUAV.class.getName());

	private static PluginManager pluginManager;
	
	public static void main(String[] args) throws Exception {
		startup();
	}

	public static void startup() {
		logger.info(">>Starting X-Plane UAV systems...");
		pluginManager = PluginManager.getInstance("X-Plane UAV");
		pluginManager.registerPluginInstance(Plugin.class, new ControlPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new XPlaneGatewaysPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new PayloadPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new SimulatedPayloadPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new UAVStoragePlugin());
		pluginManager.registerPluginInstance(Plugin.class, new StreamChannelPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new TCPTunnelPlugin());
		pluginManager.setStartupReadClasspathJars(true);
		pluginManager.setStartupReadClasspathDirs(true);
		pluginManager.setStartupJarsFileNameSufix("plugins");
		pluginManager.setUseCachedIndexForPluginElements(true);
//		pluginManager.registerPluginInstance(Plugin.class, new MonitorPlugin());
		pluginManager.startupPlugins();
		logger.info(">>X-Plane UAV systems started");
	}

	public static void shutdown() {
		pluginManager.shutdownPlugins();
	}
	
}
