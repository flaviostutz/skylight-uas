package br.skylight.commons.dli.mission;

import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.services.Message;

public abstract class Waypoint<T extends Message<T>> extends Message<T> implements Comparable<Waypoint> {

	protected int waypointNumber;//u2

	public int getWaypointNumber() {
		return waypointNumber;
	}

	public void setWaypointNumber(int waypointNumber) {
		this.waypointNumber = waypointNumber;
	}
	
	@Override
	public int compareTo(Waypoint o) {
		if(waypointNumber>o.waypointNumber) {
			return 1;
		} else {
			return -1;
		}
	}
	
}
