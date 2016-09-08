package br.skylight.commons.dli.enums;

public enum AltitudeType {

	/** Altitude MSL with default altimeter setting */
	PRESSURE("Pressure"), 
	/** Altitude MSL with real altimeter setting */
	BARO("Baro"),
	/** Altitude above ground level */
	AGL("AGL"), 
	/** Altitude above WGS-84 geoid */
	WGS84("WGS-84");

	private String name;
	
	private AltitudeType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
