package br.skylight.commons.plugins.watchdog;

import java.util.logging.Logger;

import br.skylight.commons.JVMHelper;

public class WatchDog {

	public static final String PROPERTY_LAST_KICK_TIME = "SKYLIGHT_LAST_KICK_TIME";
	public static final String PROPERTY_RESET_SUCCESSFUL = "SKYLIGHT_RESET_SUCCESSFUL";
	public static final String PROPERTY_WATCHDOG_ENABLED = "SKYLIGHT_WATCHDOG_ENABLED";
	public static final long WATCHDOG_ENABLED_TIME = 999999999;
	public static final long WATCHDOG_DISABLED_TIME = 888888888;
	private static final Logger logger = Logger.getLogger(WatchDog.class.getName());

	private Process monitoredProcess;
	private Dog dogWatcher;
	private Class mainClass;
	private String[] monitoredJVMArgs;
	
	public static void main(String[] args) {
	}

	public void startup(String identification, Class mainClass, long timeSinceLastKickForReset, long timeStableToConsiderSuccessfulReset, long timeWithoutSuccessfulResetForHardwareReset, long timeWaitingInitialKick, String ... monitoredJVMArgs) throws Exception {
		this.mainClass = mainClass;
		this.monitoredJVMArgs = monitoredJVMArgs;
		
		//start WatchDog watcher
		logger.info("WatchDog: Starting to monitor JVM");
		dogWatcher = new Dog(identification, this, timeSinceLastKickForReset, timeStableToConsiderSuccessfulReset, timeWithoutSuccessfulResetForHardwareReset, timeWaitingInitialKick);
		dogWatcher.activate();

		//start monitored JVM process
		launchMonitoredJVM();
	}
	
	public void shutdown() throws Exception {
		//stop dog watcher
		if(dogWatcher!=null) {
			dogWatcher.forceDeactivation(1000);
		}
		
		//kill monitored jvm process
		if(monitoredProcess!=null) {
			monitoredProcess.destroy();
		}
	}

	public void launchMonitoredJVM() throws Exception {
		logger.info("WatchDog: Launching monitored JVM from " + mainClass);
		monitoredProcess = JVMHelper.startJVM(mainClass, true, true, monitoredJVMArgs);
	}
	
	public void destroyMonitoredJVM() throws Exception {
		if(monitoredProcess!=null) {
			logger.warning("WatchDog: Killing monitored JVM");
			monitoredProcess.destroy();
		}
	}
	
	public Class getMainClass() {
		return mainClass;
	}
	
	public Process getMonitoredProcess() {
		return monitoredProcess;
	}
}
