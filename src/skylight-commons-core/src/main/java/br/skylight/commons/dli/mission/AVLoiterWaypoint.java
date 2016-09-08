package br.skylight.commons.dli.mission;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.LoiterDirection;
import br.skylight.commons.dli.enums.LoiterType;
import br.skylight.commons.dli.services.MessageType;

public class AVLoiterWaypoint extends Waypoint<AVLoiterWaypoint> {

//	private int waypointNumber;//u2
	private int waypointLoiterTime;//u2
	private LoiterType waypointLoiterType;//u1
	private float loiterRadius;
	private float loiterLength;
	private double loiterBearing;
	private LoiterDirection loiterDirection;//u1

	public AVLoiterWaypoint() {
		resetValues();
	}
	
	public int getWaypointNumber() {
		return waypointNumber;
	}

	public void setWaypointNumber(int waypointNumber) {
		this.waypointNumber = waypointNumber;
	}

	public int getWaypointLoiterTime() {
		return waypointLoiterTime;
	}

	public void setWaypointLoiterTime(int waypointLoiterTime) {
		this.waypointLoiterTime = waypointLoiterTime;
	}

	public LoiterType getWaypointLoiterType() {
		return waypointLoiterType;
	}

	public void setWaypointLoiterType(LoiterType waypointLoiterType) {
		this.waypointLoiterType = waypointLoiterType;
	}

	public float getLoiterRadius() {
		return loiterRadius;
	}

	public void setLoiterRadius(float loiterRadius) {
		this.loiterRadius = loiterRadius;
	}

	public float getLoiterLength() {
		return loiterLength;
	}

	public void setLoiterLength(float loiterLength) {
		this.loiterLength = loiterLength;
	}

	public double getLoiterBearing() {
		return loiterBearing;
	}

	public void setLoiterBearing(double loiterBearing) {
		this.loiterBearing = loiterBearing;
	}

	public LoiterDirection getLoiterDirection() {
		return loiterDirection;
	}

	public void setLoiterDirection(LoiterDirection loiterDirection) {
		this.loiterDirection = loiterDirection;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M803;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		waypointNumber = in.readUnsignedShort();
		waypointLoiterTime = in.readUnsignedShort();
		waypointLoiterType = LoiterType.values()[in.readUnsignedByte()];
		loiterRadius = in.readFloat();
		loiterLength = in.readFloat();
		loiterBearing = in.readDouble();
		loiterDirection = LoiterDirection.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeShort(waypointNumber);
		out.writeShort(waypointLoiterTime);
		out.writeByte(waypointLoiterType.ordinal());
		out.writeFloat(loiterRadius);
		out.writeFloat(loiterLength);
		out.writeDouble(loiterBearing);
		out.writeByte(loiterDirection.ordinal());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + waypointNumber;
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
		AVLoiterWaypoint other = (AVLoiterWaypoint) obj;
		if (waypointNumber != other.waypointNumber)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		waypointNumber = 0;
		waypointLoiterTime = 300;
		waypointLoiterType = LoiterType.CIRCULAR;
		loiterRadius = 50;
		loiterLength = 0;
		loiterBearing = 0;
		loiterDirection = LoiterDirection.VEHICLE_DEPENDENT;
	}
	
	@Override
		public String toString() {
			return "Loiter Definition";
		}
	
}
