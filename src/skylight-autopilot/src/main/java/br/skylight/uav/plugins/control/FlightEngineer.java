package br.skylight.uav.plugins.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import traer.physics.Vector3D;
import br.skylight.commons.Alert;
import br.skylight.commons.AlertWrapper;
import br.skylight.commons.Coordinates;
import br.skylight.commons.JVMHelper;
import br.skylight.commons.Region;
import br.skylight.commons.RulesOfSafety;
import br.skylight.commons.SafetyAction;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.StringHelper;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.AltitudeCommandType;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.EnginePartStatus;
import br.skylight.commons.dli.enums.EngineStatus;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.HeadingCommandType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.enums.SubsystemState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.services.ScheduledMessageReporter;
import br.skylight.commons.dli.skylight.CommandType;
import br.skylight.commons.dli.skylight.GenericSystemCommand;
import br.skylight.commons.dli.skylight.MiscInfoMessage;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.skylight.SoftwarePartReport;
import br.skylight.commons.dli.skylight.SoftwareStatus;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusReport;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusRequest;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.EngineOperatingStates;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleOperatingStates;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.MeasureHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.PluginElement;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.watchdog.WatchDogKickerService;
import br.skylight.commons.services.StorageService;
import br.skylight.uav.infra.GPSUpdate.FixQuality;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.control.instruments.GroundLevelAltitudes;
import br.skylight.uav.plugins.control.maneuvers.Maneuver;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.workers.HoldAirspeedWithPitchWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldAirspeedWithThrottleWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldAltitudeWithPitchWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldAltitudeWithThrottleWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldCourseWithRollWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldCourseWithYawWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldGroundspeedWithAirspeedWorker;
import br.skylight.uav.plugins.messaging.MessageScheduler;
import br.skylight.uav.plugins.storage.MiscStates;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsListener;
import br.skylight.uav.services.InstrumentsService;

@ManagedMember
public class FlightEngineer extends Worker implements ScheduledMessageReporter, MessageListener, InstrumentsListener {

	private static final RulesOfSafety DEFAULT_RULES_OF_SAFETY = new RulesOfSafety();
	private static final Logger logger = Logger.getLogger(FlightEngineer.class.getName());

	private TimedBoolean linkQualityVerification = new TimedBoolean(500);
	private TimedBoolean limitsVerification = new TimedBoolean(1000);
	private TimedBoolean missionVerification = new TimedBoolean(1000);
	private TimedBoolean instrumentsVerification = new TimedBoolean(1000);
	private TimedBoolean hardwareResetVerification = new TimedBoolean(3500);
	private TimedBoolean autonomyVerification = new TimedBoolean(30000);
	private TimedBoolean softwareVerification = new TimedBoolean(3000);
	private TimedBoolean alertMessagesVerification = new TimedBoolean(1000);

	private Map<Subsystem, SubsystemState> subsystemStates = Collections.synchronizedMap(new HashMap<Subsystem, SubsystemState>());
	private Map<Alert, AlertWrapper> alerts = new HashMap<Alert, AlertWrapper>();

	private String alertMessage;
	
	private double flightStartTime = -1;
	private short hardwareResetCounter = 0;
	private boolean groundLevelSetAfterStartup;
	private Coordinates homePosition;
	
	private boolean shutdownArmed = false;
	
	private BufferedWriter instrumentsWriter;
	private TimedBoolean instrumentsWriterTimer;

	@ServiceInjection
	public InstrumentsService is;
	@ServiceInjection
	public StorageService ss;
	@ServiceInjection
	public AdvancedInstrumentsService ais;
	@ServiceInjection
	public MessagingService ms;
	@ServiceInjection
	public MessageScheduler messageScheduler;
	@ServiceInjection
	public RepositoryService rs;
	@ServiceInjection
	public GPSService gs;
	@ServiceInjection
	public PluginManager pluginManager;
	@MemberInjection
	public PIDControllers pidControllers;
	@ServiceInjection
	public ActuatorsService actuatorsService;
	@MemberInjection
	public Pilot pilot;
	@ServiceInjection
	public WatchDogKickerService watchDogKickerService;

