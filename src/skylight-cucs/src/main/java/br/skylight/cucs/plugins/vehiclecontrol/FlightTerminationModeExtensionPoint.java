package br.skylight.cucs.plugins.vehiclecontrol;

import java.util.Map;

import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;

@ExtensionPointDefinition
public interface FlightTerminationModeExtensionPoint {

	/**
	 * Map with key/value pair indicating modeCode/modeLabel
	 * Mode label must be a verb
	 * @return
	 */
	public Map<Integer,String> getModeLabelItems();
	
}
