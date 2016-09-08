package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum RouteType {

	LAUNCH,
	APPROACH,
	FLIGHT,
	CONTINGENCY_A,
	CONTINGENCY_B;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
	
}
