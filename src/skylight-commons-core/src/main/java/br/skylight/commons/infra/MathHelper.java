package br.skylight.commons.infra;


public class MathHelper {

	public static double TWO_PI = Math.PI*2.0;
	public static double HALF_PI = Math.PI/2.0;
	
//	private static double ANGLE_NORMALIZATION_REFERENCE_TWOPI = TWO_PI;
//	private static double ANGLE_NORMALIZATION_TARGET_TWOPI = TWO_PI;
	
	public static double clamp(double value, double from, double to) {
		if(value<from) return from;
		if(value>to) return to;
		return value;
	}
	public static float clamp(float value, float from, float to) {
		return (float)clamp((double)value, (double)from, (double)to);
	}

	public static boolean near(double value1, double value2, double diff) {
		if(Math.abs(value1-value2)<=diff) {
			return true;
		}
		return false;
	}

	/**
	 * Return an angle between -180 and 180 degrees (in radians)
	 * @param angle in radians
	 * @return
	 */
	public static double normalizeAngle(double angle) {
		angle = angle%TWO_PI;
		if(angle>Math.PI) {
			return angle-TWO_PI;
		} else if(angle<-Math.PI) {
			return angle+TWO_PI;
		} else {
			return angle;
		}
	}

	/**
	 * Returns an angle between 0 and 360 (in radians)
	 * @param angle in radians
	 * @return
	 */
	public static double normalizeAngle2(double angle) {
		angle = angle%TWO_PI;
		if(angle<0) angle += TWO_PI;
		return angle;
	}

	/**
	 * Calculates a normalized error for values that have a 'circular' meaning, like heading for instance.
	 * For example, 350 to 0 degrees represents an error of 10 degrees.
	 * @param error in radians
	 * @return normalized error in radians
	 */
	public static double getNormalizedErrorTwoPi(double error) {
		//normalize error to TWO PI
		if(error>TWO_PI) {
			error = error - TWO_PI;
		} else if(error<-TWO_PI) {
			error = TWO_PI + error;
		}
		return error;
	}

	/**
	 * CLDC/MIDP lacks Math.atan(). This implementation uses a numerical
	 * geometric mean.
	 * 
	 * @param z
	 * @return
	 * @see http://mathworld.wolfram.com/InverseTangent.html
	 */
//	public static double arctan(double z) {
//		// special cases
//		if (Double.isNaN(z))
//			return Double.NaN;
//		if (z == 0.0)
//			return 0.0;
//	
//		// set accuracy rate here, in terms of max iterations or early
//		// convergence. This typically converges in under 20 iterations.
//		double conv = 1.0E-10;
//		int max = 20;
//	
//		// a_0 = 1/sqrt( 1 + x^2 )
//		double a = 1 / Math.sqrt(1 + z * z);
//		// b_0 = 1
//		double b = 1.0;
//	
//		double diff = 1.0;
//		int i;
//		for (i = 0; (i < max) && (diff > conv); i++) {
//			double oldA = a;
//			// a_{i+1} = 1/2 (a_i + b_i)
//			a = 0.5 * (a + b);
//			diff = Math.abs(a - oldA);
//			// b_{i+1} = sqrt( a_{i+1} * b_i )
//			b = Math.sqrt(a * b);
//		}
//	
//		// atan(x), lim n -> infty = x / (sqrt(1+x^2) * a_n)
//		double result = z / (Math.sqrt(1 + z * z) * a);
//		return result;
//	}

	/**
	 * CLDC/MIDP lacks Math.atan2(). This is an implementation using
	 * {@link arctan} and a logic table for quadrant.
	 * <p>
	 * The point of atan2() is that the signs of both inputs are known to it, so
	 * it can compute the correct quadrant for the angle. For example, atan(1)
	 * and atan2(1, 1) are both pi/4, but atan2(-1, -1) is -3*pi/4.
	 * 
	 * @param y
	 * @param x
	 * @return Return atan(y / x), in radians.
	 * @see http://en.wikipedia.org/wiki/Atan2
	 */
//	public static double arctan2(double y, double x) {
//		// subset of the special cases in the Java5 spec
//		if ((Double.isNaN(x) || Double.isNaN(y)))
//			return Double.NaN;
//		if (x == 0.0) {
//			if (y == 0.0)
//				return 0.0;
//			if (y > 0.0)
//				return Math.PI * 0.5;
//			if (y < 0.0)
//				return -Math.PI * 0.5;
//		}
//		if (y == 0.0) {
//			if (x > 0.0)
//				return 0.0;
//			if (x < 0.0)
//				return Math.PI;
//		}
//	
//		// get the quadrant right
//		double atan = arctan(y / x);
//		if (x < 0) {
//			if (y < 0) {
//				atan -= Math.PI;
//			} else {
//				atan += Math.PI;
//			}
//		}
//	
//		return atan;
//	}
	
