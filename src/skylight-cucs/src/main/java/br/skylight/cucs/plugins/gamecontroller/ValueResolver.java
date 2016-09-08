package br.skylight.cucs.plugins.gamecontroller;

import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.TimedBoolean;

public class ValueResolver {

	//max time between controller updates to consider single stick travel
	private static final int TIME_WITHOUT_UPDATES_FOR_TRAVEL_END = 200;
	//while travelling, send current state each n millis
	private int timeAutoTriggerWhileTraveling = 500;
	//min change (% of minProportionalValue) in resolved value needed to trigger any event
	private static final double MIN_CHANGE_VALUE_PROPORTIONAL = 0.05;
	private static final double MIN_CHANGE_VALUE_INCREMENTAL = 0.01;

	//set by binding definition
	private double minValueIncremental = -1;
	private double maxValueIncremental = 1;
	private double minValueProportional = -1;
	private double maxValueProportional = 1;
	private double lastControllerValue;
	
	//set by binding instance
	private boolean incremental;
	private boolean inverse;
	private boolean exponential;
	private IncrementalValue incrementalValue = new IncrementalValue();

	private TimedBoolean changingState = new TimedBoolean(TIME_WITHOUT_UPDATES_FOR_TRAVEL_END);
	private TimedBoolean triggerOnChangingState = new TimedBoolean(timeAutoTriggerWhileTraveling);
	private double lastResolvedValue;
	
	public ValueResolver() {
		incrementalValue.setMinValue(minValueIncremental);
		incrementalValue.setMaxValue(maxValueIncremental);
		incrementalValue.setMaxIncrementRate(5);
		lastControllerValue = 0;
	}
	
	public double getResolvedValue() {
		if(!incremental) {
			return (inverse?-1:1)*MathHelper.clamp(minValueProportional + (maxValueProportional-minValueProportional)*((lastControllerValue+1)/2.0), minValueProportional, maxValueProportional);
		} else {
			return incrementalValue.getCurrentValue();
		}
	}

	/**
	 * Value from -1 to 1 indicating controller state
	 */
	public void updateControllerValue(float value) {
		if(exponential) {
			value = (float)MathHelper.getExponentialCurve(value, 5);
		}
		this.lastControllerValue = value;
		incrementalValue.updateStrength(value);
	}
	
	public boolean shouldTriggerChangeEvent() {
		return false;
	}

	public boolean shouldTriggerAsyncChangeEvent() {
		//verify if there was a change in resolved value
		double r = getResolvedValue();
		if((incremental && Math.abs(r-lastResolvedValue)>(Math.abs(minValueIncremental)*MIN_CHANGE_VALUE_INCREMENTAL)) 
			|| (!incremental && Math.abs(r-lastResolvedValue)>(Math.abs(minValueProportional)*MIN_CHANGE_VALUE_PROPORTIONAL))) {
			lastResolvedValue = r;
			changingState.reset();
		}

		//trigger async change if value stopped changing
		if(changingState.isTimedOut()) {
			boolean b = changingState.isFirstTestAfterTimeOut();
//			if(b) System.out.println("Trigerred because stopped moving " + getResolvedValue());
//			return changingState.isFirstTestAfterTimeOut();
			return b;
		} else {
			//controller is changing state over time, so send an update time by time
//			System.out.println("CHANGE REPORT TIMEOUT " + triggerOnChangingState.isTimedOut());
			boolean b = triggerOnChangingState.checkTrue();
//			if(b) System.out.println("Trigerred during moving " + getResolvedValue());
//			return triggerOnChangingState.checkTrue();
			return b;
		}
	}
	
	public void setMaxValueIncremental(double maxValue) {
		this.maxValueIncremental = maxValue;
		incrementalValue.setMaxValue(maxValueIncremental);
	}
	public void setMinValueIncremental(double minValue) {
		this.minValueIncremental = minValue;
		incrementalValue.setMinValue(minValueIncremental);
	}
	
	public void setMinValueProportional(double minValueProportional) {
		this.minValueProportional = minValueProportional;
	}
	public void setMaxValueProportional(double maxValueProportional) {
		this.maxValueProportional = maxValueProportional;
	}
	public double getMinValueIncremental() {
		return minValueIncremental;
	}
	public double getMinValueProportional() {
		return minValueProportional;
	}
	public double getMaxValueIncremental() {
		return maxValueIncremental;
	}
	public double getMaxValueProportional() {
		return maxValueProportional;
	}

	public boolean isIncremental() {
		return incremental;
	}
	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
		incrementalValue.setCurrentValue(0);
	}

	public void setExponential(boolean exponential) {
		this.exponential = exponential;
	}
	public boolean isExponential() {
		return exponential;
	}
	
	public boolean isInverse() {
		return inverse;
	}
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
		incrementalValue.setMaxIncrementRate(-incrementalValue.getMaxIncrementRate());
	}
	
	public void setTimeAutoTriggerWhileTraveling(int timeAutoTriggerWhileTraveling) {
		this.timeAutoTriggerWhileTraveling = timeAutoTriggerWhileTraveling;
		triggerOnChangingState.setTime(timeAutoTriggerWhileTraveling);
	}
	
	public int getTimeAutoTriggerWhileTraveling() {
		return timeAutoTriggerWhileTraveling;
	}
	
	public void setMaxIncrementRate(double incrementRate) {
		if(inverse) {
			incrementalValue.setMaxIncrementRate(-incrementRate);
		} else {
			incrementalValue.setMaxIncrementRate(incrementRate);
		}
	}

	public void setCurrentValue(double value) {
		//this condition is used to avoid self loop - incremental will stop working when stick is stopped
		if(Math.abs(incrementalValue.getCurrentValue()-value)>(Math.abs(value)*0.01F)) {
			incrementalValue.setCurrentValue(value);
		}
	}

	public void copyFrom(ValueResolver valueResolver) {
		setIncremental(valueResolver.incremental);
		setInverse(valueResolver.inverse);
		setExponential(valueResolver.exponential);
		setTimeAutoTriggerWhileTraveling(valueResolver.getTimeAutoTriggerWhileTraveling());
		//TODO implement for proportional only controller (should create a reverse equation from resolveValue())
	}

}
