package br.skylight.commons.infra;

import javax.vecmath.Vector3d;

import br.skylight.commons.Coordinates;

public class CoordinatesHelper {

	//heading calculation
	private static Coordinates h1 = new Coordinates(0,0,0);
	private static Coordinates h2 = new Coordinates(0,0,0);
	
	//distance calculation
	private static Coordinates d1 = new Coordinates(0,0,0);
	private static Coordinates d2 = new Coordinates(0,0,0);

	//LATITUDE/LONGITUDE CALCULATIONS
	private static float M1 = 111132.92F;// latitude calculation term 1
	private static float M2 = -559.82F;// latitude calculation term 2
	private static float M3 = 1.175F; // latitude calculation term 3
	private static float M4 = -0.0023F; // latitude calculation term 4
	private static float P1 = 111412.84F; // longitude calculation term 1
	private static float P2 = -93.5F; // longitude calculation term 2
	private static float P3 = 0.118F; // longitude calculation term 3
	
	/**
	 * Calculates heading in radians from one point to another
	 * @param fromLatitude
	 * @param fromLongitude
	 * @param toLatitude
	 * @param toLongitude
	 * @return
	 */
	public static float calculateHeading(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
		synchronized(h1) {
			h1.setLatitudeRadians(fromLatitude);
			h1.setLongitudeRadians(fromLongitude);
			h2.setLatitudeRadians(toLatitude);
			h2.setLongitudeRadians(toLongitude);
			return h1.azimuthToRadians(h2);
		}
	}
	
	/**
	 * Calculates the distance between two points.
	 * Coordinates in radians.
	 * @param fromLatitude
	 * @param fromLongitude
	 * @param toLatitude
	 * @param toLongitude
	 * @return
	 */
	public static double calculateDistance(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
		synchronized(d1) {
			d1.setLatitudeRadians(fromLatitude);
			d1.setLongitudeRadians(fromLongitude);
			d2.setLatitudeRadians(toLatitude);
			d2.setLongitudeRadians(toLongitude);
			return d1.distance(d2);
		}
	}

	public static Coordinates calculateCoordinates(Coordinates reference, double distX, double distY, double rotation) {
		//rotate point
		double[] np = MathHelper.rotateAroundOrigin(distX, distY, rotation);
		//calculate relative coordinate
		Coordinates result = new Coordinates(0,0,0);
		calculateCoordinatesFromRelativePosition(result, reference, np[0], np[1]);
		return result;
	}

	/**
	 * Calculates the resulting geo position of a point according to its relative position (in meters) to another geo position
	 * relativeX is pointing to longitude
	 * relativeY is pointing to latitude
	 */
	public static void calculateCoordinatesFromRelativePosition(Coordinates result, Coordinates reference, double relativeX, double relativeY) {
		double lat = metersToLatitudeLength(relativeY, reference.getLatitudeRadians());
		double lon = metersToLongitudeLength(relativeX, reference.getLatitudeRadians());
		result.setLatitudeRadians(MathHelper.clamp(reference.getLatitudeRadians()+lat, -MathHelper.HALF_PI, MathHelper.HALF_PI));
		result.setLongitudeRadians(MathHelper.clamp(reference.getLongitudeRadians()+lon, -Math.PI, Math.PI));
		result.setAltitude(reference.getAltitude());
	}

	/**
	 * Latitude/longitude in radians
	 */
	public static double metersToLatitudeLength(double meters, double referenceLatitude) {
		return Math.toRadians(meters/getOneDegreeLatitudeLength(referenceLatitude));
	}

	/**
	 * Latitude/longitude in radians
	 */
	public static double metersToLongitudeLength(double meters, double referenceLatitude) {
		return Math.toRadians(meters/getOneDegreeLongitudeLength(referenceLatitude));
	}

	/**
	 * Latitude/longitude in radians
	 */
	public static float latitudeLengthToMeters(double latitudeLength, double referenceLatitude) {
		return (float)(Math.toDegrees(latitudeLength) * getOneDegreeLatitudeLength(referenceLatitude));
	}
	/**
	 * Latitude/longitude in radians
	 */
	public static float longitudeLengthToMeters(double longitudeLength, double referenceLatitude) {
		return (float)(Math.toDegrees(longitudeLength) * getOneDegreeLongitudeLength(referenceLatitude));
	}
	
	/**
	 * Latitude/longitude in radians
	 */
	private static double getOneDegreeLongitudeLength(double referenceLatitude) {
		return ((P1 * Math.cos(referenceLatitude)) + (P2 * Math.cos(3 * referenceLatitude)) + (P3 * Math.cos(5 * referenceLatitude)));
	}
	/**
	 * Latitude/longitude in radians
	 */
	private static double getOneDegreeLatitudeLength(double referenceLatitude) {
		return (M1 + (M2 * Math.cos(2 * referenceLatitude)) + (M3 * Math.cos(4 * referenceLatitude)) + (M4 * Math.cos(6 * referenceLatitude)));
	}
	
