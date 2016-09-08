package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import br.skylight.commons.AGLAltitudeMode;
import br.skylight.commons.Servo;
import br.skylight.commons.ServoConfiguration;
import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.infra.ExtendedSerializableState;
import br.skylight.commons.infra.IOHelper;

public class SkylightVehicleConfigurationMessage extends Message<VehicleConfigurationMessage> implements ExtendedSerializableState {

	private VehicleID vehicleIdentification = new VehicleID();
	
	private float rollMax;
	private float rollMin;
	private float pitchMax;
	private float pitchMin;
	
	private float fuelCapacityVolume;
	private int maxFlightTimeMinutes;
	private float altitudeMaxAGL;
	private AGLAltitudeMode aglAltitudeMode;
	
	private float stallIndicatedAirspeed;
	private float takeOffLiftOffIndicatedAirspeed;
	private float landingSteadyFlightIndicatedAirspeed;
	private float landingMinGroundSpeed;
	private float landingMaxGroundSpeed;

	private float sensorPitchTrim;
	private float sensorRollTrim;
	private float sensorYawTrim;
	
	private float rudderSurfaceGain;
	private float elevatorSurfaceGain;
	private float calculatedVersusRealTurnFactor;
	private float landingApproachScale;
	
	private float gpsAntennaPositionX;
	private float gpsAntennaPositionY;
	private float gpsAntennaPositionZ;
	private float groundTouchPositionZ;
	
	private float pitchRateDamp;
	private float rollRateDamp;
	private float yawRateDamp;
	
	private boolean parachuteEnabled;
	private boolean killEngineEnabled;
	private boolean safetyProceduresEnabled;
	private boolean validateSafetyProceduresBeforeExecution;
	private boolean keepStableOnNoMode;
	
	private Level operatorLoggingLevel;
	
	//m3/s per throttle position
	private float[] fuelConsumptionForThrottle = new float[11];

	//saved only for file storage purposes (not for transmission)
	private Map<Integer,PIDConfiguration> pidConfigurations = new HashMap<Integer,PIDConfiguration>();
	private Map<Integer,ServoConfiguration> servoConfigurations = new HashMap<Integer,ServoConfiguration>();
	
	public SkylightVehicleConfigurationMessage() {
		for (PIDControl pc : PIDControl.values()) {
			PIDConfiguration pcf = new PIDConfiguration();
			pcf.setPIDControl(pc);
			pidConfigurations.put(pc.ordinal(),pcf);
		}
		for (Servo sc : Servo.values()) {
			ServoConfiguration s = new ServoConfiguration();
			s.setServo(sc);
			servoConfigurations.put(sc.ordinal(),s);
		}
		resetValues();
	}
	
	public void validate(VerificationResult vr, VehicleConfigurationMessage vehicleConfiguration) {
		validateServoAndTrimValues(vr);
		validateLimitValues(vr, vehicleConfiguration);
		validatePIDValues(vr);

		vr.assertRange(gpsAntennaPositionX, -999F, 999F, "Gps antenna position X");
		vr.assertRange(gpsAntennaPositionY, -999F, 999F, "Gps antenna position Y");
		vr.assertRange(gpsAntennaPositionZ, -999F, 999F, "Gps antenna position Z");
		vr.assertRange(groundTouchPositionZ, -999F, 999F, "Ground touch position");
	}
	
	public void validatePIDValues(VerificationResult vr) {
		vr.assertRange(pitchRateDamp, 0, 40F, "Pitch rate dump");
		vr.assertRange(rollRateDamp, 0, 40F, "Roll rate dump");
		vr.assertRange(yawRateDamp, 0, 40F, "Yaw rate dump");
		vr.assertRange(elevatorSurfaceGain, 0, 100F, "Elevator surface gain");
		vr.assertRange(rudderSurfaceGain, 0, 100F, "Rudder surface gain");
		vr.assertRange(calculatedVersusRealTurnFactor, 0, 30F, "Calculated versus real turn factor");
		for (PIDConfiguration pc : pidConfigurations.values()) {
			pc.validate(vr);
		}
	}

