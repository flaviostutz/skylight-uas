package br.skylight.uav.plugins.storage;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import br.skylight.commons.MessagingConfiguration;
import br.skylight.commons.Mission;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.AirVehicleLights;
import br.skylight.commons.dli.vehicle.FlightTerminationCommand;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.ModePreferenceCommand;
import br.skylight.commons.dli.vehicle.VehicleConfigurationCommand;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.ExtendedSerializableState;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.watchdog.ResetState;
import br.skylight.commons.plugins.watchdog.WatchDogKickerService;
import br.skylight.commons.services.StorageService;
import br.skylight.uav.plugins.control.ControlMode;
import br.skylight.uav.plugins.control.instruments.GroundLevelAltitudes;
import br.skylight.uav.services.VehicleIdService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=RepositoryService.class)
/**
 * This class handles instances that defines autopilot's internal state and writes/loads
 * them to disk.
 * It will save states to disk only after a certain delay of time, so that if a problem
 * occurs due to the change in any of the states, the watchdog will be able 
 * to reset this VM before it commits the 'bad' state to disk, avoiding a subsequent reset.
 */
public class RepositoryService extends ThreadWorker {

	private static final Logger logger = Logger.getLogger(RepositoryService.class.getName());

	//use timer to avoid two things: 
	//     - too much disk IO for steering commands
	//     - saving a state that had caused an exception (in this case, before 5s the watchdog will reset this JVM and the corrupted state won't be saved)
	private Map<Class<? extends SerializableState>,TimedBoolean> saveTimers = new HashMap<Class<? extends SerializableState>,TimedBoolean>();
	private Map<Class<? extends SerializableState>,SerializableState> instances = new HashMap<Class<? extends SerializableState>,SerializableState>();
	
	private Properties configProperties;
	
	@ServiceInjection
	public StorageService storageService;

	@ServiceInjection
	public WatchDogKickerService watchDogKickerService;

	@ServiceInjection
	public VehicleIdService vehicleIdService;
	
	public RepositoryService() {
		super(1, 4000, 10000);
	}
	
	@Override
	public void onActivate() throws Exception {
		//load last states from disk
		setVehicleConfiguration(loadState(VehicleConfigurationMessage.class));
		setSkylightVehicleConfiguration(loadState(SkylightVehicleConfigurationMessage.class));
		setAirVehicleLights(loadState(AirVehicleLights.class));
		setControlMode(loadState(ControlMode.class), false);
		setFlightTerminationCommand(loadState(FlightTerminationCommand.class));
		setGroundLevelAltitudes(loadState(GroundLevelAltitudes.class));
		setLoiterConfiguration(loadState(LoiterConfiguration.class));
		setMessagingConfiguration(loadState(MessagingConfiguration.class));
		setMission(loadState(Mission.class));
		setModePreferenceCommand(loadState(ModePreferenceCommand.class));
		setSkylightMission(loadState(SkylightMission.class));
		setVehicleSteeringCommand(loadState(VehicleSteeringCommand.class));
		setVehicleConfigurationCommand(loadState(VehicleConfigurationCommand.class));
		setMiscStates(loadState(MiscStates.class));
		
		//load config properties file
		configProperties = new Properties();
		File f = storageService.resolveFile("uav-config.properties");
		if(f.exists()) {
			FileInputStream fis = new FileInputStream(f);
			configProperties.load(fis);
			fis.close();
		}
		
		//restore saved states from disk (only if we have good resets)
		if(watchDogKickerService.getLastResetState().equals(ResetState.SUCCESSFUL) 
			|| watchDogKickerService.getLastResetState().equals(ResetState.WAITING_STABILIZATION)) {
			logger.info("Restoring states from disk. lastResetState=" + watchDogKickerService.getLastResetState());
			setMission(loadState(Mission.class));
			setSkylightMission(loadState(SkylightMission.class));
			setFlightTerminationCommand(loadState(FlightTerminationCommand.class));
			setVehicleConfigurationCommand(loadState(VehicleConfigurationCommand.class));
			setLoiterConfiguration(loadState(LoiterConfiguration.class));
			setAirVehicleLights(loadState(AirVehicleLights.class));
			setVehicleSteeringCommand(loadState(VehicleSteeringCommand.class));
			setModePreferenceCommand(loadState(ModePreferenceCommand.class));
			setMessagingConfiguration(loadState(MessagingConfiguration.class));

		//if an previous reset was unsuccessful, avoid using last loaded states because they 
		//may have caused the errors that reseted the autopilot (by watchdog)
		} else {
			logger.warning("Only vehicle configurations will be restored from disk. Unsuccessful resets were detected. lastResetState=" + watchDogKickerService.getLastResetState());
		}
		
		//don't let these with 'null' values
		if(getModePreferenceCommand()==null) {
			setModePreferenceCommand(new ModePreferenceCommand());
		}
		if(getControlMode()==null) {
			ControlMode m = new ControlMode();
			m.setMode(FlightPathControlMode.NO_MODE);
			setControlMode(m, false);
		}
		if(getMessagingConfiguration()==null) {
			setMessagingConfiguration(new MessagingConfiguration());
		}
		if(getVehicleConfiguration()==null) {
			logger.warning("Vehicle configuration was not found in disk. Using default values.");
			setVehicleConfiguration(new VehicleConfigurationMessage());
		}
		if(getSkylightVehicleConfiguration()==null) {
			logger.warning("Skylight vehicle configuration was not found in disk. Using default values.");
			SkylightVehicleConfigurationMessage svc = new SkylightVehicleConfigurationMessage();
			svc.setVehicleIdentification(vehicleIdService.getInitialVehicleID());
			setSkylightVehicleConfiguration(svc);
		}
		if(getMiscStates()==null) {
			logger.warning("MiscStates was not found in disk. Using default values.");
			setMiscStates(new MiscStates());
		}
	}

