package br.skylight.commons.statemachine;

public interface StateListener {

	public void onEntry() throws Exception;
	public void onStep() throws Exception;
	public void onExit() throws Exception;

}
