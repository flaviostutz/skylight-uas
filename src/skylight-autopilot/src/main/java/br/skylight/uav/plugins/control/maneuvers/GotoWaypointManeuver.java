package br.skylight.uav.plugins.control.maneuvers;

import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.infra.Activable;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.uav.infra.CircularPathArea;
import br.skylight.uav.infra.UAVHelper;
import br.skylight.uav.plugins.control.Pilot.HeadingControlType;

@ManagedMember
public class GotoWaypointManeuver extends Maneuver implements Activable {

	private static final Logger logger = Logger.getLogger(GotoWaypointManeuver.class.getName());
	
	private static final String LOG_ID = "GotoWaypointManeuver";
	
	protected AltitudeType altitudeType = AltitudeType.AGL;
	private Coordinates fromPosition = new Coordinates(0,0,0);
	protected Coordinates targetPosition = new Coordinates(0,0,0);
	private WaypointSpeedType speedType;
	private float targetSpeed;
	private float arrivalRadius;
	private double targetTimeForArrival;
	
	private boolean activateVerticalControls = true;
	private boolean activateHorizontalControls = true;
	private boolean activateSpeedControls = true;

	private SpeedType currentSetpointSpeedType = SpeedType.GROUND_SPEED;
	private float currentSetpointSpeed;
	
	private boolean followTrack = false;

	//follow track vars
	private static final float MAX_CROSS_TRACK_ANGLE = (float)Math.toRadians(45);
	private static final float FACTOR_AHEAD = (float)((2 + Math.tan(MathHelper.HALF_PI-MAX_CROSS_TRACK_ANGLE))/Math.tan(MAX_CROSS_TRACK_ANGLE)) * 1.2F;//1.2 is an approximation due to time to change roll during turns
	private static final long TIME_RECALC_AHEAD = 2000;
	
	private static final float EXPONENTIAL_ALTITUDE_LAMBDA = 3F;
	
	private double trackAheadDist = 250;//initial values
	private double trackRecalcDist = 150;//initial values
	private double trackHeading;
	private double trackLength;
	private double targetDistOnTrack;
	private double glideRatio;
	private boolean farFromTrack;
	private HeadingControlType headingControlType = HeadingControlType.DYNAMIC;
	private boolean useExponentialAltitude = false;
	private double currentDistanceOnTrack;
	private double currentDistanceFromTrack;
	private long lastRecalcTime;
	private float glideDistanceThreshold;

	//dynamic path area (to verify if an waypoint is inside a loiter area)
	private CircularPathArea circularPathArea = new CircularPathArea(Math.toRadians(20));
	
	//max heading error so that roll will be used to maintain heading
//	private static final short MAX_HEADING_ERROR_RUDDER = 15;
	
	//max distance in meters from desired track (cross track error) so that roll will be used to maintain heading
	private static final short MAX_CROSS_TRACK_ERROR_RUDDER = 4;
	
	@Override
	public boolean isActive() {
		return true;
	}
	
	@Override
	public void onStart() throws Exception {
		farFromTrack = false;

		//disable track follow because there is no fromPosition defined  yet
		if(followTrack && (fromPosition.getLatitude()==0 || fromPosition.getLongitude()==0)) {
			throw new IllegalStateException("'followTrack' option cannot be used because 'fromWaypoint' is null");
		}
		
		if(followTrack) {
			//calculate the best ahead ref point in track so the airplane will cross the track in max of X degrees
			trackHeading = fromPosition.azimuthToRadians(targetPosition);
			trackLength = fromPosition.distance(targetPosition);
			//linear glide ratio
			glideRatio = (targetPosition.getAltitude() - fromPosition.getAltitude())/trackLength;
			targetDistOnTrack = 0;
			
			if(trackLength==0) {
				logger.info("GotoWaypointManeuver: Won't follow track because length is too short. Going straight to target waypoint");
				followTrack = false;
			}
			glideDistanceThreshold = calculateCurrentMinTurnRadius() * 3;
			circularPathArea.clear();
		}
	}

