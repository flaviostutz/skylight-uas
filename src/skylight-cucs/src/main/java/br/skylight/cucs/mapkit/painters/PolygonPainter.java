package br.skylight.cucs.mapkit.painters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import org.jdesktop.swingx.JXMapViewer;

import br.skylight.cucs.mapkit.MapElement;

public class PolygonPainter<T extends MapElement> extends MapElementPainter<T> {

	private static final MapElementColors colors = 
		new MapElementColors(new Color(64, 109, 185), new Color(18, 33, 55), Color.RED);
	
	private Color polygonFillColor = new Color(255,255,255,40);
	private Color polygonContourColor = new Color(255,255,255,190);
	private Stroke polygonStroke = null;
	
	//draw connections between map elements
	public void paint(Graphics2D go, JXMapViewer map, int w, int h) {
		Graphics2D g = (Graphics2D) go.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

		Color c = Color.WHITE;
		g.setColor(c);
        
        List<T> elements = getGroup().getElements();
        Polygon polygon = new Polygon();

		synchronized (elements) {
			for (MapElement me : elements) {
	            Point2D pt = map.getTileFactory().geoToPixel(me.getPosition(), map.getZoom());
	            polygon.addPoint((int)pt.getX(), (int)pt.getY());
			}
		}

		if(elements.size()>=2) {
			if(polygonStroke!=null) {
				g.setStroke(polygonStroke);
			}
			//draw polygon overlay
			if(polygonFillColor!=null) {
				g.setPaint(polygonFillColor);
				g.fillPolygon(polygon);
			}

			if(polygonContourColor!=null) {
				g.setPaint(polygonContourColor);
				g.drawPolygon(polygon);
			}
		}
		
        g.dispose();
        
        super.paint(go, map, w, h);
	}
	
	@Override
	//draw each map element
	protected Polygon paintElement(Graphics2D g, T elem) {
		drawDefaultLabel(g, elem);
		drawDefaultPositionInfo(g, elem);
		Polygon p = new Polygon();
		p.addPoint(-2, 2);
		p.addPoint(2, 2);
		p.addPoint(2, -2);
		p.addPoint(-2, -2);
		
		g.setColor(colors.getFill(elem));
		g.fillPolygon(p);
		g.setColor(colors.getContour(elem));
		g.drawPolygon(p);
		
		Polygon m = new Polygon();
		m.addPoint(-4, 4);
		m.addPoint(4, 4);
		m.addPoint(4, -4);
		m.addPoint(-4, -4);
		return m;
	}

	public void setPolygonContourColor(Color polygonContourColor) {
		this.polygonContourColor = polygonContourColor;
	}
	
	public void setPolygonFillColor(Color polygonFillColor) {
		this.polygonFillColor = polygonFillColor;
	}
	
	public void setPolygonStroke(Stroke polygonStroke) {
		this.polygonStroke = polygonStroke;
	}
}
