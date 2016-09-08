package br.skylight.commons;

import java.util.logging.Logger;

import br.skylight.commons.dli.enums.AlertPriority;
import br.skylight.commons.dli.enums.AlertType;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;

public enum Alert {

	ALTITUDE_OUTSIDE_VEHICLE_LIMITS		(Subsystem.NAVIGATION, 	AlertPriority.CAUTION, 		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			30000, 	true, 1000),
	VEHICLE_OUTSIDE_MISSION_BOUNDARIES	(Subsystem.NAVIGATION, 	AlertPriority.CAUTION,		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			30000, 	true, 1000),
	VEHICLE_INSIDE_PROHIBITED_REGION	(Subsystem.NAVIGATION, 	AlertPriority.CAUTION,		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			30000, 	true, 1000),
	VEHICLE_OUTSIDE_AUTHORIZED_ALTITUDE	(Subsystem.NAVIGATION, 	AlertPriority.CAUTION, 		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			30000, 	true, 1000),
	SHORT_CIRCUIT_IN_MISSION_PLAN		(Subsystem.NAVIGATION, 	AlertPriority.FAILED, 		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			-1, 	true, 0),
	DANGEROUS_VEHICLE_ATTITUDE			(Subsystem.NAVIGATION, 	AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 			 20000, true, 4000),
	STALL_WARNING						(Subsystem.NAVIGATION, 	AlertPriority.EMERGENCY, 	0,	AlertType.CLEARABLE_BY_OPERATOR, 			 20000,	true, 4000),
	IAS_ABOVE_LIMITS					(Subsystem.NAVIGATION, 	AlertPriority.EMERGENCY, 	0,	AlertType.CLEARABLE_BY_OPERATOR, 			 20000,	true, 3000),
	INSUFICIENT_FUEL_TO_RETURN_HOME		(Subsystem.NAVIGATION, 	AlertPriority.CAUTION, 		0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		300000, true, 0),
	MAX_TIME_FLYING_REACHED_VEHICLE		(Subsystem.NAVIGATION, 	AlertPriority.CAUTION, 		0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		300000,	true, 0),
	MAX_TIME_FLYING_REACHED_MISSION		(Subsystem.NAVIGATION, 	AlertPriority.CAUTION, 		0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		300000,	true, 0),
	ZERO_RPM_DETECTED					(Subsystem.ENGINE, 		AlertPriority.EMERGENCY, 	0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		 60000,	true, 0),
	ENGINE_TEMPERATURE_TOO_HIGH			(Subsystem.ENGINE, 		AlertPriority.EMERGENCY,	0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		 60000,	true, 0),
//	LOW_FUEL							(Subsystem.PROPULSION_ENERGY,AlertPriority.EMERGENCY,0,	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		 60000,	true, 0),

	ADT_LATENCY_WARNING					(Subsystem.COMMS, 		AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 			 30000, true, 2000),
	ADT_DOWNLINK_STRENGTH_WARNING		(Subsystem.COMMS, 		AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 			 30000, true, 2000),
	GDT_DOWNLINK_STRENGTH_WARNING		(Subsystem.COMMS, 		AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 			 30000, true, 2000),
	GPS_LINK_WARNING					(Subsystem.COMMS, 		AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 		 	 60000,	true, 2000),

	//SPECIAL ALERTS WHOSE SAFETY ACTION IS CONFIGURED ON RULES OF SAFETY
	ADT_DOWNLINK_FAILED					(Subsystem.COMMS, 		AlertPriority.FAILED, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 			 30000, true, 0),
	GPS_LINK_FAILED						(Subsystem.NAVIGATION,	AlertPriority.FAILED, 		0,	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		 20000,	true, 0),
	
	SENSOR_SATURATION_WARNING			(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		10,	AlertType.DISPLAY_FOR_FIXED_TIME_THEN_CLEAR, 10000,	true, 0),
	IMU_FAILED							(Subsystem.ELECTRICAL, 	AlertPriority.FAILED, 		0,	AlertType.NOT_CLEARABLE_BY_OPERATOR,  		 10000,	true, 3000),
	HARDWARE_MESSAGE_CRC_ERROR			(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR,  		 	 60000,	true, 0),
	AUTOPILOT_TEMPERATURE_TOO_HIGH		(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			 60000, true, 0),
	STATIC_PRESSURE_SENSOR_FAILED		(Subsystem.ELECTRICAL, 	AlertPriority.FAILED, 		0,	AlertType.NOT_CLEARABLE_BY_OPERATOR, 	     60000,	true, 3000),
	PITOT_PRESSURE_SENSOR_FAILED		(Subsystem.ELECTRICAL, 	AlertPriority.FAILED, 		0,	AlertType.NOT_CLEARABLE_BY_OPERATOR, 	     60000,	true, 3000),
	LOW_ACTUATOR_MESSAGES_FREQUENCY		(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 	     	 60000,	false, 0),
	HARDWARE_SYSTEM_RESET_WARNING		(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR,  		     60000,	true, 2000),
	HARDWARE_GENERIC_WARNING			(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR,  		     60000,	true, 3000),
	HARDWARE_GENERIC_FAILURE			(Subsystem.ELECTRICAL, 	AlertPriority.FAILED, 		0,	AlertType.CLEARABLE_BY_OPERATOR,  		     60000,	true, 3000),
	MAIN_BATTERY_WARNING				(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0,	AlertType.NOT_CLEARABLE_BY_OPERATOR,  		 60000,	true, 3000),
	AUX_BATTERY_WARNING					(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0,	AlertType.NOT_CLEARABLE_BY_OPERATOR,  		 60000,	true, 3000),
	
