package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum SetZoom {
	USE_FOV,
	NO_CHANGE,
	ZOOM_IN,
	ZOOM_OUT;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
}
