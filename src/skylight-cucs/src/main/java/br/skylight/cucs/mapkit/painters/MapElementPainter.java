package br.skylight.cucs.mapkit.painters;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;

import org.jdesktop.swingx.JXMapViewer;

import br.skylight.commons.Coordinates;
import br.skylight.commons.MeasureType;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.MapElementGroup;

public abstract class MapElementPainter<T extends MapElement> implements LayerPainter<JXMapViewer> {

	private MapElementGroup<T> group;
	private JXMapViewer map;
	private Color labelColor = Color.YELLOW;
	private Font labelFont = new Font(Font.DIALOG, Font.PLAIN, 12);
	
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		if(group.isVisible()) {
	        //setup graphics
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g = (Graphics2D) g.create();
	
	        //convert from viewport to world bitmap
	        Rectangle rect = map.getViewportBounds();
	        g.translate(-rect.x, -rect.y);

			for (T elem : group.getElements()) {
				//centralize graphics context to element position
				if(elem.getPosition()!=null && elem.getPosition().getLatitude()!=0 && elem.getPosition().getLongitude()!=0) {
					Point2D pos = elem.getPixelPosition();
					Graphics2D tg = (Graphics2D) g.create();
			        tg.translate(pos.getX(), pos.getY());
			        
			        //call shape paint
					Polygon mask = paintElement(tg, elem);
					if(mask!=null) {
						mask.translate((int)pos.getX(), (int)pos.getY());
						elem.setMouseMask(mask);
					}
				}
			}
		}
	}
	
	protected abstract Polygon paintElement(Graphics2D g, T elem);

	protected void drawDefaultLabel(Graphics2D g, MapElement elem) {
		if(elem.getLabel()!=null) {
		    FontRenderContext frc = g.getFontRenderContext();
		    GlyphVector gv = getLabelFont().createGlyphVector(frc, elem.getLabel());
		    g.setColor(getLabelColor());
		    g.drawGlyphVector(gv, 7, 5);
		}
	}
	
	protected void drawDefaultPositionInfo(Graphics2D g, MapElement elem) {
	    Coordinates c = new Coordinates(elem.getPosition().getLatitude(), elem.getPosition().getLongitude(), 0);
	    String position = c.getFormattedLatitude() + " " + c.getFormattedLongitude();
	    drawText(g, elem, position, 0, true);
	}

	protected void drawAltitude(Graphics2D g, MapElement element, AltitudeType altitudeType) {
	    drawText(g, element, "Alt: " + MeasureType.ALTITUDE.convertToTargetUnitStr(element.getAltitude(), true) + " ("+ altitudeType +")", 15, true);
	}
	
	protected void drawText(Graphics2D g, MapElement elem, String text, int yoffset, boolean onlyMouseOver) {
		if(!onlyMouseOver || elem.isMouseOver() || elem.isSelected()) {
		    FontRenderContext frc = g.getFontRenderContext();
		    GlyphVector gv = getLabelFont().createGlyphVector(frc, text);
		    g.setColor(getLabelColor());
		    g.drawGlyphVector(gv, 7, 17 + yoffset);
		}
	}
	
	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

	public Font getLabelFont() {
		return labelFont;
	}

	public void setLabelFont(Font labelFont) {
		this.labelFont = labelFont;
	}
	
	public void setGroup(MapElementGroup<T> group) {
		this.group = group;
	}
	
	public MapElementGroup<T> getGroup() {
		return group;
	}
	
	@Override
	public int getLayerNumber() {
		return group.getLayerNumber();
	}

	public void setMap(JXMapViewer map) {
		this.map = map;
	}
	public JXMapViewer getMap() {
		return map;
	}
	
}
