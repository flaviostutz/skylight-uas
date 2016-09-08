package br.skylight.cucs.plugins.missionplan.map;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Placemark;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class PlacemarkMapElement extends MissionMapElement {

	private Placemark placemark;
	
	public Placemark getPlacemark() {
		return placemark;
	}
	public void setPlacemark(Placemark placemark) {
		this.placemark = placemark;
		super.setPosition(CUCSViewHelper.toGeoPosition(placemark.getPoint()));
		super.setLabel(placemark.getLabel());
	}
	
	@Override
	public void setPosition(GeoPosition position) {
		super.setPosition(position);
		CUCSViewHelper.copyCoordinates(placemark.getPoint(), position);
	}
	
	@Override
	public void setLabel(String label) {
		super.setLabel(label);
		placemark.setLabel(label);
	}
	
}
