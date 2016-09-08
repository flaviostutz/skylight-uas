package br.skylight.cucs.mapkit.painters;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.List;

import org.jdesktop.swingx.JXMapViewer;

import br.skylight.commons.Coordinates;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.plugins.missionplan.map.PathMapElement;

public class PathRulerPainter extends PathPainter<PathMapElement> {

	private NumberFormat nf = NumberFormat.getNumberInstance();
	private NumberFormat nf2 = NumberFormat.getNumberInstance();
	private int minimumDistanceBetweenPoints = -1;
	
	public PathRulerPainter() {
		super();
		setLineColor(Color.YELLOW);
		setLineStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
		setLabelColor(Color.ORANGE);
//		setLabelFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf2.setMaximumFractionDigits(1);
		nf2.setMinimumFractionDigits(0);
	}
	
	@Override
	public void paint(Graphics2D go, JXMapViewer map, int w, int h) {
		super.paint(go, map, w, h);

		//create graphics
		Graphics2D g = (Graphics2D) go.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        double totalDistance = 0;
        List<PathMapElement> elements = getGroup().getElements();
		Coordinates lastPos = null;
		double lastPlottedDistance = 0;
		synchronized (elements) {
			for (MapElement me : elements) {
				Coordinates pos = new Coordinates(me.getPosition().getLatitude(), me.getPosition().getLongitude(), 0);
				if(lastPos!=null) {
					double distance = lastPos.distance(pos);
					totalDistance += distance;
					if((totalDistance-lastPlottedDistance)>=minimumDistanceBetweenPoints) {
						float bearing = lastPos.azimuthTo(pos);
						me.setLabel(nf.format(totalDistance) + "m  " + nf2.format(bearing) + "°");
						lastPlottedDistance = totalDistance;
					}
				}
				lastPos = pos;
			}
		}
	}
	
	public void setMinimumDistanceBetweenPoints(int minimumDistanceBetweenPoints) {
		this.minimumDistanceBetweenPoints = minimumDistanceBetweenPoints;
	}

}
