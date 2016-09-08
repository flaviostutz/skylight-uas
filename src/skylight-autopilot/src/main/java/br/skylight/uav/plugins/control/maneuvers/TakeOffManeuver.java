package br.skylight.uav.plugins.control.maneuvers;

import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.TimedValue;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.statemachine.StateAdapter;
import br.skylight.commons.statemachine.StateMachine;
import br.skylight.uav.plugins.control.Pilot.HeadingControlType;

public class TakeOffManeuver extends StateBasedManeuver {

	private static final Logger logger = Logger.getLogger(TakeOffManeuver.class.getName());
	
	private static final String STEP_START_MOVING = "starting to move";
	private static final String STEP_WAIT_SEMI_ROTATION = "waiting semi-rotation";
	private static final String STEP_WAIT_LIFT_OFF = "waiting for lift-off";
	private static final String STEP_DO_LIFT_OFF = "doing lift-off";
	private static final String STEP_REACH_ALTITUDE = "climbing";
	private static final String STEP_ABORT_TAKEOFF_GLIDE = "aborting (glide)";
	private static final String STEP_ABORT_TAKEOFF_FLARE = "aborting (flare)";

	private static final int GROUNDSPEED_START_ACTUATORS = 1;

	private static int ktiasValueSemiRotation;
	private static final float PITCH_VALUE_SEMI_ROTATION = 3F;

	private static int ktiasValueLiftOff;
	private static final float PITCH_VALUE_LIFT_OFF = 7.5F;

	private static final int ALTITUDE_FOR_START_CLIMBING = 10;// ft
	private static final int ALTITUDE_FINAL_TAKEOFF = 200;// ft

	private static final int DISTANCE_CLIMBING_LOITER = 200;// m
	private static final int ALTITUDE_ABORT_FLARE = 3;// ft

	private Coordinates runwayReference1;
	private Coordinates runwayReference2;
	private float runwayWidth = 4;
	private boolean trafficLeft;

	private LoiterManeuver loiterManeuver = new LoiterManeuver();
	private TimedValue throttleIncrease = new TimedValue(0, 127);

	@MemberInjection(createNewInstance=true)
	public GotoWaypointManeuver gotoWaypointManeuver;
	
	@Override
	public void onStart() throws Exception {
		SkylightVehicleConfigurationMessage ac = repositoryService.getSkylightVehicleConfiguration();
		ktiasValueLiftOff = (int) ac.getTakeOffLiftOffIndicatedAirspeed();
		ktiasValueSemiRotation = (int) (((float) ktiasValueLiftOff) * 0.85F);

		// basic waypoint maneuver setup
		gotoWaypointManeuver.reset();
		gotoWaypointManeuver.setFromPosition(runwayReference1);
		gotoWaypointManeuver.setTargetPosition(runwayReference2);
		gotoWaypointManeuver.setFollowTrack(true);
		gotoWaypointManeuver.setActivateVerticalControls(false);
		gotoWaypointManeuver.setActivateSpeedControls(false);
		gotoWaypointManeuver.setHeadingControlType(HeadingControlType.AILERON);
		gotoWaypointManeuver.start();

		getStateMachine().enterState(STEP_START_MOVING);
	}

