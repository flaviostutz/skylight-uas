package br.skylight.commons.dli.mission;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.LocationType;
import br.skylight.commons.dli.enums.TurnType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.services.MessageType;

public class AVPositionWaypoint extends Waypoint<AVPositionWaypoint> {

//	private int waypointNumber;//u2
	private double waypointToLatitudeOrRelativeY;
	private double waypointToLongitudeOrRelativeX;
	private LocationType locationType;//u1
	private float waypointToAltitude;
	private AltitudeType waypointAltitudeType;//u1
	private float waypointToSpeed;
	private WaypointSpeedType waypointSpeedType;//u1
	private int nextWaypoint;//u2
	private int contingencyWaypointA;//u2
	private int contingencyWaypointB;//u2
	private double arrivalTime;
	private TurnType turnType;//u1
	
	//transient
	private Coordinates waypointPosition = new Coordinates();

	public AVPositionWaypoint() {
		resetValues();
	}
	
	public double getWaypointToLatitudeOrRelativeY() {
		return waypointToLatitudeOrRelativeY;
	}

	public void setWaypointToLatitudeOrRelativeY(double waypointToLatitudeOrRelativeY) {
		this.waypointToLatitudeOrRelativeY = waypointToLatitudeOrRelativeY;
	}

	public double getWaypointToLongitudeOrRelativeX() {
		return waypointToLongitudeOrRelativeX;
	}

	public void setWaypointToLongitudeOrRelativeX(double waypointToLatitudeOrRelativeX) {
		this.waypointToLongitudeOrRelativeX = waypointToLatitudeOrRelativeX;
	}

	public LocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}

	public float getWaypointToAltitude() {
		return waypointToAltitude;
	}

	public void setWaypointToAltitude(float waypointToAltitude) {
		this.waypointToAltitude = waypointToAltitude;
	}

	public AltitudeType getWaypointAltitudeType() {
		return waypointAltitudeType;
	}

	public void setWaypointAltitudeType(AltitudeType waypointAltitudeType) {
		this.waypointAltitudeType = waypointAltitudeType;
	}

	public float getWaypointToSpeed() {
		return waypointToSpeed;
	}

	public void setWaypointToSpeed(float waypointToSpeed) {
		this.waypointToSpeed = waypointToSpeed;
	}

	public WaypointSpeedType getWaypointSpeedType() {
		return waypointSpeedType;
	}

	public void setWaypointSpeedType(WaypointSpeedType waypointSpeedType) {
		this.waypointSpeedType = waypointSpeedType;
	}

	public int getNextWaypoint() {
		return nextWaypoint;
	}

	public void setNextWaypoint(int nextWaypoint) {
		this.nextWaypoint = nextWaypoint;
	}

	public int getContingencyWaypointA() {
		return contingencyWaypointA;
	}

	public void setContingencyWaypointA(int contingencyWaypointA) {
		this.contingencyWaypointA = contingencyWaypointA;
	}

	public int getContingencyWaypointB() {
		return contingencyWaypointB;
	}

	public void setContingencyWaypointB(int contingencyWaypointB) {
		this.contingencyWaypointB = contingencyWaypointB;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public TurnType getTurnType() {
		return turnType;
	}

	public void setTurnType(TurnType turnType) {
		this.turnType = turnType;
	}
	
	public Coordinates getWaypointPosition() {
		waypointPosition.setLatitudeRadians(waypointToLatitudeOrRelativeY);
		waypointPosition.setLongitudeRadians(waypointToLongitudeOrRelativeX);
		waypointPosition.setAltitude(waypointToAltitude);
		return waypointPosition;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M802;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		waypointNumber = in.readUnsignedShort();//u2
		waypointToLatitudeOrRelativeY = in.readDouble();
		waypointToLongitudeOrRelativeX = in.readDouble();
		locationType = LocationType.values()[in.readUnsignedByte()];//u1
		waypointToAltitude = in.readFloat();
		waypointAltitudeType = AltitudeType.values()[in.readUnsignedByte()];//u1
		waypointToSpeed = in.readFloat();
		waypointSpeedType = WaypointSpeedType.values()[in.readUnsignedByte()];//u1
		nextWaypoint = in.readUnsignedShort();//u2
		contingencyWaypointA = in.readUnsignedShort();//u2
		contingencyWaypointB = in.readUnsignedShort();//u2
		arrivalTime = in.readDouble();
		turnType = TurnType.values()[in.readUnsignedByte()];//u1
		waypointPosition.reset();
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeShort(waypointNumber);//u2
		out.writeDouble(waypointToLatitudeOrRelativeY);
		out.writeDouble(waypointToLongitudeOrRelativeX);
		out.writeByte(locationType.ordinal());//u1
		out.writeFloat(waypointToAltitude);
		out.writeByte(waypointAltitudeType.ordinal());//u1
		out.writeFloat(waypointToSpeed);
		out.writeByte(waypointSpeedType.ordinal());//u1
		out.writeShort(nextWaypoint);//u2
		out.writeShort(contingencyWaypointA);//u2
		out.writeShort(contingencyWaypointB);//u2
		out.writeDouble(arrivalTime);
		out.writeByte(turnType.ordinal());//u1
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
		AVPositionWaypoint other = (AVPositionWaypoint) obj;
		if (waypointNumber != other.waypointNumber)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		waypointNumber = 0;
		waypointToLatitudeOrRelativeY = 0;
		waypointToLongitudeOrRelativeX = 0;
		locationType = LocationType.ABSOLUTE;
		waypointToAltitude = 200;
		waypointAltitudeType = AltitudeType.AGL;
		waypointToSpeed = 40;
		waypointSpeedType = WaypointSpeedType.GROUND_SPEED;
		nextWaypoint = 0;
		contingencyWaypointA = 0;
		contingencyWaypointB = 0;
		arrivalTime = 0;
		turnType = TurnType.SHORT_TURN;
		waypointPosition.reset();
	}

}