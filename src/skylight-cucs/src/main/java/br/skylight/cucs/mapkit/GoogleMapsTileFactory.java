package br.skylight.cucs.mapkit;

import java.awt.geom.Point2D;

import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Tile;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

public class GoogleMapsTileFactory extends DefaultTileFactory {

	public GoogleMapsTileFactory(TileFactoryInfo arg0) {
		super(arg0);
	}
	
	@Override
	public Tile getTile(int arg0, int arg1, int arg2) {
		Tile tile = super.getTile(arg0, arg1, arg2);
		if(tile.getError()!=null && tile.getError().getMessage().equals(DiskTileCache.EXCEPTION_MESSAGE)) {
			getTileMap().remove(tile.getURL());
		}
		return tile;
	}
	
	@Override
	public GeoPosition pixelToGeo(Point2D pixelCoordinate, int zoom) {
		GeoPosition gp = super.pixelToGeo(pixelCoordinate, zoom);
		gp = new GeoPosition(gp.getLatitude()%90D, gp.getLongitude()%180D);
		return gp;
	}

}
