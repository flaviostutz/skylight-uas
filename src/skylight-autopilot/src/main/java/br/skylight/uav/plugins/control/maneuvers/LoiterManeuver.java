package br.skylight.uav.plugins.control.maneuvers;

import java.util.logging.Logger;

import traer.physics.Vector3D;
import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.uav.plugins.control.Pilot.HeadingControlType;

@ManagedMember(useDependenciesFrom=GotoWaypointManeuver.class)
public class LoiterManeuver extends Maneuver implements ManeuverListener<GotoWaypointManeuver> {

	private static final Logger logger = Logger.getLogger(LoiterManeuver.class.getName());
	
	private Coordinates centerPosition;
	private float radius = 30;
	
	private double loiterStartTime;
	private double targetTimeLoitering = 0;
	private double timeoutReachingLoiterCircle = 0;
	
	private TimedBoolean verificationTimer = new TimedBoolean(4000);
	private boolean rotateClockwise;
	//time (s) to reach reference point on circle track according to current ground speed
	//we tried to use fixed distance, but time is better due to varying ground speed
	private static final float TIME_REFERENCE_ON_CIRCLE_TRACK = 3F;
	//time (s) to reach reference on circle track in which a new reference will be calculated 
	private static final float TIME_RECALCULATE_REFERENCE = 3.7F;
	
	//pre-allocated instances (could be put inside method)
	private Vector3D loiterCenterToUavPosition = new Vector3D();
	private Coordinates uavPositionPoint = new Coordinates(0,0,0);
	private Coordinates referencePoint = new Coordinates(0,0,0);

//	@MemberInjection(createNewInstance=true)
	private GotoWaypointManeuver gotoWaypointManeuver;

	@Override
	public void onActivate() throws Exception {
		loiterStartTime = 0;
		//maneuver was managed after plugins startup
		if(pluginManager.isPluginsStarted()) {
			gotoWaypointManeuver = new GotoWaypointManeuver();
			pluginManager.manageObject(gotoWaypointManeuver);
			gotoWaypointManeuver.setManeuverListener(getThis());
		//maneuver was managed before plugins startup
		} else {
			pluginManager.executeAfterStartup(new Runnable() {
				public void run() {
					gotoWaypointManeuver = new GotoWaypointManeuver();
					pluginManager.manageObject(gotoWaypointManeuver);
					gotoWaypointManeuver.setManeuverListener(getThis());
				}
			});
		}
	}
	
	private LoiterManeuver getThis() {
		return this;
	}
	
	@Override
	public void onStart() throws Exception {
		//DETERMINE BEST ROTATION DIRECTION
		
		//LAT/LONG REFERENCE
		//uav position geo point
		uavPositionPoint = gpsService.getPosition();

		//METERS REFERENCE
		//uav speed vector
		Vector3D speed = advancedInstrumentsService.getSpeed();

		//center to uav position
		float angle = (float)centerPosition.azimuthToRadians(uavPositionPoint);
		float distance = uavPositionPoint.distance(centerPosition);
//		loiterCenterToUavPosition.setX((float)(distance*Math.cos(CoordinatesHelper.headingToMathReference(angle))));
//		loiterCenterToUavPosition.setY((float)(distance*Math.sin(CoordinatesHelper.headingToMathReference(angle))));
		//use heading reference for this vector because it will be crossed with gps speed in heading reference
		loiterCenterToUavPosition.setX(CoordinatesHelper.getUComponent(angle,distance));
		loiterCenterToUavPosition.setY(CoordinatesHelper.getVComponent(angle,distance));
		
		//calculate speed CROSS centerToUav to determine rotation direction
//		System.out.println(loiterCenterToUavPosition.x() + "," + loiterCenterToUavPosition.y() + " " + angle);
		rotateClockwise = speed.cross(loiterCenterToUavPosition).z()<0;
//		System.out.println("CLOCKWISE " + rotateClockwise);
		gotoWaypointManeuver.setFollowTrack(false);
		gotoWaypointManeuver.setArrivalRadius(-1);
		gotoWaypointManeuver.start();
		
		//zero reference point
		referencePoint.setLatitude(0);
		referencePoint.setLongitude(0);
		referencePoint.setAltitude(0);

		loiterStartTime = 0;
		verificationTimer.reset();
	}
	
	@Override
	public void onStop(boolean aborted) throws Exception {
		if(gotoWaypointManeuver!=null) {
			gotoWaypointManeuver.stop(aborted);
		}
	}

