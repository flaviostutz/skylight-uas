package br.skylight.commons.infra;

public class MeasureHelper {

//	public static float metersToFeet(float metersValue){
//		return (float)(metersValue * 3.2808399);
//	}
	
	public static float feetToMeters(float feetValue) {
		return feetValue * 0.3048F;
	}
	
	public static float knotsToMetersPerSecond(float value) {
		return value * 0.514444444F;
	}

//	public static float feetToInches(float feetValue) {
//		return feetValue * 12;
//	}
//
//	public static float inchesToFeet(float inchesValue) {
//		return inchesValue / 12;
//	}
	
	public static float kilometersToMeters(float value) {
		return value * 1000;
	}

	public static float inchesToMeters(float value) {
		return value * 0.0254F;
	}

	public static double cubitMeterToLiters(double value) {
		return value * 1000F;
	}
	
	public static long secondsToMillis(double time) {
		return (long)(time * 1000.0);
	}

	public static float milesToMetersPerSecond(float value) {
		return value * 0.44704F;
	}
	
	public static float kilometersToKnots(float value){
		return value * 0.539956803F;
	}

	public static float metersPerSecondToKnots(float value) {
		return value * 1.94384449F;
	}

	public static double secondsToMinutes(double seconds) {
		return seconds/60;
	}

	public static double minutesToSeconds(double minutes) {
		return minutes * 60;
	}

	public static float calculateMagnitude(float uPart, float vPart) {
		return (float)Math.sqrt(uPart*uPart+vPart*vPart);
	}
	
	public static boolean areInRange(double v1, double v2, double diff) {
		return Math.abs(v1-v2)<=diff;
	}

	public static double convertPascalsToInHg(float value) {
//		1 pascal is equal to 0.000295299830714
//		1 inHg = 3,386.389 pascals at 0 °C
		return value * 0.000295299830714;
	}

	public static float inHgToPascal(float value) {
		return value * 3386F;
	}

}
