package br.skylight.simulation.flightgear;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.swing.JFrame;

import br.skylight.commons.Alert;
import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.CounterStats;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.MeasureHelper;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.j3d.PanTiltCameraGimbalViewer;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.uav.infra.SchmittTrigger;
import br.skylight.uav.infra.GPSUpdate.FixQuality;
import br.skylight.uav.plugins.control.instruments.GeoMagJ;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsFailures;
import br.skylight.uav.services.InstrumentsInfos;
import br.skylight.uav.services.InstrumentsListener;
import br.skylight.uav.services.InstrumentsService;
import br.skylight.uav.services.InstrumentsWarnings;

@ServiceImplementation(serviceDefinition={InstrumentsService.class, ActuatorsService.class, GPSService.class})
public class FlightGearServices extends Worker implements GPSService, InstrumentsService, ActuatorsService {

	private Logger logger = Logger.getLogger(FlightGearServices.class.getName());

	private UnitConverter knotToMPSConverter = NonSI.KNOT.getConverterTo(SI.METERS_PER_SECOND);
	private UnitConverter feetToMeterConverter = NonSI.FOOT.getConverterTo(SI.METER);
	
	private int udpOutputPort;
	private String udpOutputHost;
	private int udpInputPort;
	private Coordinates position = new Coordinates();

	private InstrumentsFailures instrumentsFailure = new InstrumentsFailures();
	private InstrumentsWarnings instrumentsWarnings = new InstrumentsWarnings();
	private InstrumentsInfos instrumentsInfos = new InstrumentsInfos();
	
	private static float INPUT_MAX_RANGE = 127;
	private static float INPUT_THROTTLE_MAX_RANGE = 127;

	private static final float MAX_RUDDER = 1f;
	private static final float MAX_AILERON = 1f;
	private static final float MAX_ELEVATOR = 1f;
	private static final float MAX_THROTTLE = 1f;
	
	private boolean emulateLowActuatorsResolution = false;
	private boolean emulateLowInstrumentsResolution = false;
	private boolean emulateLatency = false;
	
	private boolean flightTermination = false;
	private InstrumentsListener listener;
	
	private float yaw;
	private float yawRate;
	
	private float pitch;
	private float pitchRate;
	
	private float roll;
	private float rollRate;
	
	private int rpm;
	private float courseHeading;

	private float throttleInput;
	private float rudderInput;
	private float aileronInput;
	private float elevatorInput;

	private float altitudeMSLGps;
	private float groundSpeed;
	private float speedEast;
	private float speedNorth;
	private float magneticHeading;
	private float accelX;
	private float accelY;
	private float accelZ;

	public float pressurePitot;
	public float pressureStatic;
	public float battVoltage;

	private DatagramSocket udpListener;
	private DatagramSocket udpSender;

	//data to be sent
	private float aileron;
	private float elevator;
	private float rudder;
	private float throttle;
	private boolean engineIgnition;
	private boolean navLights;
	private boolean strobeLights;
	private boolean landingLights;
	private float cameraFOV = 55;
	private float cameraAzimuth;
	private float cameraElevation;
	private float cameraRoll;
	private RotationReference cameraReference = RotationReference.SERVO;

	private JFrame frame;
	private PanTiltCameraGimbalViewer cameraViewer;
	private float[] azimuthElevation = new float[2];
	private double lastGPSUpdateTime;
	private boolean transmitterOn = false;
	private TimedBoolean timedFlagsRemover = new TimedBoolean(2);
	private CounterStats stepStats = new CounterStats(1000);
	
	private TimedBoolean gpsLimiter = new TimedBoolean(100);//10Hz
	
	public FlightGearServices() {
		this(8800, 8900);
	}
	
	public FlightGearServices(int udpFromFlightGearPort, int udpToFlightGearPort) {
		this.udpInputPort = udpFromFlightGearPort;
		this.udpOutputPort = udpToFlightGearPort;
	}

