package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.AlertPriority;
import br.skylight.commons.dli.enums.AlertType;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.LoiterDirection;
import br.skylight.commons.dli.enums.LoiterType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.enums.SubsystemState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.ModePreferenceReport;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeReport;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.MeasureHelper;

public class Vehicle extends ControllableElement {

	private static final Logger logger = Logger.getLogger(Vehicle.class.getName());
	
	//serializable states
	private ArrayList<MessageConfiguration> messageConfigurations = new ArrayList<MessageConfiguration>();
	private boolean sendMessageConfigurationsOnConnect;
	private Map<Integer,Payload> payloads = new HashMap<Integer,Payload>();//by station number
	private VehicleSteeringCommand vehicleSteeringCommand;
	private ModePreferenceReport modePreferenceReport;
	private DataLinkStatusReport gdtDataLinkStatusReport;
	private DataLinkStatusReport adtDataLinkStatusReport;

	private ReentrantLock resolveLoiterLock = new ReentrantLock();

	//transient
	private Map<MessageType,Message> lastReceivedMessages = new HashMap<MessageType,Message>();
	private Map<MessageType,Message> lastSentMessages = new HashMap<MessageType,Message>();
	private VehicleConfigurationMessage vehicleConfiguration;
	private Mission mission;
	private LoiterConfiguration loiterConfiguration;
	private Map<Subsystem,SubsystemState> subsystemStates;
	private Map<Integer,AlertWrapper> subsystemStatusAlerts = new HashMap<Integer,AlertWrapper>();
	private String alertsConsole = "";
	private String lastConsoleLine = "";

	public void setLastReceivedMessage(Message message) {
		lastReceivedMessages.put(message.getMessageType(), message);
		if(message instanceof InertialStates) {
			if(loiterConfiguration==null || loiterConfiguration.getLatitude()==0) {
				InertialStates is = (InertialStates)message;
				resolveLoiterConfiguration().setLatitude(is.getLatitude());
				resolveLoiterConfiguration().setLongitude(is.getLongitude());
				resolveLoiterConfiguration().setAltitudeType(is.getAltitudeType());
				resolveLoiterConfiguration().setLoiterAltitude(is.getAltitude());
				resolveLoiterConfiguration().setLoiterSpeed(MeasureHelper.calculateMagnitude(is.getUSpeed(), is.getVSpeed()));
				resolveLoiterConfiguration().setSpeedType(SpeedType.GROUND_SPEED);
			}
		}
	}
	public void setLastSentMessage(Message message) {
		lastSentMessages.put(message.getMessageType(), message);
	}
	
	public <T extends Message> T getLastReceivedMessage(MessageType messageType) {
		return (T)lastReceivedMessages.get(messageType);
	}
	public <T extends Message> T getLastSentMessage(MessageType messageType) {
		return (T)lastSentMessages.get(messageType);
	}
	
	public Map<Integer, Payload> getPayloads() {
		return payloads;
	}
	public void setPayloads(Map<Integer, Payload> payloads) {
		this.payloads = payloads;
	}
	
	public VehicleConfigurationMessage getVehicleConfiguration() {
		return vehicleConfiguration;
	}
	public void setVehicleConfiguration(VehicleConfigurationMessage vehicleConfiguration) {
		this.vehicleConfiguration = vehicleConfiguration;
	}
	public Mission getMission() {
		return mission;
	}
	public void setMission(Mission mission) {
		this.mission = mission;
		mission.setVehicle(this);
	}
	
	public String getLabel() {
		if(getName().trim().length()>0) {
			return getName();
		} else {
			return StringHelper.formatId(getVehicleID().getVehicleID());
		}
	}
	
	public ArrayList<MessageConfiguration> getMessageConfigurations() {
		if(messageConfigurations.size()==0) {
			messageConfigurations.add(new MessageConfiguration(MessageType.M101));
			messageConfigurations.add(new MessageConfiguration(MessageType.M102));
			messageConfigurations.add(new MessageConfiguration(MessageType.M302));
			messageConfigurations.add(new MessageConfiguration(MessageType.M501));
		}
		return messageConfigurations;
	}
	
