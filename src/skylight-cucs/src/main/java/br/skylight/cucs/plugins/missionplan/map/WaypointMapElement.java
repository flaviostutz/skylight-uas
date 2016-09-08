package br.skylight.cucs.plugins.missionplan.map;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.dli.WaypointDef;
import br.skylight.cucs.mapkit.MapElement;

public class WaypointMapElement extends MapElement {

	private WaypointDef waypointDef;

	public WaypointMapElement(WaypointDef waypointDef) {
		this.waypointDef = waypointDef;
		super.setPosition(new GeoPosition(Math.toDegrees(waypointDef.getPositionWaypoint().getWaypointToLatitudeOrRelativeY()),
										  Math.toDegrees(waypointDef.getPositionWaypoint().getWaypointToLongitudeOrRelativeX())));
		super.setAltitude(waypointDef.getPositionWaypoint().getWaypointToAltitude());
		setSetLabelOnDoubleClick(false);
	}

	public WaypointDef getWaypointDef() {
		return waypointDef;
	}
	
	@Override
	public void setPosition(GeoPosition position) {
		super.setPosition(position);
		waypointDef.getPositionWaypoint().setWaypointToLatitudeOrRelativeY(Math.toRadians(position.getLatitude()));
		waypointDef.getPositionWaypoint().setWaypointToLongitudeOrRelativeX(Math.toRadians(position.getLongitude()));
	}
	
	@Override
	public void setAltitude(float altitude) {
		super.setAltitude(altitude);
		waypointDef.getPositionWaypoint().setWaypointToAltitude(altitude);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((waypointDef == null) ? 0 : waypointDef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WaypointMapElement other = (WaypointMapElement) obj;
		if (waypointDef == null) {
			if (other.waypointDef != null)
				return false;
		} else if (!waypointDef.equals(other.waypointDef))
			return false;
		return true;
	}

}
