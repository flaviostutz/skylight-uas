package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import org.jdesktop.swingx.JXMapViewer;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.infra.MathHelper;
import br.skylight.cucs.mapkit.painters.MapElementPainter;
import br.skylight.cucs.plugins.controlmap2d.PainterColors;

public class TakeOffPainter extends MapElementPainter<RunwayMapElement> {

	@Override
	public void paint(Graphics2D go, JXMapViewer map, int w, int h) {
		Graphics2D g = (Graphics2D) go.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        //put ms1 as runway start and ms2 as end
		RunwayMapElement ms1 = getGroup().getElements().get(0);
		RunwayMapElement ms2 = getGroup().getElements().get(1);
        if(ms1.isRunwayEnd()) {
    		ms1 = getGroup().getElements().get(1);
    		ms2 = getGroup().getElements().get(0);
        }
		Point2D p1 = ms1.getPixelPosition();
		Point2D p2 = ms2.getPixelPosition();
		int offx = (int)(p2.getX()-p1.getX());
		int offy = (int)(p2.getY()-p1.getY());
		
        //translate graphics to be centered on ms1
        g.translate(p1.getX(), p1.getY());
        
		//draw runway line between reference points
		g.setPaint(PainterColors.lineRunwayColor);
		g.setStroke(new BasicStroke(5));
		g.drawLine(0,0, offx, offy);
			
		//draw plane and runway end points
		//rotate graphics reference
		g = (Graphics2D) g.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		double heading = -MathHelper.getHeading(offx, offy, 0,0) - 90;
		double heading = MathHelper.getAngleTo(offx, offy, 0,0);
		g.rotate(heading);
		
		//draw plane
		g.setPaint(PainterColors.getContourAndFillColor(ms1)[1]);
		BasicStroke bs = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g.setStroke(bs);
		g.drawLine(-10, 0, 10, 0);
		g.drawLine(3, -9, 3, 9);
		g.drawLine(-7, -3, -7, 3);
			
		//draw runway end
		g.rotate(Math.toRadians(-heading));
        g.translate(offx, offy);
		g.rotate(Math.toRadians(heading));
		Polygon p = new Polygon();
		p.addPoint(5, 5);
		p.addPoint(5, -5);
		p.addPoint(-5, -5);
		p.addPoint(-5, 5);
		g.setStroke(new BasicStroke(1));
		g.setPaint(PainterColors.getContourAndFillColor(ms2)[1]);
		g.fill(p);
		g.setPaint(PainterColors.getContourAndFillColor(ms2)[0]);
		g.draw(p);
		
		super.paint(go, map, w, h);
	}
	
	public Polygon paintElement(Graphics2D g, RunwayMapElement element) {
		
		Polygon mask = new Polygon();

		if(!element.isRunwayEnd()) {
			mask.addPoint(-10, 10);
			mask.addPoint(10, 10);
			mask.addPoint(10, -10);
			mask.addPoint(-10, -10);
			
		} else {
			mask.addPoint(-5, 5);
			mask.addPoint(5, 5);
			mask.addPoint(5, -5);
			mask.addPoint(-5, -5);
		}

		drawAltitude(g, element, AltitudeType.AGL);
		
		return mask;
	}
	
}
