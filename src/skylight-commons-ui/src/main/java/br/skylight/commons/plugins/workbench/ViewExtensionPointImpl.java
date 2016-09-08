package br.skylight.commons.plugins.workbench;

import java.io.Serializable;

import br.skylight.commons.infra.Worker;

public abstract class ViewExtensionPointImpl<P extends Serializable> extends Worker implements ViewExtensionPoint<P> {

	@Override
	public String toString() {
		return getClass().getName();
	}
	
}
