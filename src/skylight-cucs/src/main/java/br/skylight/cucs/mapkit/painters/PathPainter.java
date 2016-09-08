package br.skylight.cucs.mapkit.painters;

import java.awt.BasicStroke;
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

public class PathPainter<T extends MapElement> extends MapElementPainter<T> {

	private MapElementColors vertexColors = new MapElementColors(new Color(64, 109, 185), new Color(18, 33, 55), Color.RED);
	
	private Color lineColor = Color.YELLOW;
	private Stroke lineStroke;
	private Stroke pointStroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
	private boolean showVertex = true;
	
	//draw connections between map elements
	public void paint(Graphics2D go, JXMapViewer map, int w, int h) {
		
		Graphics2D g = (Graphics2D) go.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        Point2D lastPoint = null;
        List<T> elements = getGroup().getElements();
        
		if(lineStroke!=null) {
			g.setStroke(lineStroke);
		}
		for (MapElement me : elements) {
//			float ref = ((float)getGroup().getReferenceAltitude())/me.getAltitude();
//			Color c = Color.WHITE;
//			if(lineColor==null) {
//				if(ref<1) {
	//				63, 239, 49 - green
//					c = new Color((int)MathHelper.clamp(63+(ref*171), 63, 234), 239, 49);
//				} else {
//					ref = ref-1;
	//				234, 239, 49 - yellow
//					c = new Color(234, (int)MathHelper.clamp(239-(ref*190), 63, 239), 49);
//				}
	//			239, 49, 49 - vermelho
//			} else {
//				c = lineColor;
//			}
			Color c = lineColor;
            Point2D pt = map.getTileFactory().geoToPixel(me.getPosition(), map.getZoom());
			g.setColor(c);
			if(lastPoint!=null) {
				g.drawLine((int)lastPoint.getX(), (int)lastPoint.getY(), (int)pt.getX(), (int)pt.getY());
			}
            lastPoint = pt;
		}
		g.setStroke(pointStroke);
		
        g.dispose();
        
        super.paint(go, map, w, h);
	}
	
	@Override
	//draw each map element
	protected Polygon paintElement(Graphics2D g, T elem) {
		drawDefaultLabel(g, elem);
		drawDefaultPositionInfo(g, elem);

		Polygon m = null;
		
		if(showVertex) {
			Polygon p = new Polygon();
			p.addPoint(-2, 2);
			p.addPoint(2, 2);
			p.addPoint(2, -2);
			p.addPoint(-2, -2);
			
			g.setColor(vertexColors.getFill(elem));
			g.fillPolygon(p);
			g.setColor(vertexColors.getContour(elem));
			g.drawPolygon(p);

			m = new Polygon();
			m.addPoint(-5, 5);
			m.addPoint(5, 5);
			m.addPoint(5, -5);
			m.addPoint(-5, -5);
		}
		
		return m;
	}

	public void setVertexColors(MapElementColors colors) {
		this.vertexColors = colors;
	}
	
	public MapElementColors getVertexColors() {
		return vertexColors;
	}
	
	public void setShowVertex(boolean showVertex) {
		this.showVertex = showVertex;
	}
	
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}
	
	public void setLineStroke(Stroke lineStroke) {
		this.lineStroke = lineStroke;
	}
	
	public Stroke getLineStroke() {
		return lineStroke;
	}
	
}
