package br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol;

import java.util.HashMap;
import java.util.Map;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.cucs.plugins.vehiclecontrol.FlightTerminationModeExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=FlightTerminationModeExtensionPoint.class)
public class FlightTerminationModeExtensionPointImpl implements FlightTerminationModeExtensionPoint {

	@Override
	public Map<Integer, String> getModeLabelItems() {
		Map<Integer,String> m = new HashMap<Integer, String>();
		m.put(0, "GO FOR MANUAL RECOVERY");
		m.put(11, "DEPLOY PARACHUTE");
		m.put(22, "LOITER GLIDING TO THE GROUND");
		m.put(33, "SPIN CRASHING GROUND");
		return m;
	}

}