	@Override
	protected StateMachine<String,Object> setupStateMachine() {
		StateMachine<String,Object> sm = new StateMachine<String,Object>();

		// START TO MOVE: increase throttle gradually to start moving
		sm.addState(STEP_START_MOVING, new StateAdapter() {
			public void onEntry() {
				actuatorsService.setAileron(0);
				actuatorsService.setElevator(0);
				actuatorsService.setRudder(0);
				throttleIncrease.start(0, 127, 6, false);
			}

			public void onStep() {
				if(commander.isAutoSpeedControlsEnabled()) {
					actuatorsService.setThrottle((float)throttleIncrease.getValue());
				}
				if (gpsService.getGroundSpeed() >= GROUNDSPEED_START_ACTUATORS) {
					getStateMachine().enterState(STEP_WAIT_SEMI_ROTATION);
				}
				checkForTakeoffAbort();
			}
		});

		// WAIT FOR SEMI-ROTATION: wait until there is enough speed to semi
		// rotate
		sm.addState(STEP_WAIT_SEMI_ROTATION, new StateAdapter() {
			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
				if(commander.isAutoSpeedControlsEnabled()) {
					actuatorsService.setThrottle((float)throttleIncrease.getValue());
				}
				if (advancedInstrumentsService.getIAS() >= ktiasValueSemiRotation) {
					getStateMachine().enterState(STEP_WAIT_LIFT_OFF);
				}
				checkForTakeoffAbort();
			}
		});

