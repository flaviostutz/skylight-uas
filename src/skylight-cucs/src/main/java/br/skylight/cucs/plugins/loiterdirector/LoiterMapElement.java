package br.skylight.cucs.plugins.loiterdirector;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.cucs.plugins.missionplan.map.MissionMapElement;

public abstract class LoiterMapElement extends MissionMapElement {

	private LoiterConfiguration loiterConfiguration = new LoiterConfiguration();

	public LoiterMapElement() {
		setLabel("Loiter director");
	}
	
	public void setLoiterConfiguration(LoiterConfiguration loiterConfiguration) {
		this.loiterConfiguration = loiterConfiguration;
		setAltitude(loiterConfiguration.getLoiterAltitude());
		super.setPosition(new GeoPosition(Math.toDegrees(loiterConfiguration.getLatitude()), Math.toDegrees(loiterConfiguration.getLongitude())));
	}
	public LoiterConfiguration getLoiterConfiguration() {
		return loiterConfiguration;
	}
	
	@Override
	public void setPosition(GeoPosition position) {
		super.setPosition(position);
		loiterConfiguration.setLatitude(Math.toRadians(position.getLatitude()));
		loiterConfiguration.setLongitude(Math.toRadians(position.getLongitude()));
	}
	@Override
	public void setAltitude(float altitude) {
		super.setAltitude(altitude);
		loiterConfiguration.setLoiterAltitude(altitude);
	}

}
