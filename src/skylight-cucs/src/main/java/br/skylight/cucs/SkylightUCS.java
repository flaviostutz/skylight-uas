package br.skylight.cucs;

import java.io.File;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.SplashScreenStarter;
import br.skylight.commons.plugins.workbench.Workbench;

public class SkylightUCS {

	public static void main(String[] args) throws InterruptedException {
		System.setProperty(Workbench.PROPERTY_ICON_URL, SkylightUCS.class.getResource("/br/skylight/cucs/images/plane.gif").toString());
		System.setProperty(Workbench.PROPERTY_BASE_DIR, File.separator + "Skylight");
		
		PluginManager pm = PluginManager.getInstance("CUCS");
		pm.setStartupJarsFileNamePrefix("skylight");
		pm.setStartupReadClasspathJars(true);
		pm.setUseCachedIndexForPluginElements(true);
		SplashScreenStarter.startupPlugins(pm);
	}

}
