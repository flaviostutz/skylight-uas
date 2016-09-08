package br.skylight.commons;

public enum SafetyAction {

	DO_NOTHING						(false, false, 99),
	EXECUTE_STABILIZATION_MANEUVER	(false, true, 80),
	LOITER_AROUND_POSITION			(false, true, 70),
	LOITER_WITH_ROLL				(false, false, 60),
	GO_FOR_DATA_LINK_RECOVERY		(false, true, 20),//keep same priority as GPS LINK RECOVERY (because of simultaneous data/gps link recovery action)
	GO_FOR_GPS_LINK_RECOVERY		(false, true, 20),
	GO_FOR_MANUAL_RECOVERY			(false, true, 5),
	LOITER_WITH_ROLL_DESCENDING		(true, false, 4),
	KILL_ENGINE_AND_HOLD_LEVEL		(true, false, 3),
	DEPLOY_PARACHUTE				(true, false, 2),
	HARD_SPIN_TO_GROUND				(true, false, 1);

	private int priority;
	private boolean killEngine;
	private boolean needGps;
	
	private SafetyAction(boolean killEngine, boolean needGps, int priority) {
		this.killEngine = killEngine;
		this.needGps = needGps;
		this.priority = priority;
	}
	
	public boolean isKillEngine() {
		return killEngine;
	}
	
	public boolean isNeedGps() {
		return needGps;
	}
	
	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	}
	
	/**
	 * Gets the safety action priority. 0 is higher
	 * If a higher safety action is requested when a lower is being performed, the lower will stop and
	 * the higher will take place. When a lower is requested, it will be ignored. 
	 */
	public int getPriority() {
		return priority;
	}
	
}
