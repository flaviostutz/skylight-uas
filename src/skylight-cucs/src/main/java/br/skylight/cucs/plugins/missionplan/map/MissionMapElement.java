package br.skylight.cucs.plugins.missionplan.map;

import br.skylight.commons.Mission;
import br.skylight.cucs.mapkit.MapElement;

public class MissionMapElement extends MapElement {

	private Mission mission;
	
	public Mission getMission() {
		return mission;
	}
	public void setMission(Mission mission) {
		this.mission = mission;
	}
	
}
