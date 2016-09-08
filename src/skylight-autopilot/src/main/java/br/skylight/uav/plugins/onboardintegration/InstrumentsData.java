package br.skylight.uav.plugins.onboardintegration;

import java.io.DataInputStream;
import java.io.IOException;

import br.skylight.uav.services.InstrumentsFailures;
import br.skylight.uav.services.InstrumentsInfos;
import br.skylight.uav.services.InstrumentsWarnings;

public class InstrumentsData {

	private InstrumentsFailures instrumentsFailures = new InstrumentsFailures();
	private InstrumentsWarnings instrumentsWarnings = new InstrumentsWarnings();
	private InstrumentsInfos instrumentsInfos = new InstrumentsInfos();

	private float roll;
	private float pitch;
	private float yaw;
	
	private float rollRate;
	private float pitchRate;
	private float yawRate;

	private float accelX;
	private float accelY;
	private float accelZ;

	private float engineCilinderHeadTemperature;
	private float autopilotTemperature;
	private int engineRPM;

	private float mainBattVolts;
	private float auxBattVolts;

	private int staticPressure;
	private int dynamicPressure;
	
	private int incomingHz;

	public void readState(DataInputStream is) throws IOException {
		instrumentsFailures.setData(is.read());
		instrumentsWarnings.setData(is.read());
		instrumentsInfos.setData(is.read());
		
		roll = (float)Math.toRadians(((float)is.readShort())/10F);
		pitch = (float)Math.toRadians(((float)is.readShort())/10F);
		yaw = (float)Math.toRadians(((float)is.readShort())/10F);

		rollRate = (float)Math.toRadians(((float)is.readShort())/10F);
		pitchRate = (float)Math.toRadians(((float)is.readShort())/10F);
		yawRate = (float)Math.toRadians(((float)is.readShort())/10F);
		
		accelX = ((float)is.readShort())/10F;
		accelY = ((float)is.readShort())/10F;
		accelZ = ((float)is.readShort())/10F;
		
		engineCilinderHeadTemperature = ((float)is.readShort())/10F;
		autopilotTemperature = ((float)is.readShort())/10F;
		engineRPM = is.readShort();

		mainBattVolts = ((float)is.readShort())/1000F;
		auxBattVolts = ((float)is.readShort())/1000F;

		staticPressure = (is.read() << 16) | (is.read() << 8) | is.read();//unsigned 3 bytes
		dynamicPressure = is.readUnsignedShort();

		incomingHz = is.read();
	}

	public void copyState(InstrumentsData source) {
		instrumentsFailures.setData(source.instrumentsFailures.getData());
		instrumentsWarnings.setData(source.instrumentsWarnings.getData());
		instrumentsInfos.setData(source.instrumentsInfos.getData());
		
		roll = source.roll;
		pitch = source.pitch;
		yaw = source.yaw;

		rollRate = source.rollRate;
		pitchRate = source.pitchRate;
		yawRate = source.yawRate;
		
		accelX = source.accelX;
		accelY = source.accelY;
		accelZ = source.accelZ;
		
		engineCilinderHeadTemperature = source.engineCilinderHeadTemperature;
		autopilotTemperature = source.autopilotTemperature;
		engineRPM = source.engineRPM;

		mainBattVolts = source.mainBattVolts;
		auxBattVolts = source.auxBattVolts;

		staticPressure = source.staticPressure;
		dynamicPressure = source.dynamicPressure;

		incomingHz = source.incomingHz;
	}
	
	public InstrumentsFailures getInstrumentsFailures() {
		return instrumentsFailures;
	}

	public InstrumentsWarnings getInstrumentsWarnings() {
		return instrumentsWarnings;
	}

	public InstrumentsInfos getInstrumentsInfos() {
		return instrumentsInfos;
	}

	public float getRoll() {
		return roll;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRollRate() {
		return rollRate;
	}

	public float getPitchRate() {
		return pitchRate;
	}

	public float getYawRate() {
		return yawRate;
	}

	public float getAccelX() {
		return accelX;
	}

	public float getAccelY() {
		return accelY;
	}

	public float getAccelZ() {
		return accelZ;
	}

	public float getEngineCilinderHeadTemperature() {
		return engineCilinderHeadTemperature;
	}

	public float getAutopilotTemperature() {
		return autopilotTemperature;
	}

	public int getEngineRPM() {
		return engineRPM;
	}

	public float getMainBattVolts() {
		return mainBattVolts;
	}

	public float getAuxBattVolts() {
		return auxBattVolts;
	}

	public int getStaticPressure() {
		return staticPressure;
	}

	public int getDynamicPressure() {
		return dynamicPressure;
	}

	public int getIncomingHz() {
		return incomingHz;
	}

}
