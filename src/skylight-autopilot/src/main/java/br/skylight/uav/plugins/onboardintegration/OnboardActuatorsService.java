package br.skylight.uav.plugins.onboardintegration;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.CheckedOutputStream;

import br.skylight.commons.Servo;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.infra.CRC8;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;

@ServiceImplementation(serviceDefinition=ActuatorsService.class)
public class OnboardActuatorsService extends Worker implements ActuatorsService {

	private static final Logger logger = Logger.getLogger(OnboardActuatorsService.class.getName());

	private enum Servo78Mode {
		SERVO_POSITION_US,
		ANGLE_RELATIVE_TO_VEHICLE_DEGREES,
		ANGLE_RELATIVE_TO_EARTH_DEGREES,
		SERVO_RANGE_DEGREES,
		SERVO_MIN_US,
		SERVO_MAX_US,
		SERVO_TRIM_US,
		IMU_ROLLPITCH_GAINS,
		IMU_YAW_GAINS
	}

	public static final byte[] MESSAGE_HEADER = new byte[] {-2, 0x24};//0xFE24
	
	private SkylightVehicleConfigurationMessage sc;
	
	private DataOutputStream dos = null;
	private CRC8 checksum;
	
	private float elevator;
	private float rudder;
	private float aileronL;
	private float aileronR;
	private float throttle;
	private float genericServo;
	private float cameraAzimuth;
	private float cameraElevation;
	private RotationReference cameraOrientationReference = RotationReference.VEHICLE;
	private GenericCommands1 genericCommands1 = new GenericCommands1();
	private GenericCommands2 genericCommands2 = new GenericCommands2();
	
	//servos 7/8
	private boolean sendServo78Configurations = false;
	private int sendServo78ConfigurationsStep = 0;
	private Servo78Mode servo78Mode = Servo78Mode.ANGLE_RELATIVE_TO_VEHICLE_DEGREES;
	private short servo7Value;
	private short servo8Value;
	
	private int crcValue;
	
	private TimedBoolean signalingTimer = new TimedBoolean(1000);
	
	private TimedBoolean printer = new TimedBoolean(1000);
	
	@ServiceInjection
	public AdvancedInstrumentsService advancedInstrumentsService;
	
	@ServiceInjection
	public RepositoryService repositoryService;
	
	@MemberInjection
	public OnboardConnections onboardConnections;
	
	@Override
	public void onActivate() throws Exception {
		reloadVehicleConfiguration();
		checksum = new CRC8();
		dos = new DataOutputStream(new CheckedOutputStream(onboardConnections.getAvionicsConnectionParams().resolveConnection().getOutputStream(), checksum));
//			@Override
//			public synchronized void write(int b) throws IOException {
//				super.write(b);
//				System.out.print(IOHelper.byteToHex((byte)b) + " ");
//			}
//		});
	}
	
	@Override
	public void onDeactivate() throws Exception {
		//don't close avionics connection because instruments gateway may be using it 
	}
	