	@Override
	public void step() throws Exception {
		//define next reference point in circle
		uavPositionPoint = gpsService.getPosition();

		//not loitering yet (still going to loiter zone)
		if(loiterStartTime==0) {
			//start counting time only if near loiter circle
			if(centerPosition.distance(uavPositionPoint)<=(radius*1.3)
				&& Math.abs(centerPosition.getAltitude()-advancedInstrumentsService.getAltitude(gotoWaypointManeuver.getAltitudeType()))<=3) {
				loiterStartTime = System.currentTimeMillis()/1000.0;
			}
			if(timeoutReachingLoiterCircle>0) {
				if(getElapsedTime()>timeoutReachingLoiterCircle) {
					stop(true);
					logger.info("Loiter aborted. Vehicle hasn't reached loiter zone in " + timeoutReachingLoiterCircle +  " s");
					return;
				}
			}
		}
		
		//verify if we can use rudder for heading
		if(verificationTimer.checkTrue()) {
			float tr = calculateCurrentMinTurnRadius();
//			System.out.println("min radius: " + tr);
			//don't try to use rudder to avoid rudder/roll transitions during circle circuit
//			if(radius*0.7<tr) {//works well on low rudder actuations
			if(radius*0.2<tr) {
//				System.out.println("AILERON");
				gotoWaypointManeuver.setHeadingControlType(HeadingControlType.AILERON);
			} else {
//				System.out.println("DYN");
				gotoWaypointManeuver.setHeadingControlType(HeadingControlType.DYNAMIC);
			}
		}

		//heading from loiter center to uav position
		float angle = (float)centerPosition.azimuthToRadians(uavPositionPoint);
		float refAngle = (float)centerPosition.azimuthToRadians(referencePoint);
		float diffAngle = (float)Math.abs(MathHelper.getNormalizedErrorTwoPi(angle-refAngle));

		float distanceRecalculateReference = gpsService.getGroundSpeed() * TIME_RECALCULATE_REFERENCE;
		
		//recalculate reference ahead on circle track depending on loiter direction and radius
		if(referencePoint.getLatitude()==0//no reference point yet. force calculation 
				|| diffAngle<distanceRecalculateReference/radius) {//reference to uav angle too near
			
			//calculate an angle so that distance from ref point to uav is always DIST_RECALCULATE_REFERENCE
			float distanceReferenceOnCircleTrack = gpsService.getGroundSpeed() * TIME_REFERENCE_ON_CIRCLE_TRACK;
			float angleDiff = distanceReferenceOnCircleTrack/radius;
//			System.out.println("A " + rotateClockwise  + " " + Math.toDegrees(angle) + " " + Math.toDegrees(angleDiff));
			if(rotateClockwise) {
				angle += angleDiff;
			} else {
				angle += -angleDiff;
			}
//			System.out.println("R " + Math.toDegrees(angle));
			
			//rotate reference by heading angle
			double rx = Math.cos(CoordinatesHelper.headingToMathReference(angle)) * radius;
			double ry = Math.sin(CoordinatesHelper.headingToMathReference(angle)) * radius;
//			System.out.println("R " + rx + " " + ry);
			
			//calculate reference geo point and update gotowaypoint target
			CoordinatesHelper.calculateCoordinatesFromRelativePosition(referencePoint, centerPosition, rx, ry);
//			System.out.println("LOITER SET TARGET");
			gotoWaypointManeuver.setTargetPosition(referencePoint);
			setReferencePosition(referencePoint);
		}

		gotoWaypointManeuver.step();
		
		//maneuver finished
		if(gotoWaypointManeuver.isOnDesiredAltitude(4) && targetTimeLoitering>0 && loiterStartTime>0 && ((System.currentTimeMillis()/1000.0)-loiterStartTime)>targetTimeLoitering) {
			stop(false);
		}
	}

	public float getRadius() {
		return radius;
	}
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public void setTargetTimeLoitering(double targetTimeLoitering) {
		this.targetTimeLoitering = targetTimeLoitering;
	}
	public double getTargetTimeLoitering() {
		return targetTimeLoitering;
	}

	public void setCenterPosition(Coordinates centerPosition, AltitudeType altitudeType) {
		this.centerPosition = centerPosition;
		gotoWaypointManeuver.setAltitudeType(altitudeType);
		gotoWaypointManeuver.setTargetPosition(centerPosition);
	}
	public Coordinates getCenterPosition() {
		return gotoWaypointManeuver.getTargetPosition();
	}
	public void setSpeedType(WaypointSpeedType speedType) {
		gotoWaypointManeuver.setSpeedType(speedType);
	}
	public void setTargetSpeed(float targetSpeed) {
		gotoWaypointManeuver.setTargetSpeed(targetSpeed);
	}
	public void setTimeoutReachingLoiterCircle(double timeoutReachingLoiterCircle) {
		this.timeoutReachingLoiterCircle = timeoutReachingLoiterCircle;
	}
	public double getTimeoutReachingLoiterCircle() {
		return timeoutReachingLoiterCircle;
	}

	public void copyParametersFromGotoWaypointManeuver(GotoWaypointManeuver gm) {
		setCenterPosition(gm.getTargetPosition(), gm.getAltitudeType());
		setSpeedType(gm.getSpeedType());
		setTargetSpeed(gm.getTargetSpeed());
		setTimeoutReachingLoiterCircle(gm.getTargetTimeForArrival());
	}

	public void copyParametersFromLoiterConfiguration(LoiterConfiguration lc, VehicleSteeringCommand vc) {
		if(vc!=null) {
			setCenterPosition(new Coordinates(Math.toDegrees(vc.getLoiterPositionLatitude()), Math.toDegrees(vc.getLoiterPositionLongitude()), lc.getLoiterAltitude()), lc.getAltitudeType());
		}
		if(lc!=null) {
			setSpeedType(WaypointSpeedType.getWaypointSpeedType(lc.getSpeedType()));
			setTargetSpeed(lc.getLoiterSpeed());
			setRadius(lc.getLoiterRadius());
		}
		setTimeoutReachingLoiterCircle(0);
	}

	@Override
	public void maneuverFinished(GotoWaypointManeuver maneuver, boolean interrupted) {
	}
	
	public double getCurrentTimeLoitering() {
		if(loiterStartTime>0) {
			return (System.currentTimeMillis()/1000.0)-loiterStartTime;
		} else {
			return 0;
		}
	}
	
	public float getTargetSpeed() {
		return gotoWaypointManeuver.getTargetSpeed();
	}
	
}
