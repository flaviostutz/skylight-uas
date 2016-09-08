package br.skylight.simulation.flightgear;

import traer.physics.Vector3D;
import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.Worker;
import br.skylight.flightsim.BasicAirplane;
import br.skylight.flightsim.SimulatedAirplaneUI;
import br.skylight.flightsim.flyablebody.Environment;
import br.skylight.uav.infra.GPSUpdate.FixQuality;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsFailures;
import br.skylight.uav.services.InstrumentsInfos;
import br.skylight.uav.services.InstrumentsListener;
import br.skylight.uav.services.InstrumentsService;
import br.skylight.uav.services.InstrumentsWarnings;

public class FlightSimServices extends Worker implements GPSService, InstrumentsService, ActuatorsService {

	private Environment e;
	private BasicAirplane plane;
	private SimulatedAirplaneUI sui;
	private Vector3D speed = new Vector3D();
	private InstrumentsListener listener;
	
	public FlightSimServices() throws Exception {
		e = new Environment();
		plane = new BasicAirplane(e);
		sui = new SimulatedAirplaneUI(plane);
		sui.setVisible(true);
	}

	@Override
	public void onActivate() throws Exception {
		e.activate();
	}

	@Override
	public void onDeactivate() throws Exception {
		e.deactivate();
		plane = null;
	}
	
	public float getAltitudeAGLGps() {
		return plane.getCoordinates().getAltitude();
	}

	public float getAltitudeMSLGps() {
		return plane.getCoordinates().getAltitude() + 2000;
	}

	public float getCourseHeading() {
		return (float)plane.getHeading();
	}

	public float getGroundSpeed() {
		return (float)plane.getGroundSpeed().length()/1000F;
	}

	public float getLatitude() {
		return (float)plane.getCoordinates().getLatitude();
	}

	public float getLongitude() {
		return (float)plane.getCoordinates().getLongitude();
	}

	public int getQuality() {
		return 70;
	}

	public int getSatCount() {
		return 5;
	}

	public float getAltitudeAGLBarometric() {
		return plane.getCoordinates().getAltitude();
	}

	public float getAltitudeAGLSonar() {
		return (float)plane.getCoordinates().getAltitude();
	}

	public byte getBattery1() {
		return 82;
	}

	public byte getBattery2() {
		return 69;
	}

	public byte getBattery3() {
		return 78;
	}

	public float getBestAltitudeAGL() {
		return plane.getCoordinates().getAltitude();
	}

	public float getFuel() {
		return 1.45F;
	}

	public float getIndicatedAirspeed() {
		return (float)plane.getIAS();
	}

	public float getPitch() {
		return (float)plane.getPitch();
	}

	public float getPitchRate() {
		return 0;
	}

	public float getRoll() {
		return (float)plane.getRoll();
	}

	public float getRollRate() {
		return 0;
	}

	public float getVerticalSpeed() {
		return 0;
	}

	public int getRpm() {
		return (int)(30000F*(getThrottle()/127F));
	}

	public boolean isAutoMode() {
		return sui.isAutoMode();
	}

	public boolean isCalibrated() {
		return true;
	}

	public void reloadVehicleConfiguration() {
	}

	public void activateParachute() {
		System.out.println("Simulation: Parachute activated");
	}

	public float getAileron() {
		return (float)plane.getAileron();
	}

	public float getElevator() {
		return (float)plane.getElevator();
	}

	public float getRudder() {
		return (float)plane.getRudder();
	}

	public float getThrottle() {
		return (float)plane.getThrottle();
	}

	public void killEngine() {
		if(isAutoMode()) plane.setThrottle(0);
	}

	public void releasePayload() {
		System.out.println("Simulation: Payload released");
	}

	public void setAileron(float value) {
		if(isAutoMode())
			plane.setAileron(value);
	}

	public void setElevator(float value) {
		if(isAutoMode())
			plane.setElevator(value);
	}

	public void setRudder(float value) {
		if(isAutoMode())
			plane.setRudder(value);
	}

	public void setThrottle(float value) {
		if(isAutoMode())
			plane.setThrottle(value);
	}

	public void step() throws Exception {
	}

	public int getTotalErrors() {
		return 0;
	}

	@Override
	public Coordinates getPosition() {
		return null;
	}

	@Override
	public Coordinates getPositionOnFirstFix() {
		return null;
	}

	@Override
	public void setLightsState(boolean nav, boolean strobe, boolean landing) {
		System.out.println("SET LIGHTS: nav=" + nav + "; strobe=" + strobe + "; landing=" + landing);
	}

	public Vector3D getSpeed() {
		//TODO implement full 3D speed. this implementation lacks of vertical speed
		speed.setX(CoordinatesHelper.getUComponent(getCourseHeading(), getGroundSpeed()));
		speed.setY(CoordinatesHelper.getVComponent(getCourseHeading(), getGroundSpeed()));
		return speed;
	}

	public float getMainBattVoltage() {
		return 0;
	}

	public float getStaticPressure() {
		return 0;
	}

	public float getPitotPressure() {
		return 0;
	}

	@Override
	public float getYaw() {
		return 0;
	}

	@Override
	public float getYawRate() {
		return 0;
	}

	@Override
	public void deployParachute() {
	}

	@Override
	public void setVideoTransmitterPower(boolean on) {
	}

	@Override
	public float getAltitudeMSL() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FixQuality getFixQuality() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getAccelerationX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAccelerationY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAccelerationZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAutoPilotTemperature() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAuxiliaryBatteryLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getMainBatteryLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEngineRPM() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setEngineIgnition(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyAlertActivated(SubsystemStatusAlert subsystemStatusAlert) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void performCalibrations() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getLastGPSUpdateTime() {
		// TODO Auto-generated method stub
		return 0;
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
		return null;
	}

	@Override
	public InstrumentsInfos getInstrumentsInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InstrumentsWarnings getInstrumentsWarnings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RotationReference getCameraOrientationReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConsumedFuel() {
		// TODO Auto-generated method stub
		return 0;
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
