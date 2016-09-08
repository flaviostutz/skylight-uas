package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.Mission;
import br.skylight.commons.Region;
import br.skylight.cucs.plugins.missionplan.map.MissionMapElement;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class RegionMapElement extends MissionMapElement {

	private Region region;
	private Coordinates point = new Coordinates();
	
	public RegionMapElement(Mission mission, Region region) {
		this.region = region;
		setMission(mission);
	}
	
	public void setPoint(Coordinates point) {
		this.point = point;
		super.setPosition(CUCSViewHelper.toGeoPosition(point));
	}

	public Region getRegion() {
		return region;
	}
	
	public Coordinates getPoint() {
		return point;
	}
	
	@Override
	public void setPosition(GeoPosition position) {
		super.setPosition(position);
		CUCSViewHelper.copyCoordinates(point, position);
	}
	
}
