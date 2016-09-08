package br.skylight.simulation.dummy;

import java.util.Random;

import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.TimedValue;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.infra.GPSUpdate.FixQuality;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsFailures;
import br.skylight.uav.services.InstrumentsInfos;
import br.skylight.uav.services.InstrumentsListener;
import br.skylight.uav.services.InstrumentsService;
import br.skylight.uav.services.InstrumentsWarnings;

@ServiceImplementation(serviceDefinition = { InstrumentsService.class, ActuatorsService.class, GPSService.class })
public class DummyServices extends Worker implements InstrumentsService, ActuatorsService, GPSService {

	private static final Random r = new Random();
	
	private TimedValue timedAngle;
	private TimedValue timedYaw;
	private TimedValue timedAltitude;
	private TimedValue timedLat;
	private TimedValue timedLong;
	
	@ServiceInjection
	public RepositoryService repositoryService;

	public DummyServices() {
		timedAngle = new TimedValue(0, (float)Math.toRadians(25));
		timedAngle.start(0, (float)Math.toRadians(25), 5, true);
		timedYaw = new TimedValue(0, Float.MAX_VALUE);
		timedYaw.start(0, (float)Math.toRadians(360), 30, false);
		timedAltitude = new TimedValue(0, 5000);
		timedAltitude.start(0, 1000, 1500000, true);

		Coordinates c = new Coordinates();
		CoordinatesHelper.calculateCoordinatesFromRelativePosition(c, new Coordinates(0,0,0), 0, 30000);
		timedLat = new TimedValue(0, 10);
		timedLat.start(0, 10, 3600000, true);
		timedLong = new TimedValue(0, 10);
		timedLong.start(0, 10, 3600000, true);
	}
	
	@Override
	public void onActivate() throws Exception {
		// TODO Auto-generated method stub
		super.onActivate();
	}
	
	@Override
	public float getPitch() {
		return (float)timedAngle.getValue();
	}

	@Override
	public float getPitchRate() {
		return r.nextInt(1);
	}

	@Override
	public float getRoll() {
		return (float)timedAngle.getValue();
	}

	@Override
	public float getRollRate() {
		return r.nextInt(1);
	}

	@Override
	public int getEngineRPM() {
		return 3000 + r. nextInt(5);
	}

	@Override
	public float getAileron() {

		return r.nextInt(10);
	}

	@Override
	public float getElevator() {

		return r.nextInt(10);
	}

	@Override
	public float getRudder() {

		return r.nextInt(10);
	}

	@Override
	public float getThrottle() {

		return r.nextInt(10);
	}

	@Override
	public void setAileron(float value) {

	}

	@Override
	public void setElevator(float value) {

	}

	@Override
	public void setRudder(float value) {

	}

	@Override
	public void setThrottle(float value) {

	}

	@Override
	public void step() throws Exception {

	}

	@Override
	public float getCourseHeading() {
		return (float)(timedYaw.getValue()%Math.toRadians(360));
	}

	@Override
	public float getGroundSpeed() {

		return 20 + r.nextInt(5);
	}

	@Override
	public FixQuality getFixQuality() {
		return FixQuality.GPS_FIX_SPS;
	}

	@Override
	public int getSatCount() {

		return r.nextInt(10);
	}

	public int getTotalErrors() {
		return 0;
	}

	@Override
	public Coordinates getPosition() {
		return new Coordinates(timedLat.getValue(), timedLong.getValue(), (float)timedAltitude.getValue());
	}

	@Override
	public Coordinates getPositionOnFirstFix() {
		return null;
	}

	public void reloadVehicleConfiguration() {
	}

	@Override
	public void setLightsState(boolean nav, boolean strobe, boolean landing) {
		System.out.println("SET LIGHTS: nav=" + nav + "; strobe=" + strobe + "; landing=" + landing);
	}

	@Override
	public float getYaw() {
		return r.nextInt(10);
	}

	@Override
	public float getYawRate() {
		return 0;
	}

	@Override
	public void deployParachute() {
		System.out.println("PARACHUTE DEPLOYED");
	}

	@Override
	public void setVideoTransmitterPower(boolean on) {
		System.out.println("TRANSMITTER POWER " + on);
	}

	public float getMainBattVoltage() {
		return r.nextInt(10);
	}

	public float getStaticPressure() {
		return r.nextInt(10);
	}

	public float getPitotPressure() {
		return r.nextInt(10);
	}

	@Override
	public float getAccelerationX() {
		return r.nextInt(10);
	}

	@Override
	public float getAccelerationY() {
		return r.nextInt(10);
	}

	@Override
	public float getAccelerationZ() {
		return r.nextInt(10);
	}

	@Override
	public float getAutoPilotTemperature() {
		return r.nextInt(10);
	}

	@Override
	public float getAuxiliaryBatteryLevel() {
		return r.nextInt(10);
	}

	@Override
	public float getMainBatteryLevel() {
		return r.nextInt(10);
	}

	@Override
	public float getAltitudeMSL() {
		return r.nextInt(10);
	}

	@Override
	public void setEngineIgnition(boolean enabled) {
		System.out.println("ENGINE IGNITION ENABLED: " + enabled);
	}

	@Override
	public void notifyAlertActivated(SubsystemStatusAlert subsystemStatusAlert) {
		System.out.println("ALERT "+ subsystemStatusAlert +" RECEIVED");
	}

	@Override
	public void performCalibrations() {
		System.out.println("PERFORM CALIBRATIONS RECEIVED");
	}

	@Override
	public double getLastGPSUpdateTime() {
		return System.currentTimeMillis()/1000.0;
	}

	@Override
	public float getCameraAzimuth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getCameraElevation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getEngineCilinderTemperature() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public InstrumentsFailures getInstrumentsFailures() {
		// TODO Auto-generated method stub
		return new InstrumentsFailures();
	}

	@Override
	public InstrumentsInfos getInstrumentsInfos() {
		// TODO Auto-generated method stub
		return new InstrumentsInfos();
	}

	@Override
	public InstrumentsWarnings getInstrumentsWarnings() {
		// TODO Auto-generated method stub
		return new InstrumentsWarnings();
	}

	@Override
	public RotationReference getCameraOrientationReference() {
		// TODO Auto-generated method stub
		return RotationReference.VEHICLE;
	}

	@Override
	public boolean isEngineIgnitionEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCameraOrientation(float azimuthValue, float elevationValue, RotationReference reference) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFlightTermination(boolean activated) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getConsumedFuel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setGenericServo(float genericServo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getGenericServo() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFailSafesArmState(boolean armed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectiveActuatorsMessageFrequency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInstrumentsListener(InstrumentsListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDiscardedMessagesCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

}
