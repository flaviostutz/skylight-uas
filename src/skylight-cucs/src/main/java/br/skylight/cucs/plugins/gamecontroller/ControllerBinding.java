package br.skylight.cucs.plugins.gamecontroller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.infra.SerializableState;

public class ControllerBinding implements SerializableState {

	private int definitionId;
	private boolean active;
	private String controllerName = "";
	private String componentName = "";
	private boolean inverse;
	private boolean incremental;
	private boolean exponential;
	private int timeAutoTriggerWhileTraveling;
	
	//static
	private ControllerBindingDefinition controllerBindingDefinition;

	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isActive() {
		return active;
	}
	
	public void setComponentName(String controllerComponentIdentifier) {
		this.componentName = controllerComponentIdentifier;
	}
	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}
	public String getComponentName() {
		return componentName;
	}
	public String getControllerName() {
		return controllerName;
	}
	
	public void setDefinitionId(int definitionId) {
		this.definitionId = definitionId;
	}
	public int getDefinitionId() {
		return definitionId;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		definitionId = in.readInt();
		active = in.readBoolean();
		controllerName = in.readUTF();
		componentName = in.readUTF();
		inverse = in.readBoolean();
		incremental = in.readBoolean();
		exponential = in.readBoolean();
		timeAutoTriggerWhileTraveling = in.readInt();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeInt(definitionId);
		out.writeBoolean(active);
		out.writeUTF(controllerName);
		out.writeUTF(componentName);
		out.writeBoolean(inverse);
		out.writeBoolean(incremental);
		out.writeBoolean(exponential);
		out.writeInt(timeAutoTriggerWhileTraveling);
	}
	
	public ControllerBindingDefinition getControllerBindingDefinition() {
		return controllerBindingDefinition;
	}
	public void setControllerBindingDefinition(ControllerBindingDefinition controllerBindingDefinition) {
		this.controllerBindingDefinition = controllerBindingDefinition;
	}
	
	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
	}
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}
	public boolean isIncremental() {
		return incremental;
	}
	public boolean isInverse() {
		return inverse;
	}
	
	public boolean isExponential() {
		return exponential;
	}
	public void setExponential(boolean exponential) {
		this.exponential = exponential;
	}

	public int getTimeAutoTriggerWhileTraveling() {
		return timeAutoTriggerWhileTraveling;
	}
	public void setTimeAutoTriggerWhileTraveling(int timeAutoTriggerWhileTraveling) {
		this.timeAutoTriggerWhileTraveling = timeAutoTriggerWhileTraveling;
	}
	
	public void copyFrom(ControllerBinding binding) {
		this.active = binding.active;
		this.componentName = binding.componentName;
		this.controllerBindingDefinition = binding.controllerBindingDefinition;
		this.controllerName = binding.controllerName;
		this.definitionId = binding.definitionId;
		this.inverse = binding.inverse;
		this.incremental = binding.incremental;
		this.exponential = binding.exponential;
		this.timeAutoTriggerWhileTraveling = binding.timeAutoTriggerWhileTraveling;
	}
}