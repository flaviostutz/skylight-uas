package br.skylight.cucs.mapkit;

import java.awt.geom.Point2D;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.EventType;

public interface MapActionListener<T> {

	public void onMouseClickedOnMap(GeoPosition position, Point2D mapPositionPixel);
	public void onMouseOverElement(T me);
	public void onMouseOutElement(T me);
	public void onElementEvent(T me, EventType eventType);
	public void onElementDoubleClicked(T me);
	public void onElementDragged(T me);

}
