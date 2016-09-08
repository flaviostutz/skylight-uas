package br.skylight.commons.infra;

import java.text.NumberFormat;

import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;


public class VectorHelper {

	/**
	 * Computes a quaternion according to NASA convention on aircraft euler angles.
	 * Order of rotation is z-y-x. z=yaw; y=pitch; x=roll
	 * See http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm
	 */
	public static Quat4d computeQuarternionFromEulerAngles(double roll, double pitch, double yaw) {
		double c1 = Math.cos(yaw / 2.0);
	    double c2 = Math.cos(pitch / 2.0);
	    double c3 = Math.cos(roll / 2.0);
	    double s1 = Math.sin(yaw / 2.0);
	    double s2 = Math.sin(pitch / 2.0);
	    double s3 = Math.sin(roll / 2.0);
	
	    double w = c1*c2*c3 - s1*s2*s3;
		double x = s1*s2*c3 + c1*c2*s3;
		double y = s1*c2*c3 + c1*s2*s3;
		double z = c1*s2*c3 - s1*c2*s3;
	
		return new Quat4d(x,y,z,w);
	}

	/**
	 * Rotates a vector according to NASA convention on aircraft euler angles.
	 * Order of rotation is y-z-x. y=heading; z=pitch; x=roll
	 * See http://www.euclideanspace.com/maths/geometry/rotations/euler/index.htm
	 */
	public static Vector3d rotateAirplaneVectorEuler(Vector3d vector, double roll, double pitch, double heading) {
//		Vector3d v = new Vector3d(vector);
//	
//		Matrix3d mh = new Matrix3d();
//		mh.rotY(heading);
//	
//		Matrix3d mp = new Matrix3d();
//		mp.rotZ(pitch);
//	
//		Matrix3d mr = new Matrix3d();
//		mr.rotX(roll);
//	
//		mh.mul(mp);
//		mh.mul(mr);
//		mh.transform(v);
//		return v;
		return rotateVector(vector, getRotationFromAirplaneEulerAngles(roll, heading, pitch));
	}
	
	/**
	 * Gets a quaternion representation of a rotation according to NASA convention on aircraft euler angles.
	 * Order of rotation is y-z-x. y=heading; z=pitch; x=roll
	 * See http://www.euclideanspace.com/maths/geometry/rotations/euler/index.htm
	 */
	public static Quat4d getRotationFromAirplaneEulerAngles(double x, double y, double z) {
		Matrix3d mh = new Matrix3d();
		mh.rotY(y);
	
		Matrix3d mp = new Matrix3d();
		mp.rotZ(z);
	
		Matrix3d mr = new Matrix3d();
		mr.rotX(x);
	
		mh.mul(mp);
		mh.mul(mr);
		
		Quat4d q = new Quat4d();
		q.set(mh);
		return q;
	}

	public static String str(Tuple3d dir) {
		if(dir==null) return "(null)";
		return "(" + VectorHelper.str(dir.x) + ";" + VectorHelper.str(dir.y) + ";" + VectorHelper.str(dir.z) + ")";
	}

	public static String str(double v) {
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setGroupingUsed(false);
		return nf.format(v);
	}

	public static Vector3d rotateVector(Vector3d vector, Vector3d reference, double angle) {
		Quat4d q = VectorHelper.calculateRotationAroundVector(reference, angle);
		return VectorHelper.rotateVector(vector, q);
	}

	public static Quat4d calculateRotationAroundVector(Vector3d vector, double angle) {
		Quat4d q = new Quat4d(vector.x * Math.sin(angle/2.0),
				vector.y * Math.sin(angle/2.0),
				vector.z * Math.sin(angle/2.0),
	            Math.cos(angle/2.0));
		q.normalize();
		return q;
	}

	public static Vector3d rotateVector(Vector3d vector, Quat4d rotation) {
		Quat4d vq = vectorToQuaternion(vector);

		Quat4d conj = new Quat4d(rotation);
		conj.conjugate();

		//multiplications
		Quat4d r = new Quat4d(rotation);
		r.mul(vq);
		r.mul(conj);
		
		return new Vector3d(r.x, r.y, r.z);
	}

	public static Quat4d vectorToQuaternion(Vector3d vector) {
		// We'll need the length of the vector v
		double vqScale = vector.length();
		
		// Make the vector v into a quaternion.
		Quat4d vq = new Quat4d(vector.x, vector.y, vector.z, 0.0);
		
		// The above constructor will normalize the quaternion.
		// just to remind myself that the result of Quat4d is normalized.
		vq.normalize();
		vq.scale(vqScale);  // Set the length back to the length of the vector
		return vq;
	}

	public static void main(String[] args) {
		System.out.println(rotateAirplaneVectorEuler(new Vector3d(1,0,0), 0, 0, Math.toRadians(180)));
	}

	public static Vector3d cross(Vector3d v1, Vector3d v2) {
		Vector3d result = new Vector3d();
		result.cross(v1, v2);
		return result;
	}

	public static Matrix3d toColumnVectorMatrix(Vector3d vector) {
		return new Matrix3d(vector.x,0,0,
							vector.y,0,0,
							vector.z,0,0);
	}

	public static Vector3d fromColumnVectorMatrix(Matrix3d matrix) {
		return new Vector3d(matrix.m00, matrix.m10, matrix.m20);
	}

	public static void subtract(Vector3d vector, double x, double y, double z) {
		vector.x = (vector.x - x);
		vector.y = (vector.y - y);
		vector.z = (vector.z - z);
	}

}