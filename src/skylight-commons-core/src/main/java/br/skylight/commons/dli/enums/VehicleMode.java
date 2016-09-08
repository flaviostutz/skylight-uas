package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum VehicleMode {

	LOITER_AROUND_POSITION_MODE(null),
	WAYPOINT_MODE(FlightPathControlMode.WAYPOINT),
	NO_MODE(FlightPathControlMode.NO_MODE),
	AUTOLAND_ENGAGE(FlightPathControlMode.AUTOLAND_ENGAGE),
	PREVIOUS_MODE(null);

	private FlightPathControlMode mode;
	
	private VehicleMode(FlightPathControlMode mode) {
		this.mode = mode;
	}
	
	public FlightPathControlMode getMode() {
		return mode;
	}
	
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	};
	
}
