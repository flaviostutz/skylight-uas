package br.skylight.commons.infra;

import java.util.logging.Logger;

public abstract class Worker implements Activable {

	private static final Logger logger = Logger.getLogger(Worker.class.getName());
	private boolean active;
	private boolean initialized;

	@Override
	public boolean isActive() {
		return active;
	}

	public final void init() throws Exception {
		if(!initialized) {
			onActivate();
			initialized = true;
		}
	};
	
	@Override
	public void activate() throws Exception {
		if (!initialized) {
			init();
		}
		if(!active) {
			active = true;
		}
	}

	@Override
	public void deactivate() throws Exception {
		if (isActive()) {
			onDeactivate();
			active = false;
			initialized = false;
		}
	}

	public String getStatusInfo() {
		return toString() + ": " + (isActive() ? "active" : "inactive");
	}

	public String toString() {
		if(getClass().getSimpleName().trim().length()==0) {
			return getClass().getName();
		} else {
			return getClass().getSimpleName();
		}
	}

	public void onActivate() throws Exception {
	};

	public void onDeactivate() throws Exception {
	};

	public void step() throws Exception {
	};
	
	public boolean isInitialized() {
		return initialized;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