	public static boolean isPointInsidePolygon(double[] vertx, double[] verty, double px, double py) {
	  int i, j;
	  boolean c = false;
	  for (i = 0, j = vertx.length-1; i < vertx.length; j = i++) {
	    if ( ((verty[i]>=py) != (verty[j]>=py)) &&
		 (px <= (vertx[j]-vertx[i]) * (py-verty[i]) / (verty[j]-verty[i]) + vertx[i]) )
	       c = !c;
	  }
	  return c;
	}
	
	public static float getTurnRadius(float speed, float roll) {
		speed = MeasureHelper.metersPerSecondToKnots(speed);
		float radius = MeasureHelper.feetToMeters((speed*speed)/(11.26F*(float)Math.tan(roll)));
		return radius;
	}

	public static double getAngleTo(double p1x, double p1y, double p2x, double p2y) {
		return Math.atan2(p2y-p1y, p2x-p1x);
	}
	
	/**
	 * Rotates a point around origin.
	 * Returns float[], where 0: X; 1: Y
	 */
	public static double[] rotateAroundOrigin(double px, double py, double angle) {
		return new double[]{
				(px * Math.cos(angle)) - (py * Math.sin(angle)),
				(px * Math.sin(angle)) + (py * Math.cos(angle))};
	}
	
	/**
	 * Calculates a proportional value between 0 and 1 for an input value between 0 and 1 
	 * so that it forms an exponential decay curve
	 * @param x A value between 0 and 1
	 * @param lambda An index that determines the exponential curve form
	 * @return A value between 0 and 1
	 */
	public static double getExponentialDecay(double x, double lambda) {
		return Math.exp(-x*lambda) - Math.exp(-lambda);		
	}
	
	/**
	 * Calculates a proportional value between 0 and 1 for an input value between 0 and 1
	 * so that it forms an exponential mirrored curve. Negative values will be mirrored from positive ones.
	 * This function was adjusted so that its boundaries will be x=0/y=0 and x=1/y=1
	 * @param x
	 * @param lambda
	 * @return
	 */
	public static double getExponentialCurve(double x, double lambda) {
		return Math.signum(x) * Math.exp(lambda*Math.abs(x)-1)/Math.exp(lambda-1);
	}
	
	public static int nextMultiple(float value, int multipleOf) {
		return (int)(value - value%multipleOf);
	}
	
//	public static void main(String[] args) {
//		for(float i=-1; i<=1; i+=0.01F) {
//			System.out.println(i + "=" + getExponentialCurve(i, 4));
//		}
//		System.out.println(">> "+ -1 + "=" + getExponentialCurve(-1, 4));
//		System.out.println(">> "+ 0 + "=" + getExponentialCurve(0, 4));
//		System.out.println(">> "+ 1 + "=" + getExponentialCurve(1, 4));
//	}

	/**
	 * Returns a ampliation factor given the camera initial fov (wide) and current fov
	 * Calculated based on readings from http://en.wikipedia.org/wiki/Angle_of_view
	 * The formula looks like in http://www.planetside.co.uk/terragen/dev/tgcamera.html, but there they 
	 * discarded the reference fov at wide end (seems like they considered it to be '1')
	 * horizontalFovAt1XZoom is the fov at wide end described by the camera vendor 
	 * horizontalFov given in radians
	 * Returns zoom factor (1X, 2X etc)
	 */
	public static float getZoom(float horizontalFov, float horizontalFovAt1XZoom) {
		return (float)(Math.tan(horizontalFovAt1XZoom/2.0)/Math.tan(horizontalFov/2.0));		
	}
	
	/**
	 * Calculates horizontal FOV for a camera given a zoom scale
	 * Calculated based on readings from http://en.wikipedia.org/wiki/Angle_of_view
	 * @param zoom factor (1X, 2X etc)
	 * @param horizontalFovAt1XZoom is the fov at wide end described by the camera vendor
	 * @return horizontalFov in radians
	 */
	public static float getHorizontalFOV(float zoom, float horizontalFovAt1XZoom) {
		return (float)(2.0*Math.atan(Math.tan(horizontalFovAt1XZoom/2.0)/zoom));
	}
	/**
	 * Calculates horizontal FOV for a camera given a zoom scale
	 * Calculated based on readings from http://en.wikipedia.org/wiki/Angle_of_view
	 * @param zoom factor (1X, 2X etc)
	 * @param horizontalFovAt1XZoom is the fov at wide end described by the camera vendor
	 * @return horizontalFov in radians
	 */
	public static float getVerticalFOV(float zoom, float horizontalFovAt1XZoom, float widthToHeightRatio) {
		return (float)(2.0*Math.atan(((widthToHeightRatio*Math.tan(horizontalFovAt1XZoom/2.0))*Math.tan(horizontalFovAt1XZoom/2.0))/zoom));
	}
	
}
