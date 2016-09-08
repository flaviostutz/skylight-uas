package br.skylight.commons;

import br.skylight.commons.dli.Bitmapped;

public class AddressedSensor extends Bitmapped {

	public void setEOSensor(boolean value) {
		setBit(0, value);
	}
	public void setIRSensor(boolean value) {
		setBit(1, value);
	}
	public void setPayloadSpecificSensor(boolean value) {
		setBit(2, value);
	}
	
	public boolean isEOSensor() {
		return isBit(0);
	}
	public boolean isIRSensor() {
		return isBit(1);
	}
	public boolean isPayloadSpecificSensor() {
		return isBit(2);
	}
	
}
