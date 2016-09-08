package br.skylight.cucs.plugins.subscriber;

import br.skylight.commons.EventType;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;

public interface VehicleListener {

	public void onVehicleEvent(Vehicle av, EventType type);
	public void onPayloadEvent(Payload p, EventType type);
	
}
