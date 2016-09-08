package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import br.skylight.commons.MeasureType;
import br.skylight.cucs.mapkit.painters.MapElementPainter;
import br.skylight.cucs.plugins.controlmap2d.PainterColors;

public class FromToNextLocationPainter extends MapElementPainter<VehicleMapElement> {

	@Override
	protected Polygon paintElement(Graphics2D g, VehicleMapElement elem) {
		if(elem.getPosition().getLatitude()!=0 && elem.getPosition().getLongitude()!=0) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//			g2.setColor(PainterColors.CONTOUR_COLOR_GHOST_WAYPOINT);
//			g2.drawOval(-4, -4, 8, 8);
//			g2.drawLine(0, -5, 0, 5);
//			g2.drawLine(-5, 0, 5, 0);
			
			Polygon polygon = new Polygon();
			polygon.addPoint(-5, 5);
			polygon.addPoint(5, 5);
			polygon.addPoint(5, -5);
			polygon.addPoint(-5, -5);
			g2.rotate(Math.toRadians(45));
			g2.setColor(PainterColors.FILL_COLOR_GHOST_WAYPOINT);
			g2.fill(polygon);
			g2.setColor(PainterColors.CONTOUR_COLOR_GHOST_WAYPOINT);
			g2.draw(polygon);
			g2.rotate(Math.toRadians(-45));

			if(elem.isMouseOver()) {
				g2.drawString(MeasureType.ALTITUDE.convertToTargetUnitStr(elem.getAltitude(), true), 5, -4);
			}
		}
		return null;
	}

	@Override
	public int getLayerNumber() {
		return 200;
	}
	
}
