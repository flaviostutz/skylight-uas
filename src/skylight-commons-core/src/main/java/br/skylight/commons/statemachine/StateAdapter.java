package br.skylight.commons.statemachine;

import br.skylight.commons.infra.Worker;

public abstract class StateAdapter extends Worker implements StateListener {

	@Override
	public void onStep() throws Exception {}

	@Override
	public void onExit() throws Exception {};

	@Override
	public void onEntry() throws Exception {};

}