	@Override
	public void step() throws Exception {
		UAVHelper.notifyStateFine(logger, "Going to destination", LOG_ID+"step");

		circularPathArea.addLocation(gpsService.getCourseHeading(), gpsService.getPosition());
		
		//follow the track between two waypoints
		if(followTrack) {
			double originToPlane = fromPosition.distance(gpsService.getPosition());
			double headingFromOrigin = fromPosition.azimuthToRadians(gpsService.getPosition());
			currentDistanceOnTrack = (float)(originToPlane * Math.cos(-(trackHeading-headingFromOrigin)));
			currentDistanceFromTrack = (originToPlane * Math.sin(-(trackHeading-headingFromOrigin)));
			double trackLatDiff = targetPosition.getLatitudeRadians()-fromPosition.getLatitudeRadians();
			double trackLongDiff = targetPosition.getLongitudeRadians()-fromPosition.getLongitudeRadians();
			farFromTrack = currentDistanceFromTrack>MAX_CROSS_TRACK_ERROR_RUDDER;

			//calculate ahead distance each Xs
			if(System.currentTimeMillis()-lastRecalcTime>TIME_RECALC_AHEAD) {
				trackAheadDist = (float)(calculateCurrentMinTurnRadius() * FACTOR_AHEAD);
				trackRecalcDist = trackAheadDist/2F;
				lastRecalcTime = System.currentTimeMillis();
			}
			
			//uav behind track
			if(currentDistanceOnTrack<=0) {
				targetDistOnTrack = trackAheadDist;
				
			//uav beyond track
			} else if(currentDistanceOnTrack>=trackLength) {
				targetDistOnTrack = trackLength;
	
			//uav between track begin/end
			} else {
				//recalculate only if the dist on track is less than RECALCULATION_DIST
				if(currentDistanceOnTrack>=(targetDistOnTrack-trackRecalcDist)) {
					targetDistOnTrack = MathHelper.clamp(currentDistanceOnTrack + trackAheadDist, 0, trackLength);
				}
			}
			
			//calculate current reference point on track
			getReferencePosition().setLatitudeRadians((fromPosition.getLatitudeRadians() + (targetDistOnTrack/trackLength * trackLatDiff)));
			getReferencePosition().setLongitudeRadians(fromPosition.getLongitudeRadians() + (targetDistOnTrack/trackLength * trackLongDiff));
			getReferencePosition().setAltitude(getCurrentDesiredAltitude());
			
		//don't follow track, head directly to target waypoint ignoring path
		} else {
			//reference point is straight to target waypoint
			getReferencePosition().set(targetPosition);
		}

		
		
		//HORIZONTAL CONTROL
		if(activateHorizontalControls) {
			float headingToTarget = gpsService.getPosition().azimuthToRadians(getReferencePosition());

			HeadingControlType headingType = headingControlType;
			if(headingType.equals(HeadingControlType.DYNAMIC)) {
				if(followTrack&&farFromTrack) {
					headingType = HeadingControlType.AILERON;
				}
			}
			pilot.holdCourseHeading(headingType, headingToTarget);
		}

		
		//VERTICAL CONTROL
		if(activateVerticalControls) {
			pilot.holdAltitude(altitudeType, getReferencePosition().getAltitude());
		}
		
		
		//SPEED CONTROL
		if(activateSpeedControls) {
			if(speedType.equals(WaypointSpeedType.ARRIVAL_TIME)) {
				//calculate ground speed needed to arrive at desired time
				if(targetTimeForArrival==0) targetTimeForArrival = 1;//avoid division by zero
				double remainingTime = targetTimeForArrival-getElapsedTime();
				if(remainingTime<=0) remainingTime = 1;//avoid division by zero and negative times
				
				//[distance to target] / [remaining time]
				pilot.holdSpeed(SpeedType.GROUND_SPEED, (float)(targetPosition.distance(gpsService.getPosition()) / remainingTime));
				currentSetpointSpeedType = SpeedType.GROUND_SPEED;
			} else {
				pilot.holdSpeed(SpeedType.getSpeedType(speedType), targetSpeed);
				currentSetpointSpeedType = SpeedType.getSpeedType(speedType);
			}
		}
		
		
		//ARRIVAL DETERMINATION
		//determine the minimum distance to target waypoint to consider arrival
		float desiredArrivalRadius = arrivalRadius;//fixed arrival radius

		//dynamic arrival radius (consider reached if, considering uav turn radius, you know it will undoubtely pass on target waypoint)
		if(arrivalRadius==0) {
			float trackHeadingErrorFactor = 1;
			if(followTrack) {
				//calculate optimized arrival radius
				float trackHeading = fromPosition.azimuthToRadians(targetPosition);
//					float trackHeading = (float)CoordinatesHelper.calculateHeading(fromLatitude, fromLongitude, targetLatitude, targetLongitude);
				float crossHeadingError = (float)MathHelper.getNormalizedErrorTwoPi(gpsService.getCourseHeading()-trackHeading);
				trackHeadingErrorFactor = 1F+(float)Math.abs(Math.sin(crossHeadingError));
			}
			desiredArrivalRadius = calculateCurrentMinTurnRadius()*trackHeadingErrorFactor;
		}
		//determine if uav has reached the goal
		if(isOnDesiredCoordinates(desiredArrivalRadius) || circularPathArea.isLocationInside(gpsService.getPosition())) {
			if(isOnDesiredAltitude(4)) {
				UAVHelper.notifyStateFine(logger, "Reached desired altitude/coordinates", LOG_ID+"reach");
				stop(false);
			}
		}
//		System.out.println(desiredArrivalRadius);

	}

