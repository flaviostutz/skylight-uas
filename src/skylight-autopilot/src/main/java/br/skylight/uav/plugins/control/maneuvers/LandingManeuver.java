package br.skylight.uav.plugins.control.maneuvers;

import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.Runway;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.LandingHelper;
import br.skylight.commons.infra.TimedValue;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.statemachine.StateAdapter;
import br.skylight.commons.statemachine.StateMachine;

public class LandingManeuver extends StateBasedManeuver implements ManeuverListener<GotoWaypointManeuver> {

	private static final Logger logger = Logger.getLogger(LandingManeuver.class.getName());
	
	private static final String STEP_DOWNWIND1 		= "downwind 1";
	private static final String STEP_DOWNWIND2 		= "downwind 2";
	private static final String STEP_BASE 			= "base leg";
	private static final String STEP_FINAL_APPROACH = "final approach";
	private static final String STEP_FINAL_FLARE 	= "flare";
	private static final String STEP_FINAL_TAXI 	= "taxi";
	private static final String STEP_GOAROUND 		= "go around";

	private Runway runway;

	private float maxLandingGroundSpeed;
	private float landingScale;
	private Coordinates[] landingPoints;
	
	private long flareGroundStartTime;
	
	@MemberInjection(createNewInstance=true)
	public GotoWaypointManeuver gotoWaypointManeuver;
	
	private TimedValue flareThrottleValue = new TimedValue(0, 127);

	public LandingManeuver() {
		gotoWaypointManeuver.setManeuverListener(this);
	}
	
	@Override
	public void onStart() {
		SkylightVehicleConfigurationMessage ac = repositoryService.getSkylightVehicleConfiguration();
		maxLandingGroundSpeed = ac.getLandingMaxGroundSpeed();
		gotoWaypointManeuver.setSpeedType(WaypointSpeedType.GROUND_SPEED);
		gotoWaypointManeuver.setTargetSpeed(ac.getLandingMinGroundSpeed());
		landingScale = ac.getLandingApproachScale();
		
		//TODO implement dynamic landing direction behavior
		
		landingPoints = LandingHelper.calculateLandingPoints(runway, ac.getLandingApproachScale());
		getStateMachine().enterState(STEP_DOWNWIND1);
	}

	@Override
	protected StateMachine<String,Object> setupStateMachine() {
		StateMachine<String,Object> sm = new StateMachine<String,Object>();
		
		//DOWNWIND1: UAV goes to beginning of downwind leg
		sm.addState(STEP_DOWNWIND1, new StateAdapter() {
			public void onEntry() throws Exception {
				setupGotoWaypointManeuver(null, landingPoints[0], 0, true);
			}
			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
			}
		});
		
