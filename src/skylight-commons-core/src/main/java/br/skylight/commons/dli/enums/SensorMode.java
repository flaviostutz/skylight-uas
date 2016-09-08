package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum SensorMode {

	TURN_OFF,
	TURN_ON,
	GOTO_STANDBY;
	
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	};
	
}
