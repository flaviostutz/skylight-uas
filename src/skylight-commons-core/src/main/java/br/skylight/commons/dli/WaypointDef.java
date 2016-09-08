package br.skylight.commons.dli;

import java.util.ArrayList;
import java.util.List;

import br.skylight.commons.Mission;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.infra.MeasureHelper;

public class WaypointDef {

	public enum WaypointState {IDLE_WAYPOINT, FROM_WAYPOINT, TO_WAYPOINT, NEXT_WAYPOINT}
	private static final double COORDINATES_DIFF = 0.000001;
	
	private int waypointNumber;
	private AVPositionWaypoint positionWaypoint;
	private AVLoiterWaypoint loiterWaypoint;
	private List<PayloadActionWaypoint> payloadActionWaypoints = new ArrayList<PayloadActionWaypoint>();
	private Mission mission;
	
	//last known state from FromToNextWaypointStates
	private WaypointState state = WaypointState.IDLE_WAYPOINT;
	private double waypointTime;
	
	public Mission getMission() {
		return mission;
	}
	public void setMission(Mission mission) {
		this.mission = mission;
	}
	
	public boolean  isExtended() {
		return payloadActionWaypoints.size()>0 || loiterWaypoint!=null;
	}
	
	public AVLoiterWaypoint getLoiterWaypoint() {
		return loiterWaypoint;
	}
	public void setLoiterWaypoint(AVLoiterWaypoint loiterWaypoint) {
		this.loiterWaypoint = loiterWaypoint;
	}
	public List<PayloadActionWaypoint> getPayloadActionWaypoints() {
		return payloadActionWaypoints;
	}
	public void setPayloadActionWaypoints(List<PayloadActionWaypoint> payloadActionWaypoints) {
		this.payloadActionWaypoints = payloadActionWaypoints;
	}
	public AVPositionWaypoint getPositionWaypoint() {
		return positionWaypoint;
	}
	public void setPositionWaypoint(AVPositionWaypoint positionWaypoint) {
		this.positionWaypoint = positionWaypoint;
	}
	public int getWaypointNumber() {
		return waypointNumber;
	}
	public void setWaypointNumber(int waypointNumber) {
		this.waypointNumber = waypointNumber;
	}
	
	public WaypointState getState() {
		return state;
	}
	public double getWaypointTime() {
		return waypointTime;
	}

	public void processFromToNextWaypointStates(FromToNextWaypointStates states) {
		if(states.getVehicleID()==positionWaypoint.getVehicleID()) {
			
			if(states.getFromWaypointNumber()==waypointNumber &&
				MeasureHelper.areInRange(states.getFromWaypointLatitude(), positionWaypoint.getWaypointToLatitudeOrRelativeY(), COORDINATES_DIFF) && 
				MeasureHelper.areInRange(states.getFromWaypointLongitude(),positionWaypoint.getWaypointToLongitudeOrRelativeX(), COORDINATES_DIFF)) {
				state = WaypointState.FROM_WAYPOINT;
				waypointTime = states.getFromWaypointTime();
//				System.out.println("FROM " + waypointNumber);
				
			} else if(states.getToWaypointNumber()==waypointNumber &&
				MeasureHelper.areInRange(states.getToWaypointLatitude(),positionWaypoint.getWaypointToLatitudeOrRelativeY(), COORDINATES_DIFF) && 
				MeasureHelper.areInRange(states.getToWaypointLongitude(),positionWaypoint.getWaypointToLongitudeOrRelativeX(), COORDINATES_DIFF)) {
				state = WaypointState.TO_WAYPOINT;
				waypointTime = states.getToWaypointTime();
//				System.out.println("TO " + waypointNumber);
				
			} else if(states.getNextWaypointNumber()==waypointNumber &&
				MeasureHelper.areInRange(states.getNextWaypointLatitude(),positionWaypoint.getWaypointToLatitudeOrRelativeY(), COORDINATES_DIFF) &&
				MeasureHelper.areInRange(states.getNextWaypointLongitude(),positionWaypoint.getWaypointToLongitudeOrRelativeX(), COORDINATES_DIFF)) {
				state = WaypointState.NEXT_WAYPOINT;
				waypointTime = states.getNextWaypointTime();
//				System.out.println("NEXT " + waypointNumber);
				
			} else if(!state.equals(WaypointState.FROM_WAYPOINT)) {
				state = WaypointState.IDLE_WAYPOINT;
				waypointTime = 0;
//				System.out.println("IDLE " + waypointNumber);
			}
		}
	}
	
	public void changeWaypointNumber(int newWaypointNumber, Mission mission) {
		//look for waypoints whose next waypoint was the former waypoint
		for(AVPositionWaypoint pw : mission.getPositionWaypoints()) {
			if(pw.getNextWaypoint()==getWaypointNumber()) {
				pw.setNextWaypoint(newWaypointNumber);
			}
		}

		//look for routes whose initial waypoint was the former waypoint
		for(AVRoute r : mission.getRoutes()) {
			if(r.getInitialWaypointNumber()==getWaypointNumber()) {
				r.setInitialWaypointNumber(newWaypointNumber);
			}
		}
		
		//change waypoint numbers
		if(positionWaypoint!=null) {
			positionWaypoint.setWaypointNumber(newWaypointNumber);
		}
		if(loiterWaypoint!=null) {
			loiterWaypoint.setWaypointNumber(newWaypointNumber);
		}
		for (PayloadActionWaypoint aw : payloadActionWaypoints) {
			aw.setWaypointNumber(newWaypointNumber);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (waypointNumber ^ (waypointNumber >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WaypointDef other = (WaypointDef) obj;
		if (waypointNumber != other.waypointNumber)
			return false;
		return true;
	}

}
