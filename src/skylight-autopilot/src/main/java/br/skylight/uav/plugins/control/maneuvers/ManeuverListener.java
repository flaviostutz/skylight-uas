package br.skylight.uav.plugins.control.maneuvers;


public interface ManeuverListener<T extends Maneuver> {

	public void maneuverFinished(T maneuver, boolean interrupted);
	
}
