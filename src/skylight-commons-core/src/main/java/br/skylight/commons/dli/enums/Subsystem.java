package br.skylight.commons.dli.enums;

public enum Subsystem {

	ENGINE("Engine"),
	MECHANICAL("Mechanical"),
	ELECTRICAL("Electrical"),
	COMMS("Communications"),
	PROPULSION_ENERGY("Propulsion"),
	NAVIGATION("Navigation"),
	PAYLOAD("Payload"),
	RECOVERY_SYSTEM("Recovery System"),
	ENVIRONMENTAL_CONTROL_SYSTEM("Environmental Control"),
	VSM_STATUS("VSM Status"),
	ADT("Air Data Terminal"),
	GDT("Ground Data Terminal");
	
	private String name;
	
	private Subsystem(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
