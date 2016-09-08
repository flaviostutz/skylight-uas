package br.skylight.cucs.plugins.gamecontroller;

import br.skylight.commons.infra.MathHelper;

public class IncrementalValue {

	//value per second to increment internal state when strength is '1'
	private double maxIncrementRate = 1;
	private double minValue = -Double.MAX_VALUE;
	private double maxValue = Double.MAX_VALUE;
	
	private double incrementedValue = 0;
	
	private double currentStrength = 0;
	private double lastUpdateTime;

	/**
	 * Value to be incremented per second when strength is '1'
	 * @param incrementRate
	 */
	public void setMaxIncrementRate(double incrementRate) {
		this.maxIncrementRate = incrementRate;
	}
	public double getMaxIncrementRate() {
		return maxIncrementRate;
	}
	
	/**
	 * Value between 0 and 1 to indicate strength of change
	 * @param strength
	 */
	public void updateStrength(double strength) {
		currentStrength = strength;
		updateInternalValue();
	}
	
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}
	public double getMinValue() {
		return minValue;
	}
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}
	public double getMaxValue() {
		return maxValue;
	}
	
	private void updateInternalValue() {
		double td = (System.currentTimeMillis()/1000.0) - lastUpdateTime;
		incrementedValue += (maxIncrementRate*currentStrength) * td;
		incrementedValue = MathHelper.clamp(incrementedValue, minValue, maxValue);
		lastUpdateTime = System.currentTimeMillis()/1000.0;
	}

	public double getCurrentValue() {
		updateInternalValue();
		return incrementedValue;
	}
	
	public void setCurrentValue(double incrementedValue) {
		this.incrementedValue = incrementedValue;
		currentStrength = 0;
	}
	
}
