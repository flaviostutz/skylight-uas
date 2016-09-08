package br.skylight.cucs.plugins.preferences;

import javax.swing.JPanel;

import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;

@ExtensionPointDefinition
public interface PreferencesSectionExtensionPoint {

	public JPanel getContents();
	public String getName();
	public int getOrder();
	public void save();
	public void load();

}
