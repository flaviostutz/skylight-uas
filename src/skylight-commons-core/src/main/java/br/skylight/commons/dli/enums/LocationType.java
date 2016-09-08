package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum LocationType {

	ABSOLUTE,
	RELATIVE;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	}
}
