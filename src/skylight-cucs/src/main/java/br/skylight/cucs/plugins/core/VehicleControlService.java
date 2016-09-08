package br.skylight.cucs.plugins.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import br.skylight.commons.EOIRPayload;
import br.skylight.commons.EventType;
import br.skylight.commons.MessageConfiguration;
import br.skylight.commons.Mission;
import br.skylight.commons.Payload;
import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.WaypointDef;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.enums.MissionPlanMode;
import br.skylight.commons.dli.enums.SetZoom;
import br.skylight.commons.dli.messagetypes.GenericInformationRequestMessage;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.dli.mission.MissionUploadCommand;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.dli.payload.EOIRConfigurationState;
import br.skylight.commons.dli.payload.EOIRLaserOperatingState;
import br.skylight.commons.dli.payload.EOIRLaserPayloadCommand;
import br.skylight.commons.dli.payload.PayloadConfigurationMessage;
import br.skylight.commons.dli.payload.PayloadSteeringCommand;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageSentListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusReport;
import br.skylight.commons.dli.systemid.CUCSAuthorisationRequest;
import br.skylight.commons.dli.systemid.VSMAuthorisationResponse;
import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.dli.vehicle.ModePreferenceReport;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.vehiclecontrol.CUCS;
import br.skylight.cucs.plugins.vehiclecontrol.VehicleControlListener;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=VehicleControlService.class)
public class VehicleControlService extends Worker implements MessageListener, MessageSentListener {

	private static final Logger logger = Logger.getLogger(VehicleControlService.class.getName());
	
	private Map<Integer,Vehicle> knownVehicles = new HashMap<Integer,Vehicle>();
	private Map<Integer,CUCS> knownCUCS = new HashMap<Integer,CUCS>();
	private List<VehicleControlListener> listeners = new CopyOnWriteArrayList<VehicleControlListener>();

	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public StorageService storageService;
	
	@ServiceInjection
	public PluginManager pluginManager;

	@ExtensionPointsInjection
	public List<VehicleControlExtensionPoint> vehicleControlExtensionPoints;
	
	@Override
	public void onActivate() throws Exception {
		//don't read more messages types than required because it will clone each message of the type bellow that arrives
		subscriberService.addMessageListener(MessageType.M20, this);
		subscriberService.addMessageListener(MessageType.M21, this);
		subscriberService.addMessageListener(MessageType.M100, this);
		subscriberService.addMessageListener(MessageType.M101, this);
		subscriberService.addMessageListener(MessageType.M102, this);
		subscriberService.addMessageListener(MessageType.M106, this);
		subscriberService.addMessageListener(MessageType.M107, this);
		subscriberService.addMessageListener(MessageType.M108, this);
		subscriberService.addMessageListener(MessageType.M109, this);
		subscriberService.addMessageListener(MessageType.M110, this);
		subscriberService.addMessageListener(MessageType.M300, this);
		subscriberService.addMessageListener(MessageType.M301, this);
		subscriberService.addMessageListener(MessageType.M302, this);
		subscriberService.addMessageListener(MessageType.M501, this);

		//mission download messages
		subscriberService.addMessageListener(MessageType.M801, this);
		subscriberService.addMessageListener(MessageType.M802, this);
		subscriberService.addMessageListener(MessageType.M803, this);
		subscriberService.addMessageListener(MessageType.M804, this);

		subscriberService.addMessageListener(MessageType.M1100, this);
		subscriberService.addMessageListener(MessageType.M1101, this);
		
		subscriberService.addMessageSentListener(MessageType.M43, this);
		
		pluginManager.executeAfterStartup(new Runnable() {
			public void run() {
				requestVehicleInfos();
			}
		});
	}
	
	@Override
	public void onDeactivate() throws Exception {
		saveKnownVehicles();
	}

	private void saveKnownVehicles() throws IOException {
		//save known vehicles
		for (Vehicle v : knownVehicles.values()) {
			storageService.saveState(v, "vehicles", getVehicleFileName(v.getVehicleID().getVehicleID()));
			if(v.getMission()!=null) {
				saveMission(v.getVehicleID().getVehicleID(), storageService.resolveFile("vehicles", getMissionFileName(v.getVehicleID().getVehicleID())));
			}
		}
	}

	@Override
	public void onMessageSent(Message message) {
		//keep last sent message in vehicle for future requests
		Vehicle v = resolveVehicle(message.getVehicleID());
		v.setLastSentMessage(message);
	}
	