		//DOWNWIND2: landing downwind leg
		sm.addState(STEP_DOWNWIND2, new StateAdapter() {
			public void onEntry() throws Exception {
				setupGotoWaypointManeuver(null, landingPoints[1], 0, true);
			}
			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
			}
		});
		
		//BASE: landing base leg
		sm.addState(STEP_BASE, new StateAdapter() {
			public void onEntry() throws Exception {
				setupGotoWaypointManeuver(null, landingPoints[2], 0, true);
			}
			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
			}
		});
		
		//FINAL APPROACH: alignment to runway and glide descent
		sm.addState(STEP_FINAL_APPROACH, new StateAdapter() {
			public void onEntry() throws Exception {
				setupGotoWaypointManeuver(landingPoints[2], landingPoints[3], 3, false);
				gotoWaypointManeuver.setUseExponentialAltitude(true);
			}
			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
				
				//CHECK FOR ABORTING CONDITIONS
				if(gotoWaypointManeuver.getTotalTrackDone()>0.8F) {
					if(checkTrackError(runway.getRunwayWidth()/2.2F, 1+6*landingScale, maxLandingGroundSpeed)) {
						getStateMachine().enterState(STEP_GOAROUND);
					}
				}
			}
		});
		
		//FLARE: controlled stall to touch the ground
		sm.addState(STEP_FINAL_FLARE, new StateAdapter() {
			public void onEntry() throws Exception {
				setupGotoWaypointManeuver(landingPoints[3], landingPoints[4], -1, false);
				setupHorizontalControlsForFlareAndTaxi();
				//zero throttle gradually on 6s
				flareThrottleValue.start(actuatorsService.getThrottle(),0,6,false);
			}
			public void onStep() throws Exception {
				//HORIZONTAL CONTROLS
				stepGotoWaypointManeuver();

				//VERTICAL CONTROLS
				//decrease throttle gradually
				actuatorsService.setThrottle((float)flareThrottleValue.getValue());
				
				//check for flare ending
				//TODO verify if it is pratical to use zero here
				if(advancedInstrumentsService.getAltitude(AltitudeType.AGL)<=0F) {
					if(flareGroundStartTime==-1) {
						flareGroundStartTime = System.currentTimeMillis();
					//on the ground for more than 1 second
					} else if((System.currentTimeMillis()-flareGroundStartTime)>=1000) {
						getStateMachine().enterState(STEP_FINAL_TAXI);
					}

				//release elevator to avoid bouncing
				} else if(advancedInstrumentsService.getAltitude(AltitudeType.AGL)<=1F) {
					pidControllers.unholdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_PITCH);
					pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, 0);

				} else {
					//has already hit the ground and bounced. Give up!
					if(flareGroundStartTime>0) {
						getStateMachine().enterState(STEP_GOAROUND);
					}
				}
			}
		});

		//TAXI: wait until uav stops on the ground
		sm.addState(STEP_FINAL_TAXI, new StateAdapter() {
			public void onEntry() throws Exception {
				setupGotoWaypointManeuver(landingPoints[3], landingPoints[4], -1, false);
				setupHorizontalControlsForFlareAndTaxi();
				actuatorsService.setThrottle(0);
		}
			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
				if(gpsService.getGroundSpeed()<=1) {
					deactivate();
				}
			}
		});

		
		//GO AROUND: give up landing and restart downwind
		sm.addState(STEP_GOAROUND, new StateAdapter() {
			public void onEntry() throws Exception {
				setupGotoWaypointManeuver(landingPoints[3], landingPoints[4], -1, true);
				gotoWaypointManeuver.getTargetPosition().setAltitude(200);
			}
			public void onStep() throws Exception {
				stepGotoWaypointManeuver();
				if(gotoWaypointManeuver.isOnDesiredAltitude(2)) {
					getStateMachine().enterState(STEP_DOWNWIND1);//start landing again
				}
			}
		});
		
		return sm;
	}

	@Override
	public void maneuverFinished(GotoWaypointManeuver maneuver, boolean interrupted) {
		if(!interrupted) {
			if(getStateMachine().getCurrentStateId().equals(STEP_DOWNWIND1)) {
				getStateMachine().enterState(STEP_DOWNWIND2);
			} else if(getStateMachine().getCurrentStateId().equals(STEP_DOWNWIND2)) {
				getStateMachine().enterState(STEP_BASE);
			} else if(getStateMachine().getCurrentStateId().equals(STEP_BASE)) {
				getStateMachine().enterState(STEP_FINAL_APPROACH);
			}
		}
	}
	
	private void stepGotoWaypointManeuver() throws Exception {
		gotoWaypointManeuver.step();
		getReferencePosition().set(gotoWaypointManeuver.getReferencePosition());
	}
	
	private void setupGotoWaypointManeuver(Coordinates from, Coordinates to, int arrivalRadius, boolean useVerticalControls) throws Exception {
		gotoWaypointManeuver.reset();
		gotoWaypointManeuver.setFromPosition(from);
		gotoWaypointManeuver.setTargetPosition(to);
		gotoWaypointManeuver.setFollowTrack(from!=null);
		gotoWaypointManeuver.setArrivalRadius(arrivalRadius);
		gotoWaypointManeuver.setActivateVerticalControls(useVerticalControls);
		gotoWaypointManeuver.setUseExponentialAltitude(false);
		gotoWaypointManeuver.start();
	}

	private void setupHorizontalControlsForFlareAndTaxi() {
		//unhold previous controls
		pidControllers.unholdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE);
		pidControllers.unholdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE);
		pidControllers.unholdSetpoint(PIDControl.HOLD_GROUNDSPEED_WITH_IAS);
		pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, 0);
		flareGroundStartTime = -1;
	}

	private boolean checkTrackError(float maxCrossTrackError, float maxAltitudeError, float maxGroundSpeed) {
		boolean goaround = false;
		if(Math.abs(gotoWaypointManeuver.getCurrentDistanceFromTrack())>maxCrossTrackError) {
			logger.info("Too far from landing track. Performing a go around. distance=" + gotoWaypointManeuver.getCurrentDistanceFromTrack() + "m");
			goaround = true;
		}
		if(Math.abs(gotoWaypointManeuver.getCurrentAltitudeError())>maxAltitudeError) {
			logger.info("Wrong altitude for landing. Performing a go around. altitude error=" + gotoWaypointManeuver.getCurrentAltitudeError() + "ft");
			goaround = true;
		}
		if(gpsService.getGroundSpeed()>maxGroundSpeed) {
			logger.info("UAV too fast for landing. Performing a go around. ground speed=" + gpsService.getGroundSpeed() + "km/h");
			goaround = true;
		}
		return goaround;
	}
	
	
	@Override
	public void onStop(boolean aborted) throws Exception {
		gotoWaypointManeuver.stop(aborted);
	}
	
	public void setRunway(Runway runway) {
		this.runway = runway;
	}
	
}
