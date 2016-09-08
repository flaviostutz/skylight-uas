package br.skylight.commons.infra;

public class TimedBoolean {

	private boolean enabled = true;
	private long time;
	private long expirationTime;
	private boolean testedAfterTimeout;

	public TimedBoolean(long time, boolean enabled) {
		this.time = time;
		this.enabled = enabled;
		this.expirationTime = Long.MAX_VALUE;
	}
	
	public TimedBoolean(long time) {
		this(time, true);
		reset();
	}

	public void reset() {
		this.expirationTime = System.currentTimeMillis() + time;
		testedAfterTimeout = false;
	}
	
	public void forceTimeout() {
		this.expirationTime = System.currentTimeMillis();
	}

	public boolean isTimedOut() {
		if(enabled) {
			if (System.currentTimeMillis() > expirationTime) {
				return true;
			}
		}
			
		return false;
	}

	public boolean checkTrue() {
		if (isTimedOut()) {
			reset();
			return true;
		} else {
			return false;
		}
	}

	public void setTime(long time) {
		this.time = time;
		reset();
	}

	public boolean isFirstTestAfterTimeOut() {
		if (isTimedOut() && testedAfterTimeout == false) {
			testedAfterTimeout = true;
			return true;
		}
		return false;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
}
