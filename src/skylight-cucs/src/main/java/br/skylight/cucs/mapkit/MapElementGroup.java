package br.skylight.cucs.mapkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.cucs.mapkit.painters.MapElementPainter;
import br.skylight.cucs.plugins.missionplan.map.WaypointMapElement;

public class MapElementGroup<T extends MapElement> {

	private MapKit mapKit;
	private Object id;
	private String name;
	private boolean visible;
	private int layerNumber;
	private boolean enabledToAddElements;
	private int maxAllowedElements;
	private MapElementBridge<T> bridge;
	private MapElementPainter<T> painter;
	
	private List<T> elements;
	
	public MapElementGroup(MapKit mapKit, String name, int layerNumber, MapElementBridge<T> bridge, MapElementPainter<T> painter) {
		this.mapKit = mapKit;
		this.painter = painter;
		this.bridge = bridge;
		this.layerNumber = layerNumber;
		this.elements = new CopyOnWriteArrayList<T>();//important for thread concurrency purposes
		enabledToAddElements = true;
		visible = true;
		painter.setGroup(this);
		bridge.setGroup(this);
	}
	
	public List<T> getElements() {
		return elements;
	}
	
	public MapElementPainter<T> getPainter() {
		return painter;
	}
	
	public void setEnabledToAddElements(boolean enabledToAddElements) {
		this.enabledToAddElements = enabledToAddElements;
	}
	
	public boolean isEnabledToAddElements() {
		return enabledToAddElements;
	}
	
	public MapElementBridge<T> getBridge() {
		return bridge;
	}
	
	public String getName() {
		return name;
	}
	
	public Object getId() {
		return id;
	}
	
	protected void setId(Object id) {
		this.id = id;
	}

	public int getLayerNumber() {
		return layerNumber;
	}

	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setMap(JXMapViewer map) {
		bridge.setMap(map);
		painter.setMap(map);
	}

	public T createElement(GeoPosition position, float altitude, Class<T> mapElementType) {
		return createElement(position, altitude, mapElementType, guessBestElementIndex(position));
	}

	public T createElement(GeoPosition position, float altitude, Class<T> mapElementType, int elementIndex) {
		if(bridge.getMap()==null) {
			throw new IllegalStateException("You have to bind this group to a MapKit before adding elements to it");
		}
		MapElementBridge<T> b = getBridge();
		T me = b.createMapElement(position, altitude, elementIndex, this);
		me.setPosition(position);
		if(!Float.isNaN(altitude)) {
			me.setAltitude(altitude);
		}
		
		T e = addElement(me, elementIndex, mapElementType);
		return e;
	}

	public int guessBestElementIndex(GeoPosition position) {
		//if there is a selected item from the same group, add after that element
		if(mapKit.getSelectedElement() instanceof WaypointMapElement && elements.contains(((WaypointMapElement)mapKit.getSelectedElement()))) {
			for(int i=0; i<elements.size(); i++) {
				if(elements.get(i).equals(mapKit.getSelectedElement())) {
					return i+1;
				}
			}
		}
		
		//if no element from same group is selected, then try to match the nearest element
		int c = 0;
		int bestPointIndex = 0;
		float bestPointDistance = Float.MAX_VALUE;
		Coordinates newPos = new Coordinates(position.getLatitude(), position.getLongitude(), 0);
		for (MapElement elem : elements) {
			Coordinates elemPos = new Coordinates(elem.getPosition().getLatitude(), elem.getPosition().getLongitude(), 0);
			float distance = Math.abs(newPos.distance(elemPos));
			if(distance<bestPointDistance) {
				bestPointIndex = c;
				bestPointDistance = distance;
			}
			c++;
		}
		return bestPointIndex;
	}
	
	
	public T addElement(T mapElement, int elementIndex, Class<T> mapElementType) {
		if(isFull()) {
			throw new IllegalStateException("Cannot add more items to this group because it is already full. maxAllowedElements=" + maxAllowedElements);
		}
		if(elementIndex!=-1 && elements.size()>(elementIndex+1)) {
			elements.add(elementIndex, mapElement);
		} else {
			elements.add(mapElement);
		}
		
		MapElementBridge<T> b = getBridge();
		mapElement.setMap(b.getMap());
		mapElement.setGroup(this);
		
	    b.onElementCreated(mapElement, elementIndex, this);
	    
	    return mapElement;
	}

	public boolean isFull() {
		return maxAllowedElements>0 && elements.size()>=maxAllowedElements;
	}

	public void clearElements() {
		elements.clear();
	}

	public void removeElement(T element) {
		elements.remove(element);
	    getBridge().onElementDeleted(element, this);
	}

	public T getElement(int i) {
		return elements.get(i);
	}

	public void setMaxAllowedElements(int maxAllowedElements) {
		this.maxAllowedElements = maxAllowedElements;
	}
	
	public int getMaxAllowedElements() {
		return maxAllowedElements;
	}
	
}
