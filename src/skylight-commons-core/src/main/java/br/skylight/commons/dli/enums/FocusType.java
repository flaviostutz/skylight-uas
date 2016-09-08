package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum FocusType {
	AUTO,
	MANUAL;
	
	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
	
}
