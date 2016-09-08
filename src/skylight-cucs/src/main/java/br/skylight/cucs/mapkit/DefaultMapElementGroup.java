package br.skylight.cucs.mapkit;

import br.skylight.cucs.mapkit.painters.DefaultPainter;

public class DefaultMapElementGroup extends MapElementGroup<DefaultMapElement> {

	public DefaultMapElementGroup(MapKit mapKit, String name, int layerNumber) {
		super(mapKit, name, layerNumber, new DefaultMapElementBridge(), new DefaultPainter<DefaultMapElement>());
	}

}
