package br.skylight.commons.plugins.workbench;

import java.io.Serializable;

import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;

@ExtensionPointDefinition
public interface ViewExtensionPoint<P extends Serializable> extends MenuableExtensionPoint {

	public abstract View<P> createView();

}
