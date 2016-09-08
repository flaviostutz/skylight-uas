package br.skylight.commons.dli.services;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.RulesOfSafety;
import br.skylight.commons.ServoConfiguration;
import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.configuration.DisplayUnitRequest;
import br.skylight.commons.dli.configuration.FieldConfigurationCommand;
import br.skylight.commons.dli.configuration.FieldConfigurationDoubleResponse;
import br.skylight.commons.dli.configuration.FieldConfigurationEnumeratedResponse;
import br.skylight.commons.dli.configuration.FieldConfigurationIntegerResponse;
import br.skylight.commons.dli.configuration.FieldConfigurationRequest;
import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.MessageSource;
import br.skylight.commons.dli.messagetypes.GenericInformationRequestMessage;
import br.skylight.commons.dli.messagetypes.MessageAcknowledgeConfiguration;
import br.skylight.commons.dli.messagetypes.MessageAcknowledgement;
import br.skylight.commons.dli.messagetypes.ScheduleMessageUpdateCommand;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.dli.mission.MissionUploadCommand;
import br.skylight.commons.dli.mission.MissionUploadDownloadStatus;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.dli.payload.EOIRConfigurationState;
import br.skylight.commons.dli.payload.EOIRLaserOperatingState;
import br.skylight.commons.dli.payload.EOIRLaserPayloadCommand;
import br.skylight.commons.dli.payload.PayloadBayCommand;
import br.skylight.commons.dli.payload.PayloadBayStatus;
import br.skylight.commons.dli.payload.PayloadConfigurationMessage;
import br.skylight.commons.dli.payload.PayloadSteeringCommand;
import br.skylight.commons.dli.skylight.GenericSystemCommand;
import br.skylight.commons.dli.skylight.MiscInfoMessage;
import br.skylight.commons.dli.skylight.MissionAnnotationsMessage;
import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControllerCommand;
import br.skylight.commons.dli.skylight.PIDControllerState;
import br.skylight.commons.dli.skylight.ServoActuationCommand;
import br.skylight.commons.dli.skylight.ServosStateMessage;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.skylight.SoftwarePartReport;
import br.skylight.commons.dli.skylight.SoftwareStatus;
import br.skylight.commons.dli.skylight.StreamChannelCommand;
import br.skylight.commons.dli.skylight.StreamChannelData;
import br.skylight.commons.dli.skylight.TakeoffLandingConfiguration;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusReport;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusRequest;
import br.skylight.commons.dli.systemid.CUCSAuthorisationRequest;
import br.skylight.commons.dli.systemid.VSMAuthorisationResponse;
import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.AirVehicleLights;
import br.skylight.commons.dli.vehicle.EngineCommand;
import br.skylight.commons.dli.vehicle.EngineOperatingStates;
import br.skylight.commons.dli.vehicle.FlightTerminationCommand;
import br.skylight.commons.dli.vehicle.FlightTerminationModeReport;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.ModePreferenceCommand;
import br.skylight.commons.dli.vehicle.ModePreferenceReport;
import br.skylight.commons.dli.vehicle.VehicleConfigurationCommand;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleLightsState;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeReport;
import br.skylight.commons.dli.vehicle.VehicleOperatingStates;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;

public enum MessageType {

