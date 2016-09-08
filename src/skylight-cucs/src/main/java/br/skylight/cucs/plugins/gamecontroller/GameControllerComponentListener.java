package br.skylight.cucs.plugins.gamecontroller;

import net.java.games.input.Component;
import net.java.games.input.Controller;

public interface GameControllerComponentListener {

	public void onComponentValueChanged(Controller controller, Component component, float value, double resolvedValue);
	
}