	@Override
	public void onMessageReceived(Message message) {
		Vehicle v = resolveVehicle(message.getVehicleID());
		
		//keep last message in vehicle for future requests
		Message messageCopy = message.createCopy();
		messageCopy.setReceiveTimeStamp(message.getReceiveTimeStamp());
		v.setLastReceivedMessage(messageCopy);

		//keep payload messages in vehicle payloads
//		if(message instanceof MessageTargetedToStation) {
//			MessageTargetedToStation m = (MessageTargetedToStation)message;
//			for (Integer stationNumber : m.getTargetStations().getStations()) {
//				Payload p = resolvePayload(message.getVehicleID(), stationNumber);
//				p.setLastReceivedMessage(messageCopy);
//			}
//		}
		
		//m20
		if(message instanceof VehicleID) {
			VehicleID m = (VehicleID)message;
			if(m.getVehicleID()!=m.getVehicleIDUpdate()) {
				knownVehicles.remove(m.getVehicleID());
				knownVehicles.put(m.getVehicleIDUpdate(), v);
				m.setVehicleID(m.getVehicleIDUpdate());
			}
			v.setVehicleID((VehicleID)m.createCopy());
			notifyVehiclesUpdated();
			
		//m21
		} else if(message instanceof VSMAuthorisationResponse) {
			VSMAuthorisationResponse m = (VSMAuthorisationResponse)message;
			long lastGrants = v.resolveCUCSControl(m.getCucsID()).getGrantedLOIs().getData();
			
			//vehicle authorization
			if(m.getControlledStation().getData()==0) {
				v.resolveCUCSControl(m.getCucsID()).getAuthorizedLOIs().setData(m.getLoiAuthorized().getData());
				v.resolveCUCSControl(m.getCucsID()).getGrantedLOIs().setData(m.getLoiGranted().getData());
				
			//payload authorizations
			} else {
				for (int uniqueStationNumber : m.getControlledStation().getStations()) {
					Payload p = resolvePayload(m.getVehicleID(), uniqueStationNumber);
					p.resolveCUCSControl(m.getCucsID()).getAuthorizedLOIs().setData(m.getLoiAuthorized().getData());
					p.resolveCUCSControl(m.getCucsID()).getGrantedLOIs().setData(m.getLoiGranted().getData());
				}
			}

			//request additional info
			BitmappedLOI grantedLOIs = v.resolveCUCSControl(m.getCucsID()).getGrantedLOIs();
			
			if(lastGrants!=grantedLOIs.getData()) {
				//REQUEST INFO AS CONFIGURED
				for (MessageConfiguration mc : v.getMessageConfigurations()) {
					if(mc.isRequestOnConnect()) {
						if(mc.getMessageType().getLOIs().matchAny(grantedLOIs)) {
							GenericInformationRequestMessage mr = messagingService.resolveMessageForSending(GenericInformationRequestMessage.class);
							mr.setVehicleID(m.getVehicleID());
							mr.setRequestedMessageType(mc.getMessageType());
							messagingService.sendMessage(mr);
						}
					}
				}
				
//				//SEND MESSAGE CONFIGURATIONS TO VEHICLE
//				if(v.isSendMessageConfigurationsOnConnect()) {
//					for (MessageConfiguration mc : v.getMessageConfigurations()) {
//						if(mc.getMessageType().getLOIs().matchAny(grantedLOIs)) {
//							mc.sendConfigurationToVehicle(v.getVehicleID().getVehicleID(), messagingService);
//						}
//					}
//				}
			}
			notifyVehiclesUpdated();

		//m100
		} else if(message instanceof VehicleConfigurationMessage) {
			v.setVehicleConfiguration((VehicleConfigurationMessage)message.createCopy());
			notifyVehiclesUpdated();

		//m109
		} else if(message instanceof ModePreferenceReport) {
			v.setModePreferenceReport((ModePreferenceReport)message.createCopy());

		//M110
		} else if(message instanceof FromToNextWaypointStates) {
			FromToNextWaypointStates m = (FromToNextWaypointStates)message;
			if(v.getMission()!=null) {
				for (WaypointDef wd : v.getMission().getComputedWaypointsMap().values()) {
					wd.processFromToNextWaypointStates(m);
				}
			}
			VehicleSteeringCommand vc = resolveVehicle(m.getVehicleID()).getVehicleSteeringCommand();
			if(vc!=null) {
				resolveVehicle(m.getVehicleID()).getVehicleSteeringCommand().setCommandedWaypointNumber(m.getToWaypointNumber());
			}
			
		//m300
		} else if(message instanceof PayloadConfigurationMessage) {
			PayloadConfigurationMessage m = (PayloadConfigurationMessage)message;
			for (int stationNumber : m.getStationNumber().getStations()) {
				Payload p = resolvePayload(m.getVehicleID(), stationNumber);
				p.setPayloadType(m.getPayloadType());
				p.setNumberOfPayloadRecordingDevices(m.getNumberOfPayloadRecordingDevices());
				p.setStationDoor(m.getStationDoor());
			}
			notifyPayloadUpdated(m.getVehicleID(), m.getStationNumber());

		//m301
		} else if(message instanceof EOIRConfigurationState) {
			EOIRConfigurationState m = (EOIRConfigurationState)message;
			for (int stationNumber : m.getStationNumber().getStations()) {
				EOIRPayload p = resolvePayload(m.getVehicleID(), stationNumber).resolveEoIrPayload();
				p.setEoIrConfiguration((EOIRConfigurationState)m.createCopy());
			}
			notifyPayloadUpdated(m.getVehicleID(), m.getStationNumber());
			
		//m302
		} else if(message instanceof EOIRLaserOperatingState) {
			EOIRLaserOperatingState m = (EOIRLaserOperatingState)message;
			for (int stationNumber : m.getStationNumber().getStations()) {
				EOIRPayload p = resolvePayload(m.getVehicleID(), stationNumber).resolveEoIrPayload();
				p.setOperatingState((EOIRLaserOperatingState)m.createCopy());
			}
			notifyPayloadUpdated(m.getVehicleID(), m.getStationNumber());

		//m501
		} else if(message instanceof DataLinkStatusReport) {
			DataLinkStatusReport m = (DataLinkStatusReport)messageCopy;
			if(m.getAddressedTerminal().equals(DataTerminalType.ADT)) {
				knownVehicles.get(message.getVehicleID()).setAdtDataLinkStatusReport(m);
			} else if(m.getAddressedTerminal().equals(DataTerminalType.GDT)) {
				knownVehicles.get(message.getVehicleID()).setGdtDataLinkStatusReport(m);
			}
			
		//m801
		} else if(message instanceof AVRoute) {
			knownVehicles.get(message.getVehicleID()).getMission().getRoutes().add((AVRoute)message.createCopy());
			subscriberService.notifyMissionEvent(knownVehicles.get(message.getVehicleID()).getMission(), EventType.UPDATED, null);
		//m802
		} else if(message instanceof AVPositionWaypoint) {
			knownVehicles.get(message.getVehicleID()).getMission().getPositionWaypoints().add((AVPositionWaypoint)message.createCopy());
			subscriberService.notifyMissionEvent(knownVehicles.get(message.getVehicleID()).getMission(), EventType.UPDATED, null);
		//m803
		} else if(message instanceof AVLoiterWaypoint) {
			knownVehicles.get(message.getVehicleID()).getMission().getLoiterWaypoints().add((AVLoiterWaypoint)message.createCopy());
			subscriberService.notifyMissionEvent(knownVehicles.get(message.getVehicleID()).getMission(), EventType.UPDATED, null);
		//m804
		} else if(message instanceof PayloadActionWaypoint) {
			knownVehicles.get(message.getVehicleID()).getMission().getPayloadActionWaypoints().add((PayloadActionWaypoint)message.createCopy());
			subscriberService.notifyMissionEvent(knownVehicles.get(message.getVehicleID()).getMission(), EventType.UPDATED, null);
		//m1100
		} else if(message instanceof SubsystemStatusAlert) {
			SubsystemStatusAlert m = (SubsystemStatusAlert)message;
			v.onAlertUpdated(m);
		//m1101
		} else if(message instanceof SubsystemStatusReport) {
			SubsystemStatusReport m = (SubsystemStatusReport)message;
			v.getSubsystemStates().put(m.getSubsystemID(), m.getSubsystemState());
		}
	}

