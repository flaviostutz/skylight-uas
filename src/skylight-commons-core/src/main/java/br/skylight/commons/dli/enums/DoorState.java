package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum DoorState {
	CLOSED,
	OPEN;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
	
}
