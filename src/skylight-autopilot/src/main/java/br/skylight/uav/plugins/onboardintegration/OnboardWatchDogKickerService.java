package br.skylight.uav.plugins.onboardintegration;

import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugins.watchdog.WatchDogKickerService;

@ServiceImplementation(serviceDefinition=WatchDogKickerService.class)
public class OnboardWatchDogKickerService extends WatchDogKickerService {

	public static final String IDENTIFICATION = "onboard-uav";
	
	@Override
	public String getIdentification() {
		return IDENTIFICATION;
	}

}
