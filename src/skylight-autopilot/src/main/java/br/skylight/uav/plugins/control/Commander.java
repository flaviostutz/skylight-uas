package br.skylight.uav.plugins.control;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Logger;

import br.skylight.commons.Alert;
import br.skylight.commons.AlertWrapper;
import br.skylight.commons.Coordinates;
import br.skylight.commons.SafetyAction;
import br.skylight.commons.ServoConfiguration;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.dli.Bitmapped;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.FlightTerminationState;
import br.skylight.commons.dli.enums.LoiterType;
import br.skylight.commons.dli.enums.ModeState;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.services.ScheduledMessageReporter;
import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.dli.vehicle.AirVehicleLights;
import br.skylight.commons.dli.vehicle.FlightTerminationCommand;
import br.skylight.commons.dli.vehicle.FlightTerminationModeReport;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.ModePreferenceCommand;
import br.skylight.commons.dli.vehicle.ModePreferenceReport;
import br.skylight.commons.dli.vehicle.VehicleConfigurationCommand;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleLightsState;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeReport;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.statemachine.StateMachine;
import br.skylight.commons.statemachine.StateMachineListener;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.control.pathmode.FlightPathMode;
import br.skylight.uav.plugins.control.pathmode.LoiterMode;
import br.skylight.uav.plugins.control.pathmode.ManualControlMode;
import br.skylight.uav.plugins.control.pathmode.NoMode;
import br.skylight.uav.plugins.control.pathmode.SafetyProceduresMode;
import br.skylight.uav.plugins.control.pathmode.WaypointMode;
import br.skylight.uav.plugins.messaging.MessageScheduler;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=Commander.class)
public class Commander extends ThreadWorker implements MessageListener, ScheduledMessageReporter {

	public static String UAV_VERSION = "3.0.40";
	private static final Logger logger = Logger.getLogger(Commander.class.getName());

	@ServiceInjection
	public GPSService gpsService;
	@ServiceInjection
	public RepositoryService repositoryService;
	@ServiceInjection
	public ActuatorsService actuatorsService;
	@ServiceInjection
	public InstrumentsService instrumentsService;
	@ServiceInjection
	public AdvancedInstrumentsService advancedInstrumentsService;
	@ServiceInjection
	public MessagingService messagingService;
	@ServiceInjection
	public MessageScheduler messageScheduler;
	@ServiceInjection
	public PluginManager pluginManager;
	@MemberInjection
	public Pilot pilot;
	@MemberInjection
	public FlightEngineer flightEngineer;
	@MemberInjection
	public MissionPlanCommandStateMachine missionPlanCommandStateMachine;

	private StateMachine<FlightPathControlMode,Object> modeStateMachine;
	private FlightPathControlMode lastNormalControlMode;
	private double startTime;
	private TimedBoolean alertsTimer = new TimedBoolean(200);

	public Commander() {
		super(30, 100, 2000);
	}
	
	public void onActivate() throws Exception {
		createModeStateMachine();
		
		//MESSAGE LISTENERS
		messagingService.setMessageListener(MessageType.M40, this);
		messagingService.setMessageListener(MessageType.M41, this);
		messagingService.setMessageListener(MessageType.M42, this);
		messagingService.setMessageListener(MessageType.M43, this);
		messagingService.setMessageListener(MessageType.M44, this);
		messagingService.setMessageListener(MessageType.M46, this);
		messagingService.setMessageListener(MessageType.M48, this);
		messagingService.setMessageListener(MessageType.M100, this);
		messagingService.setMessageListener(MessageType.M2000, this);

		//MESSAGE REPORTERS
		messageScheduler.setMessageReporter(MessageType.M20, this);
		messageScheduler.setMessageReporter(MessageType.M100, this);
		messageScheduler.setMessageReporter(MessageType.M106, this);
		messageScheduler.setMessageReporter(MessageType.M107, this);
		messageScheduler.setMessageReporter(MessageType.M108, this);
		messageScheduler.setMessageReporter(MessageType.M109, this);
		messageScheduler.setMessageReporter(MessageType.M110, this);
		messageScheduler.setMessageReporter(MessageType.M2000, this);
		
		//DEFINE INITIAL AUTOPILOT STATES
		//setup initial states after complete loading because some elements are optional at initialization (because of cyclic dependencies)
		pluginManager.executeAfterStartup(new Runnable() {
			public void run() {
				reloadVehicleConfiguration();
				flightEngineer.reloadMission();
				//put system in the state defined by repository service
				if(repositoryService.getAirVehicleLights()!=null) {
					onMessageReceived(repositoryService.getAirVehicleLights());
				}
				if(repositoryService.getVehicleSteeringCommand()!=null) {
					onMessageReceived(repositoryService.getVehicleSteeringCommand());
				}
				if(repositoryService.getModePreferenceCommand()!=null) {
					onMessageReceived(repositoryService.getModePreferenceCommand());
				}
				if(repositoryService.getLoiterConfiguration()!=null) {
					onMessageReceived(repositoryService.getLoiterConfiguration());
				}
				if(repositoryService.getControlMode()!=null) {
					System.out.println("INITIAL MODE="+repositoryService.getControlMode().getMode());
					changeControlMode(repositoryService.getControlMode().getMode(), repositoryService.getControlMode().getSafetyAction());
				} else {
					changeControlMode(FlightPathControlMode.NO_MODE, null);
				}
				startTime = System.currentTimeMillis()/1000.0;
			}			
		});
		
//		try {
//			//try to aggregate the build date info to software version
//			Date d = new Date(new File(Commander.class.getResource("br/skylight/uav/plugins/control/Commander.class").toURI()).lastModified());
//			UAV_VERSION += " (" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(d) + ")";
//		} catch (Exception e) {
//			logger.warning("Couldn't get build date. e=" + e.toString());
//			e.printStackTrace();
//		}
	}

