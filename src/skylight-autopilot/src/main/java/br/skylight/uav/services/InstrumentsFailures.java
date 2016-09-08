package br.skylight.uav.services;

import br.skylight.commons.dli.Bitmapped;

public class InstrumentsFailures extends Bitmapped {

	public boolean isFlightTerminationActivated() {
		return isBit(0);
	}
	public boolean isIMUSensorsFailure() {
		return isBit(1);
	}
	public boolean isDynamicPressureSensorFailure() {
		return isBit(2);
	}
	public boolean isStaticPressureSensorFailure() {
		return isBit(3);
	}
	public boolean isSystemReset() {
		return isBit(4);
	}
	public boolean isOtherFailure() {
		return isBit(5);
	}

	@Override
	public String toString() {
		return "FT=" + isFlightTerminationActivated() + ";SF=" + isIMUSensorsFailure() + ";DP=" + isDynamicPressureSensorFailure() + ";SP=" + isStaticPressureSensorFailure() + ";SR=" + isSystemReset() + ";OF=" + isOtherFailure();
	}
	
}
