package br.skylight.uav.plugins.control.maneuvers.safety;

import java.util.logging.Logger;

import br.skylight.commons.Alert;
import br.skylight.commons.Coordinates;
import br.skylight.commons.SafetyAction;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.uav.plugins.control.maneuvers.LoiterManeuver;
import br.skylight.uav.plugins.control.maneuvers.Maneuver;
import br.skylight.uav.plugins.control.maneuvers.ManeuverListener;

public class GoForDataLinkRecoveryManeuver extends Maneuver implements ManeuverListener<LoiterManeuver> {

	private static final Logger logger = Logger.getLogger(GoForManualRecoveryManeuver.class.getName());
	
	private Coordinates loiterLocation;
	private AltitudeType loiterAltitudeType;
	private double timeoutReachingLoiterLocation;
	private double timeoutLoiteringWaitingCommunicationRecovery;
	private VehicleMode modeOnLinkRecoverySuccess;
	private SafetyAction actionOnCommunicationRecoveryFailure;

	private LoiterManeuver loiterManeuver = new LoiterManeuver();
	private TimedBoolean stableLinkTimer = new TimedBoolean(3000);
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
				logger.warning("CommsRecovery: Timeout reaching loiter location. Activating '" + actionOnCommunicationRecoveryFailure + "'");
				commander.startSafetyProcedures(actionOnCommunicationRecoveryFailure);
			}
			
		//timeout loitering and waiting for communication recovery
		} else if(timeLoitering>timeoutLoiteringWaitingCommunicationRecovery) {
			logger.warning("CommsRecovery: Timeout loitering waiting for communication recovery. Activating '" + actionOnCommunicationRecoveryFailure + "'");
			commander.startSafetyProcedures(actionOnCommunicationRecoveryFailure);
		}
		
		//verify if there is a stable communication link (indicates success)
		//verify link recovery success
		if(flightEngineer.isAlertActive(Alert.ADT_DOWNLINK_FAILED)) {
			stableLinkTimer.reset();
		} else {
			//minimum time with stable link reached
			if(stableLinkTimer.isTimedOut()) {
				logger.info("CommsRecovery: Data link recovered successfuly");
				commander.changeVehicleControlMode(modeOnLinkRecoverySuccess);
			}
		}
//		System.out.println("STEP");
		loiterManeuver.step();
	}

	protected GoForDataLinkRecoveryManeuver getThis() {
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
	
	public void setActionOnCommunicationRecoveryFailure(SafetyAction actionOnManualRecoveryFailure) {
		this.actionOnCommunicationRecoveryFailure = actionOnManualRecoveryFailure;
	}
	public SafetyAction getActionOnCommunicationRecoveryFailure() {
		return actionOnCommunicationRecoveryFailure;
	}
	
	public void setTimeoutLoiteringWaitingCommunicationRecovery(double timeoutLoiteringWaitingManualRecovery) {
		this.timeoutLoiteringWaitingCommunicationRecovery = timeoutLoiteringWaitingManualRecovery;
	}
	public double getTimeoutLoiteringWaitingCommunicationRecovery() {
		return timeoutLoiteringWaitingCommunicationRecovery;
	}
	
	public LoiterManeuver getLoiterManeuver() {
		return loiterManeuver;
	}
	
	@Override
	public void maneuverFinished(LoiterManeuver maneuver, boolean interrupted) {
		try {
			stop(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTimeWithStableLinkForSuccess(double timeStableLinkForSuccess) {
		stableLinkTimer.setTime((long)timeStableLinkForSuccess*1000);
	}
	
	public void setModeOnLinkRecoverySuccess(VehicleMode modeOnLinkRecoverySuccess) {
		this.modeOnLinkRecoverySuccess = modeOnLinkRecoverySuccess;
	}
	public VehicleMode getModeOnLinkRecoverySuccess() {
		return modeOnLinkRecoverySuccess;
	}
}