	FUEL_NOT_SET						(Subsystem.ENGINE, 		AlertPriority.WARNING, 		0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR,		 10000, false, 0),
	ONBOARD_SYSTEMS_NOT_CALIBRATED		(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			300000, false, 0),
	HARDWARE_RESET_DETECTED				(Subsystem.ELECTRICAL, 	AlertPriority.WARNING, 		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			 60000, false, 0),
	GROUND_LEVEL_NOT_SET				(Subsystem.NAVIGATION,	AlertPriority.WARNING, 		0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		300000, false, 0),
	
	MISSION_WARNING						(Subsystem.NAVIGATION, 	AlertPriority.WARNING, 		30,	AlertType.DISPLAY_FOR_FIXED_TIME_THEN_CLEAR, 120000, false, 0),
	MISSION_ERROR						(Subsystem.NAVIGATION, 	AlertPriority.FAILED, 		0, 	AlertType.CLEARABLE_BY_OPERATOR, 			120000, false, 0),
	SAFETY_PROCEDURE_ACTIVATED			(Subsystem.NAVIGATION, 	AlertPriority.WARNING, 		0,	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		 60000, false, 0),
	FLIGHT_TERMINATION_HARDWARE_ACTIVATED(Subsystem.NAVIGATION, AlertPriority.EMERGENCY, 	0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		 60000,	false, 0),
	
	LOW_DISK_SPACE						(Subsystem.VSM_STATUS, 	AlertPriority.WARNING, 		0, 	AlertType.NOT_CLEARABLE_BY_OPERATOR, 		 60000, false, 0),
	SOFTWARE_FINE						(Subsystem.VSM_STATUS, 	AlertPriority.NOMINAL, 		10,	AlertType.DISPLAY_FOR_FIXED_TIME_THEN_CLEAR,   500, false, 0),
	SOFTWARE_INFO						(Subsystem.VSM_STATUS, 	AlertPriority.NOMINAL, 		10,	AlertType.DISPLAY_FOR_FIXED_TIME_THEN_CLEAR,   500, false, 0),
	SOFTWARE_WARNING					(Subsystem.VSM_STATUS, 	AlertPriority.WARNING, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 			   200, false, 0),
	SOFTWARE_ERROR						(Subsystem.VSM_STATUS, 	AlertPriority.FAILED, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 			   200, false, 0),
	SOFTWARE_WORKER_WARNING				(Subsystem.VSM_STATUS, 	AlertPriority.WARNING, 		10,	AlertType.DISPLAY_FOR_FIXED_TIME_THEN_CLEAR,  1000, false, 500),
	SOFTWARE_WORKER_ERROR				(Subsystem.VSM_STATUS, 	AlertPriority.FAILED, 		0,	AlertType.CLEARABLE_BY_OPERATOR, 			  1000, false, 0);

	private static final Logger logger = Logger.getLogger(Alert.class.getName());
	
	private SubsystemStatusAlert subsystemStatusAlert;
	private AlertPriority priorityOnActivation;
	private long minTimeForReactivationMillis;
	private boolean safetyActionEnabled;
	private long timeOnSituationForActivationMillis;

	private Alert(Subsystem subsystemID, AlertPriority priority, int persistence, AlertType type, long minTimeForReactivationMillis, boolean safetyActionEnabled, long timeOnSituationForActivationMillis) {
		this.subsystemStatusAlert = new SubsystemStatusAlert(ordinal(), subsystemID, AlertPriority.NOMINAL, (byte)persistence, type);
		this.priorityOnActivation = priority;
		this.minTimeForReactivationMillis = minTimeForReactivationMillis;
		this.safetyActionEnabled = safetyActionEnabled;
		this.timeOnSituationForActivationMillis = timeOnSituationForActivationMillis;
	}
	
	public SubsystemStatusAlert getSubsystemStatusAlert() {
		return subsystemStatusAlert;
	}
	
	public boolean isSafetyActionEnabled() {
		return safetyActionEnabled;
	}
	
	public long getMinTimeForReactivationMillis() {
		return minTimeForReactivationMillis;
	}
	
	public AlertPriority getPriorityOnActivation() {
		return priorityOnActivation;
	}
	
	public long getTimeOnSituationForActivationMillis() {
		return timeOnSituationForActivationMillis;
	}
	
	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	}
	
}
