package br.skylight.vsm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Logger;

import br.skylight.commons.Alert;
import br.skylight.commons.CUCSControl;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.AlertPriority;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.enums.MissionPlanMode;
import br.skylight.commons.dli.messagetypes.GenericInformationRequestMessage;
import br.skylight.commons.dli.messagetypes.ScheduleMessageUpdateCommand;
import br.skylight.commons.dli.mission.MissionUploadCommand;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.io.dataterminal.ac4790.AC4790DataTerminal;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.ServiceInjection;

/**
 * General abstraction for a VSM Vehicle.
 * 
 * @author Flavio Stutz
 */
@ManagedMember
public abstract class VSMVehicle extends ThreadWorker implements SerializableState {

	private static final Logger logger = Logger.getLogger(VSMVehicle.class.getName());
	
	private Vehicle vehicle;
	private boolean connected;

	private MessagingService vehicleMessagingService;
	
	private TimedBoolean dataLinkStatusReportToCUCSTimer = new TimedBoolean(Integer.MAX_VALUE);
	private TimedBoolean mainDutyTimer = new TimedBoolean(1000);
	
	//mission upload control
	private boolean uploadMissionMessages = false;
	private MissionUploadCommand loadMissionCommand;
	private Queue<Message> missionMessageQueue = new LinkedList<Message>();

	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public PluginManager pluginManager;

	public VSMVehicle() {
		super(30);
	}

	protected abstract void onConnect();

	protected abstract void onDisconnect();

	protected abstract MessagingService createMessagingService();

	protected abstract DataTerminal createDataTerminal();

	public abstract boolean isSupportsDifferentialGPSSignal();
	
	private boolean downlinkAlert = false;
	private boolean uplinkAlert = false;
	
	private TimedBoolean consoleTimer = new TimedBoolean(3000);

	@Override
	public void step() throws Exception {
		//main duty
		if(mainDutyTimer.checkTrue()) {

			//send GDT status report to vehicle
			//-if there is a AC4790 data terminal connecting VSM-vehicle, we don't need to send GDT status because 
			//-that ADT data terminal can extract the remote and local RSSIs even when the GDT is not transmitting no packets (it extracts from RF packets acknowledge)
			if(!(vehicleMessagingService.getDataTerminal() instanceof AC4790DataTerminal) ||
				vehicleMessagingService.getDataTerminal().getTimeSinceLastDownlinkActivity()>1000) {
				DataLinkStatusReport m = vehicleMessagingService.resolveMessageForSending(DataLinkStatusReport.class);
				IOHelper.copyState(m, computeGDTStatusReport());
				m.setTimeStamp(System.currentTimeMillis()/1000.0);
				vehicleMessagingService.sendMessage(m);
//				System.out.println("SENDING GDT STATUS TO VEHICLE");
			}
			
			//notify poor vehicle signal received on VSM
			if(!downlinkAlert) {
				//verify if link has been lost
				if(vehicleMessagingService.getDataTerminal().getDownlinkStatus()!=-1 &&
					vehicleMessagingService.getDataTerminal().getDownlinkStatus() < 10) {
					SubsystemStatusAlert ma = Alert.GDT_DOWNLINK_STRENGTH_WARNING.getSubsystemStatusAlert();
					ma.setText("Vehicle signal is too weak on VSM (GDT downlink). percent=" + vehicleMessagingService.getDataTerminal().getDownlinkStatus());
					ma.setPriority(AlertPriority.WARNING);
					sendMessageToAllCUCS(ma);
					downlinkAlert = true;
				}
			} else {
				//verify if link has been restored
				if(vehicleMessagingService.getDataTerminal().getDownlinkStatus()!=-1 &&
					vehicleMessagingService.getDataTerminal().getDownlinkStatus() >= 10) {
					SubsystemStatusAlert ma = Alert.GDT_DOWNLINK_STRENGTH_WARNING.getSubsystemStatusAlert();
					ma.setText("Vehicle signal is normal on VSM (GDT downlink). percent=" + vehicleMessagingService.getDataTerminal().getDownlinkStatus());
					ma.setPriority(AlertPriority.CLEARED);
					sendMessageToAllCUCS(ma);
					downlinkAlert = false;
				}
			}
			
			//notify poor vehicle signal received on vehicle
			if (!uplinkAlert) {
				//verify if link has been lost
				if(vehicleMessagingService.getDataTerminal().getUplinkStatus()!=-1 &&
					vehicleMessagingService.getDataTerminal().getUplinkStatus() < 10) {
					SubsystemStatusAlert ma = Alert.ADT_DOWNLINK_STRENGTH_WARNING.getSubsystemStatusAlert();
					ma.setText("Vehicle signal is too weak on vehicle (ADT downlink). percent=" + vehicleMessagingService.getDataTerminal().getUplinkStatus());
					ma.setPriority(AlertPriority.WARNING);
					sendMessageToAllCUCS(ma);
					uplinkAlert = true;
				}
			} else {
				//verify if link has been restored
				if(vehicleMessagingService.getDataTerminal().getUplinkStatus()!=-1 &&
					vehicleMessagingService.getDataTerminal().getUplinkStatus() >= 10) {
					SubsystemStatusAlert ma = Alert.ADT_DOWNLINK_STRENGTH_WARNING.getSubsystemStatusAlert();
					ma.setText("VSM signal is normal on vehicle (ADT downlink). percent=" + vehicleMessagingService.getDataTerminal().getUplinkStatus());
					ma.setPriority(AlertPriority.CLEARED);
					sendMessageToAllCUCS(ma);
					uplinkAlert = false;
				}
			}
		}

		// send GDT status message to all ground stations
		if(dataLinkStatusReportToCUCSTimer.checkTrue()) {
			DataLinkStatusReport m = messagingService.resolveMessageForSending(DataLinkStatusReport.class);
			IOHelper.copyState(m, computeGDTStatusReport());
			m.setTimeStamp(System.currentTimeMillis()/1000.0);
			sendMessageToAllCUCS(m);
		}
		
		//send mission to vehicle
		if(uploadMissionMessages) {
			Message m = missionMessageQueue.poll();
			if(m!=null) {
				vehicleMessagingService.sendMessage(m);
			} else {
				//all mission messages were sent. notify vehicle
				MissionUploadCommand mc = messagingService.resolveMessageForSending(MissionUploadCommand.class);
				mc.setMissionPlanMode(MissionPlanMode.LOAD_MISSION);
				mc.setVehicleID(loadMissionCommand.getVehicleID());
				mc.setCucsID(loadMissionCommand.getCucsID());
				mc.setMissionID(loadMissionCommand.getMissionID());
				vehicleMessagingService.sendMessage(mc);
				uploadMissionMessages = false;
			}
		}
		
		//show info on console
		if(consoleTimer.checkTrue()) {
			System.out.println(getVehicle().getLabel() + " - GDT: DownlinkStatus="+vehicleMessagingService.getDataTerminal().getDownlinkStatus()+"; UplinkStatus="+vehicleMessagingService.getDataTerminal().getUplinkStatus()+"; InputRate="+(int)vehicleMessagingService.getDataTerminal().getInputRate()+"B/s; OutputRate="+(int)vehicleMessagingService.getDataTerminal().getOutputRate() + "B/s; TxErrors="+vehicleMessagingService.getDataTerminal().getTxErrors());
		}
	}

