package br.skylight.cucs.mapkit;

import java.awt.geom.Point2D;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.EventType;

public class MapActionListenerAdapter<T> implements MapActionListener<T> {

	@Override
	public void onElementDoubleClicked(T me) {
	}

	@Override
	public void onElementDragged(T me) {
	}

	@Override
	public void onElementEvent(T me, EventType eventType) {
	}

	@Override
	public void onMouseClickedOnMap(GeoPosition position, Point2D mapPositionPixel) {
	}

	@Override
	public void onMouseOutElement(T me) {
	}

	@Override
	public void onMouseOverElement(T me) {
	}

}