	public void validateServoAndTrimValues(VerificationResult vr) {
		vr.assertRange(sensorPitchTrim, -180F, -10F, 10F, 180F, "Pitch trim");
		vr.assertRange(sensorRollTrim, -90F, -10F, 10F, 90F, "Roll trim");
		vr.assertRange(sensorYawTrim, -180F, -10F, 10F, 180F, "Yaw trim");
		for (ServoConfiguration sc : servoConfigurations.values()) {
			sc.validate(vr);
		}
	}

	public void validateLimitValues(VerificationResult vr, VehicleConfigurationMessage vehicleConfiguration) {
		vr.assertRange((int)Math.toDegrees(rollMax), 0, 15, 60, 180, "Max roll (degrees)");
		vr.assertRange((int)Math.toDegrees(rollMin), -180, -60, -15, 0, "Min roll (degrees)");
		vr.assertRange((int)Math.toDegrees(pitchMax), 0, 6, 25, 90, "Max pitch (degrees)");
		vr.assertRange((int)Math.toDegrees(pitchMin), -90, -25, -6, 0, "Min pitch (degrees)");
		vr.assertRange(altitudeMaxAGL, 30, 300, 5000, 100000, "Max altitude AGL (m)");
		vr.assertRange(fuelCapacityVolume, 0, 100, "Fuel capacity volume (m3)");
		vr.assertRange(maxFlightTimeMinutes, 2, 20, 600, 4000, "Max flight time (minutes)");
		
		vr.assertRange(stallIndicatedAirspeed, 2, 10, 150, 800, "Stall ktias (m/s)");
		vr.assertRange(takeOffLiftOffIndicatedAirspeed, 1, 10, 150, 800, "Take-off lift off ktias (m/s)");
		vr.assertRange(landingSteadyFlightIndicatedAirspeed, 1, 15, 300, 800, "Landing steady flight ktias (m/s)");
		vr.assertRange(landingMinGroundSpeed, 1, 600, "Landing min ground speed (m/s)");
		vr.assertRange(landingMaxGroundSpeed, 1, 600, "Landing max ground speed (m/s)");

		vr.assertRange(landingApproachScale, 0.05F, 20, "Landing approach scale");
		
		if(landingMinGroundSpeed>=landingMaxGroundSpeed) {
			vr.addError("Max landing ground speed must be greater than min");
		}
		if(stallIndicatedAirspeed>vehicleConfiguration.getMaximumIndicatedAirspeed()) {
			vr.addError("Stall speed must be less than maximum indicated airspeed");
		}
		if(landingSteadyFlightIndicatedAirspeed>vehicleConfiguration.getMaximumIndicatedAirspeed()) {
			vr.addError("Landing steady flight speed must be less than maximum indicated airspeed");
		}
		if(takeOffLiftOffIndicatedAirspeed>vehicleConfiguration.getMaximumIndicatedAirspeed()) {
			vr.addError("Take-off lift off speed must be less than maximum indicated airspeed");
		}
	}