	@Override
	public void onActivate() throws Exception {
		messageScheduler.setMessageReporter(MessageType.M101, this);
		messageScheduler.setMessageReporter(MessageType.M102, this);
		messageScheduler.setMessageReporter(MessageType.M104, this);
		messageScheduler.setMessageReporter(MessageType.M105, this);
		messageScheduler.setMessageReporter(MessageType.M501, this);
		messageScheduler.setMessageReporter(MessageType.M1100, this);
		messageScheduler.setMessageReporter(MessageType.M2005, this);
		messageScheduler.setMessageReporter(MessageType.M2013, this);
		messageScheduler.setMessageReporter(MessageType.M2018, this);
		ms.setMessageListener(MessageType.M1000, this);
		ms.setMessageListener(MessageType.M2017, this);

		// prepare alerts repository
		for (Alert a : Alert.values()) {
			SubsystemStatusAlert alert = new SubsystemStatusAlert();
			IOHelper.copyState(alert, a.getSubsystemStatusAlert());
			alerts.put(a, new AlertWrapper(alert, a.getTimeOnSituationForActivationMillis()));
		}

		// prepare subsystem states repository. show 'nominal' for subsystems
		// that may change state over time and 'no status' for unused subsystems
		for (Subsystem s : Subsystem.values()) {
			subsystemStates.put(s, SubsystemState.NO_STATUS);
		}
		updateSubsystemsStates();

		// handle logging messages and send them to operator if configured so
		Logger.getLogger("br.skylight").addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				SkylightVehicleConfigurationMessage c = rs.getSkylightVehicleConfiguration();
				if (c != null) {
					// send log message to operator
					if (record.getLevel().intValue() >= c.getOperatorLoggingLevel().intValue()) {
						if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
							activateAlert(Alert.SOFTWARE_ERROR, record.getMessage());
						} else if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
							activateAlert(Alert.SOFTWARE_WARNING, record.getMessage());
						} else if (record.getLevel().intValue() >= Level.INFO.intValue()) {
							activateAlert(Alert.SOFTWARE_INFO, record.getMessage());
						} else {
							activateAlert(Alert.SOFTWARE_FINE, record.getMessage());
						}
					}
				}
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() throws SecurityException {
			}
		});
		
		//arm failsafes if needed
		if(rs.getMiscStates().isArmFailsafesAtStartup()) {
			armAllFailsafes();
		}
	}

	@Override
	public void onMessageReceived(Message message) {
		// M1000
		if (message instanceof SubsystemStatusRequest) {
			SubsystemStatusRequest m = (SubsystemStatusRequest) message;
			SubsystemStatusReport sr = ms.resolveMessageForSending(SubsystemStatusReport.class);
			sr.setSubsystemID(m.getSubsystem());
			sr.setSubsystemState(subsystemStates.get(m.getSubsystem()));
			ms.sendMessage(sr);
			
		//2017 - Generic System Command
		} else if(message instanceof GenericSystemCommand) {
			GenericSystemCommand m = (GenericSystemCommand)message;
			if(m.getCommandType().equals(CommandType.CALIBRATE_ONBOARD_SYSTEMS)) {
				actuatorsService.performCalibrations();
				//only set onboardSystemsCalibratedAfterStartup to true when detected a true value in instrumentsService.isEFISCalibrated()

			} else if(m.getCommandType().equals(CommandType.SET_GROUND_LEVEL)) {
				GroundLevelAltitudes g = new GroundLevelAltitudes();
				if(g==null) {
					g = new GroundLevelAltitudes();
				}
				g.setAltitudeBarometric(ais.getAltitude(AltitudeType.BARO));
				g.setAltitudePressure(ais.getAltitude(AltitudeType.PRESSURE));
				g.setAltitudeGpsWGS84(ais.getAltitude(AltitudeType.WGS84));
				g.setAltitudeGpsMSL(gs.getAltitudeMSL());
				g.getGroundLevelSetupPosition().set(gs.getPosition());
				rs.setGroundLevelAltitudes(g);
				groundLevelSetAfterStartup = true;

			} else if(m.getCommandType().equals(CommandType.SYNCHRONIZE_VEHICLE_CLOCK)) {
				if(shutdownArmed) {
					try {
						JVMHelper.setDateTime((long)(m.getTimeStamp()*1000.0));
					} catch (IOException e) {
						e.printStackTrace();
						logger.info("Error setting clock. e=" + e.toString());
						logger.throwing(null,null,e);
					}
					//reboot onboard computer so that JVM will get the new clock
					rebootComputer();
				} else {
					shutdownArmed = true;
				}
			} else if(m.getCommandType().equals(CommandType.START_HIGH_FREQUENCY_RECORDING)) {
				try {
					if(instrumentsWriter==null) {
						if(m.getCommandValue1()>0) {
							instrumentsWriterTimer = new TimedBoolean((long)(1000F/m.getCommandValue1()));
						} else {
							instrumentsWriterTimer = null;//write data at highest frequency
						}
						File f = ss.resolveFile("instruments-data.csv");
						f.delete();//always replace previous session data
						instrumentsWriter = new BufferedWriter(new FileWriter(f));
						instrumentsWriter.write("Roll;Pitch;Yaw;Roll rate;Pitch rate;Yaw rate;Accel X;Accel Y;Accel Z;Pitot pressure;Static pressure;Onboard temp;CHT;Main batt;Aux batt;Engine RPM;Effective actuation frequency;Failures bitmap;Warnings bitmap;Infos bitmap\n");
						is.setInstrumentsListener(this);
						logger.info("Started recording instruments data at high frequency");
					} else {
						logger.info("High frequency instruments recording already started");
					}
				} catch (IOException e) {
					e.printStackTrace();
					logger.throwing(null,null,e);
					logger.warning("Couldn't start recording instruments at high frequency. e=" + e.toString());
				}
			} else if(m.getCommandType().equals(CommandType.STOP_HIGH_FREQUENCY_RECORDING)) {
				try {
					is.setInstrumentsListener(this);
					instrumentsWriter.close();
					instrumentsWriter = null;
				} catch (IOException e) {
					e.printStackTrace();
					logger.throwing(null,null,e);
					logger.warning("There was a problem while stopping recording instruments. e=" + e.toString());
				}
				
			} else if(m.getCommandType().equals(CommandType.ARM_FAILSAFES)) {
				armAllFailsafes();
			} else if(m.getCommandType().equals(CommandType.DISARM_FAILSAFES)) {
				disarmAllFailsafes();
			} else if(m.getCommandType().equals(CommandType.REBOOT_SYSTEMS)) {
				if(shutdownArmed) {
					rebootComputer();
				} else {
					logger.warning("Systems reboot was ARMED. Send this command again to confirm rebooting.");
					shutdownArmed = true;
				}
			} else if(m.getCommandType().equals(CommandType.SHUTDOWN_SYSTEMS)) {
				if(shutdownArmed) {
					logger.info(">>> SHUTTING DOWN ONBOARD COMPUTER");
					shutdownSystems();
					JVMHelper.shutdownOS();
				} else {
					logger.warning("Systems shutdown was ARMED. Send this command again to confirm shutdown.");
					shutdownArmed = true;
				}
//			} else if(m.getCommandType().equals(CommandType.REPLACE_DATALINK_BY_PPP)) {
//				if(activatePPPArmed) {
//					disarmAllFailsafes();
//					try {
//						//close modem port from autopilot (only for onboard uav)
//						if(is instanceof OnboardInstrumentsService) {
//							OnboardInstrumentsService ois = (OnboardInstrumentsService)is;
//							ois.getOnboardConnections().getModemConnectionParams().closeConnection();
//							logger.info("Modem connection for VSM communication was closed");
//						}
//						//launch linux ppp script
//						logger.info("Calling PPP script");
//						Runtime.getRuntime().exec("/Skylight/uav/start-ppp-winxp.sh");
//					} catch (IOException e) {
//						logger.throwing(null,null,e);
//						e.printStackTrace();
//					}
//				} else {
//					activatePPPArmed = true;
//				}
			}
		}
	}

	private void shutdownSystems() {
		disarmAllFailsafes();
		rs.getControlMode().setMode(FlightPathControlMode.NO_MODE);
		rs.setControlMode(rs.getControlMode(), true);
		pluginManager.shutdownAllPluginManagers();
	}

	private void rebootComputer() {
		logger.info(">>> REBOOTING ONBOARD COMPUTER");
		shutdownSystems();
		JVMHelper.rebootOS();
	}

	private void armAllFailsafes() {
		//arm software watchdog
		try {
			watchDogKickerService.activate();
			watchDogKickerService.setWatchDogEnabled(true);
			logger.info("Software watchdog ARMED");
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//arm mission rules of safety
		rs.getMiscStates().setSafetyActionsArmed(true);
		rs.scheduleSave(MiscStates.class);
		logger.info("Rules of Safety ARMED");
		//arm hardware failsafes
		actuatorsService.setFailSafesArmState(true);
		logger.info("Hardware failsafes ARMED");
		rs.getMiscStates().setArmFailsafesAtStartup(true);
		rs.scheduleSave(MiscStates.class);
	}
	private void disarmAllFailsafes() {
		//disarm hardware failsafes
		actuatorsService.setFlightTermination(false);//disable any ongoing flight termination state
		actuatorsService.setFailSafesArmState(false);
		logger.info("Hardware failsafes DISARMED");
		//disarm mission rules of safety
		rs.getMiscStates().setSafetyActionsArmed(false);
		rs.scheduleSave(MiscStates.class);
		logger.info("Rules of Safety DISARMED");
		//disarm software failsafes
		try {
			Thread.sleep(1000);
			watchDogKickerService.setWatchDogEnabled(false);
			watchDogKickerService.deactivate();
			logger.info("Software watchdog DISARMED");
		} catch (Exception e) {
			e.printStackTrace();
		}
		rs.getMiscStates().setArmFailsafesAtStartup(false);
		rs.scheduleSave(MiscStates.class);
	}

	protected void reloadMission() {
		//prepare safety actions according to ROS
		SkylightMission sm = rs.getSkylightMission();
		if(sm!=null) {
			//prepare data link recovery based on alert
			if(sm.getRulesOfSafety().isDataLinkRecoveryEnabled()) {
				sm.getRulesOfSafety().setSafetyActionForAlert(Alert.ADT_DOWNLINK_FAILED, SafetyAction.GO_FOR_DATA_LINK_RECOVERY);
			}
			//prepare gps link recovery based on alert
			if(sm.getRulesOfSafety().isGpsSignalRecoveryEnabled()) {
				sm.getRulesOfSafety().setSafetyActionForAlert(Alert.GPS_LINK_FAILED, SafetyAction.GO_FOR_GPS_LINK_RECOVERY);
			}
		}
	}

	public void activateAlert(Alert alert, String message) {
		alerts.get(alert).notifyActivationCondition(message);
		//update subsystems states on "verifyAlertsActivation()"
	}

	public void deactivateAlert(Alert alert, String message) {
		alerts.get(alert).notifyDeactivationCondition(message, ms);
		updateSubsystemsStates();
	}

	private void updateSubsystemsStates() {
		for (Subsystem s : Subsystem.values()) {
			// calculate overall subsystem status by analysing current alerts priority
			// set to 'no status' until it is known that there is an alert related to this subsystem
			SubsystemState worstState = SubsystemState.NO_STATUS;
			for (AlertWrapper a : alerts.values()) {
				if (a.getSubsystemStatusAlert().getSubsystemID().equals(s)) {
					if (a.isActive()) {
						if ((a.getSubsystemStatusAlert().getPriority().ordinal()) > worstState.ordinal()) {
							worstState = SubsystemState.values()[a.getSubsystemStatusAlert().getPriority().ordinal()];
						}
					}
					//there exists expected alerts for this subsystem. set it as nominal
					if (worstState.equals(SubsystemState.NO_STATUS)) {
						worstState = SubsystemState.NOMINAL;
					}
				}
			}

			// subsystem state level has changed
			if (!subsystemStates.get(s).equals(worstState)) {
				subsystemStates.put(s, worstState);

				// send new subsystem state to vsm
				SubsystemStatusReport sr = ms.resolveMessageForSending(SubsystemStatusReport.class);
				sr.setSubsystemID(s);
				sr.setSubsystemState(subsystemStates.get(s));
				ms.sendMessage(sr);
			}
		}
	}

	public void step() {
		verifyVehicleConfigurationLimits();
		verifyRulesOfSafetyLimits();
		verifyLinkQuality();
		verifyInstruments();
		verifySoftwareStability();
		stepAlertsActivation();
	}

	private void stepAlertsActivation() {
		if(alertMessagesVerification.checkTrue()) {
			boolean s = false;
			for (AlertWrapper a : alerts.values()) {
				if(a.isActive() && !a.isMessageSent()) {
					a.sendAlertMessage(ms);
					s = true;
				}
			}
			
			//if any alert state has changed, update subsystem states
			if(s) {
				updateSubsystemsStates();
			}
		}
	}

	private void verifyVehicleConfigurationLimits() {
		SkylightVehicleConfigurationMessage svc = rs.getSkylightVehicleConfiguration();
		VehicleConfigurationMessage vc = rs.getVehicleConfiguration();
		if (limitsVerification.checkTrue()) {
			if (vc == null)
				return;

			// STALL SPEED
			if (isFlying() && (getTimeFlying() > 0 && ais.getIAS() < svc.getStallIndicatedAirspeed())) {
				activateAlert(Alert.STALL_WARNING, "Indicated airspeed below stall speed. IAS=" + (long)ais.getIAS() + "m/s");
			} else {
				deactivateAlert(Alert.STALL_WARNING, "Indicated airspeed is normal. IAS=" + (long)ais.getIAS() + "m/s");
			}

			// IAS ABOVE LIMIT
			if (isFlying() && (ais.getIAS() > vc.getMaximumIndicatedAirspeed())) {
				activateAlert(Alert.IAS_ABOVE_LIMITS, "Indicated airspeed is above vehicle limits. IAS=" + (long)ais.getIAS() + "m/s");
			} else {
				deactivateAlert(Alert.IAS_ABOVE_LIMITS, "Indicated airspeed is normal. IAS=" + (long)ais.getIAS() + "m/s");
			}

			// ALTITUDE ABOVE LIMITS
			if (isFlying() && (ais.getAltitude(AltitudeType.AGL) > svc.getAltitudeMaxAGL())) {
				activateAlert(Alert.ALTITUDE_OUTSIDE_VEHICLE_LIMITS, "Vehicle altitude is too high (vehicle limit). altitude AGL=" + (long)ais.getAltitude(AltitudeType.AGL) + "m");
			} else {
				deactivateAlert(Alert.ALTITUDE_OUTSIDE_VEHICLE_LIMITS, "Vehicle altitude is normal (vehicle limit). altitude AGL=" + (long)ais.getAltitude(AltitudeType.AGL) + "m");
			}

			// MAX TIME FLYING LIMIT
			if (isFlying() && getTimeFlying() > MeasureHelper.minutesToSeconds(svc.getMaxFlightTimeMinutes())) {
				activateAlert(Alert.MAX_TIME_FLYING_REACHED_VEHICLE, "Vehicle max time of flight was reached. Time flying=" + StringHelper.formatElapsedTime(getTimeFlying()));
			} else {
				deactivateAlert(Alert.MAX_TIME_FLYING_REACHED_VEHICLE, "Normal flight time. Time flying=" + StringHelper.formatElapsedTime(getTimeFlying()));
			}
			
			// VEHICLE ATTITUDE LIMITS
			boolean dangerousAttitude = false;
			if (isFlying()) {
				//dangerous roll
				if(is.getRoll() < svc.getRollMin() || is.getRoll() > svc.getRollMax()) {
					activateAlert(Alert.DANGEROUS_VEHICLE_ATTITUDE, "Roll is outside safe limits. roll=" + (long)Math.toDegrees(is.getRoll()) + " degrees");
					dangerousAttitude = true;
				}
				//dangerous pitch
				if(is.getPitch() < svc.getPitchMin() || is.getPitch() > svc.getPitchMax()) {
					activateAlert(Alert.DANGEROUS_VEHICLE_ATTITUDE, "Pitch is outside safe limits. pitch=" + (long)Math.toDegrees(is.getPitch()) + " degrees");
					dangerousAttitude = true;
				}
			}
			if(!dangerousAttitude) {
				deactivateAlert(Alert.DANGEROUS_VEHICLE_ATTITUDE, "Vehicle attitude is normal. roll=" + (long)Math.toDegrees(is.getRoll()) + "; pitch="+(long)Math.toDegrees(is.getPitch()));
			}

			// INITIAL FUEL SET
			if (rs.getVehicleConfigurationCommand() == null) {
				activateAlert(Alert.FUEL_NOT_SET, "Initial propulsion fuel was not set yet");
			} else {
				double l = MeasureHelper.cubitMeterToLiters(svc.getFuelCapacityVolume()*(rs.getVehicleConfigurationCommand().getInitialPropulsionEnergy()/1000.0));
				deactivateAlert(Alert.FUEL_NOT_SET, "Propulsion energy set. initial volume=" + l + " litres (" + (int)rs.getVehicleConfigurationCommand().getInitialPropulsionEnergy() + "%)");
			}

		}
	}

	public double getTimeFlying() {
		if (flightStartTime == -1) {
			return 0;
		} else {
			return System.currentTimeMillis()/1000.0 - flightStartTime;
		}
	}

	public boolean isFlying() {
		return flightStartTime != -1;
	}

	protected void verifyRulesOfSafetyLimits() {
		if (missionVerification.checkTrue()) {
			VehicleConfigurationMessage vc = rs.getVehicleConfiguration();
			SkylightMission m = rs.getSkylightMission();

			if (m != null) {
				RulesOfSafety ros = m.getRulesOfSafety();

				// MAX TIME FLYING LIMIT
				if (getTimeFlying() > MeasureHelper.minutesToSeconds(ros.getMaxFlightTimeMinutes())) {
					activateAlert(Alert.MAX_TIME_FLYING_REACHED_MISSION, "Mission max time of flight was reached. Time flying=" + (long)MeasureHelper.secondsToMinutes(flightStartTime - System.currentTimeMillis()) + " min");
				} else {
					deactivateAlert(Alert.MAX_TIME_FLYING_REACHED_MISSION, "Normal mission flight time. Time flying=" + (long)MeasureHelper.secondsToMinutes(flightStartTime - System.currentTimeMillis()) + " min");
				}

				// ALTITUDE ABOVE LIMITS
				if (isFlying() && ais.getAltitude(ros.getMinMaxAltitudeType()) > ros.getMaxAltitude()) {
					activateAlert(Alert.VEHICLE_OUTSIDE_AUTHORIZED_ALTITUDE, "Vehicle altitude is too high (ROS). altitude " + ros.getMinMaxAltitudeType() + "=" + (long)ais.getAltitude(ros.getMinMaxAltitudeType()) + " m");
				} else if (isFlying() && ais.getAltitude(ros.getMinMaxAltitudeType()) < ros.getMinAltitude()) {
					activateAlert(Alert.VEHICLE_OUTSIDE_AUTHORIZED_ALTITUDE, "Vehicle altitude is too low (ROS). altitude " + ros.getMinMaxAltitudeType() + "=" + (long)ais.getAltitude(ros.getMinMaxAltitudeType()) + " m");
				} else {
					deactivateAlert(Alert.VEHICLE_OUTSIDE_AUTHORIZED_ALTITUDE, "Vehicle altitude is normal (ROS). altitude " + ros.getMinMaxAltitudeType() + "=" + (long)ais.getAltitude(ros.getMinMaxAltitudeType()) + " m");
				}

				// TIME WITHOUT INCOMING MESSAGES WARNING
				if (ms.getDataTerminal().getTimeSinceLastDownlinkActivity() > (ros.isDataLinkRecoveryEnabled()?ros.getDataLinkTimeout():10)) {
					activateAlert(Alert.ADT_DOWNLINK_FAILED, "ADT is not receiving packets from VSM. Time since last message=" + (long)ms.getDataTerminal().getTimeSinceLastPacketReceived() + " s");
				} else {
					deactivateAlert(Alert.ADT_DOWNLINK_FAILED, "ADT is receiving packets from VSM normally");
				}

				//verify these conditions only if GPS is OK
				if(!isAlertActive(Alert.GPS_LINK_FAILED) && !gs.getFixQuality().equals(FixQuality.INVALID)) {
					// MISSION  REGION
					if (ros.getAuthorizedRegion().isValidArea() && gs.getPosition().isValid()) {
						if (!ros.getAuthorizedRegion().isPointInside(gs.getPosition())) {
							activateAlert(Alert.VEHICLE_OUTSIDE_MISSION_BOUNDARIES, "Vehicle is outside mission  region");
						} else {
							deactivateAlert(Alert.VEHICLE_OUTSIDE_MISSION_BOUNDARIES, "Vehicle inside  region");
						}
					}
	
					// PROHIBITED REGIONS
					for (Region r : ros.getProhibitedRegions()) {
						if (r.isPointInside(gs.getPosition())) {
							activateAlert(Alert.VEHICLE_INSIDE_PROHIBITED_REGION, "Vehicle inside prohibited region");
							break;// avoid clearing the error due to not being
									// inside subsequent regions
						} else {
							deactivateAlert(Alert.VEHICLE_INSIDE_PROHIBITED_REGION, "Vehicle on permitted region");
						}
					}

					// RETURN TO HOME AUTONOMY
					if (autonomyVerification.checkTrue()) {
						if(homePosition!=null) {
							double currentAutonomyTime = getEstimatedAutonomyTime();
							//if -1, autonomy time could not be determined
							if(currentAutonomyTime!=-1) {
								double autonomyDistance = vc.getOptimumCruiseIndicatedAirspeed() * currentAutonomyTime;
								if (gs.getPosition().distance(homePosition) > (autonomyDistance * 0.8)) {
									activateAlert(Alert.INSUFICIENT_FUEL_TO_RETURN_HOME, "Reaching point of no return. Fuel may be insuficient. Estimated autonomy time: " + (long)(currentAutonomyTime / 60) + " min; Estimated autonomy distance: " + (long)(autonomyDistance / 1000) + " km; Distance to home: " + (long)(gs.getPosition().distance(homePosition)/1000) + " km");
								} else {
									deactivateAlert(Alert.INSUFICIENT_FUEL_TO_RETURN_HOME, "Enough fuel to return to home");
								}
							}
						}
					}
				}
					
			}
		}
	}

	protected void verifyLinkQuality() {
		if(linkQualityVerification.checkTrue()) {
			// GDT DOWNLINK SIGNAL STRENGTH
			if (ms.getDataTerminal().getUplinkStatus()!=-1) {
				if(ms.getDataTerminal().getUplinkStatus() < 30) {
					activateAlert(Alert.GDT_DOWNLINK_STRENGTH_WARNING, "Vehicle signal is too weak on VSM (GDT downlink). percent=" + ms.getDataTerminal().getUplinkStatus());
				} else {
					deactivateAlert(Alert.GDT_DOWNLINK_STRENGTH_WARNING, "Vehicle signal is normal on VSM. percent=" + ms.getDataTerminal().getUplinkStatus());
				}
			}

			// ADT DOWNLINK SIGNAL STRENGTH
			if (ms.getDataTerminal().getDownlinkStatus()!=-1) {
				if(ms.getDataTerminal().getDownlinkStatus() < 30) {
					activateAlert(Alert.ADT_DOWNLINK_STRENGTH_WARNING, "Ground station signal is too weak on vehicle (ADT downlink). percent=" + ms.getDataTerminal().getDownlinkStatus());
				} else {
					deactivateAlert(Alert.ADT_DOWNLINK_STRENGTH_WARNING, "Ground station signal is normal on vehicle. percent=" + ms.getDataTerminal().getDownlinkStatus());
				}
			}

			// ADT LINK LATENCY
			if (ms.getLastLatencyTime() > 5) {
				activateAlert(Alert.ADT_LATENCY_WARNING, "High link latency detected. latency=" + (long)ms.getLastLatencyTime() + " s");
			} else {
				deactivateAlert(Alert.ADT_LATENCY_WARNING, "Normal link latency. latency=" + (long)ms.getLastLatencyTime() + " s");
			}
		}
	}
	
	protected void verifyInstruments() {
		//flight determination
		if (flightStartTime == -1) {
			if (ais.getAltitude(AltitudeType.AGL) > 2.5F && ais.getIAS()>3) {
				flightStartTime = System.currentTimeMillis()/1000.0;
			}
		} else {
			if (ais.getAltitude(AltitudeType.AGL) < 1.5F) {
				flightStartTime = -1;
			}
		}
		
		//VERIFY IF ANY HARDWARE RESET WAS DETECTED
		if(hardwareResetVerification.checkTrue()) {
			if(is.getInstrumentsInfos().isHardwareReset()) {
				hardwareResetCounter++;
				activateAlert(Alert.HARDWARE_RESET_DETECTED, "Hardware reset detected");
			} else {
				deactivateAlert(Alert.HARDWARE_RESET_DETECTED, "No hardware reset detected");
			}
		}
		
		if (instrumentsVerification.checkTrue()) {
			// ZERO RPM DETECTION
			if (isFlying() && is.getEngineRPM() <= 300) {
				activateAlert(Alert.ZERO_RPM_DETECTED, "Zero RPM detected on engine. rpm=" + is.getEngineRPM());
			} else {
				deactivateAlert(Alert.ZERO_RPM_DETECTED, "Normal RPM on engine");
			}
			
			// ENGINE TEMPERATURE WARNING
			//TODO put this parameter in vehicle configuration
			if(is.getEngineCilinderTemperature()>100) {
				activateAlert(Alert.ENGINE_TEMPERATURE_TOO_HIGH, "Engine cilinder temperature is too high. t=" + (int)is.getEngineCilinderTemperature() + " °C");
			} else {
				deactivateAlert(Alert.ENGINE_TEMPERATURE_TOO_HIGH, "Normal engine cilinder temperature. t=" + (int)is.getEngineCilinderTemperature() + " °C");
			}

			// AUTOPILOT TEMPERATURE TOO HIGH
			if(is.getAutoPilotTemperature()>50) {
				activateAlert(Alert.AUTOPILOT_TEMPERATURE_TOO_HIGH, "Autopilot temperature is too high. t=" + (int)is.getAutoPilotTemperature() + " °C");
			} else {
				deactivateAlert(Alert.AUTOPILOT_TEMPERATURE_TOO_HIGH, "Normal autopilot temperature. t=" + (int)is.getAutoPilotTemperature() + " °C");
			}

			//HARDWARE IS RECEIVING MESSAGES IN TOO LOW FREQ
			if (is.getEffectiveActuatorsMessageFrequency()<15) {
				activateAlert(Alert.LOW_ACTUATOR_MESSAGES_FREQUENCY, "Low effective actuation messages frequency detected. freq=" + is.getEffectiveActuatorsMessageFrequency());
			} else {
				deactivateAlert(Alert.LOW_ACTUATOR_MESSAGES_FREQUENCY, "Normal effective actuation messages frequency. freq=" + is.getEffectiveActuatorsMessageFrequency());
			}
			
			//GPS LINK FAILURES
			String gpsFailed = null;
			SkylightMission sm = rs.getSkylightMission();
			double gpsDownForFailure = 5;
			if(sm!=null && sm.getRulesOfSafety().isGpsSignalRecoveryEnabled()) {
				gpsDownForFailure = sm.getRulesOfSafety().getGpsLinkTimeout();
			}
			if(((System.currentTimeMillis()/1000.0)-gs.getLastGPSUpdateTime())>gpsDownForFailure) {
				gpsFailed = "GPS link is down";
			} else if(gs.getFixQuality().equals(FixQuality.INVALID)) {
				gpsFailed = "GPS fix is INVALID";
			}
			if(gpsFailed!=null) {
				activateAlert(Alert.GPS_LINK_FAILED, gpsFailed);
			} else {
				deactivateAlert(Alert.GPS_LINK_FAILED, "GPS link OK");
			}

			//GPS LINK WARNINGS
			if(gs.getSatCount()<=6) {
				activateAlert(Alert.GPS_LINK_WARNING, "GPS with low satellite count. count=" + gs.getSatCount());
			} else {
				deactivateAlert(Alert.GPS_LINK_WARNING, "GPS sat count=" + gs.getSatCount());
			}
			
			if (is.getInstrumentsFailures().isIMUSensorsFailure()) {
				activateAlert(Alert.IMU_FAILED, "IMU failure detected");
			} else {
				deactivateAlert(Alert.IMU_FAILED, "IMU failure detected");
			}

			if (is.getInstrumentsWarnings().isHardwareReceivedMessageCRCError()) {
				activateAlert(Alert.HARDWARE_MESSAGE_CRC_ERROR, "Hardware has reported a CRC error for received messages");
			} else {
				deactivateAlert(Alert.HARDWARE_MESSAGE_CRC_ERROR, "Hardware has reported CRC OK for received messages");
			}

			if (is.getInstrumentsFailures().isStaticPressureSensorFailure()) {
				activateAlert(Alert.STATIC_PRESSURE_SENSOR_FAILED, "Static pressure sensor failed");
			} else {
				deactivateAlert(Alert.STATIC_PRESSURE_SENSOR_FAILED, "Static pressure sensor OK");
			}

			if (is.getInstrumentsFailures().isDynamicPressureSensorFailure()) {
				activateAlert(Alert.PITOT_PRESSURE_SENSOR_FAILED, "Pitot pressure sensor failed");
			} else {
				deactivateAlert(Alert.PITOT_PRESSURE_SENSOR_FAILED, "Pitot pressure sensor OK");
			}

			if (is.getInstrumentsFailures().isSystemReset()) {
				activateAlert(Alert.HARDWARE_SYSTEM_RESET_WARNING, "Hardware has reseted");
			} else {
				deactivateAlert(Alert.HARDWARE_SYSTEM_RESET_WARNING, "Hardware OK");
			}

			// SENSOR SATURATION
			alertMessage = null;
			if(is.getInstrumentsWarnings().isGyroscopesSaturation()) {
				alertMessage = "Gyroscope is saturated";
			} else if(is.getInstrumentsWarnings().isAccelerometersSaturation()) {
				alertMessage = "Accelerometer is saturated";
			} else if(is.getInstrumentsWarnings().isDynamicPressureSensorSaturation()) {
				alertMessage = "Pitot sensor is saturated";
			}
			if(alertMessage!=null) {
				activateAlert(Alert.SENSOR_SATURATION_WARNING, alertMessage);
			} else {
				deactivateAlert(Alert.SENSOR_SATURATION_WARNING, "Sensors not saturated");
			}

			//TODO put this parameter in vehicle configuration
			if(is.getMainBatteryLevel()<14.8F) {
				activateAlert(Alert.MAIN_BATTERY_WARNING, "Main battery level too low. level=" + is.getMainBatteryLevel() + " V");
			} else {
				deactivateAlert(Alert.MAIN_BATTERY_WARNING, "Main battery level is OK");
			}
			if(is.getAuxiliaryBatteryLevel()<5) {
				activateAlert(Alert.AUX_BATTERY_WARNING, "Auxiliary battery level is too low. level=" + is.getAuxiliaryBatteryLevel() + " V");
			} else {
				deactivateAlert(Alert.AUX_BATTERY_WARNING, "Aux battery level is OK");
			}
			
			//detect calibration execution in instruments gateway
			if(!is.getInstrumentsInfos().isCalibrationPerformed()) {
				activateAlert(Alert.ONBOARD_SYSTEMS_NOT_CALIBRATED, "Onboard systems were not calibrated after system startup");
			} else {
				deactivateAlert(Alert.ONBOARD_SYSTEMS_NOT_CALIBRATED, "Onboard systems are calibrated");
			}
			
			if(!groundLevelSetAfterStartup || rs.getGroundLevelAltitudes()==null) {
				activateAlert(Alert.GROUND_LEVEL_NOT_SET, "Ground level not set after system startup");
			} else {
				deactivateAlert(Alert.GROUND_LEVEL_NOT_SET, "Ground level was set to " + rs.getGroundLevelAltitudes().getAltitudeGpsWGS84() + " m (WGS84)");
			}

			if (is.getInstrumentsFailures().isOtherFailure()) {
				activateAlert(Alert.HARDWARE_GENERIC_FAILURE, "Hardware generic failure detected");
			} else {
				deactivateAlert(Alert.HARDWARE_GENERIC_FAILURE, "Hardware OK");
			}

			if (is.getInstrumentsWarnings().isOtherWarning()) {
				activateAlert(Alert.HARDWARE_GENERIC_WARNING, "Hardware generic warning detected");
			} else {
				deactivateAlert(Alert.HARDWARE_GENERIC_WARNING, "Hardware OK");
			}
			
			if(is.getInstrumentsFailures().isFlightTerminationActivated()) {
				activateAlert(Alert.FLIGHT_TERMINATION_HARDWARE_ACTIVATED, "Flight Termination hardware activated");
			} else {
				deactivateAlert(Alert.FLIGHT_TERMINATION_HARDWARE_ACTIVATED, "Flight Termination hardware NOT activated");
			}
		}
	}

	protected void verifySoftwareStability() {
		if (softwareVerification.checkTrue()) {
			
			boolean oneWarning = false;
			boolean oneError = false;
			for (PluginElement pe : pluginManager.getInitializedElements()) {
				if (pe.getElement() instanceof ThreadWorker) {
					
					ThreadWorker tw = (ThreadWorker) pe.getElement();
//					System.out.println("====" + tw + ": " + tw.getStepFrequencyAverage() + " Hz");
//					System.out.println(tw.getStackTrace(4, true));
					
					if (tw.isTimeAlert()) {
						activateAlert(Alert.SOFTWARE_WORKER_WARNING, "Thread '" + tw.toString() + "' is too slow. freq=" + (long)tw.getStepFrequencyAverage() + " Hz");
						oneWarning = true;
					} else if (tw.isTimeout()) {
						activateAlert(Alert.SOFTWARE_WORKER_ERROR, "Thread '" + tw.toString() + "' may be halted. time=" + (long)tw.getTimeSinceLastStep() + " ms; stack=" + tw.getStackTrace(2, true));
						oneError = true;
					}
				}
			}
			if (!oneWarning) {
				deactivateAlert(Alert.SOFTWARE_WORKER_WARNING, "No warnings found in active threads");
			}
			if (!oneError) {
				deactivateAlert(Alert.SOFTWARE_WORKER_ERROR, "No errors found in active threads");
			}

			if(ss.getBaseDir().getFreeSpace()<52428800) {//50MB
				activateAlert(Alert.LOW_DISK_SPACE, "Low disk space in vehicle computer. space=" + (ss.getBaseDir().getFreeSpace()/(1024*1024)) + " MB");
			} else {
				deactivateAlert(Alert.LOW_DISK_SPACE, "Normal disk space in vehicle");
			}
		}
	}

	@Override
	public boolean prepareScheduledMessage(Message message) {
		// 101
		if (message instanceof InertialStates) {
			InertialStates m = (InertialStates) message;
			m.setLatitude(gs.getPosition().getLatitudeRadians());
			m.setLongitude(gs.getPosition().getLongitudeRadians());
			m.setAltitude(ais.getAltitude(AltitudeType.AGL));
			m.setAltitudeType(AltitudeType.AGL);
			m.setUSpeed(CoordinatesHelper.getUComponent(gs.getCourseHeading(), gs.getGroundSpeed()));
			m.setVSpeed(CoordinatesHelper.getVComponent(gs.getCourseHeading(), gs.getGroundSpeed()));
			m.setWSpeed(ais.getVerticalSpeed());
			m.setPhi(is.getRoll());
			m.setPhiDot(is.getRollRate());
			m.setPsi(is.getYaw());
			m.setPsiDot(is.getYawRate());
			m.setTheta(is.getPitch());
			m.setThetaDot(is.getPitchRate());
			m.setMagneticVariation(ais.getMagneticDeclination());

		// 102
		} else if (message instanceof AirAndGroundRelativeStates) {
			Vector3D speed = ais.getSpeed();
			Vector3D windSpeed = ais.getWindSpeed();
			AirAndGroundRelativeStates m = (AirAndGroundRelativeStates) message;
			m.setAglAltitude(ais.getAltitude(AltitudeType.AGL));
			m.setAngleOfAttack(is.getPitch());
			m.setBarometricAltitude(ais.getAltitude(AltitudeType.BARO));
			m.setIndicatedAirspeed(ais.getIAS());
			m.setPressureAltitude(ais.getAltitude(AltitudeType.PRESSURE));
			m.setUGround(speed.x());
			m.setVGround(speed.y());
			m.setWgs84Altitude(ais.getAltitude(AltitudeType.WGS84));
			m.setUWind(windSpeed.x());
			m.setVWind(windSpeed.y());
			m.setAltimeterSetting(ais.getPressureAtSeaLevel());
			m.setAngleOfSideslip(gs.getCourseHeading() - is.getYaw());
			m.setTrueAirspeed(ais.getTAS());

		// 104
		} else if (message instanceof VehicleOperatingStates) {
			VehicleOperatingStates m = (VehicleOperatingStates) message;

			// commanded altitude
			HoldAltitudeWithPitchWorker aw1 = (HoldAltitudeWithPitchWorker) pidControllers.getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_PITCH);
			if (aw1.isActive()) {
				m.setAltitudeCommandType(AltitudeCommandType.ALTITUDE);
				m.setAltitudeType(aw1.getAltitudeType());
				m.setCommandedAltitude(aw1.getPIDController().getSetpointValue());
			} else {
				HoldAltitudeWithThrottleWorker aw2 = (HoldAltitudeWithThrottleWorker) pidControllers.getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE);
				if (aw2.isActive()) {
					m.setAltitudeCommandType(AltitudeCommandType.ALTITUDE);
					m.setAltitudeType(aw2.getAltitudeType());
					m.setCommandedAltitude(aw2.getPIDController().getSetpointValue());
				}
			}

			// commanded course heading
			HoldCourseWithRollWorker aw3 = (HoldCourseWithRollWorker) pidControllers.getPIDWorker(PIDControl.HOLD_COURSE_WITH_ROLL);
			if (aw3.isActive()) {
				m.setHeadingCommandType(HeadingCommandType.COURSE);
				m.setCommandedCourse(aw3.getPIDController().getSetpointValue());
			} else {
				HoldCourseWithYawWorker aw4 = (HoldCourseWithYawWorker) pidControllers.getPIDWorker(PIDControl.HOLD_COURSE_WITH_YAW);
				if (aw4.isActive()) {
					m.setHeadingCommandType(HeadingCommandType.COURSE);
					m.setCommandedCourse(aw4.getPIDController().getSetpointValue());
				}
			}

			// commanded speed
			HoldGroundspeedWithAirspeedWorker aw5 = (HoldGroundspeedWithAirspeedWorker) pidControllers.getPIDWorker(PIDControl.HOLD_GROUNDSPEED_WITH_IAS);
			if (aw5.isActive()) {
				m.setCommandedSpeed(aw5.getPIDController().getSetpointValue());
				m.setSpeedType(SpeedType.GROUND_SPEED);
			} else {
				HoldAirspeedWithPitchWorker aw6 = (HoldAirspeedWithPitchWorker) pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_PITCH);
				if (aw6.isActive()) {
					m.setCommandedSpeed(aw6.getPIDController().getSetpointValue());
					m.setSpeedType(SpeedType.INDICATED_AIRSPEED);
				} else {
					HoldAirspeedWithThrottleWorker aw7 = (HoldAirspeedWithThrottleWorker) pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_THROTTLE);
					if (aw7.isActive()) {
						m.setCommandedSpeed(aw7.getPIDController().getSetpointValue());
						m.setSpeedType(SpeedType.INDICATED_AIRSPEED);
					}
				}
			}

			// misc
			m.setPowerLevel((int) Math.round((actuatorsService.getThrottle() / 255F) * 110F));
			// TODO implement fuel level

		// 105
		} else if (message instanceof EngineOperatingStates) {
			EngineOperatingStates m = (EngineOperatingStates) message;
			m.setEngineNumber(1);
			if(is.getEngineRPM()>500) {
				m.setEngineStatus(EngineStatus.STARTED);
			} else if(actuatorsService.isEngineIgnitionEnabled()) {
				m.setEngineStatus(EngineStatus.ENABLED_RUNNING);
			} else {
				m.setEngineStatus(EngineStatus.STOPPED);
			}
			m.setEngineBodyTemperatureStatus(calculateEnginePartStatus(is.getEngineCilinderTemperature(), 10, 90, 130));
			m.setEnginePowerSetting(actuatorsService.getThrottle()/1.27F);
			m.setEngineSpeed((float)MathHelper.TWO_PI*(is.getEngineRPM()/60F));
			m.setEngineSpeedStatus(calculateEnginePartStatus(is.getEngineRPM(), 800, 5000, 15000));
			m.setCoolantTemperatureStatus(EnginePartStatus.NO_STATUS);
			m.setExhaustGasTemperatureStatus(EnginePartStatus.NO_STATUS);
			m.setFireDetectionSensorStatus(EnginePartStatus.NO_STATUS);
			m.setLubricantPressureStatus(EnginePartStatus.NO_STATUS);
			m.setLubricantTemperatureStatus(EnginePartStatus.NO_STATUS);
			
		// 501
		} else if (message instanceof DataLinkStatusReport) {
			DataLinkStatusReport m = (DataLinkStatusReport) message;
			IOHelper.copyState(m, ms.getDataTerminal().getDataLinkStatusReport());
			m.setTimeStamp(System.currentTimeMillis()/1000.0);

		// 1100
		} else if (message instanceof SubsystemStatusAlert) {
			SubsystemStatusAlert m = (SubsystemStatusAlert) message;
			for (AlertWrapper aw : alerts.values()) {
				if(aw.isActive()) {
					aw.sendAlertMessage(ms);
				}
			}
			return false;
			
		// M2005
		} else if (message instanceof MiscInfoMessage) {
			MiscInfoMessage m = (MiscInfoMessage) message;
			Maneuver mn = pilot.getCurrentManeuver();
			if (mn != null && mn.isRunning()) {
				m.setCurrentTargetAltitude(mn.getReferencePosition().getAltitude());
				m.setCurrentTargetLatitude(mn.getReferencePosition().getLatitudeRadians());
				m.setCurrentTargetLongitude(mn.getReferencePosition().getLongitudeRadians());
			}
			m.setLinkLatencyTime(ms.getLastLatencyTime());
			m.setBattery1Voltage(is.getMainBatteryLevel());
			m.setBattery2Voltage(is.getAuxiliaryBatteryLevel());
			m.setChtTemperature((short)is.getEngineCilinderTemperature());
			m.setOnboardTemperature((short)is.getAutoPilotTemperature());
			m.setManualRCControl(is.getInstrumentsInfos().isManualRemoteControl());
			m.setDataTerminalTransmitErrors(ms.getDataTerminal().getTxErrors());
			m.setNumberOfHardwareResets(hardwareResetCounter);
			m.setNumberOfSkippedHardwareMessages(is.getDiscardedMessagesCounter());
			m.setAdtPacketsSentAPCounter((int)ms.getDataTerminal().getTotalPacketsSent());
			m.setAdtPacketsSentModemCounter(ms.getDataTerminal().getPacketsSentAndGotByModem());
			
		// M2013
		} else if (message instanceof SoftwareStatus) {
			SoftwareStatus m = (SoftwareStatus) message;
			//CRCs
			if(rs.getMission()!=null) {
				m.setMissionCRC(IOHelper.calculateCRC(rs.getMission()));
			} else {
				m.setMissionCRC(-1);
			}
			m.setSkylightVehicleConfigurationCRC(IOHelper.calculateCRC(rs.getSkylightVehicleConfiguration()));
			m.setVehicleConfigurationCRC(IOHelper.calculateCRC(rs.getVehicleConfiguration()));
			m.setSoftwareVersion(Commander.UAV_VERSION);
			
		// M2018
		} else if (message instanceof SoftwarePartReport) {
			//send report for each software part
			for (PluginElement pe : pluginManager.getInitializedElements()) {
				if (pe.getElement() instanceof ThreadWorker) {
					ThreadWorker tw = (ThreadWorker) pe.getElement();
					SoftwarePartReport s = ms.resolveMessageForSending(SoftwarePartReport.class);
					s.setActive(tw.isActive());
					s.setAlert(tw.isTimeAlert());
					s.setTimeout(tw.isTimeout());
					s.setAverageFrequency(tw.getStepFrequencyAverage());
					s.setExceptionCount(tw.getExceptionCount());
					s.setTimeSinceLastStepMillis(tw.getTimeSinceLastStep());
					s.setName(tw.toString());
					if(tw.getWorkerThread()!=null && tw.isActive()) {
						s.setStackFragment(tw.getStackTrace(5, true));
					} else if(tw.getLastException()!=null) {
						s.setStackFragment(tw.getLastException().toString() + "\n" + tw.getLastExceptionStackTrace(5, true));
					}
					ms.sendMessage(s);
				}
			}
			return false;
		}

		return true;
	}

	private EnginePartStatus calculateEnginePartStatus(float realValue, float lowestValue, float idealValue, float highestValue) {
		//calculate how strong (0-1) the value is near ideal value
		//and distribute status equally based on ideal-real error
		if(realValue<=idealValue) {
			float v = MathHelper.clamp(1-(idealValue-realValue)/(idealValue-lowestValue), 0, 1);
			return EnginePartStatus.values()[(int)Math.floor(1F+(v/0.29F))];
		} else {
			float v = MathHelper.clamp(1-(realValue-idealValue)/(highestValue-idealValue), 0, 1);
			return EnginePartStatus.values()[(int)Math.floor(1F+(((1-v)+1)/0.29F))];
		}
	}

	public double getEstimatedAutonomyTime() {
		if (rs.getVehicleConfigurationCommand() != null) {
			if(rs.getVehicleConfigurationCommand().getInitialPropulsionEnergy()==0) {
				return -1;
			}
			float fuelCapacityVolume = rs.getSkylightVehicleConfiguration().getFuelCapacityVolume();
			float currentFuel = ((rs.getVehicleConfigurationCommand().getInitialPropulsionEnergy()/100F) * fuelCapacityVolume) - actuatorsService.getConsumedFuel();
			if (actuatorsService.getConsumedFuel() > 0) {
				return (int) ((getTimeFlying() * currentFuel) / actuatorsService.getConsumedFuel());
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	public RulesOfSafety getActiveRulesOfSafety() {
		SkylightMission m = rs.getSkylightMission();
		if (m != null) {
			return m.getRulesOfSafety();
		} else {
			return DEFAULT_RULES_OF_SAFETY;
		}
	}

	public Map<Alert, AlertWrapper> getAlerts() {
		return alerts;
	}

	public boolean isAlertActive(Alert alert) {
		return alerts.get(alert).isActive();
	}
	
	public void setHomePosition(Coordinates homePosition) {
		this.homePosition = homePosition;
	}

	@Override
	public void onInstrumentsDataUpdated() {
		// Write instruments data in csv format.
		// The order of columns is in the same order as instruments interface.
		// Bitmapped values are shown as unsigned byte in hexadecimal (0x00 - 0xFF).
		// It is expected that the csv file gets the realtime instruments data read by autopilot (thus in high frequency)
		if(instrumentsWriter!=null && (instrumentsWriterTimer==null || instrumentsWriterTimer.checkTrue())) {
			try {
				instrumentsWriter.write(
					is.getRoll() + ";" + 
					is.getPitch() + ";" +
					is.getYaw() + ";" +
					is.getRollRate() + ";" +
					is.getPitchRate() + ";" +
					is.getYawRate() + ";" +
					is.getAccelerationX() + ";" +
					is.getAccelerationY() + ";" +
					is.getAccelerationZ() + ";" +
					is.getPitotPressure() + ";" +
					is.getStaticPressure() + ";" +
					is.getAutoPilotTemperature() + ";" +
					is.getEngineCilinderTemperature() + ";" +
					is.getMainBatteryLevel() + ";" +
					is.getAuxiliaryBatteryLevel() + ";" +
					is.getEngineRPM() + ";" +
					is.getEffectiveActuatorsMessageFrequency() + ";" +
					IOHelper.byteToHex((byte)is.getInstrumentsFailures().getData()) + ";" +
					IOHelper.byteToHex((byte)is.getInstrumentsWarnings().getData()) + ";" +
					IOHelper.byteToHex((byte)is.getInstrumentsInfos().getData()));
				instrumentsWriter.newLine();
			} catch (IOException e) {
				try {
					instrumentsWriter.write("ERROR: " + e.toString());
				} catch (IOException e1) {
					e1.printStackTrace();
					logger.finer("Error writing log file. e=" + e.toString());
				}
			}
		}
	}

}
