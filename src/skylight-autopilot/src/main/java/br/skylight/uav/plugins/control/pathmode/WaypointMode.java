package br.skylight.uav.plugins.control.pathmode;

import java.util.logging.Logger;

import br.skylight.commons.Alert;
import br.skylight.commons.Mission;
import br.skylight.commons.dli.WaypointDef;
import br.skylight.commons.dli.enums.LoiterType;
import br.skylight.commons.dli.enums.TurnType;
import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.control.FlightEngineer;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.plugins.control.maneuvers.GotoWaypointAdvancedManeuver;
import br.skylight.uav.plugins.control.maneuvers.LoiterManeuver;
import br.skylight.uav.plugins.control.maneuvers.Maneuver;
import br.skylight.uav.plugins.control.maneuvers.ManeuverListener;
import br.skylight.uav.plugins.messaging.MessageScheduler;
import br.skylight.uav.plugins.storage.RepositoryService;

public class WaypointMode extends FlightPathMode implements ManeuverListener {

	private static final Logger logger = Logger.getLogger(WaypointMode.class.getName());

	//min time (s) to indicate a short circuit is being detected (may be waypoints are too near)
	private static final double MIN_TIME_BETWEEN_WAYPOINTS = 2.0;
	private static final int NUMBER_OF_SEQUENTIAL_SHORT_CIRCUITS_FOR_MODE_ABORT = 10;
	
//	private AVPositionWaypoint currentPositionWaypoint;
	
//	private int previousWaypointNumber;
//	private int currentWaypointNumber;
	
	private WaypointDef fromWaypoint;
	private WaypointDef toWaypoint;
	private WaypointDef nextWaypoint;
	private double lastWaypointReachTime;
	private double lastChangeWaypointTime;
	private int shortCircuitCounter;
	
	@MemberInjection(createNewInstance=true)
	public GotoWaypointAdvancedManeuver gotoWaypointManeuver;
	
	@MemberInjection(createNewInstance=true)
	public LoiterManeuver loiterManeuver;
	
	@MemberInjection
	public FlightEngineer flightEngineer;
	
	@ServiceInjection
	public RepositoryService repositoryService;
	
	@ServiceInjection
	public MessageScheduler messageScheduler;
	
	public WaypointMode(Commander commander, Pilot pilot) {
		super(commander, pilot);
	}

	@Override
	public void onActivate() throws Exception {
		gotoWaypointManeuver.setManeuverListener(this);
		loiterManeuver.setManeuverListener(this);
	}
	