	private StateMachine<FlightPathControlMode,Object> createModeStateMachine() {
		modeStateMachine = new StateMachine<FlightPathControlMode,Object>();

		NoMode noMode = new NoMode(this, pilot);
		LoiterMode loiterMode = new LoiterMode(this, pilot);
		WaypointMode waypointMode = new WaypointMode(this, pilot);
		SafetyProceduresMode safetyProceduresMode = new SafetyProceduresMode(this, pilot);
		ManualControlMode manualControlModeEngaged = new ManualControlMode(this, pilot);
		ManualControlMode manualControlModeNotEngaged = new ManualControlMode(this, pilot);
		
		pluginManager.manageObject(noMode);
		pluginManager.manageObject(loiterMode);
		pluginManager.manageObject(waypointMode);
		pluginManager.manageObject(safetyProceduresMode);
		pluginManager.manageObject(manualControlModeEngaged);
		pluginManager.manageObject(manualControlModeNotEngaged);
		
		modeStateMachine.addState(FlightPathControlMode.NO_MODE, 			noMode);
		modeStateMachine.addState(FlightPathControlMode.FLIGHT_DIRECTOR, 	manualControlModeNotEngaged);
		modeStateMachine.addState(FlightPathControlMode.LOITER, 			loiterMode);
		modeStateMachine.addState(FlightPathControlMode.WAYPOINT, 			waypointMode);
		modeStateMachine.addState(FlightPathControlMode.AUTOPILOT, 			manualControlModeEngaged);
		modeStateMachine.addState(FlightPathControlMode.SAFETY_PROCEDURES, 	safetyProceduresMode);
		modeStateMachine.setListener(new StateMachineListener<FlightPathControlMode>() {
			public void onStateChanged(FlightPathControlMode newState, FlightPathControlMode oldState) {
				logger.info("Mode changed to " + newState + " (from " + oldState + ")");
				if(!newState.equals(FlightPathControlMode.SAFETY_PROCEDURES)) {
					flightEngineer.deactivateAlert(Alert.SAFETY_PROCEDURE_ACTIVATED, "Entering mode " + newState);
				}
			}
		});
		return modeStateMachine;
	}

	public void reloadVehicleConfiguration() {
		//if this is null, use default values and never let it null
		VehicleConfigurationMessage vc = repositoryService.getVehicleConfiguration();
		if(vc==null) {
			vc = new VehicleConfigurationMessage();
			repositoryService.setVehicleConfiguration(vc);
		}
		
		//if this is null, use default values and never let it null
		SkylightVehicleConfigurationMessage svc = repositoryService.getSkylightVehicleConfiguration();
		if(svc==null) {
			svc = new SkylightVehicleConfigurationMessage();
			repositoryService.setSkylightVehicleConfiguration(svc);
		}

		pilot.reloadVehicleConfiguration();
		//flight engineer cannot reference Commander.getHomePosition() because of ciclyc dependency
		flightEngineer.setHomePosition(getHomePosition());
	}