	private <T extends SerializableState> T loadState(Class<T> stateClass) {
		try {
			return storageService.loadState("repository", stateClass.getSimpleName() + ".dat", stateClass);
		} catch (Exception e) {
			logger.warning("Error loading state from disk. e=" + e.toString());
			e.printStackTrace();
			return null;
		} finally {
			logger.info("Loaded state for " + stateClass.getSimpleName());
		}
	}

	@Override
	public void step() throws Exception {
		//look for states that should be saved to disk now
		for (Entry<Class<? extends SerializableState>,TimedBoolean> e : saveTimers.entrySet()) {
			try {
				if(e.getValue().isTimedOut() && e.getValue().isFirstTestAfterTimeOut()) {
					SerializableState instance = instances.get(e.getKey());
					if(instance!=null) {
						storageService.saveState(instance, "repository", e.getKey().getSimpleName()+".dat");
						logger.fine("Saved state " + e.getKey().getSimpleName() + " to disk");
					} else {
						File f = storageService.getFile(e.getKey().getSimpleName()+".dat");
						f.delete();
						logger.fine("Deleted state " + e.getKey().getSimpleName() + " from disk");
					}
				}
			} catch (Exception e1) {
				logger.warning("Error saving state "+ e.getKey().getSimpleName() +" to disk. e=" + e1.toString());
				e1.printStackTrace();
			}
		}
	}

	private <T extends SerializableState> T getValue(Class<T> stateClass) {
		return (T)instances.get(stateClass);
	}

	/**
	 * If value is null, delete state from disk
	 */
	private <T extends SerializableState> void setValue(T value, Class<T> stateClass) {
		if(value==null) {
			instances.remove(stateClass);
		} else { 
			//copy state to avoid problems with messaging instance pool
			SerializableState sv = value;
			if(value instanceof Message) {
				Message m = (Message)value;
				if(m instanceof ExtendedSerializableState) {
					ExtendedSerializableState em = (ExtendedSerializableState)m;
					sv = IOHelper.createCopyExtended(em);
				} else {
					sv = m.createCopy();
				}
			}
			instances.put(stateClass, sv);
		}
		scheduleSave(stateClass);
	}
	
	public void scheduleSave(Class stateClass) {
		resolveSaveTimer(stateClass).reset();
	}
	
	private TimedBoolean resolveSaveTimer(Class<? extends SerializableState> stateClass) {
		synchronized(saveTimers) {
			TimedBoolean tb = saveTimers.get(stateClass);
			if(tb==null) {
				tb = new TimedBoolean(6000);
				saveTimers.put(stateClass, tb);
			}
			return tb;
		}
	}

	
	public GroundLevelAltitudes getGroundLevelAltitudes() {
		return getValue(GroundLevelAltitudes.class);
	}

