package br.skylight.cucs.plugins.subscriber;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EventType;
import br.skylight.commons.Region;

public interface RegionListener {

	public void onRegionEvent(Region boundary, EventType type);
	public void onRegionPointEvent(Region boundary, Coordinates point, EventType type);
	
}
