package br.skylight.vsm;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugins.logrecorder.LogRecorderPlugin;
import br.skylight.vsm.plugins.core.VSMCorePlugin;
import br.skylight.vsm.plugins.gps.GPSPlugin;
import br.skylight.vsm.plugins.skylightvehicle.SkylightVehiclePlugin;
import br.skylight.vsm.plugins.storage.VSMStoragePlugin;
import br.skylight.vsm.plugins.watchdogkicker.VSMWatchDogKickerPlugin;

public class SkylightVSM {

	private static PluginManager pluginManager;

	public static void main(String[] args) throws InterruptedException {
		startup();
	}

	public static void startup() throws InterruptedException {
		pluginManager = PluginManager.getInstance("SkylightVSM");
		pluginManager.setLoadPluginsFromServiceLoader(false);
		pluginManager.registerPluginInstance(Plugin.class, new VSMCorePlugin());
		pluginManager.registerPluginInstance(Plugin.class, new GPSPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new LogRecorderPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new VSMStoragePlugin());
		
		pluginManager.registerPluginInstance(Plugin.class, new SkylightVehiclePlugin());
		pluginManager.registerPluginInstance(Plugin.class, new VSMWatchDogKickerPlugin());
		
		pluginManager.setStartupJarsFileNamePrefix("skylight");
		pluginManager.setStartupReadClasspathJars(true);
		pluginManager.setUseCachedIndexForPluginElements(true);
		pluginManager.startupPlugins();
	}

	public static void shutdown() {
		pluginManager.shutdownPlugins();
	}

}