	public void step() throws Exception {
		advancedInstrumentsService.step();
		flightEngineer.step();
		if(!instrumentsService.getInstrumentsInfos().isManualRemoteControl()) {
			//avoid performing safety actions while in manual RC control
			handleAlerts();
		}
		modeStateMachine.step();
		pilot.step();
		actuatorsService.step();
		if (!isReady()) {
			setReady(true);
		}
		missionPlanCommandStateMachine.step();
		repositoryService.step();
	}

	public void onMessageReceived(Message message) {
		//40 - vehicle configuration command
		if(message instanceof VehicleConfigurationCommand) {
			repositoryService.setVehicleConfigurationCommand((VehicleConfigurationCommand)message);
			System.out.println("VEHICLE CONFIG");
			
		//41 - loiter configuration
		} else if(message instanceof LoiterConfiguration) {
			System.out.println("RECV LOITER CONFIGURATION");
			repositoryService.setLoiterConfiguration((LoiterConfiguration)message);
			//update loiter configuration
			if(modeStateMachine.getCurrentState() instanceof LoiterMode) {
				((LoiterMode)modeStateMachine.getCurrentState()).updateLoiterConfiguration();
			}
			
		//42 - change vehicle operating mode
		} else if(message instanceof VehicleOperatingModeCommand) {
			VehicleOperatingModeCommand m = (VehicleOperatingModeCommand)message;
			System.out.println("RECV VEHICLE OPERATING COMMAND " + m.getSelectFlightPathControlMode());
			changeControlMode(m.getSelectFlightPathControlMode(), null);
			
		//43 - vehicle steering command
		} else if(message instanceof VehicleSteeringCommand) {
			System.out.println("RECV VEHICLE STEERING COMMAND");
			//store
			repositoryService.setVehicleSteeringCommand((VehicleSteeringCommand)message);
			//notify pilot for manual override commands handling
			pilot.onVehicleSteeringCommandUpdated();
			if(modeStateMachine.getCurrentState() instanceof WaypointMode) {
				//jump to another waypoint number
				((WaypointMode)modeStateMachine.getCurrentState()).startNavigationToWaypoint(repositoryService.getVehicleSteeringCommand().getCommandedWaypointNumber());
			} else if(modeStateMachine.getCurrentState() instanceof LoiterMode) {
				//update loiter center position
				((LoiterMode)modeStateMachine.getCurrentState()).updateLoiterConfiguration();
			}

		//44 - air vehicle lights
		} else if(message instanceof AirVehicleLights) {
			repositoryService.setAirVehicleLights((AirVehicleLights)message);
			Bitmapped lights = repositoryService.getAirVehicleLights().getSetLights();
			actuatorsService.setLightsState(lights.isBit(1), lights.isBit(3), lights.isBit(7));
			try {
				messageScheduler.sendScheduledMessage(MessageType.M107);
			} catch (IOException e) {
				logger.throwing(null,null,e);
			}

		//46 - flight termination command
		} else if(message instanceof FlightTerminationCommand) {
			FlightTerminationCommand ftc = (FlightTerminationCommand)message;
			if(ftc.getFlightTerminationState().equals(FlightTerminationState.EXECUTE_FT_SYSTEM)) {
				if(repositoryService.getFlightTerminationCommand()!=null && repositoryService.getFlightTerminationCommand().getFlightTerminationMode()==ftc.getFlightTerminationMode()
					&& (repositoryService.getFlightTerminationCommand().getFlightTerminationState().equals(FlightTerminationState.ARM_FT_SYSTEM) || repositoryService.getFlightTerminationCommand().getFlightTerminationState().equals(FlightTerminationState.EXECUTE_FT_SYSTEM))) {
					if(ftc.getFlightTerminationMode()==33) {
						logger.warning("INITIATING FLIGHT TERMINATION BY 'SPINNING VEHICLE TO THE GROUND'");
						startSafetyProcedures(SafetyAction.HARD_SPIN_TO_GROUND);
						
//						//FIXME REMOVE THIS LATER AND USE SAFETY PROCEDURES
//						pilot.unholdAll();
//						pilot.killEngine(false);
//						System.out.println("ACTIVATING FLIGHT TERMINATION ON ACTUATORS SERVICE!");
//						actuatorsService.setFlightTermination(true);
						
					} else if(ftc.getFlightTerminationMode()==22) {
						logger.warning("INITIATING FLIGHT TERMINATION BY 'LOITER WITH ROLL DESCENDING'");
						startSafetyProcedures(SafetyAction.LOITER_WITH_ROLL_DESCENDING);
					} else if(ftc.getFlightTerminationMode()==11) {
						logger.warning("INITIATING FLIGHT TERMINATION BY 'DEPLOYING PARACHUTE'");
						startSafetyProcedures(SafetyAction.DEPLOY_PARACHUTE);
					} else {
						logger.warning("INITIATING FLIGHT TERMINATION BY 'GOING HOME FOR MANUAL RECOVERY'");
						startSafetyProcedures(SafetyAction.GO_FOR_MANUAL_RECOVERY);
					}
				} else {
					logger.warning("Received a flight termination execute command but the system is not armed. Ignoring command. commanded mode=" + ftc.getFlightTerminationMode());
				}
			}
			repositoryService.setFlightTerminationCommand(ftc);
			try {
				messageScheduler.sendScheduledMessage(MessageType.M108);
			} catch (IOException e) {
				logger.throwing(null,null,e);
			}
	
		//48 - mode preference command
		} else if(message instanceof ModePreferenceCommand) {
			ModePreferenceCommand m = (ModePreferenceCommand)message;
			repositoryService.setModePreferenceCommand(m);
			System.out.println("RECV MODE PREFERENCE COMMAND " + m.getAltitudeMode() + " " + m.getSpeedMode() + " " + m.getCourseHeadingMode());
			try {
				messageScheduler.sendScheduledMessage(MessageType.M109);
			} catch (IOException e) {
				logger.throwing(null, null, e);
				e.printStackTrace();
			}

		//100 - vehicle configuration
		} else if(message instanceof VehicleConfigurationMessage) {
			VehicleConfigurationMessage vc = (VehicleConfigurationMessage)message;
			repositoryService.setVehicleConfiguration(vc);
			reloadVehicleConfiguration();
			
		//2000 - receive vehicle configuration
		} else if(message instanceof SkylightVehicleConfigurationMessage) {
			SkylightVehicleConfigurationMessage m = (SkylightVehicleConfigurationMessage)message;
			SkylightVehicleConfigurationMessage old = repositoryService.getSkylightVehicleConfiguration();
			if(old!=null) {
				m.getServoConfigurations().clear();
				for (ServoConfiguration sc : old.getServoConfigurations().values()) {
					m.addServoConfiguration(sc);
				}
				m.getPidConfigurations().clear();
				for (PIDConfiguration pc : old.getPidConfigurations().values()) {
					m.addPidConfiguration(pc);
				}
			}
			repositoryService.setSkylightVehicleConfiguration(m);
			reloadVehicleConfiguration();

		} else {
			logger.warning("Message " + message.getMessageType() + " unsupported");
		}
	}

