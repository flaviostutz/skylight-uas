package br.skylight.uav.plugins.control.maneuvers.safety;

import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.SafetyAction;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.uav.plugins.control.maneuvers.LoiterManeuver;
import br.skylight.uav.plugins.control.maneuvers.Maneuver;
import br.skylight.uav.plugins.control.maneuvers.ManeuverListener;

public class GoForManualRecoveryManeuver extends Maneuver implements ManeuverListener {

	private static final Logger logger = Logger.getLogger(GoForManualRecoveryManeuver.class.getName());
	
	private Coordinates loiterLocation;
	private AltitudeType loiterAltitudeType;
	private double timeoutReachingLoiterLocation;
	private double timeoutLoiteringWaitingManualRecovery;
	private SafetyAction actionOnManualRecoveryFailure;

	private LoiterManeuver loiterManeuver = new LoiterManeuver();
	private double startTime;

	@Override
	public void onActivate() throws Exception {
		pluginManager.manageObject(loiterManeuver);
	}
	
	@Override
	public void onStart() throws Exception {
		startTime = System.currentTimeMillis()/1000.0;
		loiterManeuver.setCenterPosition(loiterLocation, loiterAltitudeType);
		loiterManeuver.setTimeoutReachingLoiterCircle(0);
		loiterManeuver.setTargetTimeLoitering(0);
		loiterManeuver.setSpeedType(WaypointSpeedType.INDICATED_AIRSPEED);
		loiterManeuver.setTargetSpeed(repositoryService.getVehicleConfiguration().getOptimumCruiseIndicatedAirspeed());
		loiterManeuver.setManeuverListener(getThis());
		//FIXME put this in vehicle configuration
		loiterManeuver.setRadius(150);
		loiterManeuver.start();
	}
	
	@Override
	public void step() throws Exception {
		//timeout reaching loiter location
		double timeLoitering = loiterManeuver.getCurrentTimeLoitering();
		if(timeLoitering==0) {
			if((System.currentTimeMillis()/1000.0-startTime)>timeoutReachingLoiterLocation) {
				logger.warning("ManualRecovery: Timeout reaching loiter location. Initiating safety procedure '" + SafetyAction.LOITER_AROUND_POSITION + "'");
				commander.startSafetyProcedures(actionOnManualRecoveryFailure);
			}
			
		//timeout waiting for manual recovery
		} else if(timeLoitering>timeoutLoiteringWaitingManualRecovery) {
			logger.warning("ManualRecovery: Timeout loitering waiting for manual recovery. Activating " + actionOnManualRecoveryFailure);
			commander.startSafetyProcedures(actionOnManualRecoveryFailure);

		//verify if at any time manual control is taken (indicates success)
		} else {
			if(instrumentsService.getInstrumentsInfos().isManualRemoteControl()) {
				logger.info("Manual control detected as expected. Changing to previous mode.");
				commander.changeVehicleControlMode(VehicleMode.PREVIOUS_MODE);
			}
		}
		loiterManeuver.step();
	}

	@Override
	public void stop(boolean aborted) throws Exception {
		super.stop(aborted);
	}
	
	protected GoForManualRecoveryManeuver getThis() {
		return this;
	}

	public void setLoiterAltitudeType(AltitudeType loiterAltitudeType) {
		this.loiterAltitudeType = loiterAltitudeType;
	}
	public void setLoiterLocation(Coordinates loiterLocation) {
		this.loiterLocation = loiterLocation;
	}
	public AltitudeType getLoiterAltitudeType() {
		return loiterAltitudeType;
	}
	public Coordinates getLoiterLocation() {
		return loiterLocation;
	}
	public void setTimeoutReachingLoiterLocation(double timeoutReachingLoiterLocation) {
		this.timeoutReachingLoiterLocation = timeoutReachingLoiterLocation;
	}
	public double getTimeoutReachingLoiterLocation() {
		return timeoutReachingLoiterLocation;
	}
	
	public void setActionOnManualRecoveryFailure(SafetyAction actionOnManualRecoveryFailure) {
		this.actionOnManualRecoveryFailure = actionOnManualRecoveryFailure;
	}
	public SafetyAction getActionOnManualRecoveryFailure() {
		return actionOnManualRecoveryFailure;
	}
	
	public void setTimeoutLoiteringWaitingManualRecovery(double timeoutLoiteringWaitingManualRecovery) {
		this.timeoutLoiteringWaitingManualRecovery = timeoutLoiteringWaitingManualRecovery;
	}
	public double getTimeoutLoiteringWaitingManualRecovery() {
		return timeoutLoiteringWaitingManualRecovery;
	}
	
	public LoiterManeuver getLoiterManeuver() {
		return loiterManeuver;
	}
	
	@Override
	public void maneuverFinished(Maneuver maneuver, boolean interrupted) {
		try {
			stop(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
