package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum TurnType {

	SHORT_TURN,
	FLYOVER;
	
	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	};
	
}
