package br.skylight.cucs.plugins.payload.eoir;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.cucs.plugins.missionplan.map.MissionMapElement;

public class EOIRPayloadFOVMapElement extends MissionMapElement {

	private Vehicle vehicle;
	private Payload payload;
	
	public void setup(Payload payload, Vehicle vehicle) {
		this.payload = payload;
		this.vehicle = vehicle;
		InertialStates is = vehicle.getLastReceivedMessage(MessageType.M101);
		if(is!=null) {
			setPosition(new GeoPosition(Math.toDegrees(is.getLatitude()), Math.toDegrees(is.getLongitude())));
		}
	}
	
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	public Payload getPayload() {
		return payload;
	}

}
