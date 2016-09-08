package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import java.awt.Graphics2D;
import java.awt.Polygon;

import br.skylight.cucs.mapkit.painters.MapElementPainter;

public abstract class RunwayPainter extends MapElementPainter<RunwayMapElement> {
	
	@Override
	protected Polygon paintElement(Graphics2D g, RunwayMapElement elem) {
		if(elem.getRunway().getPoint1().getLatitude()==0 && elem.getRunway().getPoint1().getLongitude()==0) {
			return null;
		}
		Polygon mask = new Polygon();
		if(!elem.isRunwayEnd()) {
			mask.addPoint(-5, 5);
			mask.addPoint(5, 5);
			mask.addPoint(5, -5);
			mask.addPoint(-5, -5);
			
		} else {
			mask.addPoint(-10, 10);
			mask.addPoint(10, 10);
			mask.addPoint(10, -10);
			mask.addPoint(-10, -10);
		}
		return mask;
	}

}
