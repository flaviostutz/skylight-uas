package br.skylight.commons.dli.skylight;

import br.skylight.commons.MeasureType;

public enum PIDControl {
	
	HOLD_ROLL_WITH_AILERON("Hold roll with aileron", 			MeasureType.ATTITUDE_ANGLES, 	"Roll", 	"Aileron"),
	HOLD_PITCH_WITH_ELEV_RUDDER("Hold pitch with elev/rudder", 	MeasureType.ATTITUDE_ANGLES, 	"Pitch", 	"Elevator/Rudder"),
	HOLD_IAS_WITH_THROTTLE("Hold IAS with throttle",			MeasureType.AIR_SPEED,			"IAS", 		"Throttle"),
	HOLD_COURSE_WITH_YAW("Hold course with yaw",				MeasureType.HEADING,			"Course",	"Yaw"),
	HOLD_YAW_WITH_ELEV_RUDDER("Hold yaw with elev/rudder",		MeasureType.HEADING,			"Yaw",		"Elevator/Rudder"),
	HOLD_ALTITUDE_WITH_PITCH("Hold altitude with pitch",		MeasureType.ALTITUDE,			"Altitude", "Pitch"),
	HOLD_GROUNDSPEED_WITH_IAS("Hold groundspeed with IAS",		MeasureType.GROUND_SPEED, 		"Groundspeed", "IAS"),
	HOLD_IAS_WITH_PITCH("Hold IAS with pitch",					MeasureType.AIR_SPEED,			"IAS", 		"Pitch"),
	HOLD_COURSE_WITH_ROLL("Hold course with roll",				MeasureType.HEADING,			"Course", 	"Roll"),
	HOLD_ALTITUDE_WITH_THROTTLE("Hold altitude with throttle", 	MeasureType.ALTITUDE, 		"Altitude", "Throttle");
	
	private String name;
	private String objectiveElementName;
	private String actuationElementName;
	private MeasureType setpointMeasureType;

	private PIDControl(String name, MeasureType setpointMeasureType, String objectiveElementName, String actuationElementName) {
		this.name = name;
		this.setpointMeasureType = setpointMeasureType;
		this.objectiveElementName = objectiveElementName;
		this.actuationElementName = actuationElementName;
	}
	
	public MeasureType getSetpointMeasureType() {
		return setpointMeasureType;
	}
	
	public String getName() {
		return name;
	}
	
	public String getActuationElementName() {
		return actuationElementName;
	}
	
	public String getObjectiveElementName() {
		return objectiveElementName;
	}
	
}
