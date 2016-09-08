package br.skylight.commons.plugins.watchdog;

public enum ResetState {

	SUCCESSFUL(999999999), 
	UNSUCCESSFUL(888888888), 
	WAITING_STABILIZATION(777777777);
	
	private long time;
	
	private ResetState(long time) {
		this.time = time;
	}
	
	public long getTime() {
		return time;
	}
	
}
