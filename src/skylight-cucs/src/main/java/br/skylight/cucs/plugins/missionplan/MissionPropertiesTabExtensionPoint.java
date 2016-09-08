package br.skylight.cucs.plugins.missionplan;

import javax.swing.JPanel;

import br.skylight.commons.Vehicle;
import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;

@ExtensionPointDefinition
public interface MissionPropertiesTabExtensionPoint {

	public String getTabTitle();
	public JPanel createTabPanel(Vehicle vehicle);
	public void onCancelPressed();
	public boolean onOkPressed();
	public boolean isCompatibleWith(Vehicle vehicle);

}
