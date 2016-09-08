package br.skylight.simulation.flightgear;

import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugins.watchdog.WatchDogKickerService;

@ServiceImplementation(serviceDefinition=WatchDogKickerService.class)
public class FlightGearWatchDogKickerService extends WatchDogKickerService {

	public static final String IDENTIFICATION = "flightgear-uav";
	
	@Override
	public String getIdentification() {
		return IDENTIFICATION;
	}

}
