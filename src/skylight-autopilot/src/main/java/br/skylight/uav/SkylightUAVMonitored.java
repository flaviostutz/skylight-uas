package br.skylight.uav;

import br.skylight.commons.plugins.watchdog.WatchDog;
import br.skylight.uav.plugins.onboardintegration.OnboardWatchDogKickerService;

public class SkylightUAVMonitored {

	public static void main(String[] args) throws Exception {
		WatchDog watchDog = new WatchDog();
		watchDog.startup(OnboardWatchDogKickerService.IDENTIFICATION, SkylightUAV.class, 2000, 8000, 15000, 5000, "-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode");
		watchDog.getMonitoredProcess().waitFor();
	}
	
}
