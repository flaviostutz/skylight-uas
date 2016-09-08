package br.skylight.simulation.xplane;

import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugins.watchdog.WatchDogKickerService;

@ServiceImplementation(serviceDefinition=WatchDogKickerService.class)
public class XPlaneWatchDogKickerService extends WatchDogKickerService {

	public static final String IDENTIFICATION = "xplane-uav";
	
	@Override
	public String getIdentification() {
		return IDENTIFICATION;
	}

}
