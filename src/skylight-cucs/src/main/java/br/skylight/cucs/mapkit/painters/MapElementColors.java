package br.skylight.cucs.mapkit.painters;

import java.awt.Color;

import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class MapElementColors {

	private Color fill;
	private Color fillSelected;
	private Color contour;
	
	public MapElementColors(Color fill, Color contour, Color fillSelected) {
		this.fill = fill;
		this.contour = contour;
		this.fillSelected = fillSelected;
	}
	
	public Color getFill(MapElement elem) {
		if(elem.isSelected()) {
			if(elem.isMouseOver()) {
				return CUCSViewHelper.getBrighter(fillSelected, 0.3F);
			} else {
				return fillSelected;
			}
		} else {
			if(elem.isMouseOver()) {
				return CUCSViewHelper.getBrighter(fill, 0.3F);
			} else {
				return fill;
			}
		}
	}
	
	public Color getContour(MapElement elem) {
		return contour;
	}

	public Color getFill() {
		return fill;
	}

	public void setFill(Color fill) {
		this.fill = fill;
	}

	public Color getFillSelected() {
		return fillSelected;
	}

	public void setFillSelected(Color fillSelected) {
		this.fillSelected = fillSelected;
	}

	public Color getContour() {
		return contour;
	}

	public void setContour(Color contour) {
		this.contour = contour;
	}
	
	
	
}