	@Override
	public void step() throws IOException {
		//prepare servos 7/8 command
		if(!sendServo78Configurations) {
			//camera azimuth/elevation
			if(cameraOrientationReference.equals(RotationReference.SERVO)) {
				servo78Mode = Servo78Mode.SERVO_POSITION_US;
				servo7Value = (short)sc.getServoConfiguration(Servo.CAMERA_TILT).getServoTimeForZeroCenteredSetpoint(cameraElevation);
				servo8Value = (short)sc.getServoConfiguration(Servo.CAMERA_PAN).getServoTimeForZeroCenteredSetpoint(cameraAzimuth);
			} else if(cameraOrientationReference.equals(RotationReference.VEHICLE)) {
				servo78Mode = Servo78Mode.ANGLE_RELATIVE_TO_VEHICLE_DEGREES;
				servo7Value = (short)(Math.toDegrees(cameraElevation)*10);
				servo8Value = (short)(Math.toDegrees(cameraAzimuth)*10);
			} else if(cameraOrientationReference.equals(RotationReference.EARTH)) {
				servo78Mode = Servo78Mode.ANGLE_RELATIVE_TO_EARTH_DEGREES;
				servo7Value = (short)(Math.toDegrees(cameraElevation)*10);
				servo8Value = (short)(Math.toDegrees(cameraAzimuth)*10);
			} else {
				System.out.println("SHOULD NOT PASS HERE 1");
			}
			
		//prepare servos 7/8 configurations to be sent
		} else {
			//send servos range
			if(sendServo78ConfigurationsStep==0) {
				servo78Mode = Servo78Mode.SERVO_RANGE_DEGREES;
				servo7Value = (short)(Math.toDegrees(sc.getServoConfiguration(Servo.CAMERA_TILT).getRangeAngle())*10);
				servo8Value = (short)(Math.toDegrees(sc.getServoConfiguration(Servo.CAMERA_PAN).getRangeAngle())*10);
				sendServo78ConfigurationsStep++;
//				System.out.println(">>>>>> " + servo78Mode + " " + servo7Value + " " + servo8Value);
			//send servos min uS
			} else if(sendServo78ConfigurationsStep==1) {
				servo78Mode = Servo78Mode.SERVO_MIN_US;
				servo7Value = (short)sc.getServoConfiguration(Servo.CAMERA_TILT).getMinUs();
				servo8Value = (short)sc.getServoConfiguration(Servo.CAMERA_PAN).getMinUs();
				sendServo78ConfigurationsStep++;
//				System.out.println(">>>>>> " + servo78Mode + " " + servo7Value + " " + servo8Value);
			//send servos max uS
			} else if(sendServo78ConfigurationsStep==2) {
				servo78Mode = Servo78Mode.SERVO_MAX_US;
				servo7Value = (short)sc.getServoConfiguration(Servo.CAMERA_TILT).getMaxUs();
				servo8Value = (short)sc.getServoConfiguration(Servo.CAMERA_PAN).getMaxUs();
				sendServo78ConfigurationsStep++;
//				System.out.println(">>>>>> " + servo78Mode + " " + servo7Value + " " + servo8Value);
			//send trim uS
			} else if(sendServo78ConfigurationsStep==3) {
				servo78Mode = Servo78Mode.SERVO_TRIM_US;
				servo7Value = (short)sc.getServoConfiguration(Servo.CAMERA_TILT).getTrimUs();
				servo8Value = (short)sc.getServoConfiguration(Servo.CAMERA_PAN).getTrimUs();
				sendServo78ConfigurationsStep++;
//				System.out.println(">>>>>> " + servo78Mode + " " + servo7Value + " " + servo8Value);
				
			//send IMU roll pitch gains
			} else if(sendServo78ConfigurationsStep==4) {
				servo78Mode = Servo78Mode.IMU_ROLLPITCH_GAINS;
				servo7Value = (short)100;//proportional part
				servo8Value = (short)100;//integral part
				sendServo78ConfigurationsStep++;
//				System.out.println(">>>>>> " + servo78Mode + " " + servo7Value + " " + servo8Value);
				
			//send IMU yaw gains
			} else if(sendServo78ConfigurationsStep==5) {
				servo78Mode = Servo78Mode.IMU_YAW_GAINS;
				servo7Value = (short)100;//proportional part
				servo8Value = (short)100;//integral part
				sendServo78ConfigurationsStep = 0;
				sendServo78Configurations = false;
//				System.out.println(">>>>>> " + servo78Mode + " " + servo7Value + " " + servo8Value);
				
			} else {
				System.out.println("SHOULD NOT PASS HERE 2");
			}
		}

		//write header
		dos.write(MESSAGE_HEADER);

		//reset checksum
		checksum.reset();
		
		//WRITE MESSAGE BODY
		//servo 7/8 mode
//		System.out.println(">>> 78mode: "+servo78Mode.ordinal());
		dos.write(servo78Mode.ordinal());
		//servo1
		dos.writeShort(sc.getServoConfiguration(Servo.AILERON_RIGHT).getServoTimeForZeroCenteredSetpoint(aileronR));
		//servo2
		dos.writeShort(sc.getServoConfiguration(Servo.ELEVATOR).getServoTimeForZeroCenteredSetpoint(elevator));
		//servo3
		dos.writeShort(sc.getServoConfiguration(Servo.THROTTLE).getServoTimeForSetpoint(throttle*2F));
		//servo4
		dos.writeShort(sc.getServoConfiguration(Servo.RUDDER).getServoTimeForZeroCenteredSetpoint(rudder));
		//servo5
		dos.writeShort(sc.getServoConfiguration(Servo.GENERIC_SERVO).getServoTimeForZeroCenteredSetpoint(genericServo));
		//servo6
		dos.writeShort(sc.getServoConfiguration(Servo.AILERON_LEFT).getServoTimeForZeroCenteredSetpoint(aileronL));
		//servo7
//		System.out.println(">>> 7 value: "+servo7Value);
		dos.writeShort(servo7Value);
		//servo8
//		System.out.println(">>> 8 value: "+servo8Value);
		dos.writeShort(servo8Value);
		//generic commands 1
		dos.write((int)genericCommands1.getData());
		//generic commands 2
		dos.write((int)genericCommands2.getData());
		//3d speed
		dos.writeShort((int)(advancedInstrumentsService.getSpeed().length()*100));
		//crc
		crcValue = (int)(checksum.getValue() & 0xFF);//truncate to 8 bits
		dos.write(crcValue);
		
		dos.flush();

		if(printer.checkTrue()) {
			System.out.print(servo78Mode.ordinal() + ";");
			System.out.print(sc.getServoConfiguration(Servo.AILERON_RIGHT).getServoTimeForZeroCenteredSetpoint(aileronR) + ";");
			System.out.print(sc.getServoConfiguration(Servo.ELEVATOR).getServoTimeForZeroCenteredSetpoint(elevator) + ";");
			System.out.print(sc.getServoConfiguration(Servo.THROTTLE).getServoTimeForSetpoint(throttle*2F) + ";");
			System.out.print(sc.getServoConfiguration(Servo.RUDDER).getServoTimeForZeroCenteredSetpoint(rudder) + ";");
			System.out.print(sc.getServoConfiguration(Servo.GENERIC_SERVO).getServoTimeForZeroCenteredSetpoint(genericServo) + ";");
			System.out.print(sc.getServoConfiguration(Servo.AILERON_LEFT).getServoTimeForZeroCenteredSetpoint(aileronL) + ";");
			System.out.print(servo7Value + ";");
			System.out.print(servo8Value + ";");
			System.out.print(genericCommands1.isBit(0) + ";" + genericCommands1.isBit(1) + ";" + genericCommands1.isBit(2) + ";" + genericCommands1.isBit(3) + ";" + genericCommands1.isBit(4) + ";" + genericCommands1.isBit(5) + ";" + genericCommands1.isBit(6) + ";" + genericCommands1.isBit(7) + ";");
			System.out.print(genericCommands2.isBit(0) + ";" + genericCommands1.isBit(1) + ";");
			System.out.println(advancedInstrumentsService.getSpeed().length()*100);
		}
		
		//request once
		genericCommands1.setRequestCalibration(false);
		
		//these commands may be sent only for 1s and then may be turn off
		if(signalingTimer.isTimedOut()) {
			genericCommands2.setArmFailsafe(false);
			genericCommands2.setDisarmFailsafe(false);
			signalingTimer.setEnabled(false);
		}
		
	}
	
