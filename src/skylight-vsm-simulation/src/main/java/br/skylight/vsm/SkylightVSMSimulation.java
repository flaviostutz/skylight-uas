package br.skylight.vsm;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugins.logrecorder.LogRecorderPlugin;
import br.skylight.vsm.plugins.core.VSMCorePlugin;
import br.skylight.vsm.plugins.dummyvehicle.DummyVehiclePlugin;
import br.skylight.vsm.plugins.gps.GPSPlugin;
import br.skylight.vsm.plugins.storage.VSMStoragePlugin;

public class SkylightVSMSimulation {

	private static PluginManager pluginManager;

	public static void main(String[] args) throws InterruptedException {
		// VT100 TESTS
		// for(int i=0; i<100; i++) {
		// System.out.print((char) 27 + "[40m");//red color
		// System.out.print((char) 27 + "[10;40H");//position cursor
		// System.out.print("["+i+"]");
		// Thread.sleep(500);
		// }
		startup();
	}

	public static void startup() throws InterruptedException {
		pluginManager = PluginManager.getInstance("SkylightVSMSimulation");
		pluginManager.setLoadPluginsFromServiceLoader(false);
		pluginManager.registerPluginInstance(Plugin.class, new VSMCorePlugin());
		pluginManager.registerPluginInstance(Plugin.class, new GPSPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new LogRecorderPlugin());
		pluginManager.registerPluginInstance(Plugin.class, new VSMStoragePlugin());
		
		pluginManager.registerPluginInstance(Plugin.class, new DummyVehiclePlugin());
//		pluginManager.registerPluginInstance(Plugin.class, new FlightGearVehiclePlugin());
//		pluginManager.registerPluginInstance(Plugin.class, new XPlaneVehiclePlugin());
//		pluginManager.registerPluginInstance(Plugin.class, new SkylightVehiclePlugin());
		
		// pluginManager.registerPluginInstance(Plugin.class, new VSMWatchDogKickerPlugin());
		// pluginManager.setStartupJarsFileNamePrefix("skylight");
		pluginManager.setStartupReadClasspathJars(true);
		pluginManager.setUseCachedIndexForPluginElements(true);
		pluginManager.startupPlugins();
	}

	public static void shutdown() {
		pluginManager.shutdownPlugins();
	}

}
