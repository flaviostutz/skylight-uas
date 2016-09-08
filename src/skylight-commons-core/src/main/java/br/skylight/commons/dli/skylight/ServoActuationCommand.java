package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.Servo;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class ServoActuationCommand extends Message<ServoActuationCommand> {

	private Servo servo;
	private float commandedSetpoint;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2003;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		servo = Servo.values()[in.readUnsignedByte()];
		commandedSetpoint = in.readFloat();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(servo.ordinal());
		out.writeFloat(commandedSetpoint);
	}

	@Override
	public void resetValues() {
		servo = Servo.values()[0];
		commandedSetpoint = 0;
	}

	public Servo getServo() {
		return servo;
	}

	public void setServo(Servo servo) {
		this.servo = servo;
	}

	public float getCommandedSetpoint() {
		return commandedSetpoint;
	}

	public void setCommandedSetpoint(float commandedSetpoint) {
		this.commandedSetpoint = commandedSetpoint;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((servo == null) ? 0 : servo.hashCode());
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
		ServoActuationCommand other = (ServoActuationCommand) obj;
		if (servo == null) {
			if (other.servo != null)
				return false;
		} else if (!servo.equals(other.servo))
			return false;
		return true;
	}

}
