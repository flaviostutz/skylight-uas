package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Mission;
import br.skylight.commons.dli.enums.RunwayDirection;
import br.skylight.commons.dli.skylight.Runway;
import br.skylight.cucs.plugins.missionplan.map.MissionMapElement;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicle;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class RunwayMapElement extends MissionMapElement {

	public enum RunwayType {TAKE_OFF, LANDING}
	public enum RunwayPoint {POINT1, POINT2}
	
	private RunwayType runwayType;
	private RunwayPoint runwayPoint;
	private Runway runway;
	private SkylightVehicle skylightVehicle;

	public RunwayMapElement(Mission mission, Runway runway, RunwayType runwayType, RunwayPoint runwayPoint, SkylightVehicle skylightVehicle) {
		setMission(mission);
		this.runwayPoint = runwayPoint;
		this.runwayType = runwayType;
		this.skylightVehicle = skylightVehicle;
		if(runwayPoint.equals(RunwayPoint.POINT1)) {
			super.setPosition(CUCSViewHelper.toGeoPosition(runway.getPoint1()));
		} else if(runwayPoint.equals(RunwayPoint.POINT2)) {
			super.setPosition(CUCSViewHelper.toGeoPosition(runway.getPoint2()));
		}
	}
	
	@Override
	public void setPosition(GeoPosition position) {
		super.setPosition(position);
		if(runwayPoint.equals(RunwayPoint.POINT1)) {
			CUCSViewHelper.copyCoordinates(runway.getPoint1(), position);
		} else if(runwayPoint.equals(RunwayPoint.POINT2)) {
			CUCSViewHelper.copyCoordinates(runway.getPoint2(), position);
		}
	}
	
	public Runway getRunway() {
		return runway;
	}
	
	public boolean isRunwayEnd() {
		if(runway.getDirection().equals(RunwayDirection.RUNWAY12)) {
			return runwayPoint.equals(RunwayPoint.POINT2);
		} else {
			return runwayPoint.equals(RunwayPoint.POINT1);
		}
	}
	
	public SkylightVehicle getSkylightVehicle() {
		return skylightVehicle;
	}
	
	public RunwayType getRunwayType() {
		return runwayType;
	}
	
	public RunwayPoint getRunwayPoint() {
		return runwayPoint;
	}

}