	public void reloadVehicleConfiguration() {
		sc = repositoryService.getSkylightVehicleConfiguration();
		//send camera servos configurations to hardware on next steps
		sendServo78Configurations = true;
		sendServo78ConfigurationsStep = 0;
	}

	@Override
	public void setLightsState(boolean nav, boolean strobe, boolean landing) {
		genericCommands1.setActivateNavigationLights(nav);
		genericCommands1.setActivateStrobeLights(strobe);
		genericCommands1.setActivateLandingLights(landing);
	}
	
	@Override
	public void setVideoTransmitterPower(boolean on) {
		genericCommands1.setActivateVideoTransmitter(on);
	}

	@Override
	public boolean isEngineIgnitionEnabled() {
		return genericCommands1.isActivateEngineIgnition();
	}

	@Override
	public void setEngineIgnition(boolean enabled) {
		genericCommands1.setActivateEngineIgnition(enabled);
	}
	
	@Override
	public void setFlightTermination(boolean activated) {
		genericCommands1.setActivateFlightTermination(activated);
	}

	@Override
	public void performCalibrations() {
		genericCommands1.setRequestCalibration(true);
	}
	
	@Override
	public void setFailSafesArmState(boolean armed) {
		if(armed) {
			genericCommands2.setArmFailsafe(true);
		} else {
			genericCommands2.setDisarmFailsafe(true);
		}
		signalingTimer.setEnabled(true);
		signalingTimer.reset();
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
	public RotationReference getCameraOrientationReference() {
		return cameraOrientationReference;
	}

	@Override
	public float getAileron() {
		return aileronR;
	}

	@Override
	public float getElevator() {
		return elevator;
	}

	@Override
	public float getRudder() {
		return rudder;
	}

	@Override
	public float getThrottle() {
		return throttle;
	}

	@Override
	public void setAileron(float value) {
		aileronR = value;
		aileronL = -value;
	}

	@Override
	public void setCameraOrientation(float azimuthValue, float elevationValue, RotationReference reference) {
		cameraOrientationReference = reference;
		cameraAzimuth = azimuthValue;
		cameraElevation = elevationValue;
	}

	@Override
	public void setElevator(float value) {
		elevator = value;
	}

	@Override
	public void setRudder(float value) {
		rudder = value;
	}

	@Override
	public void setThrottle(float value) {
		throttle = value;
	}

	@Override
	public void notifyAlertActivated(SubsystemStatusAlert subsystemStatusAlert) {
	}

	@Override
	public int getConsumedFuel() {
		return 0;
	}

	@Override
	public void deployParachute() {
		genericCommands1.setActivateExtraOutput(true);
	}
	
	@Override
	public void setGenericServo(float genericServo) {
		this.genericServo = genericServo;
	}

	@Override
	public float getGenericServo() {
		return genericServo;
	}

}
