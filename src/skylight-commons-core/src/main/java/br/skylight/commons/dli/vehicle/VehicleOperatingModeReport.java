package br.skylight.commons.dli.vehicle;

import br.skylight.commons.dli.services.MessageType;

public class VehicleOperatingModeReport extends VehicleOperatingModeCommand {

	@Override
	public MessageType getMessageType() {
		return MessageType.M106;
	}

}
