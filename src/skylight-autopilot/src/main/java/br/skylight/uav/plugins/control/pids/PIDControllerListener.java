package br.skylight.uav.plugins.control.pids;

public interface PIDControllerListener {

	public float onSetpointSet(float targetSetPointValue);

}
