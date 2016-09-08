package br.skylight.cucs.plugins.gamecontroller;

import java.util.List;

import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;

@ExtensionPointDefinition
public abstract class BindingDefinitionsExtensionPoint extends Worker {

	public abstract List<ControllerBindingDefinition> getControllerBindingDefinitions();
	
}