	public PIDConfiguration getPIDConfiguration(PIDControl pc) {
		return pidConfigurations.get(pc.ordinal());
	}
	public ServoConfiguration getServoConfiguration(Servo servo) {
		return servoConfigurations.get(servo.ordinal());
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2000;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		
		vehicleIdentification.readState(in);
		
		rollMax = in.readFloat();
		rollMin = in.readFloat();
		pitchMax = in.readFloat();
		pitchMin = in.readFloat();
		
		altitudeMaxAGL = in.readFloat();
		aglAltitudeMode = AGLAltitudeMode.values()[in.readUnsignedByte()];
		maxFlightTimeMinutes = in.readInt();
		fuelCapacityVolume = in.readFloat();
		
		stallIndicatedAirspeed = in.readFloat();
		takeOffLiftOffIndicatedAirspeed = in.readFloat();
		landingSteadyFlightIndicatedAirspeed = in.readFloat();
		landingMinGroundSpeed = in.readFloat();
		landingMaxGroundSpeed = in.readFloat();

		sensorPitchTrim = in.readFloat();
		sensorRollTrim = in.readFloat();
		sensorYawTrim = in.readFloat();
		
		rudderSurfaceGain = in.readFloat();
		elevatorSurfaceGain = in.readFloat();
		landingApproachScale = in.readFloat();

		gpsAntennaPositionX = in.readFloat();
		gpsAntennaPositionY = in.readFloat();
		gpsAntennaPositionZ = in.readFloat();
		groundTouchPositionZ = in.readFloat();
		
		pitchRateDamp = in.readFloat();
		rollRateDamp = in.readFloat();
		yawRateDamp = in.readFloat();
		calculatedVersusRealTurnFactor = in.readFloat();
		
		parachuteEnabled = in.readBoolean();
		killEngineEnabled = in.readBoolean();
		safetyProceduresEnabled = in.readBoolean();
		validateSafetyProceduresBeforeExecution = in.readBoolean();
		keepStableOnNoMode = in.readBoolean();
		
		operatorLoggingLevel = Level.parse(in.readInt()+"");

		//fuel consumption curve
		for (int i=0; i<fuelConsumptionForThrottle.length; i++) {
			fuelConsumptionForThrottle[i] = in.readFloat();
		}
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		
		vehicleIdentification.writeState(out);
		
		out.writeFloat(rollMax);
		out.writeFloat(rollMin);
		out.writeFloat(pitchMax);
		out.writeFloat(pitchMin);
		
		out.writeFloat(altitudeMaxAGL);
		out.writeByte(aglAltitudeMode.ordinal());
		out.writeInt(maxFlightTimeMinutes);
		out.writeFloat(fuelCapacityVolume);
		
		out.writeFloat(stallIndicatedAirspeed);//Vs
		out.writeFloat(takeOffLiftOffIndicatedAirspeed);
		out.writeFloat(landingSteadyFlightIndicatedAirspeed);//Vs0
		out.writeFloat(landingMinGroundSpeed);
		out.writeFloat(landingMaxGroundSpeed);

		out.writeFloat(sensorPitchTrim);
		out.writeFloat(sensorRollTrim);
		out.writeFloat(sensorYawTrim);
		
		out.writeFloat(rudderSurfaceGain);
		out.writeFloat(elevatorSurfaceGain);
		out.writeFloat(landingApproachScale);

		out.writeFloat(gpsAntennaPositionX);
		out.writeFloat(gpsAntennaPositionY);
		out.writeFloat(gpsAntennaPositionZ);
		out.writeFloat(groundTouchPositionZ);
		
		out.writeFloat(pitchRateDamp);
		out.writeFloat(rollRateDamp);
		out.writeFloat(yawRateDamp);
		out.writeFloat(calculatedVersusRealTurnFactor);
		
		out.writeBoolean(parachuteEnabled);
		out.writeBoolean(killEngineEnabled);
		out.writeBoolean(safetyProceduresEnabled);
		out.writeBoolean(validateSafetyProceduresBeforeExecution);
		out.writeBoolean(keepStableOnNoMode);
		
		out.writeInt(operatorLoggingLevel.intValue());
		
		//fuel consumption curve
		for (int i=0; i<fuelConsumptionForThrottle.length; i++) {
			out.writeFloat(fuelConsumptionForThrottle[i]);
		}
	}

	/**
	 * Special write state for storage purposes (will save additional large configuration states)
	 */
	@Override
	public void writeStateExtended(DataOutputStream out) throws IOException {
		writeState(out);
		IOHelper.writeMapStateIntKey(pidConfigurations,out);
		IOHelper.writeMapStateIntKey(servoConfigurations,out);
	}
	
	@Override
	public void readStateExtended(DataInputStream in) throws IOException {
		readState(in);
		IOHelper.readMapStateIntKey(pidConfigurations, PIDConfiguration.class, in);
		IOHelper.readMapStateIntKey(servoConfigurations, ServoConfiguration.class, in);
	}
	
	public float getRollMax() {
		return rollMax;
	}

	public void setRollMax(float rollMax) {
		this.rollMax = rollMax;
	}

	public float getRollMin() {
		return rollMin;
	}

	public void setRollMin(float rollMin) {
		this.rollMin = rollMin;
	}

	public float getPitchMax() {
		return pitchMax;
	}

	public void setPitchMax(float pitchMax) {
		this.pitchMax = pitchMax;
	}

	public float getPitchMin() {
		return pitchMin;
	}

	public void setPitchMin(float pitchMin) {
		this.pitchMin = pitchMin;
	}

	public float getAltitudeMaxAGL() {
		return altitudeMaxAGL;
	}

	public void setAltitudeMaxAGL(float altitudeMaxAGL) {
		this.altitudeMaxAGL = altitudeMaxAGL;
	}

	public float getStallIndicatedAirspeed() {
		return stallIndicatedAirspeed;
	}

	public void setStallIndicatedAirspeed(float speedStall) {
		this.stallIndicatedAirspeed = speedStall;
	}

	public float getTakeOffLiftOffIndicatedAirspeed() {
		return takeOffLiftOffIndicatedAirspeed;
	}

	public void setTakeOffLiftOffIndicatedAirspeed(float speedTakeOffLiftOff) {
		this.takeOffLiftOffIndicatedAirspeed = speedTakeOffLiftOff;
	}

	public float getLandingSteadyFlightIndicatedAirspeed() {
		return landingSteadyFlightIndicatedAirspeed;
	}

	public void setLandingSteadyFlightIndicatedAirspeed(float speedLandingSteadyFlight) {
		this.landingSteadyFlightIndicatedAirspeed = speedLandingSteadyFlight;
	}

	public float getLandingMinGroundSpeed() {
		return landingMinGroundSpeed;
	}

	public void setLandingMinGroundSpeed(float speedLandingMinGroundSpeed) {
		this.landingMinGroundSpeed = speedLandingMinGroundSpeed;
	}

	public float getLandingMaxGroundSpeed() {
		return landingMaxGroundSpeed;
	}

	public void setLandingMaxGroundSpeed(float speedLandingMaxGroundSpeed) {
		this.landingMaxGroundSpeed = speedLandingMaxGroundSpeed;
	}

	public float getSensorPitchTrim() {
		return sensorPitchTrim;
	}

	public void setSensorPitchTrim(float sensorPitchTrim) {
		this.sensorPitchTrim = sensorPitchTrim;
	}
	
	public float getSensorYawTrim() {
		return sensorYawTrim;
	}
	public void setSensorYawTrim(float sensorYawTrim) {
		this.sensorYawTrim = sensorYawTrim;
	}

	public float getSensorRollTrim() {
		return sensorRollTrim;
	}

	public void setSensorRollTrim(float sensorRollTrim) {
		this.sensorRollTrim = sensorRollTrim;
	}

	public float getRudderSurfaceGain() {
		return rudderSurfaceGain;
	}
	public void setRudderSurfaceGain(float rudderSurfaceGain) {
		this.rudderSurfaceGain = rudderSurfaceGain;
	}
	public float getElevatorSurfaceGain() {
		return elevatorSurfaceGain;
	}
	public void setElevatorSurfaceGain(float elevatorSurfaceGain) {
		this.elevatorSurfaceGain = elevatorSurfaceGain;
	}

	public float getLandingApproachScale() {
		return landingApproachScale;
	}

	public void setLandingApproachScale(float landingApproachScale) {
		this.landingApproachScale = landingApproachScale;
	}
	
	public void setAglAltitudeMode(AGLAltitudeMode aglAltitudeMode) {
		this.aglAltitudeMode = aglAltitudeMode;
	}
	public AGLAltitudeMode getAglAltitudeMode() {
		return aglAltitudeMode;
	}

	public void setKillEngineEnabled(boolean killEngineEnabled) {
		this.killEngineEnabled = killEngineEnabled;
	}
	public boolean isKillEngineEnabled() {
		return killEngineEnabled;
	}
	
	public boolean isParachuteEnabled() {
		return parachuteEnabled;
	}

