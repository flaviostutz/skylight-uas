package br.skylight.cucs.mapkit;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

public abstract class MapElementBridge<T extends MapElement> {

	private JXMapViewer map;
	private MapElementGroup<T> group;
	
	public MapElementGroup<T> getGroup() {
		return group;
	}
	
	protected void setGroup(MapElementGroup<T> group) {
		this.group = group;
	}
	
	public JXMapViewer getMap() {
		return map;
	}
	public void setMap(JXMapViewer map) {
		this.map = map;
	}
	
	public abstract T createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<T> group);
	public void onElementCreated(T element, int elementIndex, MapElementGroup<T> group){};
	public void onElementDeleted(T element, MapElementGroup<T> group){};
	
}
