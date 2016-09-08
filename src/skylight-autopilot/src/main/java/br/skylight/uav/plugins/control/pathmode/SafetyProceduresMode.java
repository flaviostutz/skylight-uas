package br.skylight.uav.plugins.control.pathmode;

import java.util.logging.Logger;

import br.skylight.commons.Alert;
import br.skylight.commons.Coordinates;
import br.skylight.commons.SafetyAction;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.statemachine.StateMachine;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.control.FlightEngineer;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.control.maneuvers.Maneuver;
import br.skylight.uav.plugins.control.maneuvers.ManeuverListener;
import br.skylight.uav.plugins.control.maneuvers.safety.GoForDataLinkRecoveryManeuver;
import br.skylight.uav.plugins.control.maneuvers.safety.GoForGPSLinkRecoveryManeuver;
import br.skylight.uav.plugins.control.maneuvers.safety.GoForManualRecoveryManeuver;
import br.skylight.uav.plugins.control.maneuvers.safety.StabilizationManeuver;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;

public class SafetyProceduresMode extends FlightPathMode implements ManeuverListener {

	private static final Logger logger = Logger.getLogger(SafetyProceduresMode.class.getName());
	private static final float PARACHUTE_VERTICAL_SPEED = 1;

	@MemberInjection
	public FlightEngineer flightEngineer;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	@ServiceInjection
	public RepositoryService repositoryService;
	
	@ServiceInjection
	public AdvancedInstrumentsService advancedInstrumentsService;

	@ServiceInjection
	public ActuatorsService actuatorsService;

//	@MemberInjection
	private StabilizationManeuver stabilizationManeuver;
	private SkylightMission skylightMission;
	private int dataLinkRecoveryCounter;
	private int gpsLinkRecoveryCounter;
	
	private SafetyAction currentAction;
	private GoForManualRecoveryManeuver goForManualRecoveryManeuver;
	private GoForDataLinkRecoveryManeuver goForDataLinkRecoveryManeuver;
	private GoForGPSLinkRecoveryManeuver goForGpsLinkRecoveryManeuver;
	
	//deploy parachute
//	private Coordinates parachuteDeployPosition = new Coordinates();
	
	//loiter around position
	private Coordinates loiterCenterPosition = new Coordinates();
	
	private Coordinates toWaypointPositionReport = new Coordinates();
	private double toWaypointTimeReport = 0;
	
	public SafetyProceduresMode(Commander commander, Pilot pilot) {
		super(commander, pilot);
	}

