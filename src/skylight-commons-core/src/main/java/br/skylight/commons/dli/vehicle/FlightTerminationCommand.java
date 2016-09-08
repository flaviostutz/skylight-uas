package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.FlightTerminationState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class FlightTerminationCommand extends Message<FlightTerminationCommand> {

	private FlightTerminationState flightTerminationState;//u1
	private int flightTerminationMode;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M46;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		flightTerminationState = FlightTerminationState.values()[in.readUnsignedByte()];
		flightTerminationMode = in.readUnsignedByte();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(flightTerminationState.ordinal());
		out.writeByte(flightTerminationMode);
	}

	@Override
	public void resetValues() {
		flightTerminationState = FlightTerminationState.values()[0];
		flightTerminationMode = (byte)0;
	}

	public FlightTerminationState getFlightTerminationState() {
		return flightTerminationState;
	}

	public void setFlightTerminationState(
			FlightTerminationState flightTerminationState) {
		this.flightTerminationState = flightTerminationState;
	}

	public int getFlightTerminationMode() {
		return flightTerminationMode;
	}

	public void setFlightTerminationMode(int flightTerminationMode) {
		this.flightTerminationMode = flightTerminationMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((flightTerminationState == null) ? 0
						: flightTerminationState.hashCode());
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
		FlightTerminationCommand other = (FlightTerminationCommand) obj;
		if (flightTerminationState == null) {
			if (other.flightTerminationState != null)
				return false;
		} else if (!flightTerminationState.equals(other.flightTerminationState))
			return false;
		return true;
	}

}
