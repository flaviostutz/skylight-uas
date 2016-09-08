package br.skylight.commons.io;

import br.skylight.commons.dli.Bitmapped;

public class FlowControlConfig extends Bitmapped {

	public void setRTSCTSIn(boolean active) {
		setBit(0, active);
	}
	public boolean isRTSCTSIn() {
		return isBit(0);
	}
	
	public void setRTSCTSOut(boolean active) {
		setBit(1, active);
	}
	public boolean isRTSCTSOut() {
		return isBit(1);
	}

	public void setXOnXOffIn(boolean active) {
		setBit(2, active);
	}
	public boolean isXOnXOffIn() {
		return isBit(2);
	}
	
	public void setXOnXOffOut(boolean active) {
		setBit(3, active);
	}
	public boolean isXOnXOffOut() {
		return isBit(3);
	}
	
	
}
