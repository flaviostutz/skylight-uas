package br.skylight.cucs.mapkit;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.net.URL;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

public abstract class MapElement {

	private GeoPosition position;
	private float altitude;
	private Shape mouseMask;
	private boolean selected;
	private boolean mouseOver;
	private boolean editable;
	private boolean setLabelOnDoubleClick;
	private String label;
	private URL icon;
	private JXMapViewer map;
	private MapElementGroup group;
	
	public MapElement() {
		this.editable = true;
		setLabelOnDoubleClick = true;
	}
	
	protected void setGroup(MapElementGroup group) {
		this.group = group;
	}
	protected void setMap(JXMapViewer map) {
		this.map = map;
	}
	
	public MapElementGroup getGroup() {
		return group;
	}
	
	public Point2D getPixelPosition() {
		return map.getTileFactory().geoToPixel(getPosition(), map.getZoom());
	}
	
	public JXMapViewer getMap() {
		return map;
	}
	
	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}
	public float getAltitude() {
		return altitude;
	}
	
	public void setMouseMask(Shape mouseMask) {
		this.mouseMask = mouseMask;
	}
	public Shape getMouseMask() {
		return mouseMask;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public boolean isSelected() {
		return selected;
	}

	public void setMouseOver(boolean mouseOver) {
		this.mouseOver = mouseOver;
	}
	public boolean isMouseOver() {
		return mouseOver;
	}
	
	public boolean isEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public URL getIcon() {
		return icon;
	}

	public void setIcon(URL icon) {
		this.icon = icon;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public GeoPosition getPosition() {
		return position;
	}

	public void setPosition(GeoPosition position) {
		this.position = position;
	}
	
	public boolean isSetLabelOnDoubleClick() {
		return setLabelOnDoubleClick;
	}
	public void setSetLabelOnDoubleClick(boolean setLabelOnDoubleClick) {
		this.setLabelOnDoubleClick = setLabelOnDoubleClick;
	}
	
}