	@Override
	public void onActivate() throws Exception {
		if(udpListener==null) {
			udpListener = new DatagramSocket(udpInputPort);
		}
		logger.info("Waiting UDP messages from FlightGear (udpFromFlightGearPort="+ udpInputPort+"; udpToFlightGearPort="+ udpOutputPort+") in Skylight protocol...");
		udpOutputHost = null;
		position.setLatitude(-15);
		position.setLongitude(-40);
		position.setAltitude(500);
	}
	
	@Override
	public void onDeactivate() throws Exception {
	}
	
	@Override
	public void step() throws Exception {
		stepReader();
		stepSender();
		stepStats.addValue(1);
	}

	private void stepSender() throws IOException {
		if(flightTermination) {
			System.out.println("FLIGHT TERMINATION ACTIVATED. IGNORING COMMAND");
			return;
		}
		
		//CAMERA TRANSFORMATIONS
		float cameraAz = cameraAzimuth;
		float cameraEl = cameraElevation;
		azimuthElevation[0] = cameraAz;
		azimuthElevation[1] = cameraEl;
		//transform orientation from earth to vehicle reference
		if(cameraReference.equals(RotationReference.EARTH)) {
//			System.out.println("Earth   - Az: " + Math.toDegrees(cameraAz) + "; El: " + Math.toDegrees(cameraEl));
			CoordinatesHelper.transformEarthToVehicleReference(azimuthElevation, roll, pitch, yaw);
		}
//		transformGimbalOrientationDueToGimbalRestrictions(azimuthElevation, gimbalModeL, gimbalModeR);
		cameraAz = (float)MathHelper.normalizeAngle(azimuthElevation[0]);
		cameraEl = (float)MathHelper.normalizeAngle(azimuthElevation[1]);
//		System.out.println("Vehicle - Az: " + Math.toDegrees(cameraAz) + "; El: " + Math.toDegrees(cameraEl));
		//show gimbal orientation
		if(cameraViewer!=null) {
			cameraViewer.setSceneOrientation(roll, pitch, yaw);
			cameraViewer.setCameraGimbalOrientation(cameraEl, cameraAz);
		}
		
		//send data
		String out = "";
		out += -aileron + ",0,";
		out += elevator + ",0,";
		out += -rudder + ",0,";
		out += throttle + ",";
		out += (engineIgnition?1:0) + ",";
		out += (navLights?1:0) + ",";
		out += (strobeLights?1:0) + ",";
		out += (landingLights?1:0) + ",";
		if(frame!=null) {
			out += (float)Math.toDegrees(cameraFOV) + ",";
			out += -(float)Math.toDegrees(cameraAz) + ",";
			out += (float)Math.toDegrees(cameraEl) + ",";
			out += -(float)Math.toDegrees(cameraRoll) + "\n";
		} else {
			out += "\n";
		}

		if(udpSender==null) {
			udpSender = new DatagramSocket();
		}
		DatagramPacket p = new DatagramPacket(out.getBytes(), out.getBytes().length);
		if(udpOutputHost!=null) {
			p.setSocketAddress(new InetSocketAddress(udpOutputHost, udpOutputPort));
			udpSender.send(p);
		}
	}