	private void notifyPayloadUpdated(int vehicleId, BitmappedStation stationNumber) {
		for (int uniqueStationNumber : stationNumber.getStations()) {
			Payload p = resolvePayload(vehicleId, uniqueStationNumber);
			subscriberService.notifyPayloadEvent(p, EventType.UPDATED, null);
		}
	}

	public void notifyVehiclesUpdated() {
		for (VehicleControlListener listener : listeners) {
			listener.onVehiclesUpdated(knownVehicles, knownCUCS);
		}
		if(subscriberService.getLastSelectedVehicle()!=null) {
			subscriberService.notifyVehicleEvent(subscriberService.getLastSelectedVehicle(), EventType.UPDATED, null);
		}
	}

	public Payload resolvePayload(int vehicleId, int uniqueStationNumber) {
		Vehicle v = resolveVehicle(vehicleId);
		synchronized(v.getPayloads()) {
			Payload p = v.getPayloads().get(uniqueStationNumber);
			if(p==null) {
				p = new Payload();
				p.setVehicleID(v.getVehicleID());
				p.setUniqueStationNumber(uniqueStationNumber);
				v.getPayloads().put(uniqueStationNumber, p);
			}
			return p;
		}
	}

	public Vehicle resolveVehicle(int vehicleId) {
		synchronized(knownVehicles) {
			Vehicle v = knownVehicles.get(vehicleId);
			if(v==null) {
				//load vehicle configuration
				try {
					v = storageService.loadState("vehicles", getVehicleFileName(vehicleId), Vehicle.class);
				} catch (Exception e) {
					logger.throwing(null,null,e);
					e.printStackTrace();
				}
				if(v==null) {
					v = new Vehicle();
					logger.info("Creating a new vehicle configuration for '" + StringHelper.formatId(vehicleId) + "'");
				}
				v.setVehicleID(new VehicleID());
				v.getVehicleID().setVehicleID(vehicleId);
				v.setVehicleConfiguration(new VehicleConfigurationMessage());
				knownVehicles.put(vehicleId, v);
				
				//load last vehicle mission
				try {
					File f = storageService.getFile("vehicles", getMissionFileName(vehicleId));
					if(f.exists()) {
						loadMission(vehicleId, f);
					}
				} catch (IOException e) {
					logger.throwing(null,null,e);
					e.printStackTrace();
				}
			}
			return v;
		}
	}
	
