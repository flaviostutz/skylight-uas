package br.skylight.uav.plugins.control;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.Servo;
import br.skylight.commons.ServoConfiguration;
import br.skylight.commons.dli.enums.AltitudeCommandType;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.EngineStatus;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.HeadingCommandType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.services.ScheduledMessageReporter;
import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.PIDControllerCommand;
import br.skylight.commons.dli.skylight.PIDControllerState;
import br.skylight.commons.dli.skylight.ServoActuationCommand;
import br.skylight.commons.dli.skylight.ServosStateMessage;
import br.skylight.commons.dli.vehicle.EngineCommand;
import br.skylight.commons.dli.vehicle.ModePreferenceCommand;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.MovingAverage;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.infra.SchmittTrigger;
import br.skylight.uav.infra.UAVHelper;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.control.maneuvers.Maneuver;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.PIDWorker;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;
import br.skylight.uav.plugins.messaging.MessageScheduler;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsService;
import br.skylight.uav.services.ActuatorsService.RotationReference;

@ManagedMember
public class Pilot extends Worker implements MessageListener, ScheduledMessageReporter {

	private static final Logger logger = Logger.getLogger(Pilot.class.getName());
	private Maneuver currentManeuver;

	public enum HeadingControlType {DYNAMIC, AILERON, RUDDER, FIXED_ROLL}
	public enum AltitudeControlType {DYNAMIC, ELEVATOR, THROTTLE}
	
	//min/max heading error in degrees so that roll or rudder will be used to maintain heading
	private SchmittTrigger horizontalMode = new SchmittTrigger((float)Math.toRadians(13),(float)Math.toRadians(16),true);

	//min/max airspeed to be used when selecting normal/reverse vertical mode
	//these will be recalculated on "reloadVehicleConfiguration()"
	private SchmittTrigger verticalModeSpeed = new SchmittTrigger(10,12,true);
	private SchmittTrigger verticalModeSpeedError = new SchmittTrigger(-3,0,true);
	private SchmittTrigger verticalModeAltitudeError = new SchmittTrigger(15,30,true);

	//send pid controller report for inactive elements from time to time
	private Map<PIDControl,Boolean> lastReportedPids = new HashMap<PIDControl,Boolean>();
	private TimedBoolean reportAllPidsTimer = new TimedBoolean(10000);
	
	//keep track of current avg ground speed
	private MovingAverage groundSpeedAverager = new MovingAverage(30);
	private TimedBoolean groundSpeedAveragerTimer = new TimedBoolean(3000);
	
	//flight holds in auto/manual mode
	private FlightHold<AltitudeType, AltitudeControlType> altitudeHold = new FlightHold<AltitudeType, AltitudeControlType>();
	private FlightHold<AltitudeType, AltitudeControlType> autoAltitudeHold = new FlightHold<AltitudeType, AltitudeControlType>();
	private FlightHold<AltitudeType, AltitudeControlType> manualAltitudeHold = new FlightHold<AltitudeType, AltitudeControlType>();
	
	private FlightHold<HeadingCommandType, HeadingControlType> headingHold = new FlightHold<HeadingCommandType, HeadingControlType>();
	private FlightHold<HeadingCommandType, HeadingControlType> autoHeadingHold = new FlightHold<HeadingCommandType, HeadingControlType>();
	private FlightHold<HeadingCommandType, HeadingControlType> manualHeadingHold = new FlightHold<HeadingCommandType, HeadingControlType>();
	
	private FlightHold<SpeedType, Object> speedHold = new FlightHold<SpeedType, Object>();
	private FlightHold<SpeedType, Object> autoSpeedHold = new FlightHold<SpeedType, Object>();
	private FlightHold<SpeedType, Object> manualSpeedHold = new FlightHold<SpeedType, Object>();
	
	private boolean enableFlightHolds = true;
	