	protected void stepReader() throws IOException {
		insertLatency();
		
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		udpListener.receive(packet);
		
		// use sender as output host
		if(udpOutputHost==null) {
			udpOutputHost = packet.getAddress().getHostAddress();
		}

		//read data
		String data = new String(packet.getData(), packet.getOffset(), packet.getLength());
//		System.out.println("R: "+data);
		StringTokenizer st = new StringTokenizer(data, ",");
		groundSpeed = (float)knotToMPSConverter.convert(Float.parseFloat(st.nextToken()));
		speedEast = (float)MeasureHelper.feetToMeters(Float.parseFloat(st.nextToken()));
		speedNorth = (float)MeasureHelper.feetToMeters(Float.parseFloat(st.nextToken()));
		courseHeading = (float)MathHelper.normalizeAngle2(Math.atan2(speedEast,speedNorth));
		double longitude = Float.parseFloat(st.nextToken());
		double latitude = Float.parseFloat(st.nextToken());
		altitudeMSLGps = (float)feetToMeterConverter.convert(Float.parseFloat(st.nextToken()));
		yaw = (float)MathHelper.normalizeAngle(Math.toRadians(Float.parseFloat(st.nextToken())));
		yawRate = (float)Math.toRadians(Float.parseFloat(st.nextToken()));
		pitch = (float)Math.toRadians(Float.parseFloat(st.nextToken()));
		pitchRate = (float)Math.toRadians(Float.parseFloat(st.nextToken()));
		roll = (float)Math.toRadians(Float.parseFloat(st.nextToken()));
		rollRate = (float)Math.toRadians(Float.parseFloat(st.nextToken()));
		rpm = (int)Float.parseFloat(st.nextToken().replaceAll("\\n", ""));
		pressurePitot = MeasureHelper.inHgToPascal(Float.parseFloat(st.nextToken().replaceAll("\\n", "")));
		pressureStatic = MeasureHelper.inHgToPascal(Float.parseFloat(st.nextToken().replaceAll("\\n", "")));
		pressurePitot -= pressureStatic;//emulate differential pitot
		battVoltage = (int)Float.parseFloat(st.nextToken().replaceAll("\\n", ""))*1000;
		accelX = MeasureHelper.feetToMeters(Float.parseFloat(st.nextToken().replaceAll("\\n", "")));
		accelY = MeasureHelper.feetToMeters(Float.parseFloat(st.nextToken().replaceAll("\\n", "")));
		accelZ = MeasureHelper.feetToMeters(Float.parseFloat(st.nextToken().replaceAll("\\n", "")));
		magneticHeading = (float)Math.toRadians(Float.parseFloat(st.nextToken().replaceAll("\\n", "")));

		//calculate yaw based on [magnetic heading] + [magnetic declination]
//		System.out.println("Yaw: Calculated=" + MathHelper.normalizeAngle(Math.toRadians(magneticHeading + GeoMagJ.getCurrentDeclination(latitude, longitude, altitudeMSLGps))) + "; FG=" + yaw);
		yaw = (float)MathHelper.normalizeAngle(Math.toRadians(magneticHeading + GeoMagJ.getCurrentDeclination(latitude, longitude, altitudeMSLGps)));
		
		if(gpsLimiter.checkTrue()) {
			position.setLatitude(latitude);
			position.setLongitude(longitude);
			position.setAltitude(altitudeMSLGps);
			lastGPSUpdateTime = System.currentTimeMillis()/1000.0;
		}
		
		if(timedFlagsRemover.checkTrue()) {
			instrumentsInfos.setCalibrationPerformed(false);
		}
		
		if(listener!=null) {
			listener.onInstrumentsDataUpdated();
		}
		
	}
	
