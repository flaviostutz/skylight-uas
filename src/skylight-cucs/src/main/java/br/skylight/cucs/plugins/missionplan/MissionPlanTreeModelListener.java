package br.skylight.cucs.plugins.missionplan;

import br.skylight.commons.Mission;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;

public interface MissionPlanTreeModelListener {

	public void onPositionWaypointUpdated(AVPositionWaypoint pw, Object newValue, int column);
	public void onRouteUpdated(AVRoute route, Object newValue, int column);
	public void onMissionUpdated(Mission mission);

}
