package br.skylight.cucs.mapkit.painters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import br.skylight.cucs.mapkit.MapElement;

public class DefaultPainter<T extends MapElement> extends MapElementPainter<T> {

	@Override
	protected Polygon paintElement(Graphics2D g, T elem) {
		g.setColor(Color.YELLOW);
		g.drawRect(-10, -10, 20, 20);
		
		Polygon p = new Polygon();
		p.addPoint(10, 10);
		p.addPoint(10, -10);
		p.addPoint(-10, -10);
		p.addPoint(-10, 10);
		return p;
	}

}