	@ServiceInjection
	public InstrumentsService instrumentsService;
	@ServiceInjection
	public AdvancedInstrumentsService advancedInstrumentsService;
	@ServiceInjection
	public ActuatorsService actuatorsService;
	@ServiceInjection
	public RepositoryService repositoryService;
	@ServiceInjection
	public MessagingService messagingService;
	@ServiceInjection
	public MessageScheduler messageScheduler;
	@ServiceInjection
	public GPSService gpsService;
	@MemberInjection(optionalAtInitialization=true)
	public PIDControllers pidControllers;
	@ServiceInjection(optionalAtInitialization=true)
	public Commander commander;
	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public void onActivate() throws Exception {
		messagingService.setMessageListener(MessageType.M45, this);

		messagingService.setMessageListener(MessageType.M2003, this);
		messagingService.setMessageListener(MessageType.M2004, this);
		messagingService.setMessageListener(MessageType.M2009, this);
		messagingService.setMessageListener(MessageType.M2010, this);
		
		messageScheduler.setMessageReporter(MessageType.M2009, this);
		messageScheduler.setMessageReporter(MessageType.M2010, this);
		messageScheduler.setMessageReporter(MessageType.M2011, this);
		messageScheduler.setMessageReporter(MessageType.M2012, this);

		//initialize last reported map
		for (PIDControl pc : PIDControl.values()) {
			lastReportedPids.put(pc, false);
		}

		pluginManager.executeAfterStartup(new Runnable(){
			public void run() {
				reloadVehicleConfiguration();
			}
		});
	}
	
	public void step() {
		//CALCULATE AVERAGE SPEED
		if(groundSpeedAveragerTimer.checkTrue()) {
			groundSpeedAverager.addSample(gpsService.getGroundSpeed());
//			System.out.println("HORIZONTAL MODE UPPER: "+horizontalMode.isUpperRange());
//			System.out.println("VERTICAL MODE UPPER: "+verticalModeSpeed.isUpperRange());
//			System.out.println("VERTICAL MODE ALTITUDE ERROR UPPER: "+verticalModeAltitudeError.isUpperRange());
		}
		
		//HORIZONTAL/VERTICAL MODE SELECTION
		//use roll to control course when course error is high
//		System.out.println(Math.abs(pidControllers.getCurrentCourseError()));
		horizontalMode.setCurrentValue(Math.abs(pidControllers.getCurrentCourseError()));
		
		//enter vertical reverse mode if ias is low and
		verticalModeSpeed.setCurrentValue(advancedInstrumentsService.getIAS());
		//if speed error is not too much below setpoint and
		verticalModeSpeedError.setCurrentValue(pidControllers.getCurrentSpeedError());
		//if altitude error is low (pitch for altitude is better for big altitude transitions)
		verticalModeAltitudeError.setCurrentValue(pidControllers.getCurrentAltitudeError());
		
		//STEP CURRENT MANEUVER
		if(currentManeuver!=null && currentManeuver.isRunning()) {
			currentManeuver.doStep();
		}

		//STEP FLIGHT HOLDS
		if(enableFlightHolds && !FlightPathControlMode.NO_MODE.equals(commander.getCurrentFlightPathMode())) {
			stepFlightHolds();//step altitude/speed/heading targets
		}
		
		//STEP PID CONTROLLERS
		//prevent calculating maneuvers/PIDs when the UAV is being controlled manually
		if(!instrumentsService.getInstrumentsInfos().isManualRemoteControl()) {
			//step pid controllers
			pidControllers.step();
		}
	}
	