	public void setParachuteEnabled(boolean parachuteEnabled) {
		this.parachuteEnabled = parachuteEnabled;
	}

	public Level getOperatorLoggingLevel() {
		return operatorLoggingLevel;
	}
	public void setOperatorLoggingLevel(Level operatorLoggingLevel) {
		this.operatorLoggingLevel = operatorLoggingLevel;
	}
	
	public float[] getFuelConsumptionForThrottle() {
		return fuelConsumptionForThrottle;
	}
	
	public int getMaxFlightTimeMinutes() {
		return maxFlightTimeMinutes;
	}
	public void setMaxFlightTimeMinutes(int maxFlightTimeMinutes) {
		this.maxFlightTimeMinutes = maxFlightTimeMinutes;
	}
	
	public float getCalculatedVersusRealTurnFactor() {
		return calculatedVersusRealTurnFactor;
	}
	public void setCalculatedVersusRealTurnFactor(float calculatedVersusRealTurnFactor) {
		this.calculatedVersusRealTurnFactor = calculatedVersusRealTurnFactor;
	}
	public float getPitchRateDamp() {
		return pitchRateDamp;
	}
	public float getRollRateDamp() {
		return rollRateDamp;
	}
	public void setPitchRateDamp(float pitchRateDamp) {
		this.pitchRateDamp = pitchRateDamp;
	}
	public void setRollRateDamp(float rollRateDamp) {
		this.rollRateDamp = rollRateDamp;
	}
	
	public float getFuelCapacityVolume() {
		return fuelCapacityVolume;
	}
	public void setFuelCapacityVolume(float fuelCapacityVolume) {
		this.fuelCapacityVolume = fuelCapacityVolume;
	}
	
	public VehicleID getVehicleIdentification() {
		return vehicleIdentification;
	}
	public void setVehicleIdentification(VehicleID vehicleIdentification) {
		this.vehicleIdentification = vehicleIdentification;
	}
	
	public Map<Integer, PIDConfiguration> getPidConfigurations() {
		return pidConfigurations;
	}
	public void addPidConfiguration(PIDConfiguration pidConfiguration) {
		pidConfigurations.put(pidConfiguration.getPIDControl().ordinal(), pidConfiguration);
	}
	
	public Map<Integer, ServoConfiguration> getServoConfigurations() {
		return servoConfigurations;
	}
	public void addServoConfiguration(ServoConfiguration servoConfiguration) {
		servoConfigurations.put(servoConfiguration.getServo().ordinal(), servoConfiguration);
	}
	
	public float getFuelDensity(VehicleConfigurationMessage vehicleConfiguration) {
		return vehicleConfiguration.getPropulsionFuelCapacity()/getFuelCapacityVolume();
	}
	
	public boolean isKeepStableOnNoMode() {
		return keepStableOnNoMode;
	}
	public boolean isSafetyProceduresEnabled() {
		return safetyProceduresEnabled;
	}
	public void setKeepStableOnNoMode(boolean keepStableOnNoMode) {
		this.keepStableOnNoMode = keepStableOnNoMode;
	}
	public void setSafetyProceduresEnabled(boolean safetyProceduresEnabled) {
		this.safetyProceduresEnabled = safetyProceduresEnabled;
	}
	
	public void setValidateSafetyProceduresBeforeExecution(boolean validateSafetyProceduresBeforeExecution) {
		this.validateSafetyProceduresBeforeExecution = validateSafetyProceduresBeforeExecution;
	}
	public boolean isValidateSafetyProceduresBeforeExecution() {
		return validateSafetyProceduresBeforeExecution;
	}
	
	public float getGpsAntennaPositionX() {
		return gpsAntennaPositionX;
	}

	public void setGpsAntennaPositionX(float gpsAntennaPositionX) {
		this.gpsAntennaPositionX = gpsAntennaPositionX;
	}

	public float getGpsAntennaPositionY() {
		return gpsAntennaPositionY;
	}

	public void setGpsAntennaPositionY(float gpsAntennaPositionY) {
		this.gpsAntennaPositionY = gpsAntennaPositionY;
	}