	public Map<Integer, Vehicle> getKnownVehicles() {
		return knownVehicles;
	}
	
	public void addListener(VehicleControlListener listener) {
		listeners.add(listener);
	}
	public void removeListener(VehicleControlListener listener){
		listeners.remove(listener);
	}

	public Map<Integer, CUCS> getKnownCUCS() {
		return knownCUCS;
	}
	
	private String getVehicleFileName(int vehicleID) {
		return "vehicle-" + StringHelper.formatId(vehicleID).replaceAll(":", "") + "-config.dat";
	}

	private String getMissionFileName(int vehicleID) {
		return "vehicle-" + StringHelper.formatId(vehicleID).replaceAll(":", "") + "-mission.dat";
	}
	
	public <T extends Message> T getLastReceivedMessage(int vehicleId, MessageType messageType) {
		Vehicle v = knownVehicles.get(vehicleId);
		if(v!=null) {
			return v.getLastReceivedMessage(messageType);
		}
		return null;
	}

	public VerificationResult validateMission(int vehicleId) {
		Vehicle v = knownVehicles.get(vehicleId);

		//validate mission data
		VerificationResult vr = v.getMission().validate(v.getVehicleConfiguration());
		
		//extension points should validate mission too
		for (VehicleControlExtensionPoint ep : vehicleControlExtensionPoints) {
			if(ep.isCompatibleWith(v.getVehicleID().getVehicleType())) {
				ep.validateMission(v.getVehicleID().getVehicleID(), vr);
			}
		}
		return vr;
	}

	public void createNewMission(int vehicleId) {
		Vehicle v = knownVehicles.get(vehicleId);
		
		//create new mission
		v.setMission(new Mission());
		
		//extension points should create new mission too
		for (VehicleControlExtensionPoint ep : vehicleControlExtensionPoints) {
			if(ep.isCompatibleWith(v.getVehicleID().getVehicleType())) {
				ep.createNewMission(v.getVehicleID().getVehicleID());
			}
		}
		
		subscriberService.notifyMissionEvent(v.getMission(), EventType.CREATED, null);
	}
	
	public void saveMission(int vehicleId, File file) throws IOException {
		Vehicle v = knownVehicles.get(vehicleId);

		//save mission data
		IOHelper.writeStateToFile(v.getMission(), file);
		
		//extension points should save mission to file too
		for (VehicleControlExtensionPoint ep : vehicleControlExtensionPoints) {
			if(ep.isCompatibleWith(v.getVehicleID().getVehicleType())) {
				FileOutputStream fos = null;
				try {
					File fe = getExtensionFile(file, ep);
					if(fe.exists()) {
						fe.delete();
					}
					fos = new FileOutputStream(fe);
					DataOutputStream dos = new DataOutputStream(fos);
					ep.saveMission(v.getVehicleID().getVehicleID(), dos);
				} catch (IOException e) {
					e.printStackTrace();
					throw new IOException("Couldn't save mission extension for " + ep.getExtensionIdentification() + ". e=" + e.toString());
				} finally {
					IOHelper.close(fos);
				}
			}
		}
	}