	public boolean isSendMessageConfigurationsOnConnect() {
		return sendMessageConfigurationsOnConnect;
	}
	public void setSendMessageConfigurationsOnConnect(boolean sendMessageConfigurationsOnConnect) {
		this.sendMessageConfigurationsOnConnect = sendMessageConfigurationsOnConnect;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		
		IOHelper.readArrayList(in, MessageConfiguration.class, messageConfigurations);
		sendMessageConfigurationsOnConnect = in.readBoolean();

		boolean hasConfiguration = in.readBoolean();
		if(hasConfiguration) {
			vehicleConfiguration = new VehicleConfigurationMessage();
			vehicleConfiguration.readStateForStorage(in);
		} else {
			vehicleConfiguration = null;
		}
		
		boolean hasSteering = in.readBoolean();
		if(hasSteering) {
			vehicleSteeringCommand = new VehicleSteeringCommand();
			vehicleSteeringCommand.readState(in);
		} else {
			vehicleSteeringCommand = null;
		}

		boolean hasModePreference = in.readBoolean();
		if(hasModePreference) {
			modePreferenceReport = new ModePreferenceReport();
			modePreferenceReport.readState(in);
		} else {
			modePreferenceReport = null;
		}
		
		boolean hasAdtStatus = in.readBoolean();
		if(hasAdtStatus) {
			adtDataLinkStatusReport = new DataLinkStatusReport();
			adtDataLinkStatusReport.readState(in);
		} else {
			adtDataLinkStatusReport = null;
		}

		boolean hasGdtStatus = in.readBoolean();
		if(hasGdtStatus) {
			gdtDataLinkStatusReport = new DataLinkStatusReport();
			gdtDataLinkStatusReport.readState(in);
		} else {
			gdtDataLinkStatusReport = null;
		}

		//restore only payloads that were registered on 'createVehicle()'
		HashMap<Integer,Payload> readPayloads = new HashMap<Integer,Payload>();
		IOHelper.readMapStateIntKey(readPayloads, Payload.class, in);
		for (Entry<Integer,Payload> p : readPayloads.entrySet()) {
			if(payloads.containsKey(p.getKey())) {
				IOHelper.copyState(payloads.get(p.getKey()), p.getValue());
			}
		}
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		
		IOHelper.writeArrayList(out, messageConfigurations);
		out.writeBoolean(sendMessageConfigurationsOnConnect);
		
		out.writeBoolean(vehicleConfiguration!=null);
		if(vehicleConfiguration!=null) {
			vehicleConfiguration.writeStateForStorage(out);
		}
		
		out.writeBoolean(vehicleSteeringCommand!=null);
		if(vehicleSteeringCommand!=null) {
			vehicleSteeringCommand.writeState(out);
		}
		
		out.writeBoolean(modePreferenceReport!=null);
		if(modePreferenceReport!=null) {
			modePreferenceReport.writeState(out);
		}
		
		out.writeBoolean(adtDataLinkStatusReport!=null);
		if(adtDataLinkStatusReport!=null) {
			adtDataLinkStatusReport.writeState(out);
		}
		
		out.writeBoolean(gdtDataLinkStatusReport!=null);
		if(gdtDataLinkStatusReport!=null) {
			gdtDataLinkStatusReport.writeState(out);
		}
		
		IOHelper.writeMapStateIntKey(payloads, out);
	}