	private void stepFlightHolds() {
		VehicleSteeringCommand sc = repositoryService.getVehicleSteeringCommand();
		ModePreferenceCommand pc = repositoryService.getModePreferenceCommand();
		
		//ALTITUDE HOLD
		//choose manual/auto
		if(commander.isAutoAltitudeControlsEnabled(sc, pc)) {
			altitudeHold.copyFrom(autoAltitudeHold);
			UAVHelper.notifyStateFine(logger, "Altitude control: configuration " + altitudeHold.isActive(), "manualhold");
		} else {
			altitudeHold.copyFrom(manualAltitudeHold);
			UAVHelper.notifyStateFine(logger, "Altitude control: manual override " + altitudeHold.isActive(), "manualhold");
		}
		//handle inactive controls
		if(!altitudeHold.isActive()) {
			if(commander.isAutopilotEngaged()) {
				//if this is called before any vehicle steering is received
				if(altitudeHold.getControlType()==null) {
					manualAltitudeHold.set(false, advancedInstrumentsService.getAltitude(AltitudeType.AGL), AltitudeType.AGL, AltitudeControlType.DYNAMIC);
					altitudeHold.copyFrom(manualAltitudeHold);
				}
				altitudeHold.setActive(true);
			}
		}
		//activate pid controllers
		if(altitudeHold.isActive()) {
			if(!isUseReverseVerticalControl()) {
				pidControllers.holdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_PITCH, altitudeHold.getTargetValue(), altitudeHold.getTargetValueType());
				UAVHelper.notifyStateFine(logger, "Altitude control: pitch. target=" + altitudeHold.getTargetValue() + " ("+ altitudeHold.getTargetValueType() +")", "pilot-altitude-mode");
			} else {
				pidControllers.holdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE, altitudeHold.getTargetValue(), altitudeHold.getTargetValueType());
				UAVHelper.notifyStateFine(logger, "Altitude control: throttle. target=" + altitudeHold.getTargetValue() + " ("+ altitudeHold.getTargetValueType() +")", "pilot-altitude-mode");
			}
		} else {
			pidControllers.unholdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_PITCH);
			pidControllers.unholdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE);
