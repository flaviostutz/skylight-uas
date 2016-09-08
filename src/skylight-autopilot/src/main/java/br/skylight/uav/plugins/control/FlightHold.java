package br.skylight.uav.plugins.control;


public class FlightHold<V,C> {

	private boolean active;
	private float targetValue;
	private V targetValueType;
	private C controlType;

	public void set(boolean active, float targetValue, V targetValueType, C controlType) {
		this.active = active;
		this.targetValue = targetValue;
		this.targetValueType = targetValueType;
		this.controlType = controlType;
	}
	
	public float getTargetValue() {
		return targetValue;
	}
	public void setTargetValue(float targetValue) {
		this.targetValue = targetValue;
	}
	public V getTargetValueType() {
		return targetValueType;
	}
	public void setTargetValueType(V valueType) {
		this.targetValueType = valueType;
	}
	public C getControlType() {
		return controlType;
	}
	public void setControlType(C controlType) {
		this.controlType = controlType;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isActive() {
		return active;
	}
	
	public void copyFrom(FlightHold<V,C> flightHold) {
		this.active = flightHold.active;
		this.targetValue = flightHold.targetValue;
		this.targetValueType = flightHold.targetValueType;
		this.controlType = flightHold.controlType;
	}
	
}
