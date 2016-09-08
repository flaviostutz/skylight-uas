package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum EOCameraStatus {

	BW_MODE,
	COLOUR_MODE;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
	
}
