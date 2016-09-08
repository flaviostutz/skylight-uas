package br.skylight.commons;

import br.skylight.commons.infra.CoordinatesHelper;

public class CoordinatesTest {

	public static void main(String[] args) {
		
		System.out.println(">>> "+Math.toDegrees(CoordinatesHelper.metersToLatitudeLength(100000, 0)));
		
		Coordinates c1 = new Coordinates(0,0,0);
		Coordinates c2 = new Coordinates(0,1,0);
		double dist = c1.distance(c2);
		System.out.println(dist);
		
		CoordinatesHelper.calculateCoordinatesFromRelativePosition(c2, c1, dist, 0);
		System.out.println(c2.getLatitude() + " " + c2.getLongitude());
		if(c2.getLongitude()!=1) {
			throw new AssertionError(c2.getLongitude() + "!=1");
		}
	}
	
}