	public void loadMission(int vehicleId, File file) throws IOException {
		Vehicle v = knownVehicles.get(vehicleId);

		//load mission data
		v.setMission(IOHelper.readStateFromFile(file, Mission.class));
		
		//extension points should read mission from file too
		for (VehicleControlExtensionPoint ep : vehicleControlExtensionPoints) {
			if(ep.isCompatibleWith(v.getVehicleID().getVehicleType())) {
				FileInputStream fis = null;
				try {
					File fe = getExtensionFile(file, ep);
					if(fe.exists()) {
						fis = new FileInputStream(fe);
						DataInputStream dis = new DataInputStream(fis);
						ep.loadMission(v.getVehicleID().getVehicleID(), dis);
					} else {
						logger.info("Mission extension file doesn't exist for " + file.toString() + ". Ignoring. extension=" + ep.getExtensionIdentification());
					}
				} catch (IOException e) {
					e.printStackTrace();
					logger.warning("Couldn't load mission extension file " + file.toString() + ". Ignoring. extension=" + ep.getExtensionIdentification() + ". e=" + e.toString());
				} finally {
					IOHelper.close(fis);
				}
			}
		}
		
		subscriberService.notifyMissionEvent(v.getMission(), EventType.CREATED, null);
	}
	
	private File getExtensionFile(File defaultFile, VehicleControlExtensionPoint ep) {
		return new File(defaultFile.getParentFile(), defaultFile.getName() + "." + ep.getExtensionIdentification());
	}

	public void sendMissionToVehicle(int vehicleID) {
		//save vehicle mission
		try {
			saveKnownVehicles();
		} catch (IOException e) {
			logger.throwing(null, null, e);
			e.printStackTrace();
		}
		
		//send all mission messages to vsm/vehicle
		Mission mission = resolveVehicle(vehicleID).getMission();
		List<Message> mm = mission.getAllMissionMessages();
		for (Message sm : mm) {
			sm.setVehicleID(vehicleID);
			sm.setTimeStamp(System.currentTimeMillis()/1000.0);
			messagingService.sendMessage(sm);
		}
		
		//extension points should send mission to vehicle too
		for (VehicleControlExtensionPoint ep : vehicleControlExtensionPoints) {
			if(ep.isCompatibleWith(knownVehicles.get(vehicleID).getVehicleID().getVehicleType())) {
				ep.sendMissionToVehicle(vehicleID);
			}
		}

		//tell vsm to send mission to vehicle
		sendMissionUploadCommand(vehicleID, mission.getMissionID(), MissionPlanMode.LOAD_MISSION);
	}

	public void sendMissionUploadCommand(int vehicleID, String missionID, MissionPlanMode mode) {
		MissionUploadCommand m = messagingService.resolveMessageForSending(MissionUploadCommand.class);
		m.setMissionPlanMode(mode);
		m.setVehicleID(vehicleID);
		m.setMissionID(missionID);
		messagingService.sendMessage(m);
	}

	public void requestMissionDownload(int vehicleID) {
		getKnownVehicles().get(vehicleID).setMission(new Mission());
		for (VehicleControlExtensionPoint ep : vehicleControlExtensionPoints) {
			if(ep.isCompatibleWith(knownVehicles.get(vehicleID).getVehicleID().getVehicleType())) {
				ep.createNewMission(vehicleID);
			}
		}
		MissionUploadCommand m = messagingService.resolveMessageForSending(MissionUploadCommand.class);
		m.setMissionPlanMode(MissionPlanMode.DOWNLOAD_MISSION);
		m.setVehicleID(vehicleID);
		m.setMissionID(getKnownVehicles().get(vehicleID).getVehicleID().getMissionID());
		messagingService.sendMessage(m);
	}

	public void requestVehicleInfos() {
		CUCSAuthorisationRequest r = messagingService.resolveMessageForSending(CUCSAuthorisationRequest.class);
		r.setVehicleID(Message.BROADCAST_ID);
		r.setVsmID(Message.BROADCAST_ID);
		messagingService.sendMessage(r);
	}

