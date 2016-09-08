package br.skylight.commons;

public enum Servo {

	AILERON_LEFT("127 for aileron down"),
	AILERON_RIGHT("127 for aileron up"),
	RUDDER("127 for rudder full clockwise"),
	ELEVATOR("127 for elevator up"),
	THROTTLE("127 for full throttle"),
	GENERIC_SERVO("127 for full range"),
	CAMERA_PAN("127 for full clockwise"),
	CAMERA_TILT("127 for full tilt");

	private String instructions = "";

	private Servo(String instructions) {
		this.instructions = instructions;
	}
	
	public String getName() {
		return StringHelper.decapitalize(name(), true);
	}
	
	public String getInstructions() {
		return instructions;
	}
	
}
