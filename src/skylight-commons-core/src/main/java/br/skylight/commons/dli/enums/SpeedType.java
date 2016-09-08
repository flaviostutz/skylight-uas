package br.skylight.commons.dli.enums;

public enum SpeedType {

	INDICATED_AIRSPEED("IAS"), 
	TRUE_AIRSPEED("TAS"), 
	GROUND_SPEED("GSpeed");
	
	private String name;
	
	private SpeedType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static SpeedType getSpeedType(WaypointSpeedType st) {
		return SpeedType.values()[st.ordinal()];
	}
	
}
