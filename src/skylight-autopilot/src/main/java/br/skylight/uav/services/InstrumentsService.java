package br.skylight.uav.services;

import br.skylight.commons.plugin.annotations.ServiceDefinition;

@ServiceDefinition
public interface InstrumentsService {

	//SENSORS
	/**
	 * Positive value indicates a turn to the left
	 * Range: -PI to PI
	 */
	public float getRoll();

	/**
	 * Positive value indicates nose up
	 * Range: -PI to PI
	 */
	public float getPitch();
	
	/**
	 * Positive value indicates heading to the right
	 * Range: -PI to PI
	 */
	public float getYaw();

	/**
	 * Roll rate in radians/s
	 */
	public float getRollRate();

	/**
	 * Pitch rate in radians/s
	 */
	public float getPitchRate();
	
	/**
	 * Yaw rate in radians/s
	 */
	public float getYawRate();

	/**
	 * Acceleration along vehicle x-axis in m/s^2
	 */
	public float getAccelerationX();
	
	/**
	 * Acceleration along vehicle y-axis in m/s^2
	 */
	public float getAccelerationY();
	
	/**
	 * Acceleration along vehicle z-axis in m/s^2
	 */
	public float getAccelerationZ();

	/**
	 * Differential pitot pressure in Pascals
	 */
	public float getPitotPressure();

	/**
	 * External static absolute pressure in Pascals 
	 */
	public float getStaticPressure();
	
	/**
	 * Auto pilot hardware temperature in celsius
	 */
	public float getAutoPilotTemperature();

	/**
	 * Engine temperature in celsius
	 */
	public float getEngineCilinderTemperature();

	/**
	 * Main battery level in mV
	 */
	public float getMainBatteryLevel();
	
	/**
	 * Auxiliary battery level in mV
	 */
	public float getAuxiliaryBatteryLevel();

	/**
	 * Engine rotation speed in RPM
	 */
	public int getEngineRPM();

	/**
	 * Returns the frequency of messages effectively being received by the actuation device
	 * and being used
	 */
	public int getEffectiveActuatorsMessageFrequency();
	
	public InstrumentsFailures getInstrumentsFailures();
	public InstrumentsWarnings getInstrumentsWarnings();
	public InstrumentsInfos getInstrumentsInfos();
	
	public void reloadVehicleConfiguration();

	/**
	 * Notify this listener of all received messages
	 */
	public void setInstrumentsListener(InstrumentsListener listener);

	public int getDiscardedMessagesCounter();

}
