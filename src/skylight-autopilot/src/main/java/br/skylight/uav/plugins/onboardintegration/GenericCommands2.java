package br.skylight.uav.plugins.onboardintegration;

import br.skylight.commons.dli.Bitmapped;

public class GenericCommands2 extends Bitmapped {

	public void setArmFailsafe(boolean value) {
		setBit(0, value);
	}
	
	public void setDisarmFailsafe(boolean value) {
		setBit(1, value);
	}
	
}
