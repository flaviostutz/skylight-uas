package br.skylight.cucs.plugins.payload.eoir;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.payload.PayloadSteeringCommand;
import br.skylight.cucs.plugins.missionplan.map.MissionMapElement;

public class EOIRPayloadStareMapElement extends MissionMapElement {

	private Vehicle vehicle;
	private Payload payload;
	
	public void setup(Payload payload, Vehicle vehicle) {
		this.payload = payload;
		this.vehicle = vehicle;
	}
	
	@Override
	public GeoPosition getPosition() {
		PayloadSteeringCommand ps = payload.resolvePayloadSteeringCommand();
		return new GeoPosition(Math.toDegrees(ps.getLatitude()), Math.toDegrees(ps.getLongitude()));
	}

	@Override
	public void setPosition(GeoPosition position) {
		PayloadSteeringCommand ps = payload.resolvePayloadSteeringCommand();
		ps.setLatitude(Math.toRadians(position.getLatitude()));
		ps.setLongitude(Math.toRadians(position.getLongitude()));
	}
	
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	public Payload getPayload() {
		return payload;
	}
	
	@Override
	public String getLabel() {
		if(super.getLabel()==null || super.getLabel().trim().length()==0) {
			return getPayload().getLabel() + "@" + getVehicle().getLabel();
		} else {
			return super.getLabel();
		}
	}

}
