package br.skylight.uav;

import java.util.logging.Logger;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugins.streamchannel.StreamChannelPlugin;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.control.ControlPlugin;
import br.skylight.uav.plugins.messaging.OnboardMessagingPlugin;
import br.skylight.uav.plugins.onboardintegration.OnboardIntegrationPlugin;
import br.skylight.uav.plugins.onboardpayloads.OnboardPayloadsPlugin;
import br.skylight.uav.plugins.payload.PayloadPlugin;
import br.skylight.uav.plugins.storage.UAVStoragePlugin;
import br.skylight.uav.plugins.tcptunnel.TCPTunnelPlugin;

public class SkylightUAV {

	private static final Logger logger = Logger.getLogger(SkylightUAV.class.getName());
	
	private static PluginManager pluginManager;
	
	public static void main(String[] args) throws Exception {
		startup();
	}

	public static void startup() {
		logger.info(">>Starting Skylight UAV systems "+ Commander.UAV_VERSION +"...");
		pluginManager = PluginManager.getInstance("SkylightUAV");
		pluginManager.registerPluginInstance(Plugin.class, new ControlPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new OnboardIntegrationPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new OnboardMessagingPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new PayloadPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new OnboardPayloadsPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new UAVStoragePlugin());
		pluginManager.registerPluginInstance(Plugin.class, new StreamChannelPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new TCPTunnelPlugin());
		pluginManager.setStartupReadClasspathJars(true);
//		pluginManager.setStartupReadClasspathDirs(true);
		pluginManager.setUseCachedIndexForPluginElements(true);
		pluginManager.setStartupJarsFileNamePrefix("skylight");
		pluginManager.startupPlugins();
		logger.info(">>Skylight UAV systems started");
//		pluginManager.waitShutdown();
	}
	
	public static void shutdown() {
		pluginManager.shutdownPlugins();
	}

}
