package br.skylight.uav.plugins.control.maneuvers.safety;

import java.util.logging.Logger;

import br.skylight.commons.Alert;
import br.skylight.commons.SafetyAction;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.uav.plugins.control.maneuvers.Maneuver;

public class GoForGPSLinkRecoveryManeuver extends Maneuver {

	private static final Logger logger = Logger.getLogger(GoForManualRecoveryManeuver.class.getName());
	
	private double timeoutLoiteringWaitingGpsLinkRecovery;
	private SafetyAction actionOnGpsLinkRecoveryFailure;
	private VehicleMode modeOnGpsLinkRecoverySuccess;
	private TimedBoolean stableLinkTimer = new TimedBoolean(3000);
	private double startTime;

	@Override
	public void onStart() throws Exception {
		pilot.setEnableFlightHolds(false);
		startTime = System.currentTimeMillis()/1000.0;
		pilot.unholdAll();
		pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE, repositoryService.getSkylightVehicleConfiguration().getStallIndicatedAirspeed());
		pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, (float)Math.toRadians(20));
		pidControllers.holdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_PITCH, advancedInstrumentsService.getAltitude(AltitudeType.AGL), AltitudeType.AGL);
	}
	
	@Override
	public void step() throws Exception {
		//timeout waiting gps link recovery
		if((System.currentTimeMillis()/1000.0-startTime)>timeoutLoiteringWaitingGpsLinkRecovery) {
			logger.warning("GpsRecovery: Timeout waiting link recovery. Activating '" + actionOnGpsLinkRecoveryFailure + "'");
			commander.startSafetyProcedures(actionOnGpsLinkRecoveryFailure);
		}
		
		//verify link recovery success
		if(flightEngineer.isAlertActive(Alert.GPS_LINK_FAILED)) {
			stableLinkTimer.reset();
		} else {
			//minimum time with stable link reached
			if(stableLinkTimer.isTimedOut()) {
				logger.info("GpsRecovery: Gps link recovered successfuly");
				commander.changeVehicleControlMode(modeOnGpsLinkRecoverySuccess);
			}
		}
	}

	public double getTimeoutLoiteringWaitingGpsLinkRecovery() {
		return timeoutLoiteringWaitingGpsLinkRecovery;
	}

	public void setTimeoutLoiteringWaitingGpsLinkRecovery(double timeoutLoiteringWaitingGpsLinkRecovery) {
		this.timeoutLoiteringWaitingGpsLinkRecovery = timeoutLoiteringWaitingGpsLinkRecovery;
	}

	public SafetyAction getActionOnGpsLinkRecoveryFailure() {
		return actionOnGpsLinkRecoveryFailure;
	}

	public void setActionOnGpsLinkRecoveryFailure(SafetyAction actionOnGpsLinkRecoveryFailure) {
		this.actionOnGpsLinkRecoveryFailure = actionOnGpsLinkRecoveryFailure;
	}

	public VehicleMode getModeOnGpsLinkRecoverySuccess() {
		return modeOnGpsLinkRecoverySuccess;
	}

	public void setModeOnGpsLinkRecoverySuccess(VehicleMode modeOnGpsLinkRecoverySuccess) {
		this.modeOnGpsLinkRecoverySuccess = modeOnGpsLinkRecoverySuccess;
	}

	public void setTimeWithStableLinkForSuccess(double timeWithStableLinkForSuccess) {
		stableLinkTimer.setTime((long)timeWithStableLinkForSuccess*1000);
	}

}
