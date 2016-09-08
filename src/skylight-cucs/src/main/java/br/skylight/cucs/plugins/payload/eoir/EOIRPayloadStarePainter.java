package br.skylight.cucs.plugins.payload.eoir;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;

import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.painters.MapElementPainter;

public class EOIRPayloadStarePainter<T extends EOIRPayloadStareMapElement> extends MapElementPainter<T> {

	@Override
	protected Polygon paintElement(Graphics2D go, EOIRPayloadStareMapElement elem) {
		return paintElement(go, elem, elem.getPayload(), elem.getVehicle());
	}
	
	public Polygon paintElement(Graphics2D go, MapElement elem, Payload payload, Vehicle vehicle) {
		int w = 5;
		int h = 5;
		int d = 3;
		
		Rectangle2D r = new Rectangle2D.Float(-w,-h, 2*w,2*h);
		//fill rectangle if selected
		Graphics2D g = (Graphics2D) go.create();
		if(elem.isSelected() || elem.isMouseOver()) {
			g.setColor(new Color(0.7F,0F,0F,0.5F));
		}
		g.fill(r);
		
		//centre rectangle
		g.setColor(Color.LIGHT_GRAY);
//		g.drawOval(-d+1, -d+1, d+1, d+1);
		g.drawLine(-d,-d, d,d);
		g.drawLine(-d,d, d,-d);
		
		if(elem.isSelected()) {
			g.setColor(Color.YELLOW);
		} else if(elem.isMouseOver()) {
			g.setColor(Color.LIGHT_GRAY);
		} else {
			g.setColor(Color.WHITE);
		}
		g.setStroke(new BasicStroke(2));
		g.drawLine(-d, -h, -w, -h);
		g.drawLine(-w, -h, -w, h);
		g.drawLine(-w, h, -d, h);

		g.drawLine(d, -h, w, -h);
		g.drawLine(w, -h, w, h);
		g.drawLine(w, h, d, h);

		drawText(g, elem, elem.getLabel(), -12, true);
		
		Polygon mask = new Polygon();
		mask.addPoint(-w-1, -h-1);
		mask.addPoint(-w-1, h+1);
		mask.addPoint(w+1, h+1);
		mask.addPoint(w+1, -h-1);
		
		return mask;
	}

}
