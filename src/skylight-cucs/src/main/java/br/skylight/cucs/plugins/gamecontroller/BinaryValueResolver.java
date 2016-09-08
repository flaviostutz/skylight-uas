package br.skylight.cucs.plugins.gamecontroller;

public class BinaryValueResolver extends ValueResolver {

	private float thresholdValue = 0.5F;
	private boolean enabledToClick = true;
	private boolean doClick = false;

	@Override
	public void updateControllerValue(float value) {
		if(value>thresholdValue && enabledToClick) {
			doClick = true;
			enabledToClick = false;
		}
		//only click if stick reached near zero
		if(value<=thresholdValue*0.6) {
			enabledToClick = true;
		}
	}
	
	public void setThresholdValue(float thresholdValue) {
		this.thresholdValue = thresholdValue;
	}
	
	@Override
	public boolean shouldTriggerChangeEvent() {
		if(doClick) {
			doClick = false;
			return true;
		} else {
			return false;
		}
	}
	
}
