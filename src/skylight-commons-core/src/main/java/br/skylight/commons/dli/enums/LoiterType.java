package br.skylight.commons.dli.enums;

import br.skylight.commons.StringHelper;

public enum LoiterType {

	NOT_USED, 
	CIRCULAR, RACETRACK, FIGURE_8, HOVER;

	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), false);
	};
	
}
