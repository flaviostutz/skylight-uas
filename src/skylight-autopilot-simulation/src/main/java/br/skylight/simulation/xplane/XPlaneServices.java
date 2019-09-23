package br.skylight.simulation.xplane;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;

import traer.physics.Vector3D;
import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.CounterStats;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.MeasureHelper;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.j3d.PanTiltCameraGimbalViewer;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.uav.infra.GPSUpdate.FixQuality;
import br.skylight.uav.plugins.control.instruments.GeoMagJ;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsFailures;
import br.skylight.uav.services.InstrumentsInfos;
import br.skylight.uav.services.InstrumentsListener;
import br.skylight.uav.services.InstrumentsService;
import br.skylight.uav.services.InstrumentsWarnings;

/**
 * Interface to X-Plane Simulator.
 * Configure X-Plane as follows:
 *      - Settings -> Data Input/Output
 *          - Mark the first column for messages 25;32;70;71;74;75;130
 *      - Settings -> Net Connections
 *          - Open tab "Advanced"
 *          - Mark "IP for Data Output" and set 127.0.0.1:49001
 *          - Open tab "UDP"
 *          - Receive on: 49000
 *          - Sent to: 49101 (put anything but 49001!)
 * @author Edu/Stutz
 */
@ServiceImplementation(serviceDefinition = { InstrumentsService.class, ActuatorsService.class, GPSService.class })
public class XPlaneServices extends Worker implements GPSService, InstrumentsService, ActuatorsService {

	private static final Logger logger = Logger.getLogger(XPlaneServices.class.getName());

	private int udpVsmToXPlanePort;
	private String udpOutputHost;
	private int udpXPlaneToVsmPort;

	private static float INPUT_MAX_RANGE = 127;
	private static float INPUT_THROTTLE_MAX_RANGE = 127;

	private static final float MAX_RUDDER = 20f;
	private static final float MAX_AILERON = 20f;
	private static final float MAX_ELEVATOR = 20f;
	private static final float MAX_THROTTLE = 1f;

	private Coordinates position = new Coordinates();
	private Coordinates positionOnFirstFix;

	private boolean emulateLowActuatorsResolution;
	private boolean emulateLowInstrumentsResolution;
	private boolean emulateLatency;

	private InstrumentsFailures instrumentsFailures = new InstrumentsFailures();
	private InstrumentsWarnings instrumentsWarnings = new InstrumentsWarnings();
	private InstrumentsInfos instrumentsInfos = new InstrumentsInfos();

	private double lastGPSUpdateTime;
	private RotationReference cameraReference = RotationReference.VEHICLE;
	private float cameraAzimuth;
	private float cameraElevation;

	private float engineTemp;
	private boolean engineIgnition;
	private boolean flightTermination;

	private float pitch;
	private float roll;
	private float yaw;

	private float mainBatteryVoltage;

	private float staticPressure;
	private float pitotPressure;

	private int rpm;
	private float courseHeading;

	private float throttleInput;

	private float rudderInput;
	private float aileronInput;
	private float elevatorInput;

	private float latitude;
	private float longitude;
	private float altitudeMSLGps;
	private float groundSpeed;

	private float accelX;
	private float accelY;
	private float accelZ;

	private float rollRate;
	private float pitchRate;
	private float yawRate;
	private float cameraZoom;

	private boolean transmitterOn;
	
	private DatagramSocket udpListener;
	private DatagramSocket udpSender;

	private Vector3D speed = new Vector3D();
	private InstrumentsListener listener;
	
	private float vehicleX, vehicleY, vehicleZ;

	private JFrame frame;
	private PanTiltCameraGimbalViewer cameraViewer;
	private float[] azimuthElevation = new float[2];
	private CounterStats stepStats = new CounterStats(1000);
	
	private boolean recv3, recv4, recv6, recv17, recv18, recv20, recv21, recv19, recv37, recv48, recv54;

	public XPlaneServices() {
		this(49000, 49010);
		//On XPlane 9.70, configure message numbers to be sent to network and "Net Connections -> Advanced -> IP of datareceiver 127.0.0.1:49011"  
	}

	public XPlaneServices(int udpVsmToXPlanePort, int udpXPlaneToVsmPort) {
		this.udpVsmToXPlanePort = udpVsmToXPlanePort;
		this.udpXPlaneToVsmPort = udpXPlaneToVsmPort;
	}

