package br.skylight.cucs.plugins.vehicleconfiguration;

import javax.swing.JPanel;

import br.skylight.commons.Vehicle;
import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;

@ExtensionPointDefinition
public interface VehicleCommandsSectionExtensionPoint {

	public JPanel getSectionComponent();
	public String getSectionName();
	
	/**
	 * Updates current vehicle.
	 * Returns true if this section should be visible
	 */
	public boolean updateVehicle(Vehicle vehicle);
	
}
