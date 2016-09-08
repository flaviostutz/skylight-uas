package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum SystemOperatingMode {
	STOW,
	OFF,
	CAGE,
	INITIALISE,
	STANDBY,
	ACTIVE,
	CALIBRATE;
	
	public SystemOperatingModeState getSystemOperatingModeState() {
		return SystemOperatingModeState.values()[ordinal()];
	}
	
	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
}