	public void onAlertUpdated(SubsystemStatusAlert m) {
		int wid = Math.abs(m.getWarningID());
		synchronized (subsystemStatusAlerts) {
			SubsystemStatusAlert ma = (SubsystemStatusAlert)m.createCopy();
			AlertWrapper r = subsystemStatusAlerts.get(wid);
			if(r==null) {
				r = new AlertWrapper(ma, 0);
				r.setPriorityStartTime(m.getTimeStamp());
				subsystemStatusAlerts.put(wid, r);
				addConsoleAlert(m);
			} else {
				if(!r.getSubsystemStatusAlert().getPriority().equals(m.getPriority())) {
					r.setPriorityStartTime(m.getTimeStamp());
					r.getSubsystemStatusAlert().setPriority(m.getPriority());
					addConsoleAlert(m);
				}
			}
		}
	}
	
	private void addConsoleAlert(SubsystemStatusAlert m) {
		String s = StringHelper.formatTimestamp(m.getTimeStamp()) + ": ("+ m.getPriority() + ") " + m.getText() + " ("+ m.getSubsystemID() + " " + m.getWarningID() +")" + "\n";
		if(!s.equals(lastConsoleLine)) {
			alertsConsole += s;
			if(alertsConsole.length()>99999) {
				alertsConsole = alertsConsole.substring(88888);
			}
			lastConsoleLine = s;
		}
	}
	public void refreshAlerts() {
		//cleanup old alerts
		List<Integer> warningsToBeRemoved = new ArrayList<Integer>();
		for (Entry<Integer,AlertWrapper> sa : subsystemStatusAlerts.entrySet()) {
			//remove old cleared alerts
			if(sa.getValue().getSubsystemStatusAlert().getPriority().equals(AlertPriority.CLEARED)) {
				if(sa.getValue().getPriorityTime()>15) {
					warningsToBeRemoved.add(sa.getKey());
				}
			} else {
				//remove alerts that were already shown for a fixed time
				if(sa.getValue().getSubsystemStatusAlert().getType().equals(AlertType.DISPLAY_FOR_FIXED_TIME_THEN_CLEAR)) {
					if(sa.getValue().getPriorityTime()>sa.getValue().getSubsystemStatusAlert().getPersistence()) {
						warningsToBeRemoved.add(sa.getKey());
					}
				}
			}
		}
		for (Integer i : warningsToBeRemoved) {
			subsystemStatusAlerts.remove(i);
		}
	}
	
	public Map<Integer, AlertWrapper> getSubsystemStatusAlerts() {
		return subsystemStatusAlerts;
	}
	
	public Map<Subsystem, SubsystemState> getSubsystemStates() {
		if(subsystemStates==null) {
			subsystemStates = new HashMap<Subsystem,SubsystemState>();
			for (Subsystem s : Subsystem.values()) {
				subsystemStates.put(s, SubsystemState.NO_STATUS);
			}
		}
		return subsystemStates;
	}
	
	public LoiterConfiguration resolveLoiterConfiguration() {
		try {
			resolveLoiterLock.lock();
			if(loiterConfiguration==null) {
				loiterConfiguration = new LoiterConfiguration();
				loiterConfiguration.setLoiterType(LoiterType.CIRCULAR);
				loiterConfiguration.setAltitudeType(AltitudeType.AGL);
				loiterConfiguration.setLoiterAltitude(300);
				loiterConfiguration.setLoiterSpeed(15);
				loiterConfiguration.setSpeedType(SpeedType.GROUND_SPEED);
				loiterConfiguration.setLoiterDirection(LoiterDirection.VEHICLE_DEPENDENT);
				loiterConfiguration.setLoiterBearing(0);
				loiterConfiguration.setLoiterLength(0);
				loiterConfiguration.setLoiterRadius(60);
				loiterConfiguration.setVehicleID(getVehicleID().getVehicleID());
				loiterConfiguration.setTimeStamp(System.currentTimeMillis()/1000.0);
				loiterConfiguration.setVehicleID(getVehicleID().getVehicleID());
			}
			return loiterConfiguration;
		} finally {
			resolveLoiterLock.unlock();
		}
	}
	
