package br.skylight.commons.dli.enums;

import java.awt.Color;

import br.skylight.commons.StringHelper;

public enum AlertPriority {

	CLEARED(Color.GREEN),
	NOMINAL(Color.GREEN),
	CAUTION(Color.YELLOW),
	WARNING(Color.RED),
	EMERGENCY(Color.RED.darker()),
	FAILED(Color.BLACK);

	private Color color;
	
	private AlertPriority(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	}
	
}
