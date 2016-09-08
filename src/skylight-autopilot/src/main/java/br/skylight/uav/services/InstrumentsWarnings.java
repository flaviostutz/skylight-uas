package br.skylight.uav.services;

import br.skylight.commons.dli.Bitmapped;

public class InstrumentsWarnings extends Bitmapped {

	public boolean isGyroscopesSaturation() {
		return isBit(0);
	}
	public boolean isAccelerometersSaturation() {
		return isBit(1);
	}
	public boolean isDynamicPressureSensorSaturation() {
		return isBit(2);
	}
	public boolean isOtherWarning() {
		return isBit(3);
	}
	public boolean isHardwareReceivedMessageCRCError() {
		return isBit(4);
	}
	
	@Override
	public String toString() {
		return "GSAT=" + isGyroscopesSaturation() + ";ASAT=" + isAccelerometersSaturation() + ";PSAT=" + isDynamicPressureSensorSaturation() + ";OW=" + isOtherWarning() + ";CRCERR=" + isHardwareReceivedMessageCRCError();
	}
	
}
