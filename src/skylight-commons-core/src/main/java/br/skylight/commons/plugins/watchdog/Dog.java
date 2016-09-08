package br.skylight.commons.plugins.watchdog;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.statemachine.StateAdapter;
import br.skylight.commons.statemachine.StateMachine;

public class Dog extends ThreadWorker {

	private static final Logger logger = Logger.getLogger(Dog.class.getName());
	
	private WatchDog watchDog;
	private long timeStableToConsiderSuccessfulReset;
	private long timeSinceLastKickForReset;
	private long timeWithoutSuccessfulResetForHardwareReset;
	private long timeWaitingInitialKick;

	private long lastProcessedKickTime;
	private long lastSuccessfulResetTime;
	private long lastResetTime;
	
	private String identification;
	private static File watchdogsDir = IOHelper.resolveDir(new File("/Skylight"), "watchdogs");
	
	private enum WatchDogPhase {WAIT_FIRST_KICK, CHECK_LAST_KICK, PERFORM_SOFTWARE_RESET, PERFORM_HARDWARE_RESET}
	private StateMachine<WatchDogPhase, Object> stateMachine;
	
	public Dog(String identification, WatchDog watchDog, long timeSinceLastKickForReset, long timeStableToConsiderSuccessfulReset, long timeWithoutSuccessfulResetForHardwareReset, long timeWaitingInitialKick) throws IOException {
		super(3);
		this.watchDog = watchDog;
		this.timeSinceLastKickForReset = timeSinceLastKickForReset;
		this.timeStableToConsiderSuccessfulReset = timeStableToConsiderSuccessfulReset;
		this.timeWithoutSuccessfulResetForHardwareReset = timeWithoutSuccessfulResetForHardwareReset;
		this.timeWaitingInitialKick = timeWaitingInitialKick;
		this.identification = identification;
		
		createStateMachine();
	}

	public static File getKickFile(String identification) throws IOException {
		return IOHelper.resolveFile(watchdogsDir, identification +"-kick.touch");
	}
	public static File getWatchDogEnabledFile(String identification) throws IOException {
		return IOHelper.resolveFile(watchdogsDir, identification +"-enabled.touch");
	}
	
	public static File getSuccessfulResetFile(String identification) throws IOException {
		return IOHelper.resolveFile(watchdogsDir,  identification +"-sucessful-reset.touch");
	}
	
	@Override
	public void onActivate() throws Exception {
		setLastResetState(identification, ResetState.WAITING_STABILIZATION);
		lastSuccessfulResetTime = System.currentTimeMillis();
		lastResetTime = System.currentTimeMillis();
		lastProcessedKickTime = getLastKickTime(identification);
		stateMachine.enterState(WatchDogPhase.WAIT_FIRST_KICK);
	}
	
