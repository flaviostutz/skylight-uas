package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum SensorOutput {

	NONE,
	SENSOR_1,
	SENSOR_2,
	BOTH_SENSORS;
	
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	};
	
}
