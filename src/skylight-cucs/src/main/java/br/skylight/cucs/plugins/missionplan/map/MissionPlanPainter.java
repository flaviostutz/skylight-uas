package br.skylight.cucs.plugins.missionplan.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.MeasureType;
import br.skylight.commons.Mission;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.mapkit.painters.LayerPainter;
import br.skylight.cucs.plugins.controlmap2d.PainterColors;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class MissionPlanPainter implements LayerPainter<JXMapViewer> {

	private Mission mission;
	private MapKit mapKit;
	
	public MissionPlanPainter(MapKit mapKit) {
		this.mapKit = mapKit;
	}
	
	public void paint(Graphics2D go, JXMapViewer map, int w, int h) {
		if(mission==null) return;
		Graphics2D g = (Graphics2D) go.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);
		
		MapElementGroup<WaypointMapElement> group = (MapElementGroup<WaypointMapElement>)mapKit.getMapElementGroup(MissionMapExtensionPointImpl.PREFIX_MISSION+mission.getMissionID(), WaypointMapElement.class);
		if(group==null) {
			return;
		}

		//draw lines between step points
		for (WaypointMapElement me1 : group.getElements()) {
			WaypointMapElement me2 = findWaypointMapElement(group, me1.getWaypointDef().getPositionWaypoint().getNextWaypoint());
			if(me2!=null) {
	        	GeoPosition pos1 = me1.getPosition();
	        	GeoPosition pos2 = me2.getPosition();
	        	Point2D p1 = map.getTileFactory().geoToPixel(pos1, map.getZoom());
	        	Point2D p2 = map.getTileFactory().geoToPixel(pos2, map.getZoom());
	        	
	        	//draw lines
        		Color[] colours = PainterColors.getWaypointContourFillPathArrowColor(me2);
        		g.setColor(colours[2]);
    			//tracejado
        		//g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] {9}, 0));
        		g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        		g.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
        		
	        	//draw arrow inside lines
        		g.setColor(Color.DARK_GRAY);
        		g.setStroke(PainterColors.arrowStroke);
        		Polygon p = CUCSViewHelper.ARROW;
        		//rotate arrow
        		double rot = Math.toDegrees(Math.atan((p2.getY()-p1.getY())/(p2.getX()-p1.getX())));
        		if(p2.getX()<p1.getX()) {
        			rot += 90;
        		} else {
        			rot -= 90;
        		}
        		if(rot<0) rot += 360;
        		AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(rot));
        		Shape s = t.createTransformedShape(p);
        		//translate arrow to point
        		t = AffineTransform.getTranslateInstance((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2);
        		s = t.createTransformedShape(s);
        		g.fill(s);
        		
        		//draw line length
        		if(me2.isSelected() || me2.isMouseOver()) {
        			g.setColor(Color.YELLOW);
        			double distance = CoordinatesHelper.calculateDistance(
							me1.getWaypointDef().getPositionWaypoint().getWaypointToLatitudeOrRelativeY(),
							me1.getWaypointDef().getPositionWaypoint().getWaypointToLongitudeOrRelativeX(),
							me2.getWaypointDef().getPositionWaypoint().getWaypointToLatitudeOrRelativeY(),
							me2.getWaypointDef().getPositionWaypoint().getWaypointToLongitudeOrRelativeX());
        			g.drawString(MeasureType.DISTANCE.convertToTargetUnitStr(distance, true), (int)((p1.getX()+p2.getX())/2), (int)((p1.getY()+p2.getY())/2));
        		}
			}
		}
		
	}
	
	private WaypointMapElement findWaypointMapElement(MapElementGroup<WaypointMapElement> group, int waypointNumber) {
		for (WaypointMapElement me : group.getElements()) {
			if(me.getWaypointDef().getWaypointNumber()==waypointNumber) {
				return me;
			}
		}
		return null;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public Mission getMission() {
		return mission;
	}

	@Override
	public int getLayerNumber() {
		return 19;
	}
	
}
