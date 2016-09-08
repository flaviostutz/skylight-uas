package br.skylight.cucs.plugins.missionplan.map;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.TextAnnotation;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class TextMapElement extends MissionMapElement {

	private TextAnnotation textAnnotation;
	
	public TextAnnotation getTextAnnotation() {
		return textAnnotation;
	}
	public void setTextAnnotation(TextAnnotation textAnnotation) {
		this.textAnnotation = textAnnotation;
		super.setLabel(textAnnotation.getLabel());
		super.setPosition(CUCSViewHelper.toGeoPosition(textAnnotation.getPoint()));
	}
	
	@Override
	public void setPosition(GeoPosition position) {
		super.setPosition(position);
		CUCSViewHelper.copyCoordinates(textAnnotation.getPoint(), position);
	}
	
	@Override
	public void setLabel(String label) {
		super.setLabel(label);
		textAnnotation.setLabel(label);
	}
	
}