//			pidControllers.unholdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEVATOR);
			UAVHelper.notifyStateFine(logger, "No altitude set", "pilot-altitude-mode");
		}
		
		//HEADING HOLD
		//choose manual/auto
		if(commander.isAutoCourseControlsEnabled(sc, pc)) {
			headingHold.copyFrom(autoHeadingHold);
			UAVHelper.notifyStateFine(logger, "Heading control: configuration " + headingHold.isActive(), "headinghold");
		} else {
			headingHold.copyFrom(manualHeadingHold);
			UAVHelper.notifyStateFine(logger, "Heading control: manual override " + headingHold.isActive(), "headinghold");
		}
		//handle inactive controls
		if(!headingHold.isActive()) {
			if(commander.isAutopilotEngaged()) {
				//if this is called before any vehicle steering is received
				if(headingHold.getControlType()==null) {
					manualHeadingHold.set(false, gpsService.getCourseHeading(), HeadingCommandType.COURSE, HeadingControlType.DYNAMIC);
					headingHold.copyFrom(manualHeadingHold);
				}
				headingHold.setActive(true);
			}
		}
		//activate pid controllers
		if(headingHold.isActive()) {
			if(headingHold.getControlType().equals(HeadingControlType.DYNAMIC)) {
				if(isUseRudderForHeading()) {
//					System.out.println("RUDDER");
					headingHold.setControlType(HeadingControlType.RUDDER);
				} else {
//					System.out.println("AILERON");
					headingHold.setControlType(HeadingControlType.AILERON);
				}
			}
				
			//fly to heading turning with aileron
			if(headingHold.getControlType().equals(HeadingControlType.AILERON)) {
				pidControllers.holdSetpoint(PIDControl.HOLD_COURSE_WITH_ROLL, headingHold.getTargetValue());
				pidControllers.holdSetpoint(PIDControl.HOLD_COURSE_WITH_YAW, headingHold.getTargetValue());
				UAVHelper.notifyStateFine(logger, "Heading control: aileron. course=" + (int)Math.toDegrees(headingHold.getTargetValue()), "pilot-horizontal-mode");
				
			//fly to heading turning with rudder/elevator
			} else if(headingHold.getControlType().equals(HeadingControlType.RUDDER)) {
				pidControllers.unholdSetpoint(PIDControl.HOLD_COURSE_WITH_ROLL);
				pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON,0);
				pidControllers.holdSetpoint(PIDControl.HOLD_COURSE_WITH_YAW, headingHold.getTargetValue());
				UAVHelper.notifyStateFine(logger, "Heading control: rudder/elevator. course=" + (int)Math.toDegrees(headingHold.getTargetValue()), "pilot-horizontal-mode");

			//fly at constant roll
			} else if(headingHold.getControlType().equals(HeadingControlType.FIXED_ROLL)) {
				pidControllers.unholdSetpoint(PIDControl.HOLD_COURSE_WITH_ROLL);
				pidControllers.unholdSetpoint(PIDControl.HOLD_COURSE_WITH_YAW);
				pidControllers.unholdSetpoint(PIDControl.HOLD_YAW_WITH_ELEV_RUDDER);
				pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, headingHold.getTargetValue());
				UAVHelper.notifyStateFine(logger, "Heading control: roll. roll=" + (int)Math.toDegrees(headingHold.getTargetValue()), "pilot-horizontal-mode");
			}
		} else {
			pidControllers.unholdSetpoint(PIDControl.HOLD_COURSE_WITH_ROLL);
			pidControllers.unholdSetpoint(PIDControl.HOLD_COURSE_WITH_YAW);
			UAVHelper.notifyStateFine(logger, "Heading control: No heading set", "pilot-horizontal-mode");
		}
		
		
		//SPEED HOLD
		//choose manual/auto
		if(commander.isAutoSpeedControlsEnabled(sc, pc)) {
			speedHold.copyFrom(autoSpeedHold);
			UAVHelper.notifyStateFine(logger, "Speed control: configuration " + speedHold.isActive(), "speedhold");
		} else {
			speedHold.copyFrom(manualSpeedHold);
			UAVHelper.notifyStateFine(logger, "Speed control: manual override " + speedHold.isActive(), "speedhold");
		}
		//handle inactive controls
		if(!speedHold.isActive()) {
			if(commander.isAutopilotEngaged()) {
				//if this is called before any vehicle steering is received
				if(speedHold.getControlType()==null) {
					manualSpeedHold.set(false, gpsService.getGroundSpeed(), SpeedType.GROUND_SPEED, null);
					speedHold.copyFrom(manualSpeedHold);
				}
				speedHold.setActive(true);
			}
		}
		//activate pid controllers
		if(speedHold.isActive()) {
			if(speedHold.getTargetValueType().equals(SpeedType.GROUND_SPEED)) {
				pidControllers.holdSetpoint(PIDControl.HOLD_GROUNDSPEED_WITH_IAS, speedHold.getTargetValue());
				UAVHelper.notifyStateFine(logger, "Speed control: groundspeed. groundspeed=" + speedHold.getTargetValue(), "pilot-speed-mode");
			} else if(speedHold.getTargetValueType().equals(SpeedType.INDICATED_AIRSPEED)) {
				pidControllers.unholdSetpoint(PIDControl.HOLD_GROUNDSPEED_WITH_IAS);
				if(!isUseReverseVerticalControl()) {
					pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE, speedHold.getTargetValue());
					UAVHelper.notifyStateFine(logger, "Speed control: throttle from IAS. ias=" + speedHold.getTargetValue(), "pilot-speed-mode");
				} else {
					pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH, speedHold.getTargetValue());
					UAVHelper.notifyStateFine(logger, "Speed control: pitch from IAS. ias=" + speedHold.getTargetValue(), "pilot-speed-mode");
				}
			}
		} else {
			pidControllers.unholdSetpoint(PIDControl.HOLD_GROUNDSPEED_WITH_IAS);
			pidControllers.unholdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH);
			pidControllers.unholdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE);
