package br.skylight.uav.services;

import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.plugin.annotations.ServiceDefinition;

@ServiceDefinition
public interface ActuatorsService {

	public enum RotationReference {VEHICLE, EARTH, SERVO}
	
	/**
	 * Positive value will turn the uav left
	 * Surface rotation will be relative to Y axis of plane
	 * Range: -127 to 127
	 * @param value
	 */
	public void setAileron(float value);			 

	/**
	 * Positive value will rise the uav
	 * Surface rotation will be relative to Y axis of plane
	 * Range: -127 to 127
	 * @param value
	 */
	public void setElevator(float value);

	/**
	 * Positive value will turn the uav right
	 * Surface rotation will be relative to Z axis of plane
	 * Range: -127 to 127
	 * @param value
	 */
	public void setRudder(float value);

	/**
	 * Range: 0 to 127
	 * @param value
	 */
	public void setThrottle(float value);

	public void setGenericServo(float genericServo);

	/**
	 * Activates/deactivates video transmitter and camera
	 * Should only be switched on during stable autonomous flight.
	 */
	public void setVideoTransmitterPower(boolean on);
	
	/**
	 * Perform EFIS calibrations
	 */
	public void performCalibrations();

	/**
	 * Camera pan/tilt in vehicle reference
	 */
	public void setCameraOrientation(float azimuthValue, float elevationValue, RotationReference reference);
	public void setLightsState(boolean nav, boolean strobe, boolean landing);
	public void setEngineIgnition(boolean enabled);
	public void setFlightTermination(boolean activated);
	public void setFailSafesArmState(boolean armed);
	public void deployParachute();
	
	public float getCameraAzimuth();
	public float getCameraElevation();
	public RotationReference getCameraOrientationReference();
	public int getConsumedFuel();

	public float getGenericServo();
	public float getElevator();
	public float getThrottle();
	public float getAileron();
	public float getRudder();
	public boolean isEngineIgnitionEnabled();

	/**
	 * Notify this listener of all actuation messages sent
	 */
	public void notifyAlertActivated(SubsystemStatusAlert subsystemStatusAlert);
	public void step() throws Exception;
	public void reloadVehicleConfiguration();

}
