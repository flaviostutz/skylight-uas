package br.skylight.commons.plugins.workbench;

import java.awt.Frame;

import javax.swing.JDialog;

import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;

@ExtensionPointDefinition
public interface DialogExtensionPoint extends MenuableExtensionPoint {

	public JDialog getDialog(Frame owner);
	
}
