package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class VehicleOperatingModeCommand extends Message<VehicleOperatingModeCommand> {

	private FlightPathControlMode selectFlightPathControlMode;
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		selectFlightPathControlMode = FlightPathControlMode.values()[in.readUnsignedByte()];
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(selectFlightPathControlMode.ordinal());
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M42;
	}

	@Override
	public void resetValues() {
		selectFlightPathControlMode = null;
	}

	public FlightPathControlMode getSelectFlightPathControlMode() {
		return selectFlightPathControlMode;
	}

	public void setSelectFlightPathControlMode(FlightPathControlMode selectFlightPathControlMode) {
		this.selectFlightPathControlMode = selectFlightPathControlMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((selectFlightPathControlMode == null) ? 0 : selectFlightPathControlMode.hashCode());
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
		VehicleOperatingModeCommand other = (VehicleOperatingModeCommand) obj;
		if (selectFlightPathControlMode == null) {
			if (other.selectFlightPathControlMode != null)
				return false;
		} else if (!selectFlightPathControlMode.equals(other.selectFlightPathControlMode))
			return false;
		return true;
	}
	
}