	private DataLinkStatusReport computeGDTStatusReport() {
		DataLinkStatusReport dr = vehicleMessagingService.getDataTerminal().getDataLinkStatusReport();
		dr.setCucsID(Message.BROADCAST_ID);
		dr.setAddressedTerminal(DataTerminalType.GDT);
		dr.setDownlinkStatus((short)vehicleMessagingService.getDataTerminal().getDownlinkStatus());
		dr.setVehicleID(getVehicle().getVehicleID().getVehicleID());
		return dr;
	}

	private void sendMessageToAllCUCS(Message ma) {
		ma.setVehicleID(getVehicle().getVehicleID().getVehicleID());
		ma.setTimeStamp(System.currentTimeMillis()/1000.0);
		//send message to each ground station that has control over this vehicle
		for (Entry<Integer,CUCSControl> cc : getVehicle().getCucsControls().entrySet()) {
			if(cc.getValue().getGrantedLOIs().matchAny(ma.getMessageType().getLOIs())
					|| cc.getValue().getGrantedLOIs().matchAny(ma.getMessageType().getLOIs())) {
				ma.setCucsID(cc.getKey());
				messagingService.sendMessage(ma);
			}
		}
	}

	public void connect() {
		if (!connected) {
			onConnect();
			connected = true;
		}
	}

	public void disconnect() {
		if (connected) {
			onDisconnect();
			connected = false;
		}
	}

