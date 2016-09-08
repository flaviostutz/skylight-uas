package br.skylight.cucs.plugins.subscriber;

import br.skylight.commons.EventType;
import br.skylight.cucs.repository.Target;

public interface TargetListener {

	public void onTargetEvent(Target target, EventType type);
	
}