	@Override
	public void onActivate() throws Exception {
		logger.info("Binding to X-Plane (udpXPlaneToVsmPort=" + udpXPlaneToVsmPort + "; udpVsmToXPlanePort=" + udpVsmToXPlanePort + ")...");
		if (udpListener == null) {
			udpListener = new DatagramSocket(udpXPlaneToVsmPort);
		}
		logger.info("Waiting UDP messages (3;4;6;17;18;19;20;21;37;48;54) from X-Plane (udpXPlaneToVsmPort=" + udpXPlaneToVsmPort + "; udpVsmToXPlanePort=" + udpVsmToXPlanePort + ")...");
		//messages sent: 25;32;70;71;74;75;130
//		udpOutputHost = null;
		step();
//		udpOutputHost = "localhost";
		positionOnFirstFix = new Coordinates(latitude, longitude, altitudeMSLGps);

		//reset camera view
//		DatagramPacket outgoing = createDatagramDATA(new float[] { 130, -999, -999, -999, -999, -999, -999, -999, -999 });
//		sendPacket(outgoing);
	}

	@Override
	public void onDeactivate() throws Exception {
		// if(udpListener!=null) {
		// udpListener.close();
		// }
	}

	@Override
	public void step() throws Exception {
		byte[] buffer = new byte[1024];
		insertLatency();

		// 3;4;6;17;18;20;37;48;54
		while (!recv3 || !recv4 || !recv6 || !recv17 || !recv18 || !recv19 || !recv20 || !recv21 || !recv37 || !recv48 || !recv54) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			udpListener.receive(packet);
			if (packet.getData()[0] == 'D' && packet.getData()[1] == 'A' && packet.getData()[2] == 'T' && packet.getData()[3] == 'A') {
				processDataStructure(packet);
			}
		}
		
		// mark package type as not received yet for the next loop
		recv3 = false;
		recv4 = false;
		recv6 = false;
		recv17 = false;
		recv18 = false;
		recv19 = false;
		recv20 = false;
		recv21 = false;
		recv37 = false;
		recv48 = false;
		recv54 = false;

		//keep camera positioned according to vehicle
		updateCameraPosition();
		
		stepStats.addValue(1);
		
