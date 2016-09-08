package br.skylight.commons.infra;

import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.enums.RunwayDirection;
import br.skylight.commons.dli.enums.Side;
import br.skylight.commons.dli.skylight.Runway;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;

public class LandingHelper {

	/**
	 * Points: 
	 *    0-downwind start
	 *    1-base start
	 *    2-final approach start
	 *    3-flare start
	 *    4-taxi end
	 */
	public static Coordinates[] calculateLandingPoints(Runway landingRunway, float landingApproachScale) {
		//draw points in p4 reference using meters
		//traffic side: left=1; right=2
		int ts = landingRunway.getManeuversSide().equals(Side.LEFT)?-1:1;
		//dynamic approach direction: 0=no; 1=yes
		int ad = landingRunway.getDirection().equals(RunwayDirection.RUNWAY12)?1:-1;

		//numeric references for Cessna 792
		double scale = landingApproachScale;
		double a = 600*scale;//downwind length
		float ha = 400*(float)scale;//downwind final altitude
		
		double b = 800*scale;//base length
		float hb = 300*(float)scale;//base final altitude
		
		double c = 1500*scale;//final length
		double hc = 2+3*scale;//final altitude (flare start)
		
		//final approach end
		Coordinates p4 = new Coordinates(0,0,0);
		//taxi end
		Coordinates p6 = new Coordinates(0,0,0);
		if(landingRunway.getDirection().equals(RunwayDirection.RUNWAY12)) {
			p4.setLatitude(landingRunway.getPoint1().getLatitude());
			p4.setLongitude(landingRunway.getPoint1().getLongitude());
			p6.setLatitude(landingRunway.getPoint2().getLatitude());
			p6.setLongitude(landingRunway.getPoint2().getLongitude());
		} else {
			p6.setLatitude(landingRunway.getPoint1().getLatitude());
			p6.setLongitude(landingRunway.getPoint1().getLongitude());
			p4.setLatitude(landingRunway.getPoint2().getLatitude());
			p4.setLongitude(landingRunway.getPoint2().getLongitude());
		}
		p4.setAltitude((float)hc);
		p6.setAltitude(0);
		
		//p4 is the MAIN REFERENCE POINT
		Coordinates ref = p4;
		
		//determine runway angle
		double rotation = CoordinatesHelper.headingToMathReference(CoordinatesHelper.calculateHeading(landingRunway.getPoint1().getLatitudeRadians(), landingRunway.getPoint1().getLongitudeRadians(), landingRunway.getPoint2().getLatitudeRadians(), landingRunway.getPoint2().getLongitudeRadians())) - MathHelper.HALF_PI;
		
		//downwind start
		Coordinates p1 = calculateCoordinates(ref, ts * b, ad * (a-c), rotation);
		p1.setAltitude(ha);
		
		//downwind end
		Coordinates p2 = calculateCoordinates(ref, ts * b, ad * -c, rotation);
		p2.setAltitude(ha);
		
		//base end
		Coordinates p3 = calculateCoordinates(ref, 0, ad * -c, rotation);
		p3.setAltitude(hb);
		
		return new Coordinates[] {p1,p2,p3,p4,p6};
	}

	public static Coordinates calculateTakeoffLoiterCenter(double runwayLatitude1, double runwayLongitude1, double runwayLatitude2, double runwayLongitude2, boolean trafficLeft, double distanceFromRunway, SkylightVehicleConfigurationMessage uavConfiguration) {
		//traffic side: left=1; right=2
		int ts = trafficLeft?1:-1;

		//determine runway angle
		//TODO we were using headingToGraphicsReference before
		double runwayRotation = CoordinatesHelper.headingToMathReference(CoordinatesHelper.calculateHeading(runwayLatitude1, runwayLongitude1, runwayLatitude2, runwayLongitude2));
		
		return calculateCoordinates(
				new Coordinates(runwayLatitude2, runwayLongitude2, 0), 
				distanceFromRunway*Math.cos(runwayRotation), 
				distanceFromRunway*Math.sin(runwayRotation), 
				ts*45);
	}
	
	private static Coordinates calculateCoordinates(Coordinates reference, double distX, double distY, double rotation) {
		//rotate point
		double[] np = MathHelper.rotateAroundOrigin(distX, distY, rotation);
		//calculate relative coordinate
		Coordinates result = new Coordinates(0,0,0);
		CoordinatesHelper.calculateCoordinatesFromRelativePosition(result, reference, np[0], np[1]);
		return result;
	}

}
