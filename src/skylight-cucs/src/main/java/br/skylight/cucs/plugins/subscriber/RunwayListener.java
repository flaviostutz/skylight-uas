package br.skylight.cucs.plugins.subscriber;

import br.skylight.commons.EventType;
import br.skylight.commons.dli.skylight.Runway;

public interface RunwayListener {

	public void onRunwayEvent(Runway runway, EventType type);
	
}
