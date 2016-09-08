package br.skylight.cucs.widgets;

import net.java.games.input.Component;

public interface HoldControllerWidgetListener {

	public void onHoldClicked(double value);
	public void onUnholdClicked();
	public void onGraphClicked();
	public float getControllerValueToHoldValue(float controllerComponentValue, Component component);
	
}
