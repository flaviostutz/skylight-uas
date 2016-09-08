package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum LoiterDirection {

	VEHICLE_DEPENDENT, 
	CLOCKWISE, 
	COUNTER_CLOCKWISE, 
	INTO_THE_WIND;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	};
}
