package br.skylight.simulation.dummy;

import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugins.watchdog.WatchDogKickerService;

@ServiceImplementation(serviceDefinition=WatchDogKickerService.class)
public class DummyWatchDogKickerService extends WatchDogKickerService {

	public static final String IDENTIFICATION = "dummy-uav";
	
	@Override
	public String getIdentification() {
		return IDENTIFICATION;
	}

}