	private void createStateMachine() {
		stateMachine = new StateMachine<WatchDogPhase, Object>();
		stateMachine.addState(WatchDogPhase.WAIT_FIRST_KICK, new StateAdapter() {
			public void onStep() throws Exception {
				//first kick found
				if(lastProcessedKickTime!=getLastKickTime(identification)) {
					logger.info("WatchDog: First kick received");
					stateMachine.enterState(WatchDogPhase.CHECK_LAST_KICK);
				}
				//verify timeout waiting for initial kick
				if((stateMachine.getTimeInCurrentState()*1000.0)>timeWaitingInitialKick) {
					logger.warning("WatchDog: Timeout waiting for first kick. time=" + stateMachine.getTimeInCurrentState() + " ms");
					stateMachine.enterState(WatchDogPhase.PERFORM_SOFTWARE_RESET);
				}
			}
		});
		stateMachine.addState(WatchDogPhase.CHECK_LAST_KICK, new StateAdapter() {
			public void onStep() throws Exception {
				if(isWatchDogEnabled(identification)) {
					lastProcessedKickTime = getLastKickTime(identification);
					long timeSinceLastKick = System.currentTimeMillis() - lastProcessedKickTime;
					
					//FOUND BAD KICK
					if(timeSinceLastKick>timeSinceLastKickForReset) {
						logger.warning("WatchDog: Timeout waiting for kick. timeSinceLastKick=" + timeSinceLastKick);
						long timeSinceLastSuccessfulReset = System.currentTimeMillis() - lastSuccessfulResetTime;
						if(!getLastResetState(identification).equals(ResetState.SUCCESSFUL) && timeSinceLastSuccessfulReset>timeWithoutSuccessfulResetForHardwareReset) {
							stateMachine.enterState(WatchDogPhase.PERFORM_HARDWARE_RESET);
						} else {
							stateMachine.enterState(WatchDogPhase.PERFORM_SOFTWARE_RESET);
						}
					
					//FOUND GOOD KICK
					} else {
						logger.finest("WatchDog: Received good kick. timeSinceLastKick=" + timeSinceLastKick);
						
						//verify if last reset can be considered successful
						long timeSinceLastReset = System.currentTimeMillis() - lastResetTime;
						if(!getLastResetState(identification).equals(ResetState.SUCCESSFUL) && timeSinceLastReset>timeStableToConsiderSuccessfulReset) {
							setLastResetState(identification, ResetState.SUCCESSFUL);
							lastSuccessfulResetTime = lastResetTime;
							logger.info("WatchDog: Elapsed "+ timeStableToConsiderSuccessfulReset +" ms since last reset. Marking reset as successful");
						}
					}
				}
			}
		});
		stateMachine.addState(WatchDogPhase.PERFORM_SOFTWARE_RESET, new StateAdapter() {
			public void onStep() throws Exception {
				//it will be considered successful after some stable time kicking
				setLastResetState(identification, ResetState.WAITING_STABILIZATION);
				lastResetTime = System.currentTimeMillis();
				
				logger.warning("====> WatchDog: PERFORMING JVM RESET. timeSinceLastKick=" + (System.currentTimeMillis() - getLastKickTime(identification)) + " ms <====");
				watchDog.destroyMonitoredJVM();
				watchDog.launchMonitoredJVM();

				stateMachine.enterState(WatchDogPhase.WAIT_FIRST_KICK);
			}
		});
		stateMachine.addState(WatchDogPhase.PERFORM_HARDWARE_RESET, new StateAdapter() {
			public void onStep() throws Exception {
				logger.warning("====> WatchDog: PERFORMING HARDWARE RESET. timeSinceLastKick=" + (System.currentTimeMillis() - getLastKickTime(identification)) + " ms. timeSinceLastSuccessfulReset=" + (System.currentTimeMillis() - lastSuccessfulResetTime) + " ms <====");
				//FIXME implement hardware reset
				stateMachine.enterState(WatchDogPhase.PERFORM_SOFTWARE_RESET);
			}
		});
	}

	@Override
	public void step() throws Exception {
		stateMachine.step();
	}

	public static void setLastKickTime(String identification, long time) throws IOException {
		IOHelper.setFileModificationTime(getKickFile(identification), time);
	}
	public static long getLastKickTime(String identification) throws IOException {
		return getKickFile(identification).lastModified();
	}
	
	public static boolean isWatchDogEnabled(String identification) throws IOException {
		return getWatchDogEnabledFile(identification).lastModified()==WatchDog.WATCHDOG_ENABLED_TIME;
	}
	public static void setWatchDogEnabled(String identification, boolean enabled) throws IOException {
		if(enabled) {
			IOHelper.setFileModificationTime(getWatchDogEnabledFile(identification), WatchDog.WATCHDOG_ENABLED_TIME);
		} else {
			IOHelper.setFileModificationTime(getWatchDogEnabledFile(identification), WatchDog.WATCHDOG_DISABLED_TIME);
		}
	}

	private static void setLastResetState(String identification, ResetState state) throws IOException {
		IOHelper.setFileModificationTime(getSuccessfulResetFile(identification), state.getTime());
	}
	
	public static ResetState getLastResetState(String identification) throws IOException {
		if(getSuccessfulResetFile(identification).lastModified()==ResetState.SUCCESSFUL.getTime()) {
			return ResetState.SUCCESSFUL;
		} else if(getSuccessfulResetFile(identification).lastModified()==ResetState.UNSUCCESSFUL.getTime()) {
			return ResetState.UNSUCCESSFUL;
		} else {
			return ResetState.WAITING_STABILIZATION;
		}
	}
	
}
