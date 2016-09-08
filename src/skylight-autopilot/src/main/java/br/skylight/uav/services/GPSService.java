package br.skylight.uav.services;

import br.skylight.commons.Coordinates;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.uav.infra.GPSUpdate.FixQuality;

@ServiceDefinition
public interface GPSService {

	/**
	 * Course heading in degrees
	 */
	public float getCourseHeading();

	/**
	 * Vehicle speed over ground in m/s
	 */
	public float getGroundSpeed();

 	/**
 	 * Position lat/long with altitude in WGS84
 	 */
	public Coordinates getPosition();

	/**
	 * Position of GPS on first fix
	 */
	public Coordinates getPositionOnFirstFix();
	
	/**
	 * Mean Sea Level altitude in meters
	 */
	public float getAltitudeMSL();

 	public FixQuality getFixQuality(); 
 	public int getSatCount();
 	public double getLastGPSUpdateTime();

}
