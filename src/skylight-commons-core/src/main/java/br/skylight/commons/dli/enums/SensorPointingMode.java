package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum SensorPointingMode {

	NIL,
	ANGLE_RELATIVE_TO_AV,
	SLEWING_RATE_RELATIVE_TO_AV,
	SLEWING_RATE_RELATIVE_TO_INERTIAL,
	LAT_LONG_SLAVED,
	TARGET_SLAVED,
	STOW,
	LINE_SEARCH_START_LOCATION,
	LINE_SEARCH_END_LOCATION;
	
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	};
	
}