	public boolean isOnDesiredCoordinates(float desiredArrivalRadius) {
		return gpsService.getPosition().distance(targetPosition)<=desiredArrivalRadius;
	}

	public boolean isOnDesiredAltitude(float maxAltitudeError) {
		return MathHelper.near(targetPosition.getAltitude(), advancedInstrumentsService.getAltitude(altitudeType), maxAltitudeError);
	}

	public float getArrivalRadius() {
		return arrivalRadius;
	}

	public void setArrivalRadius(float arrivalRadius) {
		this.arrivalRadius = arrivalRadius;
	}
	
	public void setFollowTrack(boolean followTrack) {
		this.followTrack = followTrack;
	}
	public boolean isFollowTrack() {
		return followTrack;
	}
	
	public float getTargetSpeed() {
		return targetSpeed;
	}
	public void setTargetSpeed(float targetSpeed) {
		this.targetSpeed = targetSpeed;
	}
	
	public double getCurrentDistanceFromTrack() {
		return currentDistanceFromTrack;
	}
	public double getCurrentDistanceOnTrack() {
		return currentDistanceOnTrack;
	}
	
	public double getTotalTrackDone() {
		return currentDistanceOnTrack/trackLength;
	}
	
	private float getCurrentDesiredAltitude() {
		//use glide ratio
		if(gpsService.getPosition().distance(targetPosition)>=glideDistanceThreshold) {
			//exponential altitude glide
			if(useExponentialAltitude) {
				return (float)(fromPosition.getAltitude() + (targetPosition.getAltitude()-fromPosition.getAltitude())*(1-MathHelper.getExponentialDecay(getTotalTrackDone(), EXPONENTIAL_ALTITUDE_LAMBDA)));
				
			//linear altitude glide
			} else {
				return (float)(fromPosition.getAltitude() + currentDistanceOnTrack*glideRatio);
			}
		//go directly to target altitude
		} else {
			return targetPosition.getAltitude();			
		}
	}

	public float getCurrentAltitudeError() {
		return advancedInstrumentsService.getAltitude(altitudeType) - getCurrentDesiredAltitude();
	}
	
	public void setActivateHorizontalControls(boolean useHorizontalControls) {
		this.activateHorizontalControls = useHorizontalControls;
	}
	public void setActivateVerticalControls(boolean useVerticalControls) {
		this.activateVerticalControls = useVerticalControls;
	}
	
	public boolean isUseExponentialAltitude() {
		return useExponentialAltitude;
	}
	public void setUseExponentialAltitude(boolean useExponentialAltitude) {
		this.useExponentialAltitude = useExponentialAltitude;
	}
	public void setTargetPosition(Coordinates targetPosition) {
		this.targetPosition = targetPosition;
	}
	public void setFromPosition(Coordinates fromPosition) {
		this.fromPosition = fromPosition;
	}
	public Coordinates getTargetPosition() {
		return targetPosition;
	}
	public Coordinates getFromPosition() {
		return fromPosition;
	}
	public void setAltitudeType(AltitudeType altitudeType) {
		this.altitudeType = altitudeType;
	}
	public AltitudeType getAltitudeType() {
		return altitudeType;
	}
	
	public WaypointSpeedType getSpeedType() {
		return speedType;
	}
	public void setSpeedType(WaypointSpeedType speedType) {
		this.speedType = speedType;
	}
	
	public void setTargetTimeForArrival(double targetTimeForArrival) {
		this.targetTimeForArrival = targetTimeForArrival;
	}
	public double getTargetTimeForArrival() {
		return targetTimeForArrival;
	}
	
	public void setActivateSpeedControls(boolean useSpeedControls) {
		this.activateSpeedControls = useSpeedControls;
	}
	
	public void setHeadingControlType(HeadingControlType headingControlType) {
		this.headingControlType = headingControlType;
	}
	
	public float getCurrentSetpointSpeed() {
		return currentSetpointSpeed;
	}
	public SpeedType getCurrentSetpointSpeedType() {
		return currentSetpointSpeedType;
	}
	
}