//			pidControllers.unholdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEVATOR);
			UAVHelper.notifyStateFine(logger, "Speed control: No speed set", "pilot-speed-mode");
		}
	}

	public void onVehicleSteeringCommandUpdated() {
		//ALTITUDE CONTROL
		VehicleSteeringCommand vs = repositoryService.getVehicleSteeringCommand();
		if(vs.getAltitudeCommandType().equals(AltitudeCommandType.NO_VALID_ALTITUDE_COMMAND)) {
			manualAltitudeHold.set(false, advancedInstrumentsService.getAltitude(AltitudeType.AGL), AltitudeType.AGL, AltitudeControlType.DYNAMIC);
			System.out.println("STEERING NO ALTITUDE");
		} else if(vs.getAltitudeCommandType().equals(AltitudeCommandType.ALTITUDE)) {
			manualAltitudeHold.set(true, vs.getCommandedAltitude(), vs.getAltitudeType(), AltitudeControlType.DYNAMIC);
			System.out.println("STEERING ALTITUDE SET");
		} else {
			logger.warning("Unsupported 'altitude command type' received. type=" + vs.getAltitudeCommandType());
		}

		//HEADING CONTROL
		if(vs.getHeadingCommandType().equals(HeadingCommandType.NO_VALID_HEADING_COMMAND)) {
			manualHeadingHold.set(false, gpsService.getCourseHeading(), HeadingCommandType.COURSE, HeadingControlType.DYNAMIC);
			System.out.println("STEERING NO HEADING");
		} else if(vs.getHeadingCommandType().equals(HeadingCommandType.COURSE)) {
			manualHeadingHold.set(true, vs.getCommandedCourse(), HeadingCommandType.COURSE, HeadingControlType.DYNAMIC);
			System.out.println("STEERING COURSE HEADING");
		} else if(vs.getHeadingCommandType().equals(HeadingCommandType.ROLL)) {
			manualHeadingHold.set(true, vs.getCommandedRoll(), HeadingCommandType.ROLL, HeadingControlType.FIXED_ROLL);
			System.out.println("STEERING ROLL HEADING");
		} else {
			logger.warning("Unsupported 'heading command type' received. type=" + vs.getHeadingCommandType());
		}

		//SPEED CONTROL
		manualSpeedHold.set(true, vs.getCommandedSpeed(), vs.getSpeedType(), null);
	}

	
	public void activateManeuver(Maneuver m) {
		setEnableFlightHolds(true);
		unholdAll();
		currentManeuver = m;
		logger.info("Pilot: activated " + m.getName());
		try {
			currentManeuver.start();
		} catch (Exception e) {
			logger.throwing(null,null,e);
			throw new RuntimeException(e);
		}
	}
	public void deactivateCurrentManeuver() {
		if(currentManeuver!=null && currentManeuver.isRunning()) {
			try {
				currentManeuver.stop(true);
			} catch (Exception e) {
				logger.throwing(null,null,e);
				throw new RuntimeException(e);
			}
		}
	}
	public boolean isCurrentMeneuverOfType(Class<? extends Maneuver> class1) {
		if(currentManeuver!=null && (currentManeuver.getClass().equals(class1))) {
			return true;
		}
		return false;
	}

	
	protected void reloadVehicleConfiguration() {
		pidControllers.reloadVehicleConfiguration();
		actuatorsService.reloadVehicleConfiguration();
		instrumentsService.reloadVehicleConfiguration();
		advancedInstrumentsService.reloadVehicleConfiguration();
		float minIas = ((SkylightPIDWorker)pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_THROTTLE)).getMinKtias();
		verticalModeSpeed.setup(minIas*1.05F, minIas*1.20F, true);
		verticalModeSpeedError.setup(-minIas*0.25F, 0, true);
	}

	public void unholdAll() {
		deactivateCurrentManeuver();
		unholdAltitude();
		unholdCourseHeading();
		unholdSpeed();
		if(pidControllers!=null) {
			pidControllers.unholdAll();
		}
	}

	public Maneuver getCurrentManeuver() {
		return currentManeuver;
	}

	@Override
	public void onMessageReceived(Message message) {
		//M45
		if(message instanceof EngineCommand) {
			EngineCommand m = (EngineCommand)message;
			if(m.getEngineNumber()==1) {
				if(m.getEngineCommand().equals(EngineStatus.ENABLED_RUNNING)) {
					actuatorsService.setEngineIgnition(true);
				} else if(m.getEngineCommand().equals(EngineStatus.STARTED)) {
				} else if(m.getEngineCommand().equals(EngineStatus.STOPPED)) {
					actuatorsService.setEngineIgnition(false);
				}
			}
			
		//M2003
		} else if(message instanceof ServoActuationCommand) {
			ServoActuationCommand m = (ServoActuationCommand)message;
			System.out.println("COMMANDING " + m.getServo() + "=" + m.getCommandedSetpoint());
			if(m.getServo().equals(Servo.ELEVATOR)) {
				actuatorsService.setElevator(m.getCommandedSetpoint());
			} else if(m.getServo().equals(Servo.RUDDER)) {
				actuatorsService.setRudder(m.getCommandedSetpoint());
			} else if(m.getServo().equals(Servo.THROTTLE)) {
				actuatorsService.setThrottle(m.getCommandedSetpoint());
			} else if(m.getServo().equals(Servo.AILERON_RIGHT)) {
				actuatorsService.setAileron(m.getCommandedSetpoint());
			} else if(m.getServo().equals(Servo.AILERON_LEFT)) {
				actuatorsService.setAileron(-m.getCommandedSetpoint());
			} else if(m.getServo().equals(Servo.CAMERA_PAN)) {
				actuatorsService.setCameraOrientation(m.getCommandedSetpoint(), actuatorsService.getCameraElevation(), RotationReference.SERVO);
			} else if(m.getServo().equals(Servo.CAMERA_TILT)) {
				actuatorsService.setCameraOrientation(actuatorsService.getCameraAzimuth(), m.getCommandedSetpoint(), RotationReference.SERVO);
			} else if(m.getServo().equals(Servo.GENERIC_SERVO)) {
				actuatorsService.setGenericServo(m.getCommandedSetpoint());
			}

		//M2004
		} else if(message instanceof PIDControllerCommand) {
			PIDControllerCommand m = (PIDControllerCommand)message;
			System.out.println("SET SETPOINT FOR " + m.getPIDControl().getName() + "=" + m.getCommandedSetpoint());
			deactivateCurrentManeuver();
			if(Float.isNaN(m.getCommandedSetpoint())) {
				pidControllers.unholdSetpoint(m.getPIDControl());
			} else {
				//using reset makes hold ground speed control bad because it is integral and changing setpoint makes it reset
//				pidControllers.getPIDWorker(m.getPIDControl()).reset();
				pidControllers.holdSetpoint(m.getPIDControl(), m.getCommandedSetpoint());
			}

		//M2009
		} else if(message instanceof ServoConfiguration) {
			ServoConfiguration m = (ServoConfiguration)message;
			ServoConfiguration sc = repositoryService.getSkylightVehicleConfiguration().getServoConfiguration(m.getServo());
			sc.copyFrom(m);
			repositoryService.setSkylightVehicleConfiguration(repositoryService.getSkylightVehicleConfiguration());
			reloadVehicleConfiguration();

		//M2010
		} else if(message instanceof PIDConfiguration) {
			PIDConfiguration m = (PIDConfiguration)message;
			PIDConfiguration pc = repositoryService.getSkylightVehicleConfiguration().getPIDConfiguration(m.getPIDControl());
			pc.copyFrom(m);
			repositoryService.setSkylightVehicleConfiguration(repositoryService.getSkylightVehicleConfiguration());
			reloadVehicleConfiguration();
			logger.info("PID configuration for '"+ m.getPIDControl() +"' updated. PID="+ pc.getKp() + "," + pc.getKi() + "," + pc.getKd());
		}
	}

	@Override
	public boolean prepareScheduledMessage(Message message) {
		//2009 - Servo Configuration
		if(message instanceof ServoConfiguration) {
			//send one message per servo configuration
			for (ServoConfiguration sc : repositoryService.getSkylightVehicleConfiguration().getServoConfigurations().values()) {
				ServoConfiguration mc = messagingService.resolveMessageForSending(ServoConfiguration.class);
				mc.copyFrom(sc);
				messagingService.sendMessage(mc);
			}
			return false;

		//2010 - PID Configuration
		} else if(message instanceof PIDConfiguration) {
			//send one message per PID configuration
			for (PIDConfiguration pc : repositoryService.getSkylightVehicleConfiguration().getPidConfigurations().values()) {
				PIDConfiguration p = messagingService.resolveMessageForSending(PIDConfiguration.class);
				p.copyFrom(pc);
//				logger.finer("Sending PID configuration for '"+ p.getPIDControl() +"'. PID="+ pc.getKp() + "," + pc.getKi() + "," + pc.getKd());
				messagingService.sendMessage(p);
			}
			return false;
	
		//M2011 - PID Controller State
		} else if(message instanceof PIDControllerState) {
			for (PIDControl pc : PIDControl.values()) {
				PIDWorker w = pidControllers.getPIDWorker(pc);
				
				//report if it is active or if state has changed from last report
				if(w.isActive() || reportAllPidsTimer.checkTrue() || w.isActive()!=lastReportedPids.get(w.getPIDController().getPIDControl())) {
					lastReportedPids.put(w.getPIDController().getPIDControl(), w.isActive());
					PIDControllerState m = messagingService.resolveMessageForSending(PIDControllerState.class);
					//if this became a overhead, PIDController instances may be directly bound to the ControllerStateMessage so that no copy must be performed
					copyStateFromWorker(m, w);
					messagingService.sendMessage(m);
				}
				
			}
			return false;

		//M2012 - Servos State Message
		} else if(message instanceof ServosStateMessage) {
			ServosStateMessage m = (ServosStateMessage)message;
			m.setAileronLeftState(-actuatorsService.getAileron());
			m.setAileronRightState(actuatorsService.getAileron());
			m.setElevatorState(actuatorsService.getElevator());
			m.setRudderState(actuatorsService.getRudder());
			m.setThrottleState(actuatorsService.getThrottle());
			m.setGenericServoState(actuatorsService.getGenericServo());
			return true;
		}

		return false;
	}

	private static void copyStateFromWorker(PIDControllerState state, PIDWorker worker) {
		PIDController controller = worker.getPIDController();
		state.setPIDControl(controller.getPIDControl());
		state.setActive(worker.isActive());
		state.setDiferentialValue(controller.getLastDifferentialValue());
		state.setFeedbackValue(controller.getLastFeedbackValue());
		state.setIntegralValue(controller.getLastIntegralValue());
		state.setOutputValue(controller.getLastOutputValue());
		state.setProportionalValue(controller.getLastProportionalValue());
		state.setSetpointValue(controller.getSetpointValue());
	}

	public void killEngine(boolean setupGlide) {
		if (repositoryService.getSkylightVehicleConfiguration().isKillEngineEnabled()) {
			actuatorsService.setThrottle(0);
			actuatorsService.setEngineIgnition(false);
			unholdAll();
			if(setupGlide) {
				pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH, repositoryService.getSkylightVehicleConfiguration().getStallIndicatedAirspeed()*1.1F);
				pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, 0);
			}
			logger.info("Engine killed");
		} else {
			logger.info("] kill not permitted");
		}
	}

	public float calculateMinTurnRadius(float speed) {
		float r = MathHelper.getTurnRadius(speed, repositoryService.getSkylightVehicleConfiguration().getRollMax()*SkylightPIDWorker.LIMITS_MAX_FACTOR);
		r *= repositoryService.getSkylightVehicleConfiguration().getCalculatedVersusRealTurnFactor();
		return MathHelper.clamp(r, 35, 10000);
	}
	
	public boolean isUseReverseVerticalControl() {
//		return !verticalModeSpeed.isUpperRange() && !verticalModeSpeedError.isUpperRange();
		//FIXME disabled until solving transition problems
		return false;
	}

	private boolean isUseRudderForHeading() {
		return !horizontalMode.isUpperRange();
	}
	
	public float getAverageGroundSpeed() {
		return groundSpeedAverager.getAverage();
	}
	
	public void holdAltitude(AltitudeType altitudeType, float targetAltitude) {
		autoAltitudeHold.set(true, targetAltitude, altitudeType, AltitudeControlType.DYNAMIC);
	}
	public void holdCourseHeading(HeadingControlType headingControlType, float targetCourseHeading) {
		autoHeadingHold.set(true, targetCourseHeading, HeadingCommandType.COURSE, headingControlType);
	}
	public void holdSpeed(SpeedType speedType, float targetSpeed) {
		autoSpeedHold.set(true, targetSpeed, speedType, null);
	}
	public void unholdAltitude() {
		autoAltitudeHold.setActive(false);
	}
	public void unholdCourseHeading() {
		autoHeadingHold.setActive(false);
	}
	public void unholdSpeed() {
		autoSpeedHold.setActive(false);
	}
	
	public void setEnableFlightHolds(boolean enableFlightHolds) {
		this.enableFlightHolds = enableFlightHolds;
	}
	public boolean isEnableFlightHolds() {
		return enableFlightHolds;
	}
	
}