		if(listener!=null) {
			listener.onInstrumentsDataUpdated();
		}
	}

	private void updateCameraPosition() {
		float cameraAzimuthEarth = cameraAzimuth;
		float cameraElevationEarth = cameraElevation;
		
		//transformations to earth reference for X-Plane
		azimuthElevation[0] = cameraAzimuth;
		azimuthElevation[1] = cameraElevation;
		//transform orientation from vehicle to earth reference
		if(cameraReference.equals(RotationReference.VEHICLE)) {
//			System.out.println("Earth   - Az: " + Math.toDegrees(cameraAz) + "; El: " + Math.toDegrees(cameraEl));
			CoordinatesHelper.transformVehicleToEarthReference(azimuthElevation, cameraAzimuth, cameraElevation, roll, pitch, yaw);
			cameraAzimuthEarth = azimuthElevation[0];
			cameraElevationEarth = azimuthElevation[1];
		}

		//show gimbal
		if(cameraViewer!=null) {
			//transform to vehicle reference (needed by gimbal)
			float[] r = new float[2];
			r[0] = cameraAzimuthEarth;
			r[1] = cameraElevationEarth;
			CoordinatesHelper.transformEarthToVehicleReference(r, roll, pitch, yaw);
			float cameraAzimuthVehicle = r[0];
			float cameraElevationVehicle = r[1];
//			transformGimbalOrientationDueToGimbalRestrictions(azimuthElevation, gimbalModeL, gimbalModeR);
			//update gimbal orientation
			cameraViewer.setSceneOrientation(roll, pitch, yaw);
			cameraViewer.setCameraGimbalOrientation(cameraElevationVehicle, cameraAzimuthVehicle);
		}
		
		//send camera parameters to X-Plane
		//all camera parameters are set in Earth reference
		DatagramPacket outgoing = createDatagramDATA(new float[] { 130, vehicleX, vehicleY, vehicleZ, (float)Math.toDegrees(cameraAzimuthEarth), (float)Math.toDegrees(cameraElevationEarth), 0, cameraZoom, cameraZoom });
//		DatagramPacket outgoing = createDatagramDATA(new float[] { 130, -999, -999, -999, 0,0,0,0,0 });
//		sendPacket(outgoing);
	}

	private void processDataStructure(DatagramPacket packet) {
		// use sender as output host
		if (udpOutputHost == null) {
			udpOutputHost = packet.getAddress().getHostAddress();
		}

		List<float[]> structs = parseDataStructs(packet);

		for (float[] struct : structs) {

			// System.out.println("Struct:" + struct[0]);
			
			if (struct[0] == 3) {
				recv3 = true;
				float ktias = struct[1];
				// v = sqrt(2P/D); - P-pitot pressure; D-air density; v-airspeed
				// v2 = 2P/D -> P = v2*D
				// calculate from ktias because couldn't find raw pitot pressure on XPlane
				float s = MeasureHelper.knotsToMetersPerSecond(ktias);
				pitotPressure = (float) (s*s * 1.225F)/2.0F;
				groundSpeed = MeasureHelper.milesToMetersPerSecond(struct[8]);
//				System.out.println(pitotPressure);

			} else if (struct[0] == 4) {
				recv4 = true;
				accelX = struct[6] * 9.8F;// data comes in G
				accelY = struct[7] * 9.8F;
				accelZ = struct[5] * 9.8F;

			} else if (struct[0] == 6) {
				recv6 = true;
				staticPressure = MeasureHelper.inHgToPascal(struct[1]);

			} else if (struct[0] == 17) {
				recv17 = true;
				rollRate = struct[1];
				pitchRate = struct[2];
				yawRate = struct[3];

			} else if (struct[0] == 18) {
				recv18 = true;
				pitch = (float)Math.toRadians(struct[1]);
				roll = (float)Math.toRadians(struct[2]);
				yaw = (float)MathHelper.normalizeAngle(Math.toRadians(struct[4]));//mag heading
				if(latitude!=0) {
					//magnetic declination correction
					yaw += Math.toRadians(GeoMagJ.getCurrentDeclination(Math.toDegrees(latitude), Math.toDegrees(longitude), altitudeMSLGps));
				}

			} else if (struct[0] == 19) {
				recv19 = true;
				courseHeading = (float)MathHelper.normalizeAngle2(Math.toRadians(struct[3]));
				
			} else if (struct[0] == 20) {
				recv20 = true;
				latitude = (float)Math.toRadians(struct[1]);
				longitude = (float)Math.toRadians(struct[2]);
				altitudeMSLGps = MeasureHelper.feetToMeters(struct[3]);
				// altitudeAGLBaro = struct[4];
				lastGPSUpdateTime = System.currentTimeMillis() / 1000.0;

			} else if (struct[0] == 21) {
				recv21 = true;
				vehicleX = struct[1];
				vehicleY = struct[2];
				vehicleZ = struct[3];
				
			} else if (struct[0] == 37) {
				recv37 = true;
				rpm = (int) struct[1];

			} else if (struct[0] == 48) {
				recv48 = true;
				engineTemp = struct[1];

			} else if (struct[0] == 54) {
				recv54 = true;
				mainBatteryVoltage = struct[1];

			}

//			System.out.println(">> " + recv3 + " " + recv4 + " " + recv6 + " " + recv17 + " " + recv18 + " " + recv20 + " " + recv37 + " " + recv48 + " " + recv54);
		}

	}

	private void insertLatency() {
		if (emulateLatency) {
			try {
				Thread.sleep(7);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendPacket(DatagramPacket outgoing) {
		if (flightTermination) {
			System.out.println("FLIGHT TERMINATION ACTIVATED. IGNORING COMMAND");
			return;
		}
		try {
			if (udpSender == null) {
				udpSender = new DatagramSocket();
			}
			outgoing.setSocketAddress(new InetSocketAddress(udpOutputHost, udpVsmToXPlanePort));
			udpSender.send(outgoing);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	// public void setPlaneOrientationLocation(double lat, double lon, double
	// ele,
	// float psi, float the, float phi) {
	//
	// // "VEH1 " + 1 x 4 bytes (airc. number) + 3 x 8 bytes (lat/lon/ele) + 3
	// // x 4 bytes (psi/the/phi) + 3 x 4 bytes (gear/flap/vect)
	//
	// double[] data = new double[] { 0, lat, lon, ele, psi, the, phi, -999,
	// -999, -999 };
	// DatagramPacket outgoing = createDatagramVEH1(data, packet);
	// // float[] data = new float[]{18, 60, -999, -999, -999, -999, -999,
	// // -999, -999};
	// // DatagramPacket outgoing = createDatagramDATA(data, packet);
	//
	// try {
	// outgoing.setPort(udpOutputPort);
	// socket.send(outgoing);
	// } catch (IOException ioe) {
	// throw new RuntimeException(ioe);
	// }
	//
	// }

	@Override
	public void setAileron(float value) {
		insertLatency();
		if (emulateLowActuatorsResolution)
			value = (byte) value;
		// System.out.println("Setting ailerons to " + value);

		float multiplier = MAX_AILERON / INPUT_MAX_RANGE;

		float[] ailerons = new float[2];
		aileronInput = value;

		ailerons[0] = -value * multiplier;
		ailerons[1] = value * multiplier;

		DatagramPacket outgoing = createDatagramDATA(new float[] { 70, ailerons[0], ailerons[1], ailerons[0], ailerons[1], ailerons[0], ailerons[1], ailerons[0], ailerons[1] });

		sendPacket(outgoing);
	}

	@Override
	public void setElevator(float value) {
		insertLatency();
		if (emulateLowActuatorsResolution)
			value = (byte) value;

		float multiplier = MAX_ELEVATOR / INPUT_MAX_RANGE;

		float elevator;
		elevatorInput = value;
		elevator = value * multiplier;

		DatagramPacket outgoing = createDatagramDATA(new float[] { 74, elevator, elevator, elevator, elevator, elevator, elevator, elevator, elevator });

		sendPacket(outgoing);
	}

	@Override
	public void setRudder(float value) {
		insertLatency();
		if (emulateLowActuatorsResolution)
			value = (byte) value;

		float multiplier = MAX_RUDDER / INPUT_MAX_RANGE;

		float rudder;
		rudderInput = value;
		rudder = -value * multiplier;

		DatagramPacket outgoing = createDatagramDATA(new float[] { 75, rudder, rudder, rudder, rudder, rudder, rudder, rudder, rudder });

		sendPacket(outgoing);
	}

	@Override
	public void setThrottle(float value) {
		insertLatency();
		if (emulateLowActuatorsResolution)
			value = (byte) value;

		float multiplier = MAX_THROTTLE / INPUT_THROTTLE_MAX_RANGE;

		float throttle;
		throttleInput = value;
		throttle = value * multiplier;

		DatagramPacket outgoing = createDatagramDATA(new float[] { 25, throttle, throttle, throttle, throttle, throttle, throttle, throttle, throttle });

		sendPacket(outgoing);
	}

	// private DatagramPacket createDatagramVEH1(double[] struct, DatagramPacket
	// packet) {
	//
	// // "VEH1 " + 1 x 4 bytes (airc. number) + 3 x 8 bytes (lat/lon/ele) + 3
	// // x 4 bytes (psi/the/phi) + 3 x 4 bytes (gear/flap/vect)
	//
	// byte[] outputBuffer = new byte[5 + 4 + (3 * 8) + (3 * 4 * 2)];
	//
	// // prelogue
	// outputBuffer[0] = 'V';
	// outputBuffer[1] = 'E';
	// outputBuffer[2] = 'H';
	// outputBuffer[3] = '1';
	// outputBuffer[4] = 0;
	//
	// // aircraft number
	// ByteBuffer bb = ByteBuffer.allocate(4);
	// bb.putInt((int) struct[0]);
	//
	// outputBuffer[5] = bb.array()[3];
	// outputBuffer[6] = bb.array()[2];
	// outputBuffer[7] = bb.array()[1];
	// outputBuffer[8] = bb.array()[0];
	//
	// // lat/lon/ele
	// for (int i = 0; i < 3; i++) {
	//
	// ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
	// DataOutputStream datastream = new DataOutputStream(bytestream);
	//
	// byte[] bytes;
	//
	// try {
	// datastream.writeDouble(struct[i + 1]);
	// datastream.flush();
	// bytes = bytestream.toByteArray();
	// bytestream.close();
	// datastream.close();
	// } catch (IOException e) {
	// throw new RuntimeException(e);
	// }
	//
	// outputBuffer[9 + i * 8] = bytes[7];
	// outputBuffer[10 + i * 8] = bytes[6];
	// outputBuffer[11 + i * 8] = bytes[5];
	// outputBuffer[12 + i * 8] = bytes[4];
	// outputBuffer[13 + i * 8] = bytes[3];
	// outputBuffer[14 + i * 8] = bytes[2];
	// outputBuffer[15 + i * 8] = bytes[1];
	// outputBuffer[16 + i * 8] = bytes[0];
	// }
	//
	// // psi/the/phi + gear/flap/vect
	// for (int i = 0; i < 6; i++) {
	//
	// bb = ByteBuffer.allocate(4);
	// bb.putFloat((float) struct[i + 4]);
	//
	// outputBuffer[33 + i * 4] = bb.array()[3];
	// outputBuffer[34 + i * 4] = bb.array()[2];
	// outputBuffer[35 + i * 4] = bb.array()[1];
	// outputBuffer[36 + i * 4] = bb.array()[0];
	// }
	//
	// DatagramPacket newPacket;
	// try {
	// newPacket = new DatagramPacket(outputBuffer, outputBuffer.length, new
	// InetSocketAddress(udpOutputHost, udpOutputPort));
	// } catch (SocketException e) {
	// throw new RuntimeException(e);
	// } catch (IllegalArgumentException e) {
	// System.err.println(e);
	// return packet;
	// }
	// return newPacket;
	// }

	private DatagramPacket createDatagramDATA(float[] struct) {

		byte[] outputBuffer = new byte[5 + 9 * 4];

		// prologue
		outputBuffer[0] = 'D';
		outputBuffer[1] = 'A';
		outputBuffer[2] = 'T';
		outputBuffer[3] = 'A';
		outputBuffer[4] = 0;

		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt((int) struct[0]);

		// index
		outputBuffer[5] = bb.array()[3];
		outputBuffer[6] = bb.array()[2];
		outputBuffer[7] = bb.array()[1];
		outputBuffer[8] = bb.array()[0];

		for (int i = 0; i < 8; i++) {

			bb = ByteBuffer.allocate(4);
			bb.putFloat(struct[i + 1]);

			outputBuffer[9 + i * 4] = bb.array()[3];
			outputBuffer[10 + i * 4] = bb.array()[2];
			outputBuffer[11 + i * 4] = bb.array()[1];
			outputBuffer[12 + i * 4] = bb.array()[0];
		}

		DatagramPacket newPacket;

		try {
			newPacket = new DatagramPacket(outputBuffer, outputBuffer.length, new InetSocketAddress(udpOutputHost, udpVsmToXPlanePort));
//		} catch (SocketException e) {
//			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		return newPacket;
	}

	private static List<float[]> parseDataStructs(DatagramPacket packet) {

		ArrayList<float[]> structs = new ArrayList<float[]>();

		byte[] packetData = packet.getData();

		for (int i = 0; (i + 32 + 12) < packetData.length; i += 36) {

			float[] struct = new float[9];

			byte[] temp = new byte[4];
			// parse Index
			temp[3] = packet.getData()[5 + i];
			temp[2] = packet.getData()[6 + i];
			temp[1] = packet.getData()[7 + i];
			temp[0] = packet.getData()[8 + i];

			struct[0] = parseInt(temp);

			int var = 1;

			for (int z = 0; z < 32; z += 4) {
				temp[3] = packet.getData()[9 + i + z];
				temp[2] = packet.getData()[10 + i + z];
				temp[1] = packet.getData()[11 + i + z];
				temp[0] = packet.getData()[12 + i + z];
				struct[var] = parseFloat(temp);
				var++;
			}
			structs.add(struct);
		}

		return structs;
	}

	private static int parseInt(byte[] bytes) {

		if (bytes.length != 4)
			throw new RuntimeException("Must receive 4 bytes!");

		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.put(bytes);

		return bb.getInt(0);
	}

	private static float parseFloat(byte[] bytes) {

		if (bytes.length != 4)
			throw new RuntimeException("Must receive 4 bytes!");

		byte[] floatBytes = new byte[4];

		floatBytes[0] = bytes[0];
		floatBytes[1] = bytes[1];
		floatBytes[2] = bytes[2];
		floatBytes[3] = bytes[3];

		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.put(floatBytes);

		return bb.getFloat(0);
	}

	@Override
	public float getPitch() {
		if (emulateLowInstrumentsResolution)
			return (byte) pitch;
		return pitch;
	}

	@Override
	public float getRoll() {
		if (emulateLowInstrumentsResolution)
			return (byte) roll;
		return roll;
	}

	@Override
	public float getYaw() {
		return yaw;
	}
	
	@Override
	public float getCourseHeading() {
		if (emulateLowInstrumentsResolution)
			return (short) courseHeading;
		return courseHeading;
	}

	@Override
	public float getThrottle() {
		if (emulateLowInstrumentsResolution)
			return (byte) throttleInput;
		return throttleInput;
	}

	@Override
	public float getRudder() {
		if (emulateLowInstrumentsResolution)
			return (byte) rudderInput;
		return rudderInput;
	}

	@Override
	public float getAileron() {
		if (emulateLowInstrumentsResolution)
			return (byte) aileronInput;
		return aileronInput;
	}

	@Override
	public float getElevator() {
		if (emulateLowInstrumentsResolution)
			return (byte) elevatorInput;
		return elevatorInput;
	}

	@Override
	public int getEngineRPM() {
		return rpm;
	}

	@Override
	public float getRollRate() {
		return rollRate;
	}

	@Override
	public float getPitchRate() {
		return pitchRate;
	}

	@Override
	public float getYawRate() {
		return yawRate;
	}
	
	@Override
	public float getGroundSpeed() {
		if (emulateLowInstrumentsResolution)
			return (int) (groundSpeed);
		return groundSpeed;
	}

	public void setEmulateLowActuatorsResolution(boolean emulateLowActuatorsResolution) {
		this.emulateLowActuatorsResolution = emulateLowActuatorsResolution;
	}

	public void setEmulateLowInstrumentsResolution(boolean emulateLowInstrumentsResolution) {
		this.emulateLowInstrumentsResolution = emulateLowInstrumentsResolution;
	}

	public void setEmulateLatency(boolean emulateLatency) {
		this.emulateLatency = emulateLatency;
	}

	@Override
	public Coordinates getPosition() {
		position.setLatitudeRadians(latitude);
		position.setLongitudeRadians(longitude);
		position.setAltitude(altitudeMSLGps);
		return position;
	}

	@Override
	public Coordinates getPositionOnFirstFix() {
		return positionOnFirstFix;
	}

	@Override
	public void reloadVehicleConfiguration() {
	}

	@Override
	public void setLightsState(boolean nav, boolean strobe, boolean landing) {
		System.out.println("SET LIGHTS: nav=" + nav + "; strobe=" + strobe + "; landing=" + landing);
		// TODO send this to XPlane
	}

	@Override
	public float getMainBatteryLevel() {
		return mainBatteryVoltage;
	}

	@Override
	public float getStaticPressure() {
		return staticPressure;
	}

	@Override
	public float getPitotPressure() {
		return pitotPressure;
	}

	@Override
	public void deployParachute() {
	}

	@Override
	public void setCameraOrientation(float azimuthValue, float elevationValue, RotationReference reference) {
		this.cameraAzimuth = azimuthValue;
		this.cameraElevation = elevationValue;
		this.cameraReference = reference;
	}

	@Override
	public float getAltitudeMSL() {
		return altitudeMSLGps;
	}

	@Override
	public FixQuality getFixQuality() {
		return FixQuality.SIMUATION_MODE;
	}

	@Override
	public int getSatCount() {
		return 7;
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
		return mainBatteryVoltage;
	}

	@Override
	public void setVideoTransmitterPower(boolean on) {
		System.out.println("TURN VIDEO TRANSMITTER ON: " + on);
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
	}

	@Override
	public void setEngineIgnition(boolean enabled) {
		System.out.println("ENGINE IGNITION ENABLED: " + enabled);
		engineIgnition = enabled;
		float ig = enabled?3:0;
		DatagramPacket outgoing = createDatagramDATA(new float[] { 32, ig, ig, ig, ig, ig, ig, ig, ig });
		sendPacket(outgoing);
	}

	@Override
	public void notifyAlertActivated(SubsystemStatusAlert subsystemStatusAlert) {
		System.out.println("ALERT " + subsystemStatusAlert + " RECEIVED");
	}

	@Override
	public void performCalibrations() {
		System.out.println("PERFORM CALIBRATIONS RECEIVED");
		instrumentsInfos.setCalibrationPerformed(true);
	}

	@Override
	public double getLastGPSUpdateTime() {
		return lastGPSUpdateTime;
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
		return engineTemp;
	}

	@Override
	public InstrumentsFailures getInstrumentsFailures() {
		return instrumentsFailures;
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
	public int getConsumedFuel() {
		return 0;
	}

	@Override
	public boolean isEngineIgnitionEnabled() {
		return engineIgnition;
	}

	@Override
	public void setFlightTermination(boolean activated) {
		if (activated) {
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
	public void setGenericServo(float genericServo) {
	}

	@Override
	public float getGenericServo() {
		return 0;
	}

	public void setCameraZoom(float zoom) {
		this.cameraZoom = zoom;
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
