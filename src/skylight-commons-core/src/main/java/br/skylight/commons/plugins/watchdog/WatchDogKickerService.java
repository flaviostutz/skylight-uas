package br.skylight.commons.plugins.watchdog;

import java.io.IOException;
import java.util.logging.Logger;

import br.skylight.commons.JVMHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.PluginElement;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceInjection;

@ServiceDefinition
public abstract class WatchDogKickerService extends ThreadWorker {

	private static final Logger logger = Logger.getLogger(WatchDogKickerService.class.getName());

	@ServiceInjection
	public PluginManager pluginManager;
	
	public WatchDogKickerService() {
		super(1);
	}

	@Override
	public void step() throws Exception {
		//verify current threads sanity
		boolean allThreadsOK = true;
		for (PluginElement pe : pluginManager.getInitializedElements()) {
			if(pe.getElement() instanceof ThreadWorker) {
				ThreadWorker ow = (ThreadWorker)pe.getElement();
				synchronized(ow) {
					logger.finest("Verifying " + ow + " status. timeSinceLastStep=" + ow.getTimeSinceLastStep());
					if(ow.isTimeout()) {
						logger.info(ow + ": Timeout. timeSinceLastStep=" + ow.getTimeSinceLastStep() + " ms");
						allThreadsOK = false;
						break;
					}
					if(!ow.isActive() || ow.getLastException()!=null) {
						logger.info(ow + ": Thread inactive. lastException=" + JVMHelper.getExceptionString(ow.getLastException(), 300));
						allThreadsOK = false;
						break;
					}
				}
			}
		}

		try {
			//kick watchdog if everything is OK
			if(allThreadsOK) {
				logger.finest("All threads are OK. Kicking WatchDog");
				Dog.setLastKickTime(getIdentification(), System.currentTimeMillis());
			//register bad kick to notify that something went wrong here
			} else {
				logger.severe("Some threads are timedout. Notifying WatchDog for a reset");
				Dog.setLastKickTime(getIdentification(), 0);
				deactivate();
			}
		} catch (Exception e) {
			logger.warning("Problem when kicking watchdog. Will try to kick watchdog in the next step. e=" + e.toString());
			e.printStackTrace();
		}
	}

	public boolean wasLastResetSuccessful() {
		try {
			return Boolean.parseBoolean(System.getProperty(WatchDog.PROPERTY_RESET_SUCCESSFUL, "true"));
		} catch (Exception e) {
			logger.warning("A bad 'successful reset' property value was found. e=" + e.toString());
			return true;
		}
	}
	
	public abstract String getIdentification();

	public ResetState getLastResetState() throws IOException {
		return Dog.getLastResetState(getIdentification());
	}
	public void setWatchDogEnabled(boolean enabled) throws IOException {
		if(enabled) {
			//kick before enabling watchdog to avoid startup timeouts
			Dog.setLastKickTime(getIdentification(), System.currentTimeMillis());
		}
		Dog.setWatchDogEnabled(getIdentification(), enabled);
	}
	
}