	public static float getUComponent(float headingAngle, float magnitude) {
		return (float)(magnitude * Math.cos(headingAngle));
	}
	public static float getVComponent(float headingAngle, float magnitude) {
		return (float)(magnitude * Math.sin(headingAngle));
	}
	
	public static void main(String[] args) {
		System.out.println(Math.toDegrees(metersToLatitudeLength(110607.76, Math.toRadians(10))));
	}

	public static double headingToMathReference(double angleHeading) {
		return MathHelper.HALF_PI-angleHeading;
	}

	public static double mathToHeadingReference(double angleMath) {
		return MathHelper.HALF_PI-angleMath;
	}

	public static float calculateHeading(float uPart, float vPart) {
		return (float)mathToHeadingReference(Math.atan2(uPart, vPart));
	}

	/**
	 * Transforms a azimuth/elevation coordinate from vehicle to world reference
	 */
	public static void transformVehicleToEarthReference(float[] azimuthElevationResult, float azimuthFromVehicle, float elevationFromVehicle, float vehicleYaw, float vehiclePitch, float vehicleRoll) {
		float rollPitchInTiltWorld = (float)(vehiclePitch*Math.cos(azimuthFromVehicle) - vehicleRoll*Math.sin(azimuthFromVehicle));
		float rollPitchInRollWorld = (float)(vehiclePitch*Math.sin(azimuthFromVehicle) + vehicleRoll*Math.cos(azimuthFromVehicle));
		
		//elevation relative to vehicle pitch/roll in world ref before orthogonal rotation
		float elevationInWorld1 = elevationFromVehicle + rollPitchInTiltWorld;
		
		//orthogonal rotation
		Vector3d rotationRef = new Vector3d(1,0,-(float)Math.tan(rollPitchInTiltWorld));
		Vector3d vect1 = new Vector3d(-1/(Math.tan(elevationInWorld1)),0,1);
		Vector3d vect2 = VectorHelper.rotateVector(vect1, rotationRef, rollPitchInRollWorld);
		
		azimuthElevationResult[0] = (float)Math.atan2(vect2.y,vect2.x) + azimuthFromVehicle + vehicleYaw;
		azimuthElevationResult[1] = (float)Math.atan2(vect2.z, Math.sqrt(vect2.x*vect2.x+vect2.y*vect2.y))-(float)Math.PI;
		
		azimuthElevationResult[0] = (float)MathHelper.normalizeAngle(azimuthElevationResult[0]);
		azimuthElevationResult[1] = (float)MathHelper.normalizeAngle(azimuthElevationResult[1]);
	}

	public static void transformEarthToVehicleReference(float[] azimuthElevationResult, float vehicleRoll, float vehiclePitch, float vehicleYaw) {
		//FIXME azimuth/elevation transformation is different from reverse method. why? FIX THIS!
		vehicleYaw = (float)MathHelper.normalizeAngle(vehicleYaw);
		azimuthElevationResult[0] -= vehicleYaw;//(vehicleYaw*Math.cos(vehicleRoll) + vehiclePitch*Math.sin(vehicleRoll));
		azimuthElevationResult[0] = (float)MathHelper.normalizeAngle(azimuthElevationResult[0]);
		azimuthElevationResult[1] -= (vehiclePitch*Math.cos(azimuthElevationResult[0]) - vehicleRoll*Math.sin(azimuthElevationResult[0]));

		azimuthElevationResult[0] = (float)MathHelper.normalizeAngle(azimuthElevationResult[0]);
		azimuthElevationResult[1] = (float)MathHelper.normalizeAngle(azimuthElevationResult[1]);
	}

	public static boolean calculateRayGroundIntersectionLocation(Coordinates result, Coordinates raySourcePosition, float rayAzimuthVehicle, float rayElevationVehicle, float vehicleRoll, float vehiclePitch, float vehicleYaw) {
		float[] azimuthElevationResult = new float[2];
		CoordinatesHelper.transformVehicleToEarthReference(azimuthElevationResult, rayAzimuthVehicle, rayElevationVehicle, vehicleYaw, vehiclePitch, vehicleRoll);
		//ray won't intersect ground
		if(azimuthElevationResult[1]>=0) {
			return false;
		}
		float de = (float)(raySourcePosition.getAltitude()/(Math.tan(azimuthElevationResult[1])));
		float dx = de * (float)Math.sin(azimuthElevationResult[0]);
		float dy = de * (float)Math.cos(azimuthElevationResult[0]);
//		System.out.println(">>" + dx + " " + dy + " " + de + " " + Math.toDegrees(azimuthElevationResult[0]) + " " + Math.toDegrees(azimuthElevationResult[1]));
		CoordinatesHelper.calculateCoordinatesFromRelativePosition(result, raySourcePosition, dx, dy);
		return true;
	}
	
}