	public float getGpsAntennaPositionZ() {
		return gpsAntennaPositionZ;
	}

	public void setGpsAntennaPositionZ(float gpsAntennaPositionZ) {
		this.gpsAntennaPositionZ = gpsAntennaPositionZ;
	}

	public float getGroundTouchPositionZ() {
		return groundTouchPositionZ;
	}

	public void setGroundTouchPositionZ(float groundTouchPositionZ) {
		this.groundTouchPositionZ = groundTouchPositionZ;
	}
	
	public float getYawRateDamp() {
		return yawRateDamp;
	}
	public void setYawRateDamp(float yawRateDamp) {
		this.yawRateDamp = yawRateDamp;
	}

	@Override
	public void resetValues() {
		vehicleIdentification.setVehicleID(0);
		
		rollMax = (float)Math.toRadians(30);
		rollMin = (float)Math.toRadians(-30);
		pitchMax = (float)Math.toRadians(15);
		pitchMin = (float)Math.toRadians(-15);
		
		fuelCapacityVolume = 0.001F;//1 dm3 - 1 litre
		maxFlightTimeMinutes = 120;
		altitudeMaxAGL = 350;
		aglAltitudeMode = AGLAltitudeMode.PRESSURE_AT_AGL_SETUP;
		
		stallIndicatedAirspeed = 20;//Vs
		takeOffLiftOffIndicatedAirspeed = 23;
		landingSteadyFlightIndicatedAirspeed = 24;//Vs0
		landingMinGroundSpeed = 20;
		landingMaxGroundSpeed = 28;

		sensorPitchTrim = 0;
		sensorRollTrim = 0;
		sensorYawTrim = 0;
		
		rudderSurfaceGain = 1;
		elevatorSurfaceGain = 3;
		calculatedVersusRealTurnFactor = 1;
		landingApproachScale = 1;

		gpsAntennaPositionX = 0;
		gpsAntennaPositionY = 0;
		gpsAntennaPositionZ = 0;
		groundTouchPositionZ = 0;
		
		pitchRateDamp = 0;
		rollRateDamp = 0;
		yawRateDamp = 0;
		
		parachuteEnabled = false;
		killEngineEnabled = false;
		safetyProceduresEnabled = false;
		validateSafetyProceduresBeforeExecution = false;
		keepStableOnNoMode = false;
		
		operatorLoggingLevel = Level.INFO;
		
		//m3/s per throttle position
		for (int i=0; i<fuelConsumptionForThrottle.length; i++) {
			fuelConsumptionForThrottle[i] = 0;
		}
		
		//default PID configurations
		pidConfigurations.get(PIDControl.HOLD_ALTITUDE_WITH_PITCH.ordinal())	.setup(1F, 0F, 0F);
		pidConfigurations.get(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE.ordinal())	.setup(1F, 0.1F, 0F);
		pidConfigurations.get(PIDControl.HOLD_COURSE_WITH_ROLL.ordinal())		.setup(1F, 0F, 0F);
		pidConfigurations.get(PIDControl.HOLD_COURSE_WITH_YAW.ordinal())		.setup(1F, 0F, 0F);
		pidConfigurations.get(PIDControl.HOLD_GROUNDSPEED_WITH_IAS.ordinal())	.setup(0F, 1F, 0F);
		pidConfigurations.get(PIDControl.HOLD_IAS_WITH_PITCH.ordinal())			.setup(1F, 0F, 0F);
		pidConfigurations.get(PIDControl.HOLD_IAS_WITH_THROTTLE.ordinal())		.setup(0F, 1F, 0F);
		pidConfigurations.get(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER.ordinal())	.setup(1F, 0F, 0F);
		pidConfigurations.get(PIDControl.HOLD_ROLL_WITH_AILERON.ordinal())		.setup(1F, 0F, 0F);
		pidConfigurations.get(PIDControl.HOLD_YAW_WITH_ELEV_RUDDER.ordinal())	.setup(1F, 0F, 0F);
	}

}
