package br.skylight.vsm;

import br.skylight.commons.plugins.watchdog.WatchDog;
import br.skylight.vsm.plugins.watchdogkicker.VSMWatchDogKickerService;

public class SkylightVSMMonitored {

	public static void main(String[] args) throws Exception {
		WatchDog watchDog = new WatchDog();
		watchDog.startup(VSMWatchDogKickerService.IDENTIFICATION, SkylightVSM.class, 2000, 8000, 15000, 5000, "-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode");
		watchDog.getMonitoredProcess().waitFor();
	}
	
}
