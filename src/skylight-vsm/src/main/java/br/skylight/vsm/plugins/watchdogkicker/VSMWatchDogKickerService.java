package br.skylight.vsm.plugins.watchdogkicker;

import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugins.watchdog.WatchDogKickerService;

@ServiceImplementation(serviceDefinition=WatchDogKickerService.class)
public class VSMWatchDogKickerService extends WatchDogKickerService {

	public static final String IDENTIFICATION = "vsm";
	
	@Override
	public String getIdentification() {
		return IDENTIFICATION;
	}

}
