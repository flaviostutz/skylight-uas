package br.skylight.cucs.mapkit.painters;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import br.skylight.cucs.plugins.missionplan.map.TextMapElement;

public class TextAnnotationPainter extends MapElementPainter<TextMapElement> {

	private Color fontColor = Color.WHITE;
	private Font fontType = new Font(Font.DIALOG, Font.PLAIN, 14);
	
	@Override
	protected Polygon paintElement(Graphics2D g, TextMapElement elem) {
		g.setColor(fontColor);
		g.setFont(fontType);
		String text = elem.getTextAnnotation().getLabel();
		if(text==null || text.trim().length()==0) {
			text = "[double click to set text]";
		}

		FontRenderContext frc = g.getFontRenderContext();
		TextLayout tl = new TextLayout(text, fontType, frc);
		Rectangle2D bounds = tl.getBounds();
		int hw = (int)(bounds.getWidth()/2);
		int hh = (int)(bounds.getHeight()/2);
		g.drawString(text, -hw, hh);
		
		Polygon p = new Polygon();
		p.addPoint(-hw,-hh);
		p.addPoint(hw,-hh);
		p.addPoint(hw,hh);
		p.addPoint(-hw,hh);
		return p;
	}

}
