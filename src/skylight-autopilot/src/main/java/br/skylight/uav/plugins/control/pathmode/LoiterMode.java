package br.skylight.uav.plugins.control.pathmode;

import java.util.logging.Logger;

import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.plugins.control.maneuvers.LoiterManeuver;
import br.skylight.uav.plugins.control.maneuvers.ManeuverListener;
import br.skylight.uav.plugins.storage.RepositoryService;

public class LoiterMode extends FlightPathMode implements ManeuverListener<LoiterManeuver> {

	private static final Logger logger = Logger.getLogger(LoiterMode.class.getName());
	
	@MemberInjection(createNewInstance=true)
	public LoiterManeuver loiterManeuver;
	
	@ServiceInjection
	public RepositoryService repositoryService;
	
	public LoiterMode(Commander commander, Pilot pilot) {
		super(commander, pilot);
	}
	
	@Override
	public void onActivate() throws Exception {
		loiterManeuver.setManeuverListener(this);
	}

	@Override
	public void onEntry() throws Exception {
		super.onEntry();
		pilot.setEnableFlightHolds(true);
		if(repositoryService.getLoiterConfiguration()==null) {
			logger.warning("Cannot enter loiter mode because no loiter configuration was found. Initiating '" + VehicleMode.LOITER_AROUND_POSITION_MODE + "'");
			commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);
		} else {
			loiterManeuver.copyParametersFromLoiterConfiguration(repositoryService.getLoiterConfiguration(), repositoryService.getVehicleSteeringCommand());
			pilot.activateManeuver(loiterManeuver);
		}
	}
	
	@Override
	public void onExit() throws Exception {
		super.onExit();
		pilot.unholdAll();
	}

	public void updateLoiterConfiguration() {
		loiterManeuver.copyParametersFromLoiterConfiguration(repositoryService.getLoiterConfiguration(), repositoryService.getVehicleSteeringCommand());
		pilot.activateManeuver(loiterManeuver);
	}

	@Override
	public void maneuverFinished(LoiterManeuver maneuver, boolean interrupted) {
	}

	@Override
	public boolean prepareFromToNextWaypointStates(FromToNextWaypointStates fromToNextWaypointStates) {
		return false;
	}

}
