package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum SetEOIRPointingMode {
	NO_VALUE,
	ANGLE_RELATIVE_TO_UAV,
	SLEWING_RATE_RELATIVE_TO_UAV,
	SLEWING_RATE_RELATIVE_TO_INERTIAL,
	LAT_LONG_SLAVED,
	TARGET_SLAVED;

	public PointingModeState getPointingModeState() {
		if(ordinal()<=4) {
			return PointingModeState.values()[ordinal()];
		} else {
			return PointingModeState.NO_VALUE;
		}
	}
	
	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
	
}
