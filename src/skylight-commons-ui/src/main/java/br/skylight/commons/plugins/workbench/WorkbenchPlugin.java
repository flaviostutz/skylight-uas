package br.skylight.commons.plugins.workbench;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.PluginManagerListener;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.PluginDefinition;
import br.skylight.commons.plugin.annotations.ServiceInjection;

@PluginDefinition(members={DialogExtensionPoint.class, ViewExtensionPoint.class})
public class WorkbenchPlugin extends Plugin implements PluginManagerListener {

	private static final Logger logger = Logger.getLogger(WorkbenchPlugin.class.getName());
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	@ExtensionPointsInjection
	public List<ViewExtensionPoint> viewExtensionPoints;
	
	@ExtensionPointsInjection
	public List<DialogExtensionPoint> dialogExtensionPoints;
	
	@ServiceInjection
	public Workbench workbench;
	
	@Override
	public void onActivate() throws Exception {
		pluginManager.addPluginManagerListener(this);
	}
	
	@Override
	public void onDeactivate() throws Exception {
		if(workbench!=null) {
			workbench.setVisible(false);
		}
	}
	
	@Override
	public void onPluginsStartupFinished(boolean partial) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					pluginManager.log("Loading views and dialogs...");
					workbench.loadExtensionPoints();
					
					pluginManager.log("Loading perspectives...");
					workbench.loadPerspectives();

					pluginManager.log("Loading preferences...");
					workbench.loadPreferences();
					
					pluginManager.log("Opening workbench...");
					workbench.updateGUI();
					workbench.setVisible(true);
					workbench.toFront();
				}
			});
		} catch (Exception e) {
			logger.throwing(null,null,e);
			e.printStackTrace();
		}
	}
	@Override
	public void onPluginsStartupFailed() {
	}
	@Override
	public void onStartupStatusChanged(String message, int percent) {
	}
	
	@Override
	public String getName() {
		return "Workbench";
	}
	
}
