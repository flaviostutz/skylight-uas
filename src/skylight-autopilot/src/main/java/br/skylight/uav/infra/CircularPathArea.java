package br.skylight.uav.infra;

import java.util.ArrayList;
import java.util.List;

import br.skylight.commons.Coordinates;
import br.skylight.commons.Region;
import br.skylight.commons.infra.FixedList;
import br.skylight.commons.infra.MathHelper;

public class CircularPathArea {

	private Region pathRegion = new Region();
	private float lastCourseHeading;
	private boolean clockwise;
	private float error;
	private double angleBetweenPoints;
	
	//dynamic flight path area
	private FixedList<Double> lastPosLat;
	private FixedList<Double> lastPosLong;
	
	//just for optimization
	private List<Coordinates> tempCoordinates = new ArrayList<Coordinates>();

	public CircularPathArea(double angleBetweenPoints) {
		this.angleBetweenPoints = angleBetweenPoints;
		int size = (int)(MathHelper.TWO_PI/angleBetweenPoints);
		//just for optimization
		for(int i=0; i<size; i++) {
			tempCoordinates.add(new Coordinates());
		}
		lastPosLat = new FixedList<Double>(size);
		lastPosLong = new FixedList<Double>(size);
	}
	
	public void addLocation(float courseHeading, Coordinates pathLocation) {
		error = courseHeading-lastCourseHeading;
		if(Math.abs(error)>=angleBetweenPoints) {
			//doing a circular path, add to polygon
			if((clockwise && error>0) 
				|| (!clockwise && error<0)) {
				lastPosLat.addItem(pathLocation.getLatitudeRadians());
				lastPosLong.addItem(pathLocation.getLongitudeRadians());
//				System.out.println("SAME DIRECTION");
				
			//not doing a circular path, reset polygon
			} else {
				lastPosLat.clear();
				lastPosLong.clear();
//				System.out.println("DIFFERENT DIRECTION");
			}
			lastCourseHeading = courseHeading;
			
			clockwise = error>0;
		}
	}
	
	public boolean isLocationInside(Coordinates position) {
		pathRegion.clear();
		for(int i=0; i<lastPosLat.getSize(); i++) {
			Coordinates c = tempCoordinates.get(i);
			c.setLatitudeRadians(lastPosLat.getItem(i));
			c.setLongitudeRadians(lastPosLong.getItem(i));
			pathRegion.addPoint(c);
		}
		return pathRegion.isPointInside(position);
	}
	
	public void clear() {
		lastPosLat.clear();
		lastPosLong.clear();
	}
	
}