	@Override
	public void onEntry() throws Exception {
		Mission m = repositoryService.getMission();
		pilot.setEnableFlightHolds(true);
		shortCircuitCounter = 0;
		if(m==null) {
			logger.warning("Cannot enter Waypoint mode because no mission was uploaded to vehicle. Initiating '" + VehicleMode.LOITER_AROUND_POSITION_MODE + "'");
			commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);
		} else {
			int waypointNumber = 1;
			if(repositoryService.getVehicleSteeringCommand()!=null) {
				waypointNumber = repositoryService.getVehicleSteeringCommand().getCommandedWaypointNumber();
			}
			fromWaypoint = null;
			lastWaypointReachTime = 0;
			lastChangeWaypointTime = 0;
			startNavigationToWaypoint(waypointNumber);
		}
	}

	public void startNavigationToWaypoint(int waypointNumber) {
		if(repositoryService.getMission()==null) {
			logger.warning("Cannot start navigation to next waypoint because no mission is present. Initiating '" + VehicleMode.LOITER_AROUND_POSITION_MODE + "'");
			commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);
			return;
		}
		//detect short circuit (loop among waypoints that are too near)
		if(((System.currentTimeMillis()/1000.0)-lastChangeWaypointTime)<MIN_TIME_BETWEEN_WAYPOINTS) {
			shortCircuitCounter++;
		} else {
			shortCircuitCounter = 0;
		}
		if(shortCircuitCounter>=NUMBER_OF_SEQUENTIAL_SHORT_CIRCUITS_FOR_MODE_ABORT) {
			flightEngineer.activateAlert(Alert.SHORT_CIRCUIT_IN_MISSION_PLAN, "Navigation short circuit detected at waypoint #" + waypointNumber + ". Initiating '" + VehicleMode.LOITER_AROUND_POSITION_MODE + "'");
			commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);
			return;
		} else {
			flightEngineer.deactivateAlert(Alert.SHORT_CIRCUIT_IN_MISSION_PLAN, "No short circuit found in navigation");
		}
		lastChangeWaypointTime = System.currentTimeMillis()/1000.0;
		
		//perform normal navigation
		repositoryService.getVehicleSteeringCommand().setCommandedWaypointNumber(waypointNumber);
		repositoryService.setVehicleSteeringCommand(repositoryService.getVehicleSteeringCommand());
		fromWaypoint = toWaypoint;
		toWaypoint = repositoryService.getMission().getComputedWaypointsMap().get(waypointNumber);
		if(toWaypoint==null) {
			logger.info("Waypoint #" + waypointNumber + " was not found in mission. Ending waypoint mode.");
			commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);
			return;
			
		} else {
			//activate maneuvers and execute actions for the next waypoint
			logger.info("Going to waypoint #" + waypointNumber);
			if(toWaypoint.getPositionWaypoint()!=null) {
				nextWaypoint = repositoryService.getMission().getComputedWaypointsMap().get(toWaypoint.getPositionWaypoint().getNextWaypoint());
			} else {
				nextWaypoint = null;
			}
			
			//EXECUTE WAYPOINT EXTENSIONS
			//loiter extensions
			boolean loiterWaypoint = false;
			if(toWaypoint.getLoiterWaypoint()!=null) {
				AVLoiterWaypoint aw = toWaypoint.getLoiterWaypoint();
				if(!aw.getWaypointLoiterType().equals(LoiterType.CIRCULAR)) {
					logger.warning("Loiter type "+ aw.getWaypointLoiterType() +" is not supported. Using circular loiter type instead. WP #" + toWaypoint.getWaypointNumber());
				}
				loiterManeuver.setRadius(aw.getLoiterRadius());
				loiterManeuver.setTargetTimeLoitering(aw.getWaypointLoiterTime());
				loiterWaypoint = true;
			}
			//payload extensions
			for (PayloadActionWaypoint aw : toWaypoint.getPayloadActionWaypoints()) {
				if(payloadService!=null) {
					for (int stationNumber : aw.getStationNumber().getStations()) {
						payloadService.sendMessageToPayloadOperator(stationNumber, aw);
					}
				}
			}
			//position definition
			if(toWaypoint.getPositionWaypoint()!=null) {
				//setup maneuver
				gotoWaypointManeuver.setAltitudeType(toWaypoint.getPositionWaypoint().getWaypointAltitudeType());
				gotoWaypointManeuver.setFollowTrack(fromWaypoint!=null && toWaypoint.getPositionWaypoint().getTurnType().equals(TurnType.SHORT_TURN));
				if(fromWaypoint!=null) {
					gotoWaypointManeuver.getFromPosition().set(fromWaypoint.getPositionWaypoint().getWaypointPosition());
				} else {
					gotoWaypointManeuver.getFromPosition().reset();
				}
				gotoWaypointManeuver.getTargetPosition().set(toWaypoint.getPositionWaypoint().getWaypointPosition());
				gotoWaypointManeuver.setSpeedType(toWaypoint.getPositionWaypoint().getWaypointSpeedType());
				gotoWaypointManeuver.setTargetSpeed(toWaypoint.getPositionWaypoint().getWaypointToSpeed());
				gotoWaypointManeuver.setTargetTimeForArrival(toWaypoint.getPositionWaypoint().getArrivalTime());
				if(toWaypoint.getPositionWaypoint().getTurnType().equals(TurnType.FLYOVER)) {
					gotoWaypointManeuver.setArrivalRadius(10);
				} else if(toWaypoint.getPositionWaypoint().getTurnType().equals(TurnType.SHORT_TURN)) {
					gotoWaypointManeuver.setArrivalRadius(0);//dynamic arrival
				}
			} else {
				logger.warning("Waypoint #" + toWaypoint.getWaypointNumber() + " should have a position waypoint associated to it. Ending waypoint mode.");
				commander.changeVehicleControlMode(VehicleMode.LOITER_AROUND_POSITION_MODE);
				return;
			}

			//activate position/loiter maneuvers
			if(loiterWaypoint) {
				loiterManeuver.copyParametersFromGotoWaypointManeuver(gotoWaypointManeuver);
				pilot.activateManeuver(loiterManeuver);
			} else {
				pilot.activateManeuver(gotoWaypointManeuver);
			}
		}
	}

	@Override
	public boolean prepareFromToNextWaypointStates(FromToNextWaypointStates fromToNextWaypointStates) {
		if(fromWaypoint!=null) {
			fromToNextWaypointStates.setFromWaypointAltitude(fromWaypoint.getPositionWaypoint().getWaypointToAltitude());
			fromToNextWaypointStates.setFromWaypointLatitude(fromWaypoint.getPositionWaypoint().getWaypointToLatitudeOrRelativeY());
			fromToNextWaypointStates.setFromWaypointLongitude(fromWaypoint.getPositionWaypoint().getWaypointToLongitudeOrRelativeX());
			fromToNextWaypointStates.setFromWaypointNumber(fromWaypoint.getWaypointNumber());
			fromToNextWaypointStates.setFromWaypointTime(lastWaypointReachTime);
			
		}
		if(toWaypoint!=null) {
			fromToNextWaypointStates.setSpeedType(gotoWaypointManeuver.getCurrentSetpointSpeedType());
			fromToNextWaypointStates.setAltitudeType(toWaypoint.getPositionWaypoint().getWaypointAltitudeType());
			fromToNextWaypointStates.setToWaypointSpeed(gotoWaypointManeuver.getCurrentSetpointSpeed());
			fromToNextWaypointStates.setToWaypointAltitude(toWaypoint.getPositionWaypoint().getWaypointToAltitude());
			fromToNextWaypointStates.setToWaypointLatitude(toWaypoint.getPositionWaypoint().getWaypointToLatitudeOrRelativeY());
			fromToNextWaypointStates.setToWaypointLongitude(toWaypoint.getPositionWaypoint().getWaypointToLongitudeOrRelativeX());
			fromToNextWaypointStates.setToWaypointNumber(toWaypoint.getWaypointNumber());
			//calculate time for arrival at toWaypoint
			double distance = CoordinatesHelper.calculateDistance(gpsService.getPosition().getLatitudeRadians(), 							   gpsService.getPosition().getLongitudeRadians(), 
																  toWaypoint.getPositionWaypoint().getWaypointToLatitudeOrRelativeY(), toWaypoint.getPositionWaypoint().getWaypointToLongitudeOrRelativeX());
			double toLoiterTime = 0;
			if(toWaypoint.getLoiterWaypoint()!=null) {
				distance -= toWaypoint.getLoiterWaypoint().getLoiterRadius();
				toLoiterTime = toWaypoint.getLoiterWaypoint().getWaypointLoiterTime();
			}
			fromToNextWaypointStates.setToWaypointTime((System.currentTimeMillis()/1000.0) + (distance/pilot.getAverageGroundSpeed()));
			
			if(nextWaypoint!=null) {
				fromToNextWaypointStates.setNextWaypointAltitude(nextWaypoint.getPositionWaypoint().getWaypointToAltitude());
				fromToNextWaypointStates.setNextWaypointLatitude(nextWaypoint.getPositionWaypoint().getWaypointToLatitudeOrRelativeY());
				fromToNextWaypointStates.setNextWaypointLongitude(nextWaypoint.getPositionWaypoint().getWaypointToLongitudeOrRelativeX());
				fromToNextWaypointStates.setNextWaypointNumber(nextWaypoint.getWaypointNumber());
				fromToNextWaypointStates.setNextWaypointSpeed(nextWaypoint.getPositionWaypoint().getWaypointToSpeed());
				//calculate time for arrival at nextWaypoint
				distance += CoordinatesHelper.calculateDistance(toWaypoint.getPositionWaypoint().getWaypointToLatitudeOrRelativeY(),   toWaypoint.getPositionWaypoint().getWaypointToLongitudeOrRelativeX(), 
																nextWaypoint.getPositionWaypoint().getWaypointToLatitudeOrRelativeY(), nextWaypoint.getPositionWaypoint().getWaypointToLongitudeOrRelativeX());
				if(nextWaypoint.getLoiterWaypoint()!=null) {
					distance -= nextWaypoint.getLoiterWaypoint().getLoiterRadius();
				}
				fromToNextWaypointStates.setNextWaypointTime((System.currentTimeMillis()/1000.0) + (distance/pilot.getAverageGroundSpeed()) + toLoiterTime);
			}
		}
		return fromWaypoint!=null || toWaypoint!=null;
	}

	@Override
	public void maneuverFinished(Maneuver maneuver, boolean aborted) {
		if(!aborted && toWaypoint!=null) {
			logger.info("Arrived at waypoint #" + toWaypoint.getWaypointNumber() + ". Next waypoint is #" + toWaypoint.getPositionWaypoint().getNextWaypoint());
			lastWaypointReachTime = System.currentTimeMillis()/1000.0;
			startNavigationToWaypoint(toWaypoint.getPositionWaypoint().getNextWaypoint());
		}
	}
	
}
