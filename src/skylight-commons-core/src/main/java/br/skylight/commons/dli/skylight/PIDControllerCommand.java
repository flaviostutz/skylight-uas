package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class PIDControllerCommand extends Message<PIDControllerCommand> {

	private PIDControl pidControl;
	private float commandedSetpoint;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2004;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		pidControl = PIDControl.values()[in.readUnsignedByte()];
		commandedSetpoint = in.readFloat();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(pidControl.ordinal());
		out.writeFloat(commandedSetpoint);
	}

	@Override
	public void resetValues() {
		pidControl = PIDControl.values()[0];
		commandedSetpoint = 0;
	}

	
	public PIDControl getPIDControl() {
		return pidControl;
	}

	public void setPIDControl(PIDControl pidControl) {
		this.pidControl = pidControl;
	}

	/**
	 * NaN values indicates unhold controller
	 */
	public float getCommandedSetpoint() {
		return commandedSetpoint;
	}

	/**
	 * NaN values indicates unhold controller
	 */
	public void setCommandedSetpoint(float commandedSetpoint) {
		this.commandedSetpoint = commandedSetpoint;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((pidControl == null) ? 0 : pidControl.hashCode());
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
		PIDControllerCommand other = (PIDControllerCommand) obj;
		if (pidControl == null) {
			if (other.pidControl != null)
				return false;
		} else if (!pidControl.equals(other.pidControl))
			return false;
		return true;
	}
	
}
