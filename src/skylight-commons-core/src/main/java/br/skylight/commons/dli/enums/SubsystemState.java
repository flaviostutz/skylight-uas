package br.skylight.commons.dli.enums;

import java.awt.Color;

public enum SubsystemState {

	NO_STATUS(Color.LIGHT_GRAY, Color.BLACK,"No status"),
	NOMINAL(Color.GREEN, Color.BLACK, "Nominal"),
	CAUTION(Color.YELLOW, Color.BLACK, "Caution"),
	WARNING(Color.ORANGE, Color.BLACK, "Warning"),
	EMERGENCY(Color.RED, Color.WHITE, "Emergency"),
	FAILED(Color.BLACK, Color.WHITE, "Failed");

	private Color color;
	private Color foreground;
	private String name;
	
	private SubsystemState(Color color, Color foreground, String name) {
		this.color = color;
		this.foreground = foreground;
		this.name = name;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}

	public Color getForeground() {
		return foreground;
	}
	
}
