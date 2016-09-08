package br.skylight.commons.infra;


public class TimedValue {

	private double initialValue;
	private long startTime;
	private double diffPerSecond;
	private double min, max;
	
	private boolean reachedLimits;
	private boolean reverseOnLimits;
	private double lastValue;
	
	public TimedValue(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public void start(double initialValue, double finalValue, double time, boolean reverseOnLimits) {
		if(time==0) {
			start(initialValue, 0, reverseOnLimits);
		} else {
			start(initialValue, (finalValue-initialValue)/time, reverseOnLimits);
		}
	}
	
	public void start(double initialValue, double valueDiffPerSecond, boolean reverseOnLimits) {
		startTime = System.currentTimeMillis();
		this.diffPerSecond = valueDiffPerSecond;
		this.initialValue = initialValue;
		this.reverseOnLimits = reverseOnLimits;
		this.reachedLimits = false;
	}
	
	public double getValue() {
		double valueDiff = ((System.currentTimeMillis()-startTime)/1000.0)*diffPerSecond;
		lastValue = initialValue + valueDiff;
		if((valueDiff<0 && lastValue<=min) || (valueDiff>0 && lastValue>=max)) {
			reachedLimits = true;
			if(reverseOnLimits) {
				reverse();
			}
			lastValue = MathHelper.clamp(lastValue, min, max);
		}
		return lastValue;
	}
	
	public boolean isReachedLimits() {
		return reachedLimits;
	}
	
	public void reverse() {
		start(lastValue, -diffPerSecond, reverseOnLimits);
	}
	
	public void setMax(double max) {
		this.max = max;
	}
	public void setMin(double min) {
		this.min = min;
	}
	
}
