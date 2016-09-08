package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum SetIRPolarity {
	BLACK_HOT,
	WHITE_HOT;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
}
