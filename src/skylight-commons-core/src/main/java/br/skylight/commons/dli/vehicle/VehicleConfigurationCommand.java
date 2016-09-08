package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class VehicleConfigurationCommand extends Message<VehicleConfigurationCommand> {

	private float initialPropulsionEnergy;
	
	public float getInitialPropulsionEnergy() {
		return initialPropulsionEnergy;
	}
	public void setInitialPropulsionEnergy(float initialPropulsionEnergy) {
		this.initialPropulsionEnergy = initialPropulsionEnergy;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		initialPropulsionEnergy = in.readFloat();
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeFloat(initialPropulsionEnergy);
	}
	@Override
	public MessageType getMessageType() {
		return MessageType.M40;
	}
	@Override
	public void resetValues() {
		initialPropulsionEnergy = 0;
	}
	
}