	private void insertLatency() {
		if(emulateLatency) {
			try {
				Thread.sleep(7);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setAileron(float value) {
		insertLatency();
		if(emulateLowActuatorsResolution) value = (byte)value;
// System.out.println("Setting ailerons to " + value);

		float multiplier = MAX_AILERON / INPUT_MAX_RANGE;

		aileronInput = value;
		aileron = value * multiplier;
	}

	@Override
	public void setElevator(float value) {
		insertLatency();
		if(emulateLowActuatorsResolution) value = (byte)value;

// System.out.println("Setting elevator to " + value);

		float multiplier = MAX_ELEVATOR / INPUT_MAX_RANGE;

		elevatorInput = value;
		elevator = value * multiplier;
	}

	@Override
	public void setRudder(float value) {
		insertLatency();
		if(emulateLowActuatorsResolution) value = (byte)value;

		// System.out.println("Setting rudder to " + percentage);

		float multiplier = MAX_RUDDER / INPUT_MAX_RANGE;

		rudderInput = value;
		rudder = value * multiplier;
	}

	@Override
	public void setThrottle(float value) {
		insertLatency();
		if(emulateLowActuatorsResolution) value = (byte)value;
// System.out.println("Setting throttle to " + value);

		float multiplier = MAX_THROTTLE / INPUT_THROTTLE_MAX_RANGE;

		throttleInput = value;
		throttle = value * multiplier;
	}
	
	@Override
	public float getPitch() {
		if(emulateLowInstrumentsResolution) return (float)Math.toRadians((byte)Math.toDegrees(pitch));
		return pitch;
	}

	@Override
	public float getRoll() {
		if(emulateLowInstrumentsResolution) return (float)Math.toRadians((byte)Math.toDegrees(roll));
		return roll;
	}

	@Override
	public float getCourseHeading() {
		if(emulateLowInstrumentsResolution) return (float)Math.toRadians((short)Math.toDegrees(courseHeading));
		return courseHeading;
	}

	@Override
	public float getThrottle() {
		if(emulateLowInstrumentsResolution) return (byte)throttleInput;
		return throttleInput;
	}

	@Override
	public float getRudder() {
		if(emulateLowInstrumentsResolution) return (byte)rudderInput;
		return rudderInput;
	}

	@Override
	public float getAileron() {
		if(emulateLowInstrumentsResolution) return (byte)aileronInput;
		return aileronInput;
	}

	@Override
	public float getElevator() {
		if(emulateLowInstrumentsResolution) return (byte)elevatorInput;
		return elevatorInput;
	}

	@Override
	public int getEngineRPM() {
		return rpm;
	}

	@Override
	public float getRollRate() {
		if(emulateLowInstrumentsResolution) return (short)rollRate;
		return rollRate;
	}
	
	@Override
	public float getPitchRate() {
		if(emulateLowInstrumentsResolution) return (short)pitchRate;
		return pitchRate;
	}

	@Override
	public float getGroundSpeed() {
		if(emulateLowInstrumentsResolution) return (int)(groundSpeed);
		return groundSpeed;
	}
	
	@Override
	public Coordinates getPosition() {
		return position;
	}

	@Override
	public Coordinates getPositionOnFirstFix() {
		return new Coordinates(0,0,0);
	}

	@Override
	public void reloadVehicleConfiguration() {
	}

	@Override
	public void setLightsState(boolean nav, boolean strobe, boolean landing) {
		this.navLights = nav;
		this.strobeLights = strobe;
		this.landingLights = landing;
	}

	@Override
	public float getYaw() {
		return yaw;
	}

	@Override
	public float getYawRate() {
		return yawRate;
	}

	@Override
	public void deployParachute() {
		logger.info("FlightGear simulation: parachute deployed");
	}

	@Override
	public void setVideoTransmitterPower(boolean on) {
		//show gimbal viewer if this is the second 'power on' command
		if(transmitterOn && on) {
			if(frame==null) {
				cameraViewer = new PanTiltCameraGimbalViewer();
				frame = new JFrame();
				frame.add(cameraViewer);
				frame.setSize(300, 300);
				frame.setVisible(true);
			}
		} else {
			if(frame!=null) {
				frame.setVisible(false);
				frame = null;
			}
		}
		this.transmitterOn = on;
		logger.info("FlightGear simulation: video power " + on);
	}
	
	@Override
	public float getPitotPressure() {
		return pressurePitot;
	}
	
	@Override
	public float getStaticPressure() {
		return pressureStatic;
	}

	@Override
	public float getAltitudeMSL() {
		return altitudeMSLGps;
	}

	@Override
	public FixQuality getFixQuality() {
		return FixQuality.DGPS_FIX;
	}

	@Override
	public float getAccelerationX() {
		return accelX;
	}

	@Override
	public float getAccelerationY() {
		return accelY;
	}

	@Override
	public float getAccelerationZ() {
		return accelZ;
	}

	@Override
	public float getAutoPilotTemperature() {
		return 23;
	}

	@Override
	public float getAuxiliaryBatteryLevel() {
		return battVoltage;
	}

	@Override
	public float getMainBatteryLevel() {
		return battVoltage;
	}

	@Override
	public int getSatCount() {
		return 7;
	}
	
	/**
	 * FOV in radians
	 * @param cameraFOV
	 */
	public void setCameraFOV(float cameraFOV) {
		this.cameraFOV = cameraFOV;
	}

	@Override
	public void setEngineIgnition(boolean enabled) {
		engineIgnition = enabled;
	}

	public static void transformGimbalOrientationDueToGimbalRestrictions(float[] azimuthElevationResult, SchmittTrigger gimbalModeL, SchmittTrigger gimbalModeR) {
		//emulate gimbal constraints (azimuth -90 to 90)
		float az = (float)MathHelper.normalizeAngle(azimuthElevationResult[0]);
		gimbalModeL.setCurrentValue(az);
		gimbalModeR.setCurrentValue(az);
		
		//normal mode
		if(gimbalModeL.isUpperRange() && !gimbalModeR.isUpperRange()) {
			//keep the same reference
			
		//inverted az/el mode
		} else {
			azimuthElevationResult[0] = (float)MathHelper.normalizeAngle(az+Math.PI);
			azimuthElevationResult[1] = (float)-Math.PI - azimuthElevationResult[1];
		}
	}

	@Override
	public void notifyAlertActivated(SubsystemStatusAlert subsystemStatusAlert) {
		if(subsystemStatusAlert.getAlert().equals(Alert.ADT_DOWNLINK_FAILED)) {
			setVideoTransmitterPower(false);
		}
	}

	@Override
	public void performCalibrations() {
		System.out.println("PERFORM CALIBRATIONS RECEIVED");
		instrumentsInfos.setCalibrationPerformed(true);
	}

	@Override
	public double getLastGPSUpdateTime() {
		if(!navLights) {
			return lastGPSUpdateTime;
		} else {
			return 0;//force old GPS update
		}
	}

	@Override
	public float getCameraAzimuth() {
		return cameraAzimuth;
	}

	@Override
	public float getCameraElevation() {
		return cameraElevation;
	}

	@Override
	public float getEngineCilinderTemperature() {
		return 90;
	}

	@Override
	public InstrumentsFailures getInstrumentsFailures() {
		return instrumentsFailure;
	}

	@Override
	public InstrumentsInfos getInstrumentsInfos() {
		return instrumentsInfos;
	}

	@Override
	public InstrumentsWarnings getInstrumentsWarnings() {
		return instrumentsWarnings;
	}

	@Override
	public RotationReference getCameraOrientationReference() {
		return cameraReference;
	}

	@Override
	public boolean isEngineIgnitionEnabled() {
		return engineIgnition;
	}

	@Override
	public void setCameraOrientation(float azimuthValue, float elevationValue, RotationReference reference) {
		this.cameraElevation = elevationValue;
		this.cameraAzimuth = azimuthValue;
		this.cameraReference = reference;
	}

	@Override
	public void setFlightTermination(boolean activated) {
		if(activated) {
			System.out.println("ACTIVATED FLIGHT TERMINATION");
			setAileron(127);
			setRudder(127);
			setElevator(127);
			setThrottle(0);
			engineIgnition = false;
			flightTermination = true;
		}
	}

	@Override
	public int getConsumedFuel() {
		return 0;
	}

	@Override
	public void setGenericServo(float genericServo) {
	}

	@Override
	public float getGenericServo() {
		return 0;
	}

	@Override
	public void setFailSafesArmState(boolean armed) {
		System.out.println("FAILSAFES ARM STATE: " + armed);
	}

	@Override
	public int getEffectiveActuatorsMessageFrequency() {
		return (int)stepStats.getRate();
	}

	@Override
	public void setInstrumentsListener(InstrumentsListener listener) {
		this.listener = listener;
	}

	@Override
	public int getDiscardedMessagesCounter() {
		return -1;
	}

}