	/**CUCS Authorization Request*/
	M1(1, 	MessageSource.CUCS, true, false, 2000, 	"CUCS Authorization Request", CUCSAuthorisationRequest.class, new BitmappedLOI(2,3,4,5), 5),
	/**Vehicle ID*/
	M20(20, MessageSource.VSM, true, true, 1000, 	"Vehicle ID", VehicleID.class, new BitmappedLOI(2,3,4,5), 5),
	/**VSM Authorization Response*/
	M21(21, MessageSource.VSM, true, true, 2000, 	"VSM Authorization Response", VSMAuthorisationResponse.class, new BitmappedLOI(2,3,4,5), 5),
	/**Vehicle Configuration Command*/
	M40(40, MessageSource.CUCS, true, false, 2000, 	"Vehicle Configuration Command", VehicleConfigurationCommand.class, new BitmappedLOI(4,5), 5),
	/**Loiter Configuration*/
	M41(41, MessageSource.CUCS, true, false, 2000, 	"Loiter Configuration", LoiterConfiguration.class, new BitmappedLOI(4,5), 5),
	/**Vehicle Operating Mode Command*/
	M42(42, MessageSource.CUCS, true, false, 1000, 	"Vehicle Operating Mode Command", VehicleOperatingModeCommand.class, new BitmappedLOI(4,5), 5),
	/**Vehicle Steering Command*/
	M43(43, MessageSource.CUCS, true, false, 1000, 	"Vehicle Steering Command", VehicleSteeringCommand.class, new BitmappedLOI(4,5), 0),
	/**Air Vehicle Lights*/
	M44(44, MessageSource.CUCS, true, false, 500, 	"Air Vehicle Lights", AirVehicleLights.class, new BitmappedLOI(4,5), 5),
	/**Engine Command*/
	M45(45, MessageSource.CUCS, true, false, 500, 	"Engine Command", EngineCommand.class, new BitmappedLOI(4,5), 5),
	/**Flight Termination Command*/
	M46(46, MessageSource.CUCS, true, false, 1000, 	"Flight Termination Command", FlightTerminationCommand.class, new BitmappedLOI(4,5), 0),
	/**Preference Mode Command*/
	M48(48, MessageSource.CUCS, true, false, 1000, 	"Vehile Steering Command", ModePreferenceCommand.class, new BitmappedLOI(4,5), 5),
	/**Vehicle Configuration*/
	M100(100, MessageSource.CUCS_VSM, false, true, 10000, "Vehicle Configuration", VehicleConfigurationMessage.class, new BitmappedLOI(4,5), 5),
	/**Inertial States*/
	M101(101, MessageSource.VSM, true, false, 1000, "Inertial States", InertialStates.class, new BitmappedLOI(2,3,4,5), 0),
	/**Air and Ground Relative States*/
	M102(102, MessageSource.VSM, true, false, 1000, "Air and Ground Relative States", AirAndGroundRelativeStates.class, new BitmappedLOI(4,5), 1),
	/**Vehicle Operating States*/
	M104(104, MessageSource.VSM, true, true, 1000, 	"Vehicle Operating States", VehicleOperatingStates.class, new BitmappedLOI(4,5), 2),
	/**Engine Operating States*/
	M105(105, MessageSource.VSM, true, true, 500, 	"Engine Operating States", EngineOperatingStates.class, new BitmappedLOI(4,5), 2),
	/**Vehicle Operating Mode Report*/
	M106(106, MessageSource.VSM, true, true, 2000, 	"Vehicle Operating Mode Report", VehicleOperatingModeReport.class, new BitmappedLOI(4,5), 2),
	/**Vehicle Lights State*/
	M107(107, MessageSource.VSM, true, false, 500, 	"Vehicle Lights State", VehicleLightsState.class, new BitmappedLOI(4,5), 5),
	/**Flight Termination Mode Report*/
	M108(108, MessageSource.VSM, true, true, 2000, 	"Flight Termination Mode Report", FlightTerminationModeReport.class, new BitmappedLOI(4,5), 0), 
	/**Mode Preference Report*/
	M109(109, MessageSource.VSM, true, false, 2000, "Mode Preference Report", ModePreferenceReport.class, new BitmappedLOI(4,5), 2),
	/**From-To-Next Waypoint States*/
	M110(110, MessageSource.VSM, true, false, 2000, "From-To-Next Waypoint States", FromToNextWaypointStates.class, new BitmappedLOI(2,3,4,5), 5),
	/**Payload Steering Command*/
	M200(200, MessageSource.CUCS, true, false, 200, "Payload Steering Command", PayloadSteeringCommand.class, new BitmappedLOI(3), 0),
	/**EO/IR/Laser Payload Command*/
	M201(201, MessageSource.CUCS, true, false, 1000, "EO/IR/Laser Payload Command", EOIRLaserPayloadCommand.class, new BitmappedLOI(3), 5),
	/**Payload Bay Command*/
	M206(206, MessageSource.CUCS, true, false, 2000, "Payload Bay Command", PayloadBayCommand.class, new BitmappedLOI(3), 5),
	/**Payload Configuration*/
	M300(300, MessageSource.VSM, true, true, 1000, 	"Payload Configuration", PayloadConfigurationMessage.class, new BitmappedLOI(2,3), 5),
	/**EO/IR Configuration State*/
	M301(301, MessageSource.VSM, false, true, 200, 	"EO/IR Configuration State", EOIRConfigurationState.class, new BitmappedLOI(2,3), 6),
	/**EO/IR/Laser Operating State*/
	M302(302, MessageSource.VSM, true, true, 2000, 	"EO/IR/Laser Operating State", EOIRLaserOperatingState.class, new BitmappedLOI(2,3), 6),
	/**Payload Bay Status*/
	M308(308, MessageSource.VSM, true, true, 2000, 	"Payload Bay Status", PayloadBayStatus.class, new BitmappedLOI(3), 6),
	/**Data Link Set Up Message*/
	M400(400, MessageSource.CUCS, true, false, 1000, "Data Link Set Up Message", DataLinkSetupMessage.class, new BitmappedLOI(2,3,4,5), 2),
	/**Data Link Control Command*/
	M401(401, MessageSource.CUCS, true, false, 2000, "Data Link Control Command", DataLinkControlCommand.class, new BitmappedLOI(2,3,4,5), 2),
	/**Data Link Status Report*/
	M501(501, MessageSource.CUCS_VSM, true, true, 1000, "Data Link Status Report", DataLinkStatusReport.class, new BitmappedLOI(2,3,4,5), 2),
	/**Mission Upload Command*/
	M800(800, MessageSource.CUCS, true, false, 1000, "Mission Upload Command", MissionUploadCommand.class, new BitmappedLOI(4,5), 5),
	/**AV Route*/
	M801(801, MessageSource.CUCS_VSM, true, true, 2000, "AV Route", AVRoute.class, new BitmappedLOI(4,5), 5),
	/**AV Position Waypoint*/
	M802(802, MessageSource.CUCS_VSM, true, true, 2000, "AV Position Waypoint", AVPositionWaypoint.class, new BitmappedLOI(4,5), 5),
	/**AV Loiter Waypoint*/
	M803(803, MessageSource.CUCS_VSM, true, true, 2000, "AV Loiter Waypoint", AVLoiterWaypoint.class, new BitmappedLOI(4,5), 5),
	/**Payload Action Waypoint*/
	M804(804, MessageSource.CUCS_VSM, true, true, 2000, "Payload Action Waypoint", PayloadActionWaypoint.class, new BitmappedLOI(3,4,5), 5),
	/**Mission Upload/Download Status*/
	M900(900, MessageSource.VSM, true, false, 2000, 	"Mission Upload/Download Status", MissionUploadDownloadStatus.class, new BitmappedLOI(3,4,5), 7),
	/**Subsystem Status Request*/
	M1000(1000, MessageSource.CUCS, true, false, 1000, 	"Subsystem Status Request", SubsystemStatusRequest.class, new BitmappedLOI(2,3,4,5), 7),
	/**Subsystem Status Alert*/
	M1100(1100, MessageSource.CUCS_VSM, true, false, 1000, "Subsystem Status Alert", SubsystemStatusAlert.class, new BitmappedLOI(2,3,4,5), 5),
	/**Subsystem Status Report*/
	M1101(1101, MessageSource.VSM, true, true, 1000, 	"Subsystem Status Report", SubsystemStatusReport.class, new BitmappedLOI(2,3,4,5), 5),
	/**Field Configuration Request*/
	M1200(1200, MessageSource.CUCS, true, false, 2000, 	"Field Configuration Request", FieldConfigurationRequest.class, new BitmappedLOI(2,3,4,5), 5),
	/**Display Unit Request*/
	M1201(1201, MessageSource.CUCS, true, false, 2000, 	"Display Unit Request", DisplayUnitRequest.class, new BitmappedLOI(2,3,4,5), 5),
	/**Field Configuration Integer Response*/
	M1300(1300, MessageSource.VSM, false, true, 2000, 	"Field Configuration Integer Response", FieldConfigurationIntegerResponse.class, new BitmappedLOI(2,3,4,5), 5),
	/**Field Configuration Double Response*/
	M1301(1301, MessageSource.VSM, false, true, 2000, 	"Field Configuration Double Response", FieldConfigurationDoubleResponse.class, new BitmappedLOI(2,3,4,5), 5),
	/**Field Configuration Enumerated Response*/
	M1302(1302, MessageSource.VSM, false, true, 2000, 	"Field Configuration Enumerated Response", FieldConfigurationEnumeratedResponse.class, new BitmappedLOI(2,3,4,5), 5),
	/**Field Configuration Command*/
	M1303(1303, MessageSource.VSM, true, false, 2000, 	"Field Configuration Command", FieldConfigurationCommand.class, new BitmappedLOI(2,3,4,5), 5),
	/**Message Acknowledgement*/
	M1400(1400, MessageSource.CUCS_VSM, false, true, 1000, "Message Acknowledgement", MessageAcknowledgement.class, new BitmappedLOI(2,3,4,5), 1),
	/**Message Acknowledge Configuration*/
	M1401(1401, MessageSource.CUCS_VSM, true, false, 2000, "Message Acknowledge Configuration", MessageAcknowledgeConfiguration.class, new BitmappedLOI(2,3,4,5), 5),
	/**Schedule Message Update Command*/
	M1402(1402, MessageSource.CUCS_VSM, true, false, 2000, "Schedule Message Update Command", ScheduleMessageUpdateCommand.class, new BitmappedLOI(2,3,4,5), 5),
	/**Generic Information Request Message*/
	M1403(1403, MessageSource.CUCS_VSM, true, false, 1000, "Generic Information Request Message", GenericInformationRequestMessage.class, new BitmappedLOI(2,3,4,5), 5),
	/**Skylight Vehicle Configuration Message*/
	M2000(2000, MessageSource.CUCS_VSM, true, false, 10000, "Skylight Vehicle Configuration Message", SkylightVehicleConfigurationMessage.class, new BitmappedLOI(4,5), 5),
	/**Skylight Vehicle Configuration Command*/
//	M2001(2001, MessageSource.CUCS, true, false, 2000, "Skylight Vehicle Configuration Command", SkylightVehicleConfigurationCommand.class, new BitmappedLOI(4,5), 5),
	/**Servo Actuation Command*/
	M2003(2003, MessageSource.CUCS, true, false, 200, 	"Servo Actuation Command", ServoActuationCommand.class, new BitmappedLOI(4,5), 1),
	/**PID Controller Command*/
	M2004(2004, MessageSource.CUCS, true, false, 1000, 	"PID Controller Command", PIDControllerCommand.class, new BitmappedLOI(4,5), 5),
	/**Misc Info Message*/
	M2005(2005, MessageSource.VSM, true, false, 2000, 	"Misc Info Message", MiscInfoMessage.class, new BitmappedLOI(2,3,4,5), 5),
	/**Takeoff Landing Configuration*/
	M2006(2006, MessageSource.CUCS_VSM, true, true, 2000, "Takeoff Landing Configuration", TakeoffLandingConfiguration.class, new BitmappedLOI(5), 5),
	/**Rules Of Safety*/
	M2007(2007, MessageSource.CUCS_VSM, true, true, 2000, "Rules Of Safety", RulesOfSafety.class, new BitmappedLOI(4,5), 5), 
	/**Ping Message*/
//	M2008(2008, MessageSource.CUCS_VSM, true, true, 3000, "Ping Message", PingMessage.class, new BitmappedLOI(2,3,4,5), 5), 
	/**Servo Configuration*/
	M2009(2009, MessageSource.CUCS_VSM, true, true, 3000, "Servo Configuration", ServoConfiguration.class, new BitmappedLOI(4,5), 5), 
	/**PID Configuration*/
	M2010(2010, MessageSource.CUCS_VSM, true, true, 3000, "PID Configuration", PIDConfiguration.class, new BitmappedLOI(4,5), 5), 
	/**PID Controller State*/
	M2011(2011, MessageSource.VSM, true, true, 1000, 	"PID Controller State", PIDControllerState.class, new BitmappedLOI(4,5), 8),
	/**Servos State Message*/
	M2012(2012, MessageSource.VSM, true, true, 1000, 	"Servos State Message", ServosStateMessage.class, new BitmappedLOI(4,5), 8),
	/**Software Status*/
	M2013(2013, MessageSource.VSM, true, true, 1000, 	"Software Status", SoftwareStatus.class, new BitmappedLOI(4,5), 5),
	/**Stream Channel Data*/
	M2014(2014, MessageSource.CUCS_VSM, false, true, 2000, "Stream Channel Data", StreamChannelData.class, new BitmappedLOI(4,5), 4),
	/**Stream Channel Command*/
	M2015(2015, MessageSource.CUCS_VSM, false, true, 2000, "Stream Channel Command", StreamChannelCommand.class, new BitmappedLOI(4,5), 4), 
	/**Mission Annotations Message*/
	M2016(2016, MessageSource.CUCS_VSM, true, true, 2000, "Mission Annotations Message", MissionAnnotationsMessage.class, new BitmappedLOI(4,5), 4), 
	/**Generic System Command*/
	M2017(2017, MessageSource.CUCS, false, true, 2000, "Generic System Command", GenericSystemCommand.class, new BitmappedLOI(4,5), 3), 
	/**Software Part Report*/
	M2018(2018, MessageSource.VSM, true, true, 1000, "Software Part Report", SoftwarePartReport.class, new BitmappedLOI(4,5), 4); 
	
