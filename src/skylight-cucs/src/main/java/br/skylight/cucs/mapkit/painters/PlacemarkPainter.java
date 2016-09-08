package br.skylight.cucs.mapkit.painters;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.HashSet;
import java.util.Set;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;

import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.plugins.missionplan.map.PlacemarkMapElement;

public class PlacemarkPainter extends MapElementPainter<PlacemarkMapElement> {

	private WaypointPainter<JXMapViewer> wp = new WaypointPainter<JXMapViewer>();

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		Set<Waypoint> waypoints = new HashSet<Waypoint>();
		for (MapElement me : getGroup().getElements()) {
			Waypoint wt = new Waypoint();
			wt.setPosition(me.getPosition());
			waypoints.add(wt);
		}
		wp.setWaypoints(waypoints);
		wp.paint(g, map, w, h);
		super.paint(g, map, w, h);
	}
	
	@Override
	public Polygon paintElement(Graphics2D g, PlacemarkMapElement elem) {
		drawDefaultLabel(g, elem);
		drawDefaultPositionInfo(g, elem);
		Polygon p = new Polygon();
		p.addPoint(10, -20);
		p.addPoint(10, 3);
		p.addPoint(-10, 3);
		p.addPoint(-10, -20);
		return p;
	}

}
