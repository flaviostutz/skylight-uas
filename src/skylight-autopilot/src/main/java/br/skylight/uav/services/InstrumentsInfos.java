package br.skylight.uav.services;

import br.skylight.commons.dli.Bitmapped;

public class InstrumentsInfos extends Bitmapped {

	public boolean isManualRemoteControl() {
		return isBit(0);
	}
	public boolean isCalibrationPerformed() {
		return isBit(1);
	}
	public boolean isGimbalInverted() {
		return isBit(2);
	}
	
	public boolean isHardwareReset() {
		return isBit(3);
	}
	
	public boolean isFailsafeArmed() {
		return isBit(4);
	}
	
	public void setCalibrationPerformed(boolean value) {
		setBit(1, value);
	}
	
	@Override
	public String toString() {
		return "RC=" + isManualRemoteControl() + ";CAL=" + isCalibrationPerformed() + ";GI=" + isGimbalInverted() + ";HR=" + isHardwareReset() + ";FA=" + isFailsafeArmed();
	}
	
}