	public boolean isCurrentMode(FlightPathControlMode flightPathMode) {
		VehicleOperatingModeReport recvMode = getLastReceivedMessage(MessageType.M106);
		VehicleOperatingModeCommand sentMode = getLastSentMessage(MessageType.M42);
		if(recvMode!=null) {
			return recvMode.getSelectFlightPathControlMode().equals(flightPathMode);
		} else if(sentMode!=null) {
			return sentMode.getSelectFlightPathControlMode().equals(flightPathMode);
		} else {
			return false;
		}
	}
	
	public LoiterConfiguration getLoiterConfiguration() {
		return loiterConfiguration;
	}
	
	public float getCurrentAltitude(AltitudeType altitudeType) {
		//try to get altitude from inertial states
		InertialStates is = getLastReceivedMessage(MessageType.M101);
		if(is!=null && is.getAltitudeType().equals(altitudeType)) {
			return is.getAltitude();
		}
		
		//try to get from air and ground relative states
		AirAndGroundRelativeStates as = getLastReceivedMessage(MessageType.M102);
		if(as!=null) {
			if(altitudeType.equals(AltitudeType.AGL)) {
				return as.getAglAltitude();
			} else if(altitudeType.equals(AltitudeType.BARO)) {
				return as.getBarometricAltitude();
			} else if(altitudeType.equals(AltitudeType.PRESSURE)) {
				return as.getPressureAltitude();
			} else if(altitudeType.equals(AltitudeType.WGS84)) {
				return as.getWgs84Altitude();
			}
		}
		
		//return 150 if altitude not found (to avoid any accidental command to 0m)
		return 150;
	}

	public float getCurrentCourseHeading() {
		//try to get altitude from inertial states
		InertialStates is = getLastReceivedMessage(MessageType.M101);
		if(is!=null) {
			return CoordinatesHelper.calculateHeading(is.getUSpeed(), is.getVSpeed());
		}

		//try to get from air and ground relative states
		AirAndGroundRelativeStates as = getLastReceivedMessage(MessageType.M102);
		if(as!=null) {
			return CoordinatesHelper.calculateHeading(as.getUGround(), as.getVGround());
		}
		
		//return 0 if heading not found
		return 0;
	}	

	public float getCurrentSpeed(SpeedType speedType) {
		//try to get speed from inertial states
		InertialStates is = getLastReceivedMessage(MessageType.M101);
		if(is!=null && speedType.equals(SpeedType.GROUND_SPEED)) {
			return MeasureHelper.calculateMagnitude(is.getUSpeed(), is.getVSpeed());
		}
		
		//try to get from air and ground relative states
		AirAndGroundRelativeStates as = getLastReceivedMessage(MessageType.M102);
		if(as!=null) {
			return as.getSpeed(speedType);
		}

		//return 0 if heading not found
		return 0;
	}
	
	public VehicleSteeringCommand getVehicleSteeringCommand() {
		return vehicleSteeringCommand;
	}
	public void setVehicleSteeringCommand(VehicleSteeringCommand vehicleSteeringCommand) {
		this.vehicleSteeringCommand = vehicleSteeringCommand;
	}
	
	public ModePreferenceReport getModePreferenceReport() {
		return modePreferenceReport;
	}
	public void setModePreferenceReport(ModePreferenceReport modePreferenceReport) {
		this.modePreferenceReport = modePreferenceReport;
	}
	
	public String getAlertsConsole() {
		return alertsConsole;
	}
	
	public DataLinkStatusReport getAdtDataLinkStatusReport() {
		return adtDataLinkStatusReport;
	}
	public DataLinkStatusReport getGdtDataLinkStatusReport() {
		return gdtDataLinkStatusReport;
	}
	public void setAdtDataLinkStatusReport(DataLinkStatusReport adtDataLinkStatusReport) {
		this.adtDataLinkStatusReport = adtDataLinkStatusReport;
	}
	public void setGdtDataLinkStatusReport(DataLinkStatusReport gdtDataLinkStatusReport) {
		this.gdtDataLinkStatusReport = gdtDataLinkStatusReport;
	}
	
}
