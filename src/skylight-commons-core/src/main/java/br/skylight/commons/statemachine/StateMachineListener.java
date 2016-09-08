package br.skylight.commons.statemachine;

public interface StateMachineListener<I> {

	public void onStateChanged(I newState, I oldState);
	
}
