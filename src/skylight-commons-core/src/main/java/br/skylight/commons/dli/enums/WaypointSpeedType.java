package br.skylight.commons.dli.enums;

public enum WaypointSpeedType {

	INDICATED_AIRSPEED("IAS"), 
	TRUE_AIRSPEED("TAS"), 
	GROUND_SPEED("Ground Speed"),
	ARRIVAL_TIME("Arrival time");

	private String name;
	
	private WaypointSpeedType(String name) {
		this.name = name;
	}
	
	public static WaypointSpeedType getWaypointSpeedType(SpeedType speedType) {
		return values()[speedType.ordinal()];
	}
	
	@Override
	public String toString() {
		return name;
	}

}