	@Override
	public void onEntry() throws Exception {
		super.onEntry();
		
//		//FIXME REMOVE THIS LATER! JUST FOR OUTBACK!
//		if(currentAction.equals(SafetyAction.HARD_SPIN_TO_GROUND)) {
//			System.out.println(">>>> CALLING FLIGHT TERMINATION!");
//			actuatorsService.setFlightTermination(true);
//			return;
//		}

		
		skylightMission = repositoryService.getSkylightMission();
		toWaypointPositionReport.set(gpsService.getPosition());
		toWaypointTimeReport = 0;
		
		StateMachine<FlightPathControlMode,Object> msm = commander.getModeStateMachine();
		currentAction = (SafetyAction)msm.getCurrentStateData();
		System.out.println(">>> CURRENT ACTION="+currentAction);
		if(currentAction==null) {
			logger.warning("'current action' cannot be null. Using 'Loiter Around Position'");
			currentAction = SafetyAction.LOITER_AROUND_POSITION;
		}
		SkylightVehicleConfigurationMessage svc = repositoryService.getSkylightVehicleConfiguration();

		//VERIFY IF DESIRED SAFETY PROCEDURE SHOULD BE EXECUTED
		if(currentAction.isKillEngine() && !repositoryService.getSkylightVehicleConfiguration().isKillEngineEnabled()) {
			logger.warning("Cannot perform safety action '"+ currentAction +"' because killing engine is not permitted by configuration. Changing action to 'Loiter with roll'.");
			currentAction = SafetyAction.LOITER_WITH_ROLL;
			
		} else if(currentAction.equals(SafetyAction.DEPLOY_PARACHUTE)) {
			if(!svc.isParachuteEnabled()) {
				logger.warning("Parachute launch not enabled. Changing action to 'Loiter with roll descending'");
				currentAction = SafetyAction.LOITER_WITH_ROLL_DESCENDING;
			}

		} else if(currentAction.equals(SafetyAction.GO_FOR_DATA_LINK_RECOVERY)) {
			//verify if this action is enabled
			if(skylightMission==null || !skylightMission.getRulesOfSafety().isDataLinkRecoveryEnabled()) {
				logger.warning("'Go for link recovery' is not enabled. Using 'Go for manual recovery'");
				currentAction = SafetyAction.GO_FOR_MANUAL_RECOVERY;
			//limit number of retries
			} else if(skylightMission.getRulesOfSafety().getDataLinkMaxRecoveryRetries()<dataLinkRecoveryCounter) {
				logger.warning("'Go for data link recovery' max retries reached ("+ dataLinkRecoveryCounter + "/" + skylightMission.getRulesOfSafety().getDataLinkMaxRecoveryRetries() +"). Using action '"+ skylightMission.getRulesOfSafety().getDataLinkActionOnLinkRecoveryFailure() +"'.");
				currentAction = skylightMission.getRulesOfSafety().getDataLinkActionOnLinkRecoveryFailure();
			//should be executed. increment counter
			} else {
				dataLinkRecoveryCounter++;
			}

		} else if(currentAction.equals(SafetyAction.GO_FOR_GPS_LINK_RECOVERY)) {
			//verify if this action is enabled
			if(skylightMission==null || !skylightMission.getRulesOfSafety().isGpsSignalRecoveryEnabled()) {
				logger.warning("'Go for gps link recovery' is not enabled. Using 'Loiter around position descending'");
				currentAction = SafetyAction.LOITER_WITH_ROLL_DESCENDING;
			//limit number of retries
			} else if(skylightMission.getRulesOfSafety().getGpsMaxRecoveryRetries()<gpsLinkRecoveryCounter) {
				logger.warning("'Go for gps link recovery' max retries reached ("+ gpsLinkRecoveryCounter + "/" + skylightMission.getRulesOfSafety().getGpsMaxRecoveryRetries() +"). Using action '"+ skylightMission.getRulesOfSafety().getGpsActionOnRecoveryFailure() +"'.");
				currentAction = skylightMission.getRulesOfSafety().getGpsActionOnRecoveryFailure();
			//should be executed. increment counter
			} else {
				gpsLinkRecoveryCounter++;
			}
		}

		
		//EXECUTE SAFETY PROCEDURE
		System.out.println(">>> WILL EXECUTE "+currentAction);
		if(currentAction.equals(SafetyAction.DEPLOY_PARACHUTE)) {
			pilot.setEnableFlightHolds(false);
			pilot.unholdAll();
			pilot.killEngine(true);
			logger.info("PARACHUTE DEPLOYMENT: Engine killed at h(agl)=" + advancedInstrumentsService.getAltitude(AltitudeType.AGL) + "m");
			actuatorsService.setAileron(0);
			actuatorsService.setElevator(0);
			actuatorsService.setRudder(0);
			logger.info("PARACHUTE DEPLOYMENT: Actuators in neutral position");
			Thread.sleep(1000);
			actuatorsService.deployParachute();
			toWaypointTimeReport = (System.currentTimeMillis()/1000.0) + (gpsService.getPosition().getAltitude()/PARACHUTE_VERTICAL_SPEED);
			logger.info("PARACHUTE DEPLOYMENT: Parachute deployed");

		} else if(currentAction.equals(SafetyAction.EXECUTE_STABILIZATION_MANEUVER)) {
			if(stabilizationManeuver==null) {
				stabilizationManeuver = new StabilizationManeuver();
				pluginManager.manageObject(stabilizationManeuver);
			}
			stabilizationManeuver.setTimeInStableAttitude(7);
			stabilizationManeuver.setManeuverListener(this);
			pilot.activateManeuver(stabilizationManeuver);
			toWaypointTimeReport = (System.currentTimeMillis()/1000.0) + 7;
			
		} else if(currentAction.equals(SafetyAction.GO_FOR_MANUAL_RECOVERY)) {
			if(goForManualRecoveryManeuver==null) {
				goForManualRecoveryManeuver = new GoForManualRecoveryManeuver();
				pluginManager.manageObject(goForManualRecoveryManeuver);
			}
			
			if(skylightMission!=null) {
				logger.finer("Using manual recovery location from mission Rules of Safery");
				goForManualRecoveryManeuver.setLoiterAltitudeType(skylightMission.getRulesOfSafety().getManualRecoveryLoiterAltitudeType());
				goForManualRecoveryManeuver.setLoiterLocation(skylightMission.getRulesOfSafety().getManualRecoveryLoiterLocation());
				goForManualRecoveryManeuver.setTimeoutReachingLoiterLocation(skylightMission.getRulesOfSafety().getManualRecoveryReachLoiterLocationTimeout());
				goForManualRecoveryManeuver.setTimeoutLoiteringWaitingManualRecovery(skylightMission.getRulesOfSafety().getManualRecoveryLoiterTimeout());
				goForManualRecoveryManeuver.setActionOnManualRecoveryFailure(skylightMission.getRulesOfSafety().getManualRecoveryActionOnLoiterTimeout());
				
			} else {
				Coordinates position = gpsService.getPositionOnFirstFix();
				if(repositoryService.getGroundLevelAltitudes()!=null) {
					position = repositoryService.getGroundLevelAltitudes().getGroundLevelSetupPosition();
					logger.warning("Mission home position not found. Going to AGL setup position (at h=150m) for manual recovery.");
				} else {
					logger.warning("Mission home position not found. Going to first known GPS position (at h=150m) for manual recovery.");
				}
				goForManualRecoveryManeuver.setLoiterAltitudeType(AltitudeType.AGL);
				goForManualRecoveryManeuver.setLoiterLocation(position);
				goForManualRecoveryManeuver.getLoiterLocation().setAltitude(150);
				goForManualRecoveryManeuver.setTimeoutReachingLoiterLocation(0);
				goForManualRecoveryManeuver.setTimeoutLoiteringWaitingManualRecovery(1800);
				goForManualRecoveryManeuver.setActionOnManualRecoveryFailure(SafetyAction.LOITER_WITH_ROLL_DESCENDING);
			}
			pilot.activateManeuver(goForManualRecoveryManeuver);

		} else if(currentAction.equals(SafetyAction.GO_FOR_DATA_LINK_RECOVERY)) {
			if(goForDataLinkRecoveryManeuver==null) {
				goForDataLinkRecoveryManeuver = new GoForDataLinkRecoveryManeuver();
				pluginManager.manageObject(goForDataLinkRecoveryManeuver);
			}
			//enablement condition was verified at the beginning of this method (mission not null and comm recovery enabled)
			SkylightMission sm = repositoryService.getSkylightMission();
			logger.finer("Initiating 'Go for data link recovery' procedures as defined in Rules of Safety");
			goForDataLinkRecoveryManeuver.setLoiterLocation(sm.getRulesOfSafety().getDataLinkRecoveryLoiterLocation());
			goForDataLinkRecoveryManeuver.setLoiterAltitudeType(sm.getRulesOfSafety().getDataLinkRecoveryLoiterAltitudeType());
			goForDataLinkRecoveryManeuver.setTimeoutReachingLoiterLocation(sm.getRulesOfSafety().getDataLinkTimeoutReachingLoiterLocation());
			goForDataLinkRecoveryManeuver.setTimeoutLoiteringWaitingCommunicationRecovery(sm.getRulesOfSafety().getDataLinkTimeoutLoiteringWaitingRecovery());
			goForDataLinkRecoveryManeuver.setActionOnCommunicationRecoveryFailure(sm.getRulesOfSafety().getDataLinkActionOnLinkRecoveryFailure());
			goForDataLinkRecoveryManeuver.setModeOnLinkRecoverySuccess(sm.getRulesOfSafety().getDataLinkModeOnLinkRecoverySuccess());
			goForDataLinkRecoveryManeuver.setTimeWithStableLinkForSuccess(sm.getRulesOfSafety().getDataLinkTimeStableLinkForSuccess());
			pilot.activateManeuver(goForDataLinkRecoveryManeuver);
			
		} else if(currentAction.equals(SafetyAction.GO_FOR_GPS_LINK_RECOVERY)) {
			if(goForGpsLinkRecoveryManeuver==null) {
				goForGpsLinkRecoveryManeuver = new GoForGPSLinkRecoveryManeuver();
				pluginManager.manageObject(goForGpsLinkRecoveryManeuver);
			}
			//enablement condition was verified at the beginning of this method (mission not null and gps recovery enabled)
			SkylightMission sm = repositoryService.getSkylightMission();
			logger.finer("Initiating 'Go for gps link recovery' procedures as defined in Rules of Safety");
			goForGpsLinkRecoveryManeuver.setActionOnGpsLinkRecoveryFailure(sm.getRulesOfSafety().getGpsActionOnRecoveryFailure());
			goForGpsLinkRecoveryManeuver.setModeOnGpsLinkRecoverySuccess(sm.getRulesOfSafety().getGpsModeOnRecoverySuccess());
			goForGpsLinkRecoveryManeuver.setTimeoutLoiteringWaitingGpsLinkRecovery(sm.getRulesOfSafety().getGpsTimeoutTryingRecoverLink());
			goForGpsLinkRecoveryManeuver.setTimeWithStableLinkForSuccess(sm.getRulesOfSafety().getGpsTimeWithStableLinkForSuccess());
			pilot.activateManeuver(goForGpsLinkRecoveryManeuver);
			toWaypointTimeReport = (System.currentTimeMillis()/1000.0) + goForGpsLinkRecoveryManeuver.getTimeoutLoiteringWaitingGpsLinkRecovery();
			
		} else if(currentAction.equals(SafetyAction.HARD_SPIN_TO_GROUND)) {
			pilot.setEnableFlightHolds(false);
			logger.info("HARD SPIN TO GROUND: Commanding Flight Termination hardware. h(agl)=" + advancedInstrumentsService.getAltitude(AltitudeType.AGL) + "m");
			pilot.unholdAll();
			pilot.killEngine(false);
			System.out.println("ACTIVATING FLIGHT TERMINATION ON ACTUATOS SERVICE!");
			actuatorsService.setFlightTermination(true);
//			actuatorsService.setThrottle(0);
//			actuatorsService.setAileron(127);
//			actuatorsService.setElevator(127);
//			actuatorsService.setRudder(127);
			
		} else if(currentAction.equals(SafetyAction.KILL_ENGINE_AND_HOLD_LEVEL)) {
			pilot.setEnableFlightHolds(false);
			pilot.unholdAll();
			logger.info("KILL ENGINE AND HOLD LEVEL: Killing engine at h(agl)=" + advancedInstrumentsService.getAltitude(AltitudeType.AGL) + "m");
			pilot.killEngine(true);
			
		} else if(currentAction.equals(SafetyAction.LOITER_AROUND_POSITION)) {
			loiterCenterPosition.set(gpsService.getPosition());//used just for toWaypoint reporting
			commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);

		} else if(currentAction.equals(SafetyAction.LOITER_WITH_ROLL)) {
			pilot.setEnableFlightHolds(false);
			float altitudeAgl = advancedInstrumentsService.getAltitude(AltitudeType.AGL);
			logger.info("LOITER WITH ROLL: Initiating a roll based loiter holding level h(agl)=" + altitudeAgl + "m");
			pilot.unholdAll();
			pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE, repositoryService.getSkylightVehicleConfiguration().getStallIndicatedAirspeed());
			pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, (float)Math.toRadians(25));
			pidControllers.holdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_PITCH, advancedInstrumentsService.getAltitude(AltitudeType.AGL), AltitudeType.AGL);
			
		} else if(currentAction.equals(SafetyAction.LOITER_WITH_ROLL_DESCENDING)) {
			pilot.setEnableFlightHolds(false);
			logger.info("LOITER WITH ROLL DESCENDING: Initiating glide in a roll based loiter");
			pilot.unholdAll();
			actuatorsService.setThrottle(0);//idle throttle
			pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH, repositoryService.getSkylightVehicleConfiguration().getStallIndicatedAirspeed());
			pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, (float)Math.toRadians(10));
			
		} else {
			throw new IllegalArgumentException("Safety Procedure "+ currentAction +" not implemented yet! " + currentAction);
		}

		//activate alert
		flightEngineer.activateAlert(Alert.SAFETY_PROCEDURE_ACTIVATED, "Safety action '"+ currentAction +"' is active");
	}

	@Override
	public void onStep() throws Exception {
		//waiting to kill engine at the right moment
		if(currentAction.equals(SafetyAction.LOITER_WITH_ROLL_DESCENDING)) {
			if(actuatorsService.isEngineIgnitionEnabled() && advancedInstrumentsService.getAltitude(AltitudeType.AGL)<30) {
				logger.info("LOITER WITH ROLL DESCENDING: Killing engine at h(agl)=" + advancedInstrumentsService.getAltitude(AltitudeType.AGL) + "m");
				pilot.killEngine(true);
				pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, (float)Math.toRadians(0));
				pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH, repositoryService.getSkylightVehicleConfiguration().getStallIndicatedAirspeed()*0.8F);
			}
		}
		
		//verify if GPS AND DataLink were lost simultaneously
		if(skylightMission!=null 
			&& skylightMission.getRulesOfSafety().isDataLinkRecoveryEnabled() 
			&& skylightMission.getRulesOfSafety().isGpsSignalRecoveryEnabled() 
			&& !skylightMission.getRulesOfSafety().getActionOnGpsAndDataLinkLost().equals(SafetyAction.DO_NOTHING)) {
			//perform ROS action
			if(flightEngineer.isAlertActive(Alert.ADT_DOWNLINK_FAILED) 
				&& flightEngineer.isAlertActive(Alert.GPS_LINK_FAILED)
				&& !currentAction.equals(skylightMission.getRulesOfSafety().getActionOnGpsAndDataLinkLost())) {
				logger.warning("GPS and Data link were lost simultaneously. Executing action '"+ skylightMission.getRulesOfSafety().getActionOnGpsAndDataLinkLost() +"'");
				commander.changeControlMode(FlightPathControlMode.SAFETY_PROCEDURES, skylightMission.getRulesOfSafety().getActionOnGpsAndDataLinkLost());
			}
			
		}
	}
	
	@Override
	public void maneuverFinished(Maneuver maneuver, boolean interrupted) {
		if(maneuver instanceof StabilizationManeuver) {
			logger.info("Maneuver '"+ maneuver +"' finished. Returning to previous mode");
			commander.changeVehicleControlMode(VehicleMode.PREVIOUS_MODE);
		} else {
			logger.info("Maneuver '"+ maneuver +"' finished. Initiating loiter around current position");
			commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);
		}
	}

	@Override
	public boolean prepareFromToNextWaypointStates(FromToNextWaypointStates fromToNextWaypointStates) {
		if(currentAction.equals(SafetyAction.GO_FOR_MANUAL_RECOVERY)) {
			fromToNextWaypointStates.setSpeedType(SpeedType.INDICATED_AIRSPEED);
			fromToNextWaypointStates.setAltitudeType(goForManualRecoveryManeuver.getLoiterAltitudeType());
			fromToNextWaypointStates.setToWaypointSpeed(goForManualRecoveryManeuver.getLoiterManeuver().getTargetSpeed());
			fromToNextWaypointStates.setToWaypointAltitude(goForManualRecoveryManeuver.getLoiterLocation().getAltitude());
			fromToNextWaypointStates.setToWaypointLatitude(goForManualRecoveryManeuver.getLoiterLocation().getLatitudeRadians());
			fromToNextWaypointStates.setToWaypointLongitude(goForManualRecoveryManeuver.getLoiterLocation().getLongitudeRadians());
			fromToNextWaypointStates.setToWaypointNumber(99999);
			//calculate time for arrival at toWaypoint
			double distance = CoordinatesHelper.calculateDistance(gpsService.getPosition().getLatitudeRadians(), gpsService.getPosition().getLongitudeRadians(), 
															   fromToNextWaypointStates.getToWaypointLatitude(), fromToNextWaypointStates.getToWaypointLongitude());
			distance -= goForManualRecoveryManeuver.getLoiterManeuver().getRadius();
			fromToNextWaypointStates.setToWaypointTime((System.currentTimeMillis()/1000.0) + (distance/pilot.getAverageGroundSpeed()));
			return true;
			
		} else if(currentAction.equals(SafetyAction.GO_FOR_DATA_LINK_RECOVERY)) {
			fromToNextWaypointStates.setSpeedType(SpeedType.INDICATED_AIRSPEED);
			fromToNextWaypointStates.setAltitudeType(goForDataLinkRecoveryManeuver.getLoiterAltitudeType());
			fromToNextWaypointStates.setToWaypointSpeed(goForDataLinkRecoveryManeuver.getLoiterManeuver().getTargetSpeed());
			fromToNextWaypointStates.setToWaypointAltitude(goForDataLinkRecoveryManeuver.getLoiterLocation().getAltitude());
			fromToNextWaypointStates.setToWaypointLatitude(goForDataLinkRecoveryManeuver.getLoiterLocation().getLatitudeRadians());
			fromToNextWaypointStates.setToWaypointLongitude(goForDataLinkRecoveryManeuver.getLoiterLocation().getLongitudeRadians());
			fromToNextWaypointStates.setToWaypointNumber(99999);
			//calculate time for arrival at toWaypoint
			double distance = CoordinatesHelper.calculateDistance(gpsService.getPosition().getLatitudeRadians(), gpsService.getPosition().getLongitudeRadians(), 
															   fromToNextWaypointStates.getToWaypointLatitude(), fromToNextWaypointStates.getToWaypointLongitude());
			distance -= goForDataLinkRecoveryManeuver.getLoiterManeuver().getRadius();
			fromToNextWaypointStates.setToWaypointTime((System.currentTimeMillis()/1000.0) + (distance/pilot.getAverageGroundSpeed()));
			return true;
			
		} else if(toWaypointPositionReport.isValid()) {
			fromToNextWaypointStates.setAltitudeType(AltitudeType.WGS84);
			fromToNextWaypointStates.setToWaypointSpeed(0);
			fromToNextWaypointStates.setToWaypointAltitude(toWaypointPositionReport.getAltitude());
			fromToNextWaypointStates.setToWaypointLatitude(toWaypointPositionReport.getLatitudeRadians());
			fromToNextWaypointStates.setToWaypointLongitude(toWaypointPositionReport.getLongitudeRadians());
			fromToNextWaypointStates.setToWaypointNumber(99999);
			fromToNextWaypointStates.setToWaypointTime(toWaypointTimeReport);
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit() throws Exception {
		flightEngineer.deactivateAlert(Alert.SAFETY_PROCEDURE_ACTIVATED, "Safety procedure '"+ currentAction + "' deactivated");
	}

}