	public void sendMessageToVehicle(Message message) throws IOException {
		System.out.println("SEND0");
		boolean sendToVehicle = true;
		long messageType = message.getMessageType().getNumber();
		
		//look for VSM schedules
		if(message instanceof ScheduleMessageUpdateCommand) {
			ScheduleMessageUpdateCommand m = (ScheduleMessageUpdateCommand)message;
			
			//update data link status report frequency for GDT
			if(m.getRequestedMessageType().equals(MessageType.M501)) {
				if(m.getFrequency()==0) {
					dataLinkStatusReportToCUCSTimer.setEnabled(false);
				} else {
					dataLinkStatusReportToCUCSTimer.setTime((long)(1000.0/m.getFrequency()));
					dataLinkStatusReportToCUCSTimer.setEnabled(true);
				}
			}
		}
		
		//look for 'requests for information' that are supposed to be handled by VSM
		if(message instanceof GenericInformationRequestMessage) {
			GenericInformationRequestMessage m = (GenericInformationRequestMessage)message;
			if(m.getRequestedMessageType().equals(MessageType.M501)) {
				DataLinkStatusReport r = messagingService.resolveMessageForSending(DataLinkStatusReport.class);
				IOHelper.copyState(r, computeGDTStatusReport());
				r.setTimeStamp(System.currentTimeMillis()/1000.0);
				messagingService.sendMessage(r);
			}
		}
		
		//look for DataLinkSetupMessages addressed to GDT
		if(message instanceof DataLinkSetupMessage) {
			DataLinkSetupMessage m = (DataLinkSetupMessage)message;
			if(m.getAddressedTerminal().equals(DataTerminalType.GDT)) {
				vehicleMessagingService.getDataTerminal().setupDataLink(m);
				sendToVehicle = false;
			}
		}

		//look for DataLinkControlCommands addressed to GDT
		if(message instanceof DataLinkControlCommand) {
			DataLinkControlCommand m = (DataLinkControlCommand)message;
			if(m.getAddressedTerminal().equals(DataTerminalType.GDT)) {
				vehicleMessagingService.getDataTerminal().controlDataLink(m);
				sendToVehicle = false;
			}
		}
		
		//look for mission messages
		if(messageType==801 || messageType==802 || messageType==803	|| messageType==804	|| messageType==2006 || messageType==2007 || messageType==2016) {
			if(uploadMissionMessages) {
				logger.warning("Received mission message "+ message.getMessageType() +" while uploading a previous mission to vehicle. Discarding it.");
			} else {
				missionMessageQueue.offer(message.createCopy());
			}
			sendToVehicle = false;
		
		//mission upload commands
		} else if(messageType==800) {
			MissionUploadCommand m = (MissionUploadCommand)message;
			
			//cancel current upload/download
			if(m.getMissionPlanMode().equals(MissionPlanMode.CANCEL_UPLOAD_OR_DOWNLOAD)) {
				uploadMissionMessages = false;
				missionMessageQueue.clear();
				//this message will be forwarded to vehicle so any download in progress will be canceled too
			
			//clear temp mission messages from vsm
			} else if(m.getMissionPlanMode().equals(MissionPlanMode.CLEAR_MISSION)) {
				uploadMissionMessages = false;
				missionMessageQueue.clear();
				//this message will be forwarded to vehicle so that the vehicle mission will be cleared too

			//start sending mission data to vehicle
			} else if(m.getMissionPlanMode().equals(MissionPlanMode.LOAD_MISSION)) {
				loadMissionCommand = (MissionUploadCommand)m.createCopy();
				sendToVehicle = false;
				
				//tell the vehicle to start receiving mission messages
				MissionUploadCommand mc = messagingService.resolveMessageForSending(MissionUploadCommand.class);
				mc.setMissionPlanMode(MissionPlanMode.RECEIVE_MISSION);
				mc.setWaypointNumber(missionMessageQueue.size());//used for telling the vehicle how many items it should receive
				mc.setVehicleID(message.getVehicleID());
				mc.setCucsID(message.getCucsID());
				mc.setMissionID(m.getMissionID());
				vehicleMessagingService.sendMessage(mc);
				uploadMissionMessages = true;
			}
		}
		
		//send messages to vehicle
		if(sendToVehicle) {
//			System.out.println("Forwarding message from CUCS to vehicle " + message.getMessageType());
			vehicleMessagingService.sendMessage(message);
		}
	}

	public Vehicle getVehicle() {
		if(vehicle==null) {
			vehicle = createVehicle();
		}
		return vehicle;
	}
	
	public abstract Vehicle createVehicle();

	public void setMessageListener(MessageListener messageListener) {
		vehicleMessagingService.setMessageListener(messageListener);
	}

	@Override
	public void onActivate() throws Exception {
		vehicleMessagingService = createMessagingService();
		try {
			pluginManager.manageObject(vehicleMessagingService);
			DataTerminal dataTerminal = createDataTerminal();
			vehicleMessagingService.bindTo(dataTerminal);
			pluginManager.manageObject(dataTerminal);
			getVehicle();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		super.onActivate();
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		getVehicle().readState(in);
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		getVehicle().writeState(out);
	}
	
}
