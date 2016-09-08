package br.skylight.cucs.plugins.preferences;

import java.awt.Frame;

import javax.swing.JDialog;

import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.DialogExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=DialogExtensionPoint.class,
								useDependenciesFrom=PreferencesDialog.class)
public class PreferencesDialogExtensionPointImpl extends Worker implements DialogExtensionPoint {

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public JDialog getDialog(Frame owner) {
		PreferencesDialog pd = new PreferencesDialog(owner);
		pluginManager.manageObject(pd);
		return pd;
	}

	@Override
	public String getMenuLabel() {
		return "Preferences...";
	}

	@Override
	public int getMenuOrder() {
		return 99;
	}

}