		// WAIT FOR LIFT-OFF: wait until there is enough speed to lift-off
		sm.addState(STEP_WAIT_LIFT_OFF, new StateAdapter() {
			public void onEntry() {
				pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, PITCH_VALUE_SEMI_ROTATION);
			}

			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
				if(commander.isAutoSpeedControlsEnabled()) {
					actuatorsService.setThrottle((float)throttleIncrease.getValue());
				}
				if (advancedInstrumentsService.getIAS() >= ktiasValueLiftOff) {
					getStateMachine().enterState(STEP_DO_LIFT_OFF);
				}
				checkForTakeoffAbort();
			}
		});

		// DO LIFT-OFF: perform lift-off
		sm.addState(STEP_DO_LIFT_OFF, new StateAdapter() {
			public void onEntry() {
				pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, PITCH_VALUE_LIFT_OFF);
			}

			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
				if(commander.isAutoSpeedControlsEnabled()) {
					actuatorsService.setThrottle((float)throttleIncrease.getValue());
				}
				if (advancedInstrumentsService.getAltitude(AltitudeType.AGL) >= ALTITUDE_FOR_START_CLIMBING) {
					getStateMachine().enterState(STEP_REACH_ALTITUDE);
				}
				checkForTakeoffAbort();
			}
		});

		// REACH ALTITUDE: already take-off, now climb to a safe altitude
		sm.addState(STEP_REACH_ALTITUDE, new StateAdapter() {
			public void onEntry() throws Exception {
				Coordinates c = calculateTakeoffLoiterCenter(DISTANCE_CLIMBING_LOITER);
				loiterManeuver.reset();
				loiterManeuver.getCenterPosition().set(c);
				loiterManeuver.getCenterPosition().setAltitude(ALTITUDE_FINAL_TAKEOFF*2);
				loiterManeuver.setRadius(calculateCurrentMinTurnRadius());
				loiterManeuver.setTargetTimeLoitering(-1);
				loiterManeuver.start();
			}

			public void onStep() throws Exception {
				stepLoiterManeuver();
				if (advancedInstrumentsService.getAltitude(AltitudeType.AGL) >= ALTITUDE_FINAL_TAKEOFF) {
					deactivate();
				}
			}
		});

		// ABORT (GLIDE): Abort take-off. Glide because uav is high
		sm.addState(STEP_ABORT_TAKEOFF_GLIDE, new StateAdapter() {
			public void onEntry() {
				pidControllers.unholdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE);
				//glide with airspeed
				if(commander.isAutoAltitudeControlsEnabled() && commander.isAutoSpeedControlsEnabled()) {
					if(advancedInstrumentsService.getIAS()>repositoryService.getSkylightVehicleConfiguration().getStallIndicatedAirspeed()) {
						pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH, 
													repositoryService.getSkylightVehicleConfiguration().getStallIndicatedAirspeed());
						logger.warning("Take-off abort: Gliding with pitch");
					//don't glide with airspeed (uav would have a dangerous negative pitch to increase ias)
					} else {
						pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, 0);
						actuatorsService.setThrottle(20);
						logger.warning("Take-off abort: Gliding with throttle");
					}
				}
			}

			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
				if (advancedInstrumentsService.getAltitude(AltitudeType.AGL) <= ALTITUDE_ABORT_FLARE) {
					getStateMachine().enterState(STEP_ABORT_TAKEOFF_FLARE);
				}
			}
		});

		// ABORT (FLARE): Abort take-off. Landing flare
		sm.addState(STEP_ABORT_TAKEOFF_FLARE, new StateAdapter() {
			public void onEntry() {
			}

			public void onStep() throws Exception {
				if(commander.isAutoAltitudeControlsEnabled() && commander.isAutoSpeedControlsEnabled()) {
					pidControllers.unholdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE);
					pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, 0);
					actuatorsService.setThrottle(0);
				}
				
				stepGotoWaypointManeuver();
				if (gpsService.getGroundSpeed() <= 0.8) {
					stop(true);
				}
			}
		});

		return sm;
	}

	// CHECK FOR TAKE-OFF ABORT
	protected void checkForTakeoffAbort() {
		if (advancedInstrumentsService.getAltitude(AltitudeType.AGL) <= 3) {
			// ALWAY FROM DESIRED TRACK
			double dist = Math.abs(gotoWaypointManeuver.getCurrentDistanceFromTrack());
			if (dist > 3) {
				logger.warning("Aborting take-off: Too alway from track. distance=" + dist);
				abortTakeoff();
			}
			// TOO NEAR RUNWAY END
			if (gpsService.getPosition().distance(runwayReference2) <= 10) {
				logger.warning("Aborting take-off: Too near runway end");
				abortTakeoff();
			}
			// TOO LONG ON CURRENT STEP
			if (getStateMachine().getTimeInCurrentState() > 10) {
				logger.warning("Aborting take-off. Taking too long on current step (>10s). step=" + getStateMachine().getCurrentStateId());
				abortTakeoff();
			}
		}
	}

	private void stepGotoWaypointManeuver() throws Exception {
		gotoWaypointManeuver.step();
		getReferencePosition().set(gotoWaypointManeuver.getReferencePosition());
	}

	private void stepLoiterManeuver() throws Exception {
		loiterManeuver.step();
		getReferencePosition().set(loiterManeuver.getReferencePosition());
	}

	private void abortTakeoff() {
		if (advancedInstrumentsService.getAltitude(AltitudeType.AGL) <= ALTITUDE_ABORT_FLARE) {
			getStateMachine().enterState(STEP_ABORT_TAKEOFF_FLARE);
		} else {
			getStateMachine().enterState(STEP_ABORT_TAKEOFF_GLIDE);
		}
	}

	public Coordinates calculateTakeoffLoiterCenter(double distanceFromRunway) {
		// traffic side: left=1; right=2
		int ts = trafficLeft ? 1 : -1;

		// determine runway angle
		double runwayRotation = runwayReference1.azimuthTo(runwayReference2);

		return CoordinatesHelper.calculateCoordinates(
				runwayReference2, 
				distanceFromRunway * Math.cos(runwayRotation), 
				distanceFromRunway * Math.sin(runwayRotation), 
				ts * 90);
	}

	public void setTrafficLeft(boolean trafficLeft) {
		this.trafficLeft = trafficLeft;
	}

	public boolean isTrafficLeft() {
		return trafficLeft;
	}

	public void setRunwayReference1(Coordinates runwayReference1) {
		this.runwayReference1 = runwayReference1;
	}

	public Coordinates getRunwayReference1() {
		return runwayReference1;
	}

	public void setRunwayReference2(Coordinates runwayReference2) {
		this.runwayReference2 = runwayReference2;
	}

	public Coordinates getRunwayReference2() {
		return runwayReference2;
	}

	public void setRunwayWidth(float runwayWidth) {
		this.runwayWidth = runwayWidth;
	}

	public float getRunwayWidth() {
		return runwayWidth;
	}
}
