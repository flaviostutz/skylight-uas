package br.skylight.uav.plugins.control.maneuvers;

import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.uav.infra.UAVHelper;

@ManagedMember(useDependenciesFrom=LoiterManeuver.class)
public class GotoWaypointAdvancedManeuver extends GotoWaypointManeuver implements ManeuverListener<LoiterManeuver> {

	private static final Logger logger = Logger.getLogger(GotoWaypointAdvancedManeuver.class.getName());
	
	private static final String LOG_ID = "GotoWaypointManeuver";
	
	private static final int STEP_GOTO_END = 1;
	private static final int STEP_REACH_ALTITUDE = 2;
	
	private int step = 0;

	//pre instantiated objects
	private Coordinates loiterCenter = new Coordinates(0,0,0);
	private Coordinates currentPosition = new Coordinates(0,0,0);

	@MemberInjection(createNewInstance=true, optionalAtInitialization=true)
	public LoiterManeuver loiterManeuver;
	
	public GotoWaypointAdvancedManeuver() {
	}

	@Override
	public void onActivate() throws Exception {
		pluginManager.executeAfterStartup(new Runnable() {
			public void run() {
				loiterManeuver.setManeuverListener(getThis());
			}
		});
	}
	
	private GotoWaypointAdvancedManeuver getThis() {
		return this;
	}
	
	@Override
	public void onStart() throws Exception {
		super.onStart();
//		if(followTrack) {
			//first, goto desired altitude
			if(Math.abs(advancedInstrumentsService.getAltitude(altitudeType)-targetPosition.getAltitude())<=50) {
				step = STEP_GOTO_END;
			} else {
				step = STEP_REACH_ALTITUDE;
				loiterManeuver.stop(true);
			}
//		} else {
//			step = STEP_GOTO_END;
//		}
	}

	@Override
	public void step() throws Exception {
		//reach desired altitude, loiter for climbing/descending
		if(step==STEP_GOTO_END) {
			super.step();
			
		} else if(step==STEP_REACH_ALTITUDE) {
			if(!loiterManeuver.isRunning()) {
				//calculate loiter center coordinates
				double radius = calculateCurrentMinTurnRadius()*1.2F + 1;
				double angle = gpsService.getCourseHeading() + MathHelper.HALF_PI;
				double xcenter = radius * Math.cos(angle);
				double ycenter = radius * Math.sin(angle);
				currentPosition.setLatitude(gpsService.getPosition().getLatitude());
				currentPosition.setLongitude(gpsService.getPosition().getLongitude());
				currentPosition.setAltitude(advancedInstrumentsService.getAltitude(altitudeType));
				CoordinatesHelper.calculateCoordinatesFromRelativePosition(loiterCenter, currentPosition, xcenter, ycenter);
				
				//setup loiter
				loiterManeuver.reset();
				loiterManeuver.setCenterPosition(loiterCenter, altitudeType);
				loiterManeuver.setRadius((float)radius);
				loiterManeuver.setTargetTimeLoitering(-1);
				
				UAVHelper.notifyStateFine(logger, "Loitering to reach altitude", LOG_ID+"step");
			}
			setReferencePosition(loiterManeuver.getReferencePosition());
			loiterManeuver.step();
		}
//		System.out.println(getName() + " step 2");
	}

	@Override
	public void maneuverFinished(LoiterManeuver maneuver, boolean interrupted) {
		//uav reached altitude (loiter has finished). continue to waypoint
		step = STEP_GOTO_END;
	}

}
