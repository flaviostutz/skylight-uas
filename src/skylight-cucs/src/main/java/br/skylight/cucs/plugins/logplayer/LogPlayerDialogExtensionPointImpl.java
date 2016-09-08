package br.skylight.cucs.plugins.logplayer;

import java.awt.Frame;

import javax.swing.JDialog;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.DialogExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=DialogExtensionPoint.class)
public class LogPlayerDialogExtensionPointImpl implements DialogExtensionPoint {

	private LogPlayerDialog logPlayerDialog;
	
	@ServiceInjection
	public PluginManager pluginManager;

	@Override
	public JDialog getDialog(Frame owner) {
		return getLogPlayerDialog(owner);
	}
	
	public LogPlayerDialog getLogPlayerDialog(Frame owner) {
		if(logPlayerDialog==null) {
			logPlayerDialog = new LogPlayerDialog(owner);
			pluginManager.manageObject(logPlayerDialog);
		}
		return logPlayerDialog;
	}
	
	@Override
	public String getMenuLabel() {
		return "Log Player";
	}

	@Override
	public int getMenuOrder() {
		return 10;
	}

}
