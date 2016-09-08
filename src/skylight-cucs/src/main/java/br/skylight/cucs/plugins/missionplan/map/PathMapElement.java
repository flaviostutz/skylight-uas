package br.skylight.cucs.plugins.missionplan.map;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.Path;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class PathMapElement extends MissionMapElement {

	private Path path;
	private Coordinates point = new Coordinates();
	
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	
	public void setPoint(Coordinates point) {
		this.point = point;
		super.setPosition(CUCSViewHelper.toGeoPosition(point));
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
