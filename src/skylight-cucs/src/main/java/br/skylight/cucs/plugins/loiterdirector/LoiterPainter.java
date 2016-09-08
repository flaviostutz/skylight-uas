package br.skylight.cucs.plugins.loiterdirector;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import br.skylight.commons.MeasureType;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.LoiterDirection;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.painters.MapElementPainter;
import br.skylight.cucs.plugins.controlmap2d.PainterColors;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class LoiterPainter<T extends LoiterMapElement> extends MapElementPainter<T> {

	@Override
	protected Polygon paintElement(Graphics2D go, LoiterMapElement elem) {
		return paintElement(go, elem, elem.getLoiterConfiguration());
	}
	
	public Polygon paintElement(Graphics2D go, MapElement elem, LoiterConfiguration loiterConfiguration) {
		//SHOW CENTER POINT
		Shape c = new Ellipse2D.Float(-4f, -4f, 8f, 8f);
		Graphics2D g = (Graphics2D) go.create();
		g.setColor(PainterColors.getContourAndFillColor(elem)[1]);
		g.fill(c);
		g.setColor(PainterColors.getContourAndFillColor(elem)[0]);
		g.draw(c);

		//SHOW RADIUS
		float[] xyLength = ViewHelper.metersToPixels(loiterConfiguration.getLoiterRadius(), elem.getPosition(), elem.getMap());
		c = new Ellipse2D.Float(-xyLength[0], -xyLength[1], xyLength[0]*2, xyLength[1]*2);
		g = (Graphics2D) go.create();
		g.setColor(PainterColors.radiusContourColor);
		g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] {9}, 0));
		g.draw(c);
		if(elem.isSelected() || elem.isMouseOver()) {
			g.setColor(PainterColors.radiusFillColor);
			g.fill(c);
		}
		
		//SHOW LOITER DIRECTION
		if(loiterConfiguration.getLoiterDirection().equals(LoiterDirection.COUNTER_CLOCKWISE) || loiterConfiguration.getLoiterDirection().equals(LoiterDirection.CLOCKWISE)) {
			g.setColor(PainterColors.radiusDirectionColor);
			g.setStroke(PainterColors.arrowStroke);
			Shape s = CUCSViewHelper.ARROW;
			AffineTransform t = AffineTransform.getTranslateInstance(0, -s.getBounds().getHeight()/2);
			s = t.createTransformedShape(s);
			t = AffineTransform.getScaleInstance(xyLength[0]/50, xyLength[0]/50);
			s = t.createTransformedShape(s);
			//rotate to show the right direction
			if(loiterConfiguration.getLoiterDirection().equals(LoiterDirection.COUNTER_CLOCKWISE)) {
				t = AffineTransform.getRotateInstance(Math.PI);
				s = t.createTransformedShape(s);
			}
			//translate arrow from center
			t = AffineTransform.getTranslateInstance(xyLength[0], 0);
			s = t.createTransformedShape(s);
			for(int i=0; i<5; i++) {
				//rotate arrow along loiter radius
				t = AffineTransform.getRotateInstance(Math.toRadians(72));
				s = t.createTransformedShape(s);
				g.fill(s);
			}
		}
		
		//DRAW INFO TEXT
		drawDefaultPositionInfo(go, elem);
		drawAltitude(go, elem, loiterConfiguration.getAltitudeType());
		drawText(g, elem, "Radius: " + MeasureType.DISTANCE.convertToTargetUnitStr(loiterConfiguration.getLoiterRadius(), true), 55, true);
		
		Polygon mask = new Polygon();
		mask.addPoint(-8, 8);
		mask.addPoint(8, 8);
		mask.addPoint(8, -8);
		mask.addPoint(-8, -8);

		drawDefaultLabel(g, elem);
		
		return mask;
	}

}
