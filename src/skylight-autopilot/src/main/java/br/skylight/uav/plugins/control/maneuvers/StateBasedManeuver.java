package br.skylight.uav.plugins.control.maneuvers;

import br.skylight.commons.statemachine.StateMachine;

public abstract class StateBasedManeuver extends Maneuver {
	
	private StateMachine<String,Object> stateMachine;
	
	public StateBasedManeuver() {
		stateMachine = setupStateMachine();
	}
	
	@Override
	public void step() {
		stateMachine.step();
	}
	
	public StateMachine<String,Object> getStateMachine() {
		return stateMachine;
	}
	
	protected abstract StateMachine<String,Object> setupStateMachine();
	
}
