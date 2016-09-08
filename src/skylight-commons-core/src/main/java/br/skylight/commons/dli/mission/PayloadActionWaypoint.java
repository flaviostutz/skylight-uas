package br.skylight.commons.dli.mission;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.SensorMode;
import br.skylight.commons.dli.enums.SensorOutput;
import br.skylight.commons.dli.enums.SensorPointingMode;
import br.skylight.commons.dli.services.MessageType;

public class PayloadActionWaypoint extends Waypoint<PayloadActionWaypoint> {

//	private int waypointNumber;//u2
	private BitmappedStation stationNumber = new BitmappedStation();//u4
	private SensorMode setSensor1Mode;//u1
	private SensorMode setSensor2Mode;//u1
	private SensorOutput sensorOutput;//u1
	private SensorPointingMode setSensorPointingMode;//u1
	private double starepointLatitude;
	private double starepointLongitude;
	private float starepointAltitude;
	private AltitudeType starepointAltitudeType;//u1
	private float payloadAz;
	private float payloadEl;
	private float payloadSensorRotationAngle;

	public PayloadActionWaypoint() {
		resetValues();
	}
	
	public int getWaypointNumber() {
		return waypointNumber;
	}

	public void setWaypointNumber(int waypointNumber) {
		this.waypointNumber = waypointNumber;
	}

	public BitmappedStation getStationNumber() {
		return stationNumber;
	}

	public void setStationNumber(BitmappedStation stationNumber) {
		this.stationNumber = stationNumber;
	}

	public SensorMode getSetSensor1Mode() {
		return setSensor1Mode;
	}

	public void setSetSensor1Mode(SensorMode setSensor1Mode) {
		this.setSensor1Mode = setSensor1Mode;
	}

	public SensorMode getSetSensor2Mode() {
		return setSensor2Mode;
	}

	public void setSetSensor2Mode(SensorMode setSensor2Mode) {
		this.setSensor2Mode = setSensor2Mode;
	}

	public SensorOutput getSensorOutput() {
		return sensorOutput;
	}

	public void setSensorOutput(SensorOutput sensorOutput) {
		this.sensorOutput = sensorOutput;
	}

	public SensorPointingMode getSetSensorPointingMode() {
		return setSensorPointingMode;
	}

	public void setSetSensorPointingMode(SensorPointingMode setSensorPointingMode) {
		this.setSensorPointingMode = setSensorPointingMode;
	}

	public double getStarepointLatitude() {
		return starepointLatitude;
	}

	public void setStarepointLatitude(double starepointLatitude) {
		this.starepointLatitude = starepointLatitude;
	}

	public double getStarepointLongitude() {
		return starepointLongitude;
	}

	public void setStarepointLongitude(double starepointLongitude) {
		this.starepointLongitude = starepointLongitude;
	}

	public float getStarepointAltitude() {
		return starepointAltitude;
	}

	public void setStarepointAltitude(float starepointAltitude) {
		this.starepointAltitude = starepointAltitude;
	}

	public AltitudeType getStarepointAltitudeType() {
		return starepointAltitudeType;
	}

	public void setStarepointAltitudeType(AltitudeType starepointAltitudeType) {
		this.starepointAltitudeType = starepointAltitudeType;
	}

	public float getPayloadAz() {
		return payloadAz;
	}

	public void setPayloadAz(float payloadAz) {
		this.payloadAz = payloadAz;
	}

	public float getPayloadEl() {
		return payloadEl;
	}

	public void setPayloadEl(float payloadEl) {
		this.payloadEl = payloadEl;
	}

	public float getPayloadSensorRotationAngle() {
		return payloadSensorRotationAngle;
	}

	public void setPayloadSensorRotationAngle(float payloadSensorRotationAngle) {
		this.payloadSensorRotationAngle = payloadSensorRotationAngle;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M804;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		waypointNumber = in.readUnsignedShort();
		stationNumber.setData(readUnsignedInt(in));
		setSensor1Mode = SensorMode.values()[in.readUnsignedByte()];
		setSensor2Mode = SensorMode.values()[in.readUnsignedByte()];
		sensorOutput = SensorOutput.values()[in.readUnsignedByte()];
		setSensorPointingMode = SensorPointingMode.values()[in.readUnsignedByte()];
		starepointLatitude = in.readDouble();
		starepointLongitude = in.readDouble();
		starepointAltitude = in.readFloat();
		starepointAltitudeType = AltitudeType.values()[in.readUnsignedByte()];
		payloadAz = in.readFloat();
		payloadEl = in.readFloat();
		payloadSensorRotationAngle = in.readFloat();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeShort(waypointNumber);
		out.writeInt((int)stationNumber.getData());
		out.writeByte(setSensor1Mode.ordinal());
		out.writeByte(setSensor2Mode.ordinal());
		out.writeByte(sensorOutput.ordinal());
		out.writeByte(setSensorPointingMode.ordinal());
		out.writeDouble(starepointLatitude);
		out.writeDouble(starepointLongitude);
		out.writeFloat(starepointAltitude);
		out.writeByte(starepointAltitudeType.ordinal());
		out.writeFloat(payloadAz);
		out.writeFloat(payloadEl);
		out.writeFloat(payloadSensorRotationAngle);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (stationNumber.getData() ^ (stationNumber.getData() >>> 32));
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
		PayloadActionWaypoint other = (PayloadActionWaypoint) obj;
		if (stationNumber != other.stationNumber)
			return false;
		if (waypointNumber != other.waypointNumber)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		waypointNumber = 0;
		stationNumber.setData(0);
		setSensor1Mode = SensorMode.TURN_OFF;
		setSensor2Mode = SensorMode.TURN_OFF;
		sensorOutput = SensorOutput.NONE;
		setSensorPointingMode = SensorPointingMode.NIL;
		starepointLatitude = 0;
		starepointLongitude = 0;
		starepointAltitude = 0;
		starepointAltitudeType = AltitudeType.AGL;
		payloadAz = 0;
		payloadEl = 0;
		payloadSensorRotationAngle = 0;
	}
	
	@Override
		public String toString() {
			return "Payload Action";
		}
	
}