	/**
	 * 'data' should be a SafetyAction enum and should be set only for the SAFETY_PROCEDURES mode
	 * @param mode
	 * @param data
	 */
	public void changeControlMode(FlightPathControlMode mode, SafetyAction safetyAction) {
		//avoid transitions to the same mode
		//this may happen for example, in GPSLinkRecovery because the maneuver waits for an specified time of link up until considering good link, but the alert will be activated/deactivated at each link up/down event
		if(mode.equals(modeStateMachine.getCurrentStateId()) && safetyAction!=null && safetyAction.equals(modeStateMachine.getCurrentStateData())) {
			return;
		}
		modeStateMachine.enterState(mode, safetyAction);

		//save mode
		if(safetyAction==null) {
			safetyAction = SafetyAction.DO_NOTHING;
		}
		if(!mode.equals(FlightPathControlMode.SAFETY_PROCEDURES)) {
			lastNormalControlMode = mode;
		}
		
		repositoryService.getControlMode().setMode(mode);
		repositoryService.getControlMode().setSafetyAction(safetyAction);
		repositoryService.setControlMode(repositoryService.getControlMode(), false);
		
		try {
			messageScheduler.sendScheduledMessage(MessageType.M106);
		} catch (IOException e) {
			logger.throwing(null,null,e);
			e.printStackTrace();
		}
	}
	
