package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import br.skylight.commons.MeasureType;
import br.skylight.cucs.mapkit.painters.MapElementPainter;
import br.skylight.cucs.plugins.vehiclecontrol.VehicleMapElement;

public class ReferencePointPainter extends MapElementPainter<VehicleMapElement> {

	private static final Font FONT = new Font(Font.DIALOG, Font.PLAIN, 11);
	
	@Override
	protected Polygon paintElement(Graphics2D g, VehicleMapElement elem) {
		//draw current reference points
		if(elem.getPosition().getLatitude()!=0 && elem.getPosition().getLongitude()!=0) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Point2D pt2 = elem.getMap().getTileFactory().geoToPixel(elem.getPosition(), elem.getMap().getZoom());
			Point2D ep = elem.getPixelPosition();
			g2.translate(-ep.getX(), -ep.getY());
			g2.translate(pt2.getX(), pt2.getY());

			g2.setStroke(new BasicStroke(2));
//			g2.setColor(Color.YELLOW);
			g2.setFont(FONT);
			g2.setColor(new Color(1, 1, 1, 0.7F));
			g2.drawLine(-4, -4, 4, 4);
			g2.drawLine(-4, 4, 4, -4);

			g2.drawString(MeasureType.ALTITUDE.convertToTargetUnitStr(elem.getAltitude(), true), 5, -4);
		}
		return null;
	}

	@Override
	public int getLayerNumber() {
		return 3;
	}
	
}
