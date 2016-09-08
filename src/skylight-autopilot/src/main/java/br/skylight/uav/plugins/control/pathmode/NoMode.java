package br.skylight.uav.plugins.control.pathmode;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.services.GPSService;

public class NoMode extends FlightPathMode {

	@ServiceInjection
	public PluginManager pluginManager;
	
	//the following injection is used only to force gps load before mode activation in order to avoid NaN in altitudes read before GPS gateway is running
	@ServiceInjection
	public GPSService gpsService;
	
	public NoMode(Commander commander, Pilot pilot) {
		super(commander, pilot);
	}
	
	@Override
	public void onEntry() throws Exception {
		VehicleConfigurationMessage vc = repositoryService.getVehicleConfiguration();
		SkylightVehicleConfigurationMessage svc = repositoryService.getSkylightVehicleConfiguration();
		if(svc.isKeepStableOnNoMode()) {
			pilot.setEnableFlightHolds(false);
			pilot.unholdAll();
			
			pidControllers.getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_PITCH).setSetpoint(Math.max(advancedInstrumentsService.getAltitude(AltitudeType.AGL), 100));
			pidControllers.getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_PITCH).activate();
			
			//keep roll in 15 degrees to perform an open curve
			pidControllers.getPIDWorker(PIDControl.HOLD_ROLL_WITH_AILERON).setSetpoint((float)Math.toRadians(15));
			pidControllers.getPIDWorker(PIDControl.HOLD_ROLL_WITH_AILERON).activate();

			//keep optimum airspeed
			pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_THROTTLE).setSetpoint(Math.max(vc.getOptimumEnduranceIndicatedAirspeed(), 15));
			pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_THROTTLE).activate();
		}
	}

	@Override
	public void onExit() throws Exception {
		pilot.setEnableFlightHolds(true);
	}
	
	@Override
	public boolean prepareFromToNextWaypointStates(FromToNextWaypointStates fromToNextWaypointStates) {
		return false;
	}

}
