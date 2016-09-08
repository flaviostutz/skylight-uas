package br.skylight.commons.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.infra.Worker;

public class StateMachine<I,D> extends Worker {

	private static final Logger logger = Logger.getLogger(StateMachine.class.getName());
	
	private StateMachineListener<I> listener = null;
	private Map<I,StateAdapter> states = new HashMap<I,StateAdapter>();
	private StateListener currentState;
	private I currentStateId = null;
	private I previousStateId = null;
	private D currentStateData = null;
	private long currentStateStartTime;
	private boolean changingState = false;
	
	public void addState(I stateId, StateAdapter stateAdapter) {
		if(states.get(stateId)!=null) {
			throw new IllegalArgumentException("State '" + stateId + "' already exists");
		} else {
			states.put(stateId, stateAdapter);
		}
	}

	public void step() {
		if(currentState!=null) {
			try {
				if(!changingState) {//avoid stepping while executing onEntry()/onExit() procedures
					currentState.onStep();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void enterState(I stateId) {
		enterState(stateId, null);
	}	
	public void enterState(I stateId, D data) {
		this.currentStateData = data;
		if(currentState!=null) {
			try {
				this.changingState = true;
				currentState.onExit();
				previousStateId = currentStateId;
			} catch (Exception e) {
				logger.severe("Exception calling onExit() of " + currentState.getClass() + ". e=" + e.toString());
				logger.throwing("StateMachine", "enterState", e);
				throw new RuntimeException(e);
			} finally {
				this.changingState = false;
			}
		}
		if(stateId==null) return;
		currentState = states.get(stateId);
		currentStateId = stateId;
		if(currentState==null) {
			throw new IllegalArgumentException("State 'stateId' not found");
		}
		try {
			this.changingState = true;
			currentState.onEntry();
		} catch (Exception e) {
			logger.severe("Exception calling onEntry() of " + currentState.getClass() + ". e=" + e.toString());
			logger.throwing("StateMachine", "enterState", e);
			throw new RuntimeException(e);
		} finally {
			this.changingState = false;
		}
		currentStateStartTime = System.currentTimeMillis();
		if(listener!=null) {
			if(!currentStateId.equals(previousStateId)) {
				listener.onStateChanged(currentStateId, previousStateId);
			}
		}
		logger.fine("State: "+currentStateId);
	}
	
	public I getCurrentStateId() {
		return currentStateId;
	}
	
	public StateListener getCurrentState() {
		return currentState;
	}
	
	public double getTimeInCurrentState() {
		return (System.currentTimeMillis()-currentStateStartTime)/1000.0;
	}
	
	public I getPreviousStateId() {
		return previousStateId;
	}
	
	public void setCurrentStateData(D stateData) {
		this.currentStateData = stateData;
	}
	public D getCurrentStateData() {
		return currentStateData;
	}
	
	public void setListener(StateMachineListener<I> listener) {
		this.listener = listener;
	}
	
	public void end() {
		enterState(null);
		this.currentState = null;
	}
	
}
