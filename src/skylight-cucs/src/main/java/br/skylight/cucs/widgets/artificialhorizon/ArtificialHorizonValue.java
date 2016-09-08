package br.skylight.cucs.widgets.artificialhorizon;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

public class ArtificialHorizonValue {

	private String name;
	private boolean targetValueVisible;
	private Unit baseUnit;
	private Unit displayUnit;
	private float currentValue;
	private float maxValue = Float.NaN;
	private float minValue = Float.NaN;
	private float targetValue = Float.NaN;
	private UnitConverter toDisplayConverter;
	private UnitConverter fromDisplayConverter;
	
	public ArtificialHorizonValue(Unit baseUnit) {
		this.baseUnit = baseUnit;
		this.displayUnit = baseUnit;
		updateConverters();
	}
	private void updateConverters() {
		toDisplayConverter = baseUnit.getConverterTo(displayUnit);
		fromDisplayConverter = displayUnit.getConverterTo(baseUnit);
	}
	public void setDisplayUnit(Unit displayUnit) {
		if(!displayUnit.getDimension().equals(baseUnit.getDimension())) {
			throw new IllegalArgumentException("The 'dimension' of base and display units must be equal. " + displayUnit.getDimension().toString() + "!=" + baseUnit.getDimension().toString());
		}
		this.displayUnit = displayUnit;
		updateConverters();
	}
	public Unit getDisplayUnit() {
		return displayUnit;
	}
	public void setBaseUnit(Unit baseUnit) {
		this.baseUnit = baseUnit;
		updateConverters();
	}
	public Unit getBaseUnit() {
		return baseUnit;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(float maxValue) {
		this.maxValue = maxValue;
	}
	public float getMinValue() {
		return minValue;
	}
	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}
	public float getTargetValue() {
		return targetValue;
	}
	public void setTargetValue(float targetValue) {
		this.targetValue = targetValue;
	}
	public float getCurrentValue() {
		return currentValue;
	}
	public void setCurrentValue(float currentValue) {
		this.currentValue = currentValue;
	}
	
	public float getTargetValueOnDisplayUnit() {
		return (float)toDisplayConverter.convert(targetValue);
	}
	public float getCurrentValueOnDisplayUnit() {
		return (float)toDisplayConverter.convert(currentValue);
	}
	public float getMaxValueOnDisplayUnit() {
		return (float)toDisplayConverter.convert(maxValue);
	}
	public float getMinValueOnDisplayUnit() {
		return (float)toDisplayConverter.convert(minValue);
	}

	public void setTargetValueFromDisplayUnit(float value) {
		this.targetValue = (float)fromDisplayConverter.convert(value);
	}
	public void setCurrentValueFromDisplayUnit(float value) {
		this.currentValue = (float)fromDisplayConverter.convert(value);
	}
	public void setMinValueFromDisplayUnit(float value) {
		this.minValue = (float)fromDisplayConverter.convert(value);
	}
	public void setMaxValueFromDisplayUnit(float value) {
		this.maxValue = (float)fromDisplayConverter.convert(value);
	}
	public float convertToDisplayUnit(float value) {
		return (float)toDisplayConverter.convert(value);
	}
	
	public boolean isTargetValueVisible() {
		return targetValueVisible;
	}
	public void setTargetValueVisible(boolean enabled) {
		this.targetValueVisible = enabled;
		if(enabled && Float.isNaN(targetValue)) {
			if(!Float.isNaN(currentValue)) {
				targetValue = currentValue;
			} else {
				targetValue = 0;
			}
		}
	}
	
	public boolean isCurrentValueOutsideMinMax() {
		return (!Float.isNaN(maxValue) && currentValue>maxValue) || (!Float.isNaN(minValue) && currentValue<minValue);
	}
	
}