	public void setGroundLevelAltitudes(GroundLevelAltitudes groundLevelAltitudes) {
		setValue(groundLevelAltitudes, GroundLevelAltitudes.class);
	}

	
	public FlightTerminationCommand getFlightTerminationCommand() {
		return getValue(FlightTerminationCommand.class);
	}

	public void setFlightTerminationCommand(FlightTerminationCommand flightTerminationCommand) {
		setValue(flightTerminationCommand, FlightTerminationCommand.class);
	}

	public VehicleConfigurationCommand getVehicleConfigurationCommand() {
		return getValue(VehicleConfigurationCommand.class);
	}

	public void setVehicleConfigurationCommand(VehicleConfigurationCommand vehicleConfigurationCommand) {
		setValue(vehicleConfigurationCommand, VehicleConfigurationCommand.class);
	}

	public LoiterConfiguration getLoiterConfiguration() {
		return getValue(LoiterConfiguration.class);
	}

	public void setLoiterConfiguration(LoiterConfiguration loiterConfiguration) {
		setValue(loiterConfiguration, LoiterConfiguration.class);
	}

	public ModePreferenceCommand getModePreferenceCommand() {
		return getValue(ModePreferenceCommand.class);
	}

	public void setModePreferenceCommand(ModePreferenceCommand modePreferenceCommand) {
		setValue(modePreferenceCommand, ModePreferenceCommand.class);
	}

	public VehicleSteeringCommand getVehicleSteeringCommand() {
		return getValue(VehicleSteeringCommand.class);
	}

	public void setVehicleSteeringCommand(VehicleSteeringCommand vehicleSteeringCommand) {
		setValue(vehicleSteeringCommand, VehicleSteeringCommand.class);
	}

	public AirVehicleLights getAirVehicleLights() {
		return getValue(AirVehicleLights.class);
	}

	public void setAirVehicleLights(AirVehicleLights airVehicleLights) {
		setValue(airVehicleLights, AirVehicleLights.class);
	}

	public Mission getMission() {
		return getValue(Mission.class);
	}

	public void setMission(Mission mission) {
		if(mission!=null) {
			mission.computeWaypointsMap();
		}
		setValue(mission, Mission.class);
	}

	public SkylightMission getSkylightMission() {
		return getValue(SkylightMission.class);
	}

	public void setSkylightMission(SkylightMission skylightMission) {
		setValue(skylightMission, SkylightMission.class);
	}

	public VehicleConfigurationMessage getVehicleConfiguration() {
		return getValue(VehicleConfigurationMessage.class);
	}

	public void setVehicleConfiguration(VehicleConfigurationMessage vehicleConfiguration) {
		setValue(vehicleConfiguration, VehicleConfigurationMessage.class);
	}

	public SkylightVehicleConfigurationMessage getSkylightVehicleConfiguration() {
		return getValue(SkylightVehicleConfigurationMessage.class);
	}

	public void setSkylightVehicleConfiguration(SkylightVehicleConfigurationMessage skylightVehicleConfiguration) {
		setValue(skylightVehicleConfiguration, SkylightVehicleConfigurationMessage.class);
	}

	public MessagingConfiguration getMessagingConfiguration() {
		return getValue(MessagingConfiguration.class);
	}

	public void setMessagingConfiguration(MessagingConfiguration messagingConfiguration) {
		setValue(messagingConfiguration, MessagingConfiguration.class);
	}
	
	public MiscStates getMiscStates() {
		return getValue(MiscStates.class);
	}

	public void setMiscStates(MiscStates miscStates) {
		setValue(miscStates, MiscStates.class);
	}
	
	public ControlMode getControlMode() {
		return getValue(ControlMode.class);
	}
	
	public void setControlMode(ControlMode controlMode, boolean saveNow) {
		setValue(controlMode, ControlMode.class);
		if(saveNow) {
			resolveSaveTimer(ControlMode.class).forceTimeout();
		}
	}
	
	public void clearMission() {
		setValue(null, Mission.class);
		setValue(null, SkylightMission.class);
	}
	
	public Properties getConfigProperties() {
		return configProperties;
	}
	
}
