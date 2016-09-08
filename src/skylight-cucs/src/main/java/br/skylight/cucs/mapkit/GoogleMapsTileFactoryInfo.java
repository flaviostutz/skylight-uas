package br.skylight.cucs.mapkit;

import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.Locale;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.mapviewer.util.GeoUtil;

public class GoogleMapsTileFactoryInfo extends TileFactoryInfo {

	private String maptype;
	private static NumberFormat nf;
	static {
		nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setMaximumFractionDigits(7);
		nf.setGroupingUsed(false);
	}
	
	public GoogleMapsTileFactoryInfo() {
		super(0, 20, 21, 256, true, true, "http://maps.googleapis.com/maps/api/staticmap", "x", "y", "zoom");
		this.maptype = "satellite";
	}
	
	@Override
	public String getTileUrl(int x, int y, int zoom) {
		float tileSize = getTileSize(zoom);
		
		//got this by trial and error
//		float t = tileSize * (tileSize/256F);
		float t = tileSize;
		
		GeoPosition pos = GeoUtil.getPosition(new Point2D.Double(x*t + (t/2F), y*t + (t/2F)), zoom, this);
		double longitude = pos.getLongitude();
		double latitude = pos.getLatitude();
		
		zoom = getMaximumZoomLevel() - zoom +1;
System.out.println(x + " " + y + " " + zoom + " " + tileSize);
//System.out.println(baseURL + "?key=AIzaSyD1M5AnC7k3Ugxk48PeaorFownSBzP8WkY&sensor=false&zoom=" + zoom + "&size=" + (int)tileSize + "x" + (int)tileSize + "&maptype=" + maptype + "&center=" + nf.format(latitude) + "," + nf.format(longitude));
		return baseURL + "?key=AIzaSyD1M5AnC7k3Ugxk48PeaorFownSBzP8WkY&format=jpg&sensor=false&zoom=" + zoom + "&size=" + (int)tileSize + "x" + (int)tileSize + "&maptype=" + maptype + "&center=" + nf.format(latitude) + "," + nf.format(longitude);
	}

}
