package br.skylight.cucs.plugins.subscriber;

import br.skylight.commons.EventType;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;

public interface LoiterListener {

	public void onLoiterEvent(LoiterConfiguration lc, EventType type);

}
