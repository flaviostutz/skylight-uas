package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum ImageOutput {
	NONE,
	EO,
	IR,
	BOTH;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
}
