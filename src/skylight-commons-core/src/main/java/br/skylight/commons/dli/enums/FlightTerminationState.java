package br.skylight.commons.dli.enums;

public enum FlightTerminationState {

	RESET_FT_SYSTEM("RESET"),
	ARM_FT_SYSTEM("ARMED"),
	EXECUTE_FT_SYSTEM("EXECUTED");

	private String name;
	
	private FlightTerminationState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
