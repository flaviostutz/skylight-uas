package br.skylight.commons.dli.mission;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.RouteType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class AVRoute extends Message<AVRoute> {

	private int initialWaypointNumber;//u2
	private String routeID = "";//c33
	private RouteType routeType = RouteType.FLIGHT;//u1
	
	public int getInitialWaypointNumber() {
		return initialWaypointNumber;
	}

	public void setInitialWaypointNumber(int initialWaypointNumber) {
		this.initialWaypointNumber = initialWaypointNumber;
	}

	public String getRouteID() {
		return routeID;
	}

	public void setRouteID(String routeID) {
		this.routeID = routeID;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public void setRouteType(RouteType routeType) {
		this.routeType = routeType;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M801;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		initialWaypointNumber = in.readUnsignedShort();//u2
		routeID = readNullTerminatedString(in);//c33
		routeType = RouteType.values()[in.readByte()];//u1
	}
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeShort(initialWaypointNumber);//u2
		writeNullTerminatedString(out, routeID);//c33
		out.writeByte(routeType.ordinal());//u1
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + initialWaypointNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AVRoute other = (AVRoute) obj;
		if (initialWaypointNumber != other.initialWaypointNumber)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		initialWaypointNumber = 0;
		routeID = "";
		routeType = null;
	}

}
