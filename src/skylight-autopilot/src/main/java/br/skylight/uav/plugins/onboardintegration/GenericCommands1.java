package br.skylight.uav.plugins.onboardintegration;

import br.skylight.commons.dli.Bitmapped;

public class GenericCommands1 extends Bitmapped {

	public void setRequestCalibration(boolean value) {
		setBit(0, value);
	}
	public void setActivateNavigationLights(boolean value) {
		setBit(1, value);
	}
	public void setActivateStrobeLights(boolean value) {
		setBit(2, value);
	}
	public void setActivateLandingLights(boolean value) {
		setBit(3, value);
	}
	public void setActivateEngineIgnition(boolean value) {
		setBit(4, value);
	}
	public boolean isActivateEngineIgnition() {
		return isBit(4);
	}
	public void setActivateVideoTransmitter(boolean value) {
		setBit(5, value);
	}
	public void setActivateExtraOutput(boolean value) {
		setBit(6, value);
	}
	public void setActivateFlightTermination(boolean value) {
		setBit(7, value);
	}
	
}
