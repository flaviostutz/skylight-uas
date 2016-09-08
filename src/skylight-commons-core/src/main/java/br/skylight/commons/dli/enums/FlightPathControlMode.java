package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum FlightPathControlMode {

	NO_MODE,
	RESERVED,
	FLIGHT_DIRECTOR,
	RESERVED3,RESERVED4,RESERVED5,RESERVED6,RESERVED7,RESERVED8,RESERVED9,RESERVED10,
	WAYPOINT,
	LOITER,
	RESERVED13,RESERVED14,
	AUTOPILOT,
	TERRAIN_AVOIDANCE,
	NAV_AID_SLAVED_NAVIGATION_BEACON,
	RESERVED18,
	AUTOLAND_ENGAGE,
	AUTOLAND_WAVEOFF,
	LAUNCH,
	SLAVE_TO_SENSOR,
	RESERVED23,RESERVED24,RESERVED25,RESERVED26,RESERVED27,RESERVED28,RESERVED29,RESERVED30,RESERVED31,
	
	//skylight specific
	SAFETY_PROCEDURES;

	public boolean isAutopilotEngaged() {
		return ordinal()>=11;
	}

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	}
	
}
