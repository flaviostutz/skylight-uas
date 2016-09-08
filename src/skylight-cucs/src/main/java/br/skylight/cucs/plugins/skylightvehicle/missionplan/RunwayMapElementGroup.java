package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.Mission;
import br.skylight.commons.dli.skylight.Runway;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.mapkit.painters.MapElementPainter;
import br.skylight.cucs.plugins.skylightvehicle.missionplan.RunwayMapElement.RunwayPoint;
import br.skylight.cucs.plugins.skylightvehicle.missionplan.RunwayMapElement.RunwayType;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicle;

public class RunwayMapElementGroup extends MapElementGroup<RunwayMapElement> {

	public RunwayMapElementGroup(String name, int layerNumber, final RunwayType runwayType, final Mission mission, final SkylightVehicle sv, final Runway runway, MapElementPainter<RunwayMapElement> painter, final MapKit mapKit, final Object groupId) {
		super(mapKit, name, layerNumber, 
			new MapElementBridge<RunwayMapElement>() {
			@Override
			public RunwayMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<RunwayMapElement> group) {
				Coordinates pos = new Coordinates(position.getLatitude(), position.getLongitude(), 0);
				runway.getPoint1().set(pos);
				Coordinates pos2 = new Coordinates();
				CoordinatesHelper.calculateCoordinatesFromRelativePosition(pos2, pos, -200, 0);
				runway.getPoint2().set(pos2);
				RunwayMapElement me1 = new RunwayMapElement(mission, runway, runwayType, RunwayPoint.POINT1, sv);
				
				//create the second point of runtway
				RunwayMapElement me2 = new RunwayMapElement(mission, runway, runwayType, RunwayPoint.POINT2, sv);
				mapKit.addMapElement(groupId, me2, -1, RunwayMapElement.class);
				
				return me1;
			};
//			@Override
//			public void onElementMoved(RunwayMapElement me, MapElementGroup<RunwayMapElement> group) {
//				if(me.getRunwayPoint().equals(RunwayPoint.POINT1)) {
//					me.getRunway().getPoint1().setLatitude(me.getPosition().getLatitude());
//					me.getRunway().getPoint1().setLongitude(me.getPosition().getLongitude());
//				} else if(me.getRunwayPoint().equals(RunwayPoint.POINT2)) {
//					me.getRunway().getPoint2().setLatitude(me.getPosition().getLatitude());
//					me.getRunway().getPoint2().setLongitude(me.getPosition().getLongitude());
//				}
//			}
			@Override
			public void onElementDeleted(RunwayMapElement me, MapElementGroup<RunwayMapElement> group) {
				if(me.getRunwayType().equals(RunwayType.TAKE_OFF)) {
					sv.getSkylightMission().getTakeoffLandingConfiguration().setValidTakeOffRunway(false);
				} else if(me.getRunwayType().equals(RunwayType.LANDING)) {
					sv.getSkylightMission().getTakeoffLandingConfiguration().setValidLandingRunway(false);
				}
			}
		}, painter);
	}
	
}
