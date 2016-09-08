package br.skylight.commons.dli.configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.AngleUnit;
import br.skylight.commons.dli.enums.DistanceUnit;
import br.skylight.commons.dli.enums.FuelQttyUnit;
import br.skylight.commons.dli.enums.MassUnit;
import br.skylight.commons.dli.enums.PositionUnit;
import br.skylight.commons.dli.enums.PressureUnit;
import br.skylight.commons.dli.enums.SpeedUnit;
import br.skylight.commons.dli.enums.TemperatureUnit;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class DisplayUnitRequest extends Message<DisplayUnitRequest> {

	private int vsmID;
	private DistanceUnit distance;//u1
	private DistanceUnit altitude;//u1
	private SpeedUnit speed;//u1
	private PositionUnit position;//u1
	private TemperatureUnit temperature;//u1
	private MassUnit mass;//u1
	private AngleUnit angle;//u1
	private PressureUnit pressure;//u1
	private FuelQttyUnit fuelQtty;//u1
	
	public int getVsmID() {
		return vsmID;
	}

	public void setVsmID(int vsmID) {
		this.vsmID = vsmID;
	}

	public DistanceUnit getDistance() {
		return distance;
	}

	public void setDistance(DistanceUnit distance) {
		this.distance = distance;
	}

	public DistanceUnit getAltitude() {
		return altitude;
	}

	public void setAltitude(DistanceUnit altitude) {
		this.altitude = altitude;
	}

	public SpeedUnit getSpeed() {
		return speed;
	}

	public void setSpeed(SpeedUnit speed) {
		this.speed = speed;
	}

	public PositionUnit getPosition() {
		return position;
	}

	public void setPosition(PositionUnit position) {
		this.position = position;
	}

	public TemperatureUnit getTemperature() {
		return temperature;
	}

	public void setTemperature(TemperatureUnit temperature) {
		this.temperature = temperature;
	}

	public MassUnit getMass() {
		return mass;
	}

	public void setMass(MassUnit mass) {
		this.mass = mass;
	}

	public AngleUnit getAngle() {
		return angle;
	}

	public void setAngle(AngleUnit angle) {
		this.angle = angle;
	}

	public PressureUnit getPressure() {
		return pressure;
	}

	public void setPressure(PressureUnit pressure) {
		this.pressure = pressure;
	}

	public FuelQttyUnit getFuelQtty() {
		return fuelQtty;
	}

	public void setFuelQtty(FuelQttyUnit fuelQtty) {
		this.fuelQtty = fuelQtty;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1201;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		distance = DistanceUnit.values()[in.readUnsignedByte()];
		altitude = DistanceUnit.values()[in.readUnsignedByte()];
		speed = SpeedUnit.values()[in.readUnsignedByte()];
		position = PositionUnit.values()[in.readUnsignedByte()];
		temperature = TemperatureUnit.values()[in.readUnsignedByte()];
		mass = MassUnit.values()[in.readUnsignedByte()];
		angle = AngleUnit.values()[in.readUnsignedByte()];
		pressure = PressureUnit.values()[in.readUnsignedByte()];
		fuelQtty = FuelQttyUnit.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeByte(distance.ordinal());
		out.writeByte(altitude.ordinal());
		out.writeByte(speed.ordinal());
		out.writeByte(position.ordinal());
		out.writeByte(temperature.ordinal());
		out.writeByte(mass.ordinal());
		out.writeByte(angle.ordinal());
		out.writeByte(pressure.ordinal());
		out.writeByte(fuelQtty.ordinal());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + vsmID;
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
		DisplayUnitRequest other = (DisplayUnitRequest) obj;
		if (vsmID != other.vsmID)
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		vsmID = 0;
		distance = null;
		altitude = null;
		speed = null;
		position = null;
		temperature = null;
		mass = null;
		angle = null;
		pressure = null;
		fuelQtty = null;
	}
	
}
