package br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.EventType;
import br.skylight.commons.Mission;
import br.skylight.commons.RulesOfSafety;
import br.skylight.commons.ServoConfiguration;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.StringHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.skylight.MissionAnnotationsMessage;
import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.skylight.TakeoffLandingConfiguration;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=SkylightVehicleControlService.class)
public class SkylightVehicleControlService extends Worker implements MessageListener {

	private static final Logger logger = Logger.getLogger(SkylightVehicleControlService.class.getName());
	private Map<Integer,SkylightVehicle> knownSkylightVehicles = new HashMap<Integer,SkylightVehicle>();
	
	@ServiceInjection
	public SubscriberService subscriberService;
	
	@ServiceInjection
	public StorageService storageService;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@Override
	public void onActivate() throws Exception {
		//vehicle info/configuration messages
		subscriberService.addMessageListener(MessageType.M2000, this);
		subscriberService.addMessageListener(MessageType.M2005, this);
		subscriberService.addMessageListener(MessageType.M2009, this);
		subscriberService.addMessageListener(MessageType.M2010, this);
		subscriberService.addMessageListener(MessageType.M2013, this);
		
		//mission download messages
		subscriberService.addMessageListener(MessageType.M2006, this);
		subscriberService.addMessageListener(MessageType.M2007, this);
		subscriberService.addMessageListener(MessageType.M2016, this);
	}

	@Override
	public void onDeactivate() throws Exception {
		//save known skylight vehicles
		for (SkylightVehicle v : knownSkylightVehicles.values()) {
			logger.info("Saving skylight vehicle '"+ StringHelper.formatId(v.getVehicleID()) +"'");
			storageService.saveState(v, "vehicles", getSkylightVehicleFileName(v.getVehicleID()));
		}
	}
	
	@Override
	public void onMessageReceived(Message message) {
		SkylightVehicle v = resolveSkylightVehicle(message.getVehicleID());
		Message messageCopy = message.createCopy();
		messageCopy.setReceiveTimeStamp(message.getReceiveTimeStamp());
		vehicleControlService.resolveVehicle(message.getVehicleID()).setLastReceivedMessage(messageCopy);

		//m2000 - vehicle config
		if(message instanceof SkylightVehicleConfigurationMessage) {
			SkylightVehicleConfigurationMessage m = (SkylightVehicleConfigurationMessage)message.createCopy();
			//maintain current custom settings
			SkylightVehicleConfigurationMessage old = v.getSkylightVehicleConfiguration();
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
			v.setSkylightVehicleConfiguration(m);
			vehicleControlService.notifyVehiclesUpdated();

		//m2006 - takeoff landing config
		} else if(message instanceof TakeoffLandingConfiguration) {
			resolveSkylightMission(message.getVehicleID()).setTakeoffLandingConfiguration((TakeoffLandingConfiguration)message.createCopy());
			subscriberService.notifyMissionEvent(vehicleControlService.getKnownVehicles().get(message.getVehicleID()).getMission(), EventType.UPDATED, null);
			
		//m2007 - rules of safety
		} else if(message instanceof RulesOfSafety) {
			resolveSkylightMission(message.getVehicleID()).setRulesOfSafety((RulesOfSafety)message.createCopy());
			subscriberService.notifyMissionEvent(vehicleControlService.getKnownVehicles().get(message.getVehicleID()).getMission(), EventType.UPDATED, null);

		//m2009 - servo configuration
		} else if(message instanceof ServoConfiguration) {
			SkylightVehicleConfigurationMessage cm = resolveSkylightVehicleConfiguration(message.getVehicleID());
			ServoConfiguration m = (ServoConfiguration)message;
			cm.getServoConfiguration(m.getServo()).copyFrom(m);
			vehicleControlService.notifyVehiclesUpdated();
	
		//m2010 - pid configuration
		} else if(message instanceof PIDConfiguration) {
			SkylightVehicleConfigurationMessage cm = resolveSkylightVehicleConfiguration(message.getVehicleID());
			PIDConfiguration m = (PIDConfiguration)message;
			cm.getPIDConfiguration(m.getPIDControl()).copyFrom(m);
			vehicleControlService.notifyVehiclesUpdated();

		//m2016 - mission annotations
		} else if(message instanceof MissionAnnotationsMessage) {
			Mission mission = vehicleControlService.getKnownVehicles().get(message.getVehicleID()).getMission();
			mission.setMissionAnnotations((MissionAnnotationsMessage)message.createCopy());
			subscriberService.notifyMissionEvent(mission, EventType.UPDATED, null);
		}
	}
	
	public SkylightVehicle resolveSkylightVehicle(int vehicleId) {
		synchronized(knownSkylightVehicles) {
			SkylightVehicle v = knownSkylightVehicles.get(vehicleId);
			if(v==null) {
				try {
					v = storageService.loadState("vehicles", getSkylightVehicleFileName(vehicleId), SkylightVehicle.class);
				} catch (Exception e) {
					//do nothing
				}
				if(v==null) {
					v = new SkylightVehicle();
					v.setVehicleID(vehicleId);
					v.setSkylightVehicleConfiguration(new SkylightVehicleConfigurationMessage());
					logger.info("Creating a new skylight vehicle configuration for '"+ StringHelper.formatId(vehicleId) +"'");
				}
				knownSkylightVehicles.put(vehicleId, v);
				v.setVehicleID(vehicleId);
				vehicleControlService.notifyVehiclesUpdated();
			}
			return v;
		}
	}

	public SkylightVehicleConfigurationMessage resolveSkylightVehicleConfiguration(int vehicleId) {
		SkylightVehicle v = resolveSkylightVehicle(vehicleId);
		synchronized (v) {
			if(v.getSkylightVehicleConfiguration()==null) {
				v.setSkylightVehicleConfiguration(new SkylightVehicleConfigurationMessage());
			}
		}
		return v.getSkylightVehicleConfiguration();
	}
	
	public SkylightMission resolveSkylightMission(int vehicleId) {
		SkylightVehicle v = resolveSkylightVehicle(vehicleId);
		synchronized (v) {
			if(v.getSkylightMission()==null) {
				v.setSkylightMission(new SkylightMission());
			}
		}
		return v.getSkylightMission();
	}
	
	private String getSkylightVehicleFileName(int vehicleId) {
		if(StringHelper.formatId(vehicleId).contains("04")) {
			System.out.println("EITA!");
		}
		return "vehicle-" + StringHelper.formatId(vehicleId).replaceAll(":", "") + "-config.dat.skylight";
	}
	
}