	/*
	 * THESES METHODS ARE FOR MESSAGES THAT ARE SENT/SHARED AMONG VARIOUS VIEWS AND NEED
	 * TO BE IN SYNC
	 */
	
	public VehicleSteeringCommand resolveVehicleSteeringCommandForSending(int vehicleId) {
		VehicleSteeringCommand vs = messagingService.resolveMessageForSending(VehicleSteeringCommand.class);
		VehicleSteeringCommand evs = resolveVehicle(vehicleId).getVehicleSteeringCommand();
		if(evs!=null) {
			IOHelper.copyState(vs, evs);
		}
		vs.setVehicleID(vehicleId);
		return vs;
	}
	public void sendVehicleSteeringCommand(VehicleSteeringCommand vs) {
		resolveVehicle(vs.getVehicleID()).setVehicleSteeringCommand((VehicleSteeringCommand)vs.createCopy());
		vs.setTimeStamp(System.currentTimeMillis()/1000.0);
		messagingService.sendMessage(vs);
		subscriberService.notifyVehicleSteeringEvent(resolveVehicle(vs.getVehicleID()));
	}

	public PayloadSteeringCommand resolvePayloadSteeringCommandForSending(int vehicleId, int uniqueStationNumber) {
		PayloadSteeringCommand vs = messagingService.resolveMessageForSending(PayloadSteeringCommand.class);
		Payload p = resolveVehicle(vehicleId).getPayloads().get(uniqueStationNumber);
		if(p!=null) {
			PayloadSteeringCommand evs = p.resolvePayloadSteeringCommand();
			IOHelper.copyState(vs, evs);
		}
		vs.setVehicleID(vehicleId);
		vs.getStationNumber().setUniqueStationNumber(uniqueStationNumber);
		return vs;
	}
	public void sendPayloadSteeringCommand(PayloadSteeringCommand vs) {
		if(vs.getStationNumber().getData()>0) {
			Payload p = resolveVehicle(vs.getVehicleID()).getPayloads().get(vs.getStationNumber().getStations().get(0));
			p.setPayloadSteeringCommand((PayloadSteeringCommand)vs.createCopy());
			//zoom only once (don't keep zoom in/out actions)
			p.resolvePayloadSteeringCommand().setSetZoom(SetZoom.NO_CHANGE);
			vs.setTimeStamp(System.currentTimeMillis()/1000.0);
			messagingService.sendMessage(vs);
			subscriberService.notifyPayloadSteeringEvent(p);
		}
	}

	public EOIRLaserPayloadCommand resolveEOIRLaserPayloadCommandForSending(int vehicleId, int uniqueStationNumber) {
		EOIRLaserPayloadCommand vs = messagingService.resolveMessageForSending(EOIRLaserPayloadCommand.class);
		Payload p = resolveVehicle(vehicleId).getPayloads().get(uniqueStationNumber);
		if(p!=null && p.getEoIrPayload()!=null) {
			EOIRLaserPayloadCommand evs = p.getEoIrPayload().getEoIrLaserPayloadCommand();
			if(evs!=null) {
				IOHelper.copyState(vs, evs);
			}
		}
		vs.setVehicleID(vehicleId);
		vs.getStationNumber().setUniqueStationNumber(uniqueStationNumber);
		return vs;
	}
	public void sendEOIRLaserPayloadCommand(EOIRLaserPayloadCommand vs) {
		Payload p = resolveVehicle(vs.getVehicleID()).getPayloads().get(vs.getStationNumber().getStations().get(0));
		p.resolveEoIrPayload().setEoIrLaserPayloadCommand((EOIRLaserPayloadCommand)vs.createCopy());
		vs.setTimeStamp(System.currentTimeMillis()/1000.0);
		messagingService.sendMessage(vs);
		subscriberService.notifyPayloadSteeringEvent(p);
	}

	public void sendWaypointDefToVehicle(WaypointDef wd, int vehicleID) {
		if(wd.getPositionWaypoint()!=null) {
			wd.getPositionWaypoint().setTimeStamp(System.currentTimeMillis()/1000.0);
			messagingService.sendMessage(wd.getPositionWaypoint());
		}
		if(wd.getLoiterWaypoint()!=null) {
			wd.getLoiterWaypoint().setTimeStamp(System.currentTimeMillis()/1000.0);
			messagingService.sendMessage(wd.getLoiterWaypoint());
		}
		for(PayloadActionWaypoint aw : wd.getPayloadActionWaypoints()) {
			aw.setTimeStamp(System.currentTimeMillis()/1000.0);
			messagingService.sendMessage(aw);
		}
	}
	
}
