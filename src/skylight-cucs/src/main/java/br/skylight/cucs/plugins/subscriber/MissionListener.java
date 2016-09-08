package br.skylight.cucs.plugins.subscriber;

import br.skylight.commons.EventType;
import br.skylight.commons.Mission;
import br.skylight.commons.dli.mission.AVPositionWaypoint;

public interface MissionListener {

	public void onMissionEvent(Mission mission, EventType type);
//	public void onRegionEvent(Mission mission, Region region, EventType type);
	public void onWaypointEvent(Mission mission, AVPositionWaypoint pw, EventType type);
	
}