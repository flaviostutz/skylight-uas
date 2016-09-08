package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.Bitmapped;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class VehicleLightsState extends Message<VehicleLightsState> {

	private Bitmapped navigationLightsState = new Bitmapped();//u2
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M107;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		navigationLightsState.setData(in.readUnsignedShort());
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeShort((int)navigationLightsState.getData());
	}

	@Override
	public void resetValues() {
		navigationLightsState.setData(0);
	}
	
	public Bitmapped getNavigationLightsState() {
		return navigationLightsState;
	}
	
}