	public void changeVehicleControlMode(VehicleMode vehicleMode) {
		logger.info("Changing vehicle control mode to '" + vehicleMode + "'");
		
		if(vehicleMode.getMode()!=null) {
			changeControlMode(vehicleMode.getMode(), null);
			
		} else if(vehicleMode.equals(VehicleMode.LOITER_AROUND_POSITION_MODE)) {
			VehicleConfigurationMessage vc = repositoryService.getVehicleConfiguration();
			
			//adjust loiter configuration
			LoiterConfiguration lc = repositoryService.getLoiterConfiguration();
			Coordinates currentPosition = gpsService.getPosition();
			lc.setLoiterAltitude(Math.max(100,currentPosition.getAltitude()));//avoid loitering too near the ground
			lc.setAltitudeType(AltitudeType.AGL);
			//TODO put this in vehicle configuration (use the same attribute as manual recovery and data link recovery loiter radius)
			lc.setLoiterRadius(150);
			lc.setLoiterSpeed(vc.getOptimumEnduranceIndicatedAirspeed());
			lc.setSpeedType(SpeedType.INDICATED_AIRSPEED);
			lc.setLoiterType(LoiterType.CIRCULAR);
			
			//adjust vehicle steering command
			VehicleSteeringCommand vs = repositoryService.getVehicleSteeringCommand();
			vs.setLoiterPositionLatitude(currentPosition.getLatitudeRadians());
			vs.setLoiterPositionLongitude(currentPosition.getLongitudeRadians());
			repositoryService.setVehicleSteeringCommand(vs);
			
			//activate loiter mode
			changeControlMode(FlightPathControlMode.LOITER, null);
			
		} else if(vehicleMode.equals(VehicleMode.PREVIOUS_MODE)) {
			if(lastNormalControlMode!=null) {
				changeControlMode(lastNormalControlMode, null);
			} else {
				logger.info("No previous path control mode found. Using 'No mode'");
				changeControlMode(FlightPathControlMode.NO_MODE, null);
			}
		}
	}

	//HANDLE CURRENT FLIGHT ENGINEER ALERTS
	protected void handleAlerts() {
		if(alertsTimer.checkTrue() && getTimeActivated()>3) {
			for (Entry<Alert,AlertWrapper> alert : flightEngineer.getAlerts().entrySet()) {
				if(alert.getValue().isActive() 
					&& !alert.getValue().isHandled()) { 
//					&& isAutopilotEngaged()) {
//					&& !instrumentsService.getInstrumentsInfos().isManualRemoteControl()) {
					SafetyAction sa = flightEngineer.getActiveRulesOfSafety().getSafetyActionForAlert(alert.getKey());
					alert.getValue().setHandled(true);
					if(sa!=null && alert.getKey().isSafetyActionEnabled()) {
						if(!sa.equals(SafetyAction.DO_NOTHING)) {
							if(repositoryService.getSkylightVehicleConfiguration().isSafetyProceduresEnabled()
								&& repositoryService.getMiscStates().isSafetyActionsArmed()) {
								//avoid executing a safety action that has lower priority when another safety action is in progress
								if(repositoryService.getSkylightVehicleConfiguration().isValidateSafetyProceduresBeforeExecution() 
									&& modeStateMachine.getCurrentStateId()!=null && modeStateMachine.getCurrentStateId().equals(FlightPathControlMode.SAFETY_PROCEDURES)) {
									if(modeStateMachine.getCurrentStateData() instanceof SafetyAction) {
										SafetyAction currentAction = (SafetyAction)modeStateMachine.getCurrentStateData();
										//avoid switching to another safety action if engine is killed
										if(instrumentsService.getEngineRPM()<200) {
											logger.warning("Won't execute '" + sa + "' because engine is killed. current='"+ currentAction + "'");
											return;
										} else if(currentAction.getPriority()<sa.getPriority()) {
											logger.warning("Won't execute '" + sa + "' because it has lower priority. current='"+ currentAction + "'");
											return;
										}
									}
								}
								
								//some kind of HW notification may be performed
								actuatorsService.notifyAlertActivated(alert.getValue().getSubsystemStatusAlert());
								
								//activate safety action
								logger.info("Alert '" + alert.getKey() + "' was activated. Initiating safety action '"+ sa +"'");
								startSafetyProcedures(sa);
							} else {
								logger.info("Alert '" + alert.getKey() + "' was activated. Safety actions are not enabled in global configurations");
							}
						} else {
							logger.finer("Alert '" + alert.getKey() + "' was activated but no safety action is associated to it");
						}
					}
				}
			}
		}
	}

