package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.Bitmapped;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class AirVehicleLights extends Message<AirVehicleLights> {

	private Bitmapped setLights = new Bitmapped();//u2
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M44;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		setLights.setData(in.readUnsignedShort());
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeShort((int)setLights.getData());
	}

	public Bitmapped getSetLights() {
		return setLights;
	}
	
	@Override
	public void resetValues() {
		setLights.setData(0);
	}
	
}