	private static final Logger logger = Logger.getLogger(MessageType.class.getName());
	
	private long number;
	private String name;
	private Class<? extends Message> implementation;
	private MessageSource source;
	private boolean push;
	private boolean pull;
	private long maxLatency;
	private BitmappedLOI lois;
	private int priority;
	private Map<MessageType,Map<Integer,Field>> fieldsMap = new HashMap<MessageType,Map<Integer,Field>>();
	
	private MessageType(long number, MessageSource source, boolean push, boolean pull, long maxLatency, String name, Class<? extends Message> implementation, BitmappedLOI lois, int priority) {
		this.number = number;
		this.name = name;
		this.implementation = implementation;
		this.source = source;
		this.push = push;
		this.pull = pull;
		this.maxLatency = maxLatency;
		this.lois = lois;
		this.priority = priority;
		
		//index fields by number
		Map<Integer,Field> fds = fieldsMap.get(this);
		if(fds==null) {
			fds = new HashMap<Integer,Field>();
			Field[] fs = getImplementation().getFields();
			for (int a=1; a<=fs.length; a++) {
				for (int i=0; i<fs.length; i++) {
					MessageField mf = fs[i].getAnnotation(MessageField.class);
					if(mf!=null) {
						if(mf.number()==a) {
							fds.put(a, fs[i]);
							break;
						}
					}
				}
			}
			fieldsMap.put(this, fds);
		}
	}
	
	public Map<Integer,Field> getFields() {
		return fieldsMap.get(this);
	}
	
	public long getNumber() {
		return number;
	}
	
	public Class<? extends Message> getImplementation() {
		return implementation;
	}
	public long getMaxLatency() {
		return maxLatency;
	}
	public String getName() {
		return name;
	}
	public MessageSource getSource() {
		return source;
	}
	
	public BitmappedLOI getLOIs() {
		return lois;
	}
	
	public static MessageType getMessageType(long number) {
		for (MessageType mt : values()) {
			if(mt.getNumber()==number) {
				return mt;
			}
		}
		throw new IllegalArgumentException("Message type " + number + " was not found");
	}
	
	@Override
	public String toString() {
		return getName() + " (#" + number + ")";
	}
	
	public int getPriority() {
		return priority;
	}
	
	/**
	 * Messages sent upon an event or periodically
	 */
	public boolean isPush() {
		return push;
	}
	/**
	 * Messages sent when requested
	 */
	public boolean isPull() {
		return pull;
	}
	
}