	public void startSafetyProcedures(SafetyAction safetyAction) {
		if(safetyAction.equals(SafetyAction.DO_NOTHING)) {
			return;
		}
		if(repositoryService.getSkylightVehicleConfiguration().isSafetyProceduresEnabled()
			&& repositoryService.getMiscStates().isSafetyActionsArmed()) {
			if(!repositoryService.getSkylightVehicleConfiguration().isValidateSafetyProceduresBeforeExecution()) {
				logger.info("Executing safety procedure '"+ safetyAction +"' without futher validations");
				changeControlMode(FlightPathControlMode.SAFETY_PROCEDURES, safetyAction);
			} else {
				logger.info("Validating current conditions before executing safety procedure '"+ safetyAction +"'");
				if(advancedInstrumentsService.getAltitude(AltitudeType.AGL) >= 3) {
					if(gpsService.getGroundSpeed() >= 2) {
						if(!instrumentsService.getInstrumentsInfos().isManualRemoteControl()) {
							changeControlMode(FlightPathControlMode.SAFETY_PROCEDURES, safetyAction);
						} else {
							logger.info("Will not execute safety procedures because vehicle is in manual control mode. Ignoring.");
						}
					} else {
						logger.info("Will not execute safety procedures because vehicle is too slow. Ignoring. groundspeed=" + gpsService.getGroundSpeed() + " m/s");
					}
				} else {
					logger.info("Will not execute configured safety procedures because vehicle is too low. Killing engine and holding level instead. altitude AGL=" + advancedInstrumentsService.getAltitude(AltitudeType.AGL) + " m");
					changeControlMode(FlightPathControlMode.SAFETY_PROCEDURES, SafetyAction.KILL_ENGINE_AND_HOLD_LEVEL);
				}
			}
		} else {
			logger.info("Won't perform safety procedure '" + safetyAction + "' because safety procedures are not enabled or armed. Ignoring.");
//			changeControlMode(FlightPathControlMode.NO_MODE, null);
		}
	}

	@Override
	public boolean prepareScheduledMessage(Message message) {
		//20 - vehicle id
		if(message instanceof VehicleID) {
			VehicleID m = (VehicleID)message;
			m.copyFrom(repositoryService.getSkylightVehicleConfiguration().getVehicleIdentification());
			
		//100 - vehicle configuration
		} else if(message instanceof VehicleConfigurationMessage) {
			VehicleConfigurationMessage target = (VehicleConfigurationMessage)message;
			VehicleConfigurationMessage source = repositoryService.getVehicleConfiguration();
			IOHelper.copyState(target, source);
			target.setTimeStamp(System.currentTimeMillis()/1000.0);
			
		//106 - vehicle operating mode report
		} else if(message instanceof VehicleOperatingModeReport) {
			VehicleOperatingModeReport m = (VehicleOperatingModeReport)message;
			m.setSelectFlightPathControlMode(modeStateMachine.getCurrentStateId());

		//107 - vehicle lights state
		} else if(message instanceof VehicleLightsState) {
			VehicleLightsState m = (VehicleLightsState)message;
			if(repositoryService.getAirVehicleLights()!=null) {
				m.getNavigationLightsState().setData(repositoryService.getAirVehicleLights().getSetLights().getData());
			} else {
				m.getNavigationLightsState().setData(0);
			}
			
		//108 - flight termination state
		} else if(message instanceof FlightTerminationModeReport) {
			FlightTerminationModeReport m = (FlightTerminationModeReport)message;
			FlightTerminationState fts = FlightTerminationState.RESET_FT_SYSTEM;
			int mode = 0;
			if(repositoryService.getFlightTerminationCommand()!=null) {
				fts = repositoryService.getFlightTerminationCommand().getFlightTerminationState();
				mode = repositoryService.getFlightTerminationCommand().getFlightTerminationMode();
			}
			m.setFlightTerminationState(fts);
			m.setFlightTerminationMode(mode);

		//109 - mode preference report
		} else if(message instanceof ModePreferenceReport) {
			ModePreferenceReport m = (ModePreferenceReport)message;
			m.setAltitudeModeState(repositoryService.getModePreferenceCommand().getAltitudeMode());
			m.setSpeedModeState(repositoryService.getModePreferenceCommand().getSpeedMode());
			m.setCourseHeadingModeState(repositoryService.getModePreferenceCommand().getCourseHeadingMode());
			
		//110 - from-to-next waypoint states
		} else if(message instanceof FromToNextWaypointStates) {
			FromToNextWaypointStates m = (FromToNextWaypointStates)message;
			if(getModeStateMachine().getCurrentState()!=null) {
				return ((FlightPathMode)getModeStateMachine().getCurrentState()).prepareFromToNextWaypointStates(m);
			} else {
				return false;
			}
			
		//2000 - skylight vehicle configuration
		} else if(message instanceof SkylightVehicleConfigurationMessage) {
			SkylightVehicleConfigurationMessage target = (SkylightVehicleConfigurationMessage)message;
			SkylightVehicleConfigurationMessage source = repositoryService.getSkylightVehicleConfiguration();
			IOHelper.copyState(target, source);
			target.setTimeStamp(System.currentTimeMillis()/1000.0);
		}
		
		return true;
	}

