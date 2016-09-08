package br.skylight.uav.plugins.control.pathmode;

import java.util.logging.Logger;

import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.plugins.storage.RepositoryService;

public class ManualControlMode extends FlightPathMode {

	private static final Logger logger = Logger.getLogger(ManualControlMode.class.getName());
	
	@ServiceInjection
	public RepositoryService repositoryService;
	
	public ManualControlMode(Commander commander, Pilot pilot) {
		super(commander, pilot);
	}

	@Override
	public void onEntry() throws Exception {
		super.onEntry();
		pilot.setEnableFlightHolds(true);
		if(repositoryService.getVehicleSteeringCommand()==null) {
			logger.warning("Cannot enter manual control because no vehicle steering was found. Initiating '" + VehicleMode.LOITER_AROUND_POSITION_MODE + "'");
			commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);
		}
	}
	
	@Override
	public boolean prepareFromToNextWaypointStates(FromToNextWaypointStates fromToNextWaypointStates) {
		return false;
	}

}
