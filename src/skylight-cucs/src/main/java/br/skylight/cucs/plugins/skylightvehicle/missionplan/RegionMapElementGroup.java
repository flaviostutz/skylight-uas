package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.Mission;
import br.skylight.commons.Region;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.mapkit.painters.MapElementPainter;

public class RegionMapElementGroup extends MapElementGroup<RegionMapElement> {

	public RegionMapElementGroup(MapKit mapKit, String name, int layerNumber, final Mission mission, final Region region, MapElementPainter<RegionMapElement> painter) {
		super(mapKit, name, layerNumber, 
			new MapElementBridge<RegionMapElement>() {
			@Override
			public RegionMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<RegionMapElement> group) {
				Coordinates pos = new Coordinates(position.getLatitude(), position.getLongitude(), 0);
				region.addPoint(pos, elementIndex);
				RegionMapElement r = new RegionMapElement(mission, region);
				r.setPoint(pos);
				return r;
			};
			@Override
			public void onElementDeleted(RegionMapElement me, MapElementGroup<RegionMapElement> group) {
				me.getRegion().getPoints().remove(me.getPoint());
				if(!me.getRegion().isValidArea()) {
					me.getRegion().clear();
				}
			}
		}, painter);
	}
	
}