	public StateMachine<FlightPathControlMode, Object> getModeStateMachine() {
		return modeStateMachine;
	}
	
	public boolean isCurrentFlightPathMode(FlightPathControlMode mode) {
		return modeStateMachine.getCurrentStateId()!=null && modeStateMachine.getCurrentStateId().equals(mode);
	}
	
	public FlightPathControlMode getCurrentFlightPathMode() {
		return modeStateMachine.getCurrentStateId();
	}

	public boolean isAutoCourseControlsEnabled(VehicleSteeringCommand vehicleSteeringCommand, ModePreferenceCommand modePreferenceCommand) {
		//for Loiter mode, always return true
		return !isCurrentFlightPathMode(FlightPathControlMode.FLIGHT_DIRECTOR) &&  
					(isCurrentFlightPathMode(FlightPathControlMode.SAFETY_PROCEDURES) || 
					modePreferenceCommand==null || 
					vehicleSteeringCommand==null || 
					modePreferenceCommand.getCourseHeadingMode().equals(ModeState.CONFIGURATION) || 
					isCurrentFlightPathMode(FlightPathControlMode.LOITER));
	}

	public boolean isAutoAltitudeControlsEnabled(VehicleSteeringCommand vehicleSteeringCommand, ModePreferenceCommand modePreferenceCommand) {
		return !isCurrentFlightPathMode(FlightPathControlMode.FLIGHT_DIRECTOR) &&
					(isCurrentFlightPathMode(FlightPathControlMode.SAFETY_PROCEDURES)	||
					modePreferenceCommand==null || 
					vehicleSteeringCommand==null || 
					modePreferenceCommand.getAltitudeMode().equals(ModeState.CONFIGURATION));
	}
	public boolean isAutoAltitudeControlsEnabled() {
		return isAutoAltitudeControlsEnabled(repositoryService.getVehicleSteeringCommand(), repositoryService.getModePreferenceCommand());
	}

	public boolean isAutoSpeedControlsEnabled(VehicleSteeringCommand vehicleSteeringCommand, ModePreferenceCommand modePreferenceCommand) {
		return !isCurrentFlightPathMode(FlightPathControlMode.FLIGHT_DIRECTOR) &&
					(isCurrentFlightPathMode(FlightPathControlMode.SAFETY_PROCEDURES)	||
					modePreferenceCommand==null || 
					vehicleSteeringCommand==null || 
					modePreferenceCommand.getSpeedMode().equals(ModeState.CONFIGURATION));
	}
	public boolean isAutoSpeedControlsEnabled() {
		return isAutoSpeedControlsEnabled(repositoryService.getVehicleSteeringCommand(), repositoryService.getModePreferenceCommand());
	}
	
	public boolean isAutopilotEngaged() {
//		return true;//be careful using this option because the initial state (NO MODE) of autopilot may be inconsistent
		return getCurrentFlightPathMode()!=null && getCurrentFlightPathMode().ordinal()>=11 && getCurrentFlightPathMode().ordinal()<=31;
	}
	
	public double getTimeActivated() {
		return (System.currentTimeMillis()/1000.0) - startTime;
	}

	public Coordinates getHomePosition() {
		SkylightMission m = repositoryService.getSkylightMission();
		if(m!=null) {
			return m.getRulesOfSafety().getManualRecoveryLoiterLocation();
		} else if(repositoryService.getGroundLevelAltitudes()!=null) {
			return repositoryService.getGroundLevelAltitudes().getGroundLevelSetupPosition();
		} else {
			return gpsService.getPositionOnFirstFix();
		}
	}
	
}
