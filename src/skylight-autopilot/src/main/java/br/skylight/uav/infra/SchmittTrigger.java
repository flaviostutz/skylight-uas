package br.skylight.uav.infra;

public class SchmittTrigger {

	private boolean upperRange;
	private float upperLimit;
	private float lowerLimit;
	
	public SchmittTrigger(float lowerLimitValue, float upperLimitValue, boolean upperRange) {
		this.lowerLimit = lowerLimitValue;
		this.upperLimit = upperLimitValue;
		this.upperRange = upperRange;
	}

	public void setCurrentValue(float value) {
		if(upperRange) {
			if(value<lowerLimit) {
				upperRange= false;
			}
		} else {
			if(value>upperLimit) {
				upperRange = true;
			}
		}
	}
	
	public boolean isUpperRange() {
		return upperRange;
	}
	
	public void setup(float lowerLimitValue, float upperLimitValue, boolean upperRange) {
		this.lowerLimit = lowerLimitValue;
		this.upperLimit = upperLimitValue;
		this.upperRange = upperRange;
	}
	
}
