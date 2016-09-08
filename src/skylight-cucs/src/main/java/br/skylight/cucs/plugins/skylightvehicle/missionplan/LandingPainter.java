package br.skylight.cucs.plugins.skylightvehicle.missionplan;

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

import br.skylight.commons.Coordinates;
import br.skylight.commons.infra.LandingHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.cucs.plugins.controlmap2d.PainterColors;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class LandingPainter extends RunwayPainter {

	private float currentRotation;
	private float currentOffx;
	private float currentOffy;
	
	@Override
	public void paint(Graphics2D go, JXMapViewer map, int w, int h) {
		currentOffx = 0;
		currentOffy = 0;
		currentRotation = 0;
		
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
		int endx = (int)(p2.getX()-p1.getX());
		int endy = (int)(p2.getY()-p1.getY());
		
		//draw runway line between reference points
		translate(g, p1);
		g.setPaint(PainterColors.lineRunwayColor);
		g.setStroke(new BasicStroke(5));
		g.drawLine(0,0, endx, endy);
			
		//draw plane and runway end points
		g = (Graphics2D) g.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		double heading = -MathHelper.getHeading(endx, endy, 0,0)-90;
		double heading = MathHelper.getAngleTo(endx, endy, 0,0);
//		System.out.println("endx: " + endx + "; endy: " + endy + "; ch: " + currentHeading);
		rotate(g, heading);

		//draw runway start
		Polygon p = new Polygon();
		p.addPoint(5, 5);
		p.addPoint(5, -5);
		p.addPoint(-5, -5);
		p.addPoint(-5, 5);
		g.setStroke(new BasicStroke(1));
		g.setPaint(PainterColors.getContourAndFillColor(ms1)[1]);
		g.fill(p);
		g.setPaint(PainterColors.getContourAndFillColor(ms1)[0]);
		g.draw(p);

		//draw plane on runway end
		translate(g, p2);
		g.setPaint(PainterColors.getContourAndFillColor(ms1)[1]);
		BasicStroke bs = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g.setStroke(bs);
		g.drawLine(-10, 0, 10, 0);
		g.drawLine(3, -9, 3, 9);
		g.drawLine(-7, -3, -7, 3);
		
		//calculate reference points
		translate(g, p1);
		rotate(g, -heading);//back to initial rotation
		Coordinates[] points = LandingHelper.calculateLandingPoints(ms1.getRunway(), ms1.getSkylightVehicle().getSkylightVehicleConfiguration().getLandingApproachScale());
		Point2D pp1 = map.getTileFactory().geoToPixel(new GeoPosition(points[0].getLatitude(), points[0].getLongitude()), map.getZoom());
		Point2D pp2 = map.getTileFactory().geoToPixel(new GeoPosition(points[1].getLatitude(), points[1].getLongitude()), map.getZoom());
		Point2D pp3 = map.getTileFactory().geoToPixel(new GeoPosition(points[2].getLatitude(), points[2].getLongitude()), map.getZoom());
		Point2D pp4 = map.getTileFactory().geoToPixel(new GeoPosition(points[3].getLatitude(), points[3].getLongitude()), map.getZoom());
		Point2D pp5 = map.getTileFactory().geoToPixel(new GeoPosition(points[4].getLatitude(), points[4].getLongitude()), map.getZoom());
//		System.out.println(">>> "+pp1.x + "," + pp1.y);

		//draw downwind leg
		drawPath(g, pp1, pp2);
		drawPath(g, pp2, pp3);

		//draw base leg
		drawPath(g, pp3, pp4);
		
		//draw final leg
		translate(g, pp4);
		translate(g, pp5);
		g.fillOval(-1, -1, 2, 2);
		
		super.paint(go, map, w, h);
	}

	private void drawPath(Graphics2D g, Point2D pp1, Point2D pp2) {
		translate(g, pp1);
		
		//draw line between points
		g.setColor(new Color(255,255,255,150));
		g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(0, 0, (int)(pp2.getX()-pp1.getX()), (int)(pp2.getY()-pp1.getY()));
		
		//draw arrow
		g.setColor(PainterColors.normalPathArrowColor);
		g.setStroke(PainterColors.arrowStroke);
		
		//rotate arrow
		double angle = MathHelper.getAngleTo(pp1.getX(), pp1.getY(), pp2.getX(), pp2.getY());
		AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(angle-90));
		Shape s = t.createTransformedShape(CUCSViewHelper.ARROW);

		//translate arrow to the center of the line
		t = AffineTransform.getTranslateInstance((pp2.getX()-pp1.getX())/2, (pp2.getY()-pp1.getY())/2);
		s = t.createTransformedShape(s);
		g.fill(s);
	}

	private void rotate(Graphics2D g, double angleDegrees) {
		g.rotate(-currentRotation);
		currentRotation += Math.toRadians(angleDegrees);
		g.rotate(currentRotation);
	}

	private void translate(Graphics2D g, Point2D point) {
		translate(g, (int)point.getX(), (int)point.getY());
	}
	
	private void translate(Graphics2D g, int offx, int offy) {
		g.rotate(-currentRotation);
        g.translate(-currentOffx, -currentOffy);
        g.translate(offx, offy);
		g.rotate(currentRotation);
		currentOffx = offx;
		currentOffy = offy;
	}

}
