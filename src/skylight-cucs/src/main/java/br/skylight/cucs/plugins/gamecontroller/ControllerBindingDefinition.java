package br.skylight.cucs.plugins.gamecontroller;

import br.skylight.commons.Vehicle;

public abstract class ControllerBindingDefinition {

	private int bindingDefinitionId;
	private String name;
	private ValueResolver valueResolver;
	
	public ControllerBindingDefinition(int bindingDefinitionId, String name, ValueResolver valueResolver) {
		this.bindingDefinitionId = bindingDefinitionId;
		this.name = name;
		this.valueResolver = valueResolver;
	}
	
	public int getBindingDefinitionId() {
		return bindingDefinitionId;
	}
	public String getName() {
		return name;
	}
	public ValueResolver getValueResolver() {
		return valueResolver;
	}
	public abstract void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle);
	
}
