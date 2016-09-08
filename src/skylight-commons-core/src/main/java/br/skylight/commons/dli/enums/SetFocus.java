package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum SetFocus {
	NO_CHANGE,
	FOCUS_CLOSER,
	FOCUS_FARTHER;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
}
