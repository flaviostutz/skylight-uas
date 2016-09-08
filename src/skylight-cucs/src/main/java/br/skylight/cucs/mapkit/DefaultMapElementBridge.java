package br.skylight.cucs.mapkit;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class DefaultMapElementBridge extends MapElementBridge<DefaultMapElement> {

	@Override
	public DefaultMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<DefaultMapElement> group) {
		return new DefaultMapElement();
	}

}
