package br.skylight.commons.dli.enums;

public enum MissionPlanMode {

	NOT_USED,
	CLEAR_MISSION,
	LOAD_MISSION,
	DOWNLOAD_MISSION,
	DOWNLOAD_SINGLE_WAYPOINT,
	CANCEL_UPLOAD_OR_DOWNLOAD,
	
	//internal state for receiving waypoints
	RECEIVE_MISSION;
	
}
