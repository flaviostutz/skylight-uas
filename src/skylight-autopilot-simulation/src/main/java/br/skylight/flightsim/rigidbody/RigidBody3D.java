package br.skylight.flightsim.rigidbody;

import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import br.skylight.commons.infra.VectorHelper;

/**
 * The RigidBody3D class provides an object for encapsulating rigid bodies that
 * live in 3D. Modified by Flávio Stutz
 * 
 * @version 1.0
 * @author Mark Hale
 */
public class RigidBody3D extends ClassicalParticle3D {

	private static final double TWO_PI = Math.PI * 2;

	/**
	 * Moment of inertia.
	 */
	protected Matrix3d momentOfInertiaTensor;
	protected Matrix3d momentOfInertiaTensorInv;

	/**
	 * Angles (orientation).
	 */
	protected Vector3d orientationAngles = new Vector3d();

	/**
	 * Angular velocity.
	 */
	protected Vector3d angularVelocity = new Vector3d();

	/**
	 * Sets the moment of inertia.
	 */
	public void setMomentOfInertiaTensor(Matrix3d momentOfInertiaTensor) {
		this.momentOfInertiaTensor = momentOfInertiaTensor;
		momentOfInertiaTensorInv = new Matrix3d(momentOfInertiaTensor);
		momentOfInertiaTensorInv.invert();
	}

	/**
	 * Returns the moment of inertia.
	 */
	public Matrix3d getMomentOfInertiaTensor() {
		return momentOfInertiaTensor;
	}

	/**
	 * Sets the angles (orientation) of this body.
	 */
	public void setOrientationAngles(Vector3d orientationAngles) {
		this.orientationAngles = orientationAngles;
	}
	public Vector3d getOrientationAngles() {
		return orientationAngles;
	}

	public Quat4d getOrientationRotation() {
		return VectorHelper.computeQuarternionFromEulerAngles(orientationAngles.x, orientationAngles.z, orientationAngles.y);
	}

	public void setAngularVelocity(Vector3d angularVelocity) {
		this.angularVelocity = angularVelocity;
	}
	public Vector3d getAngularVelocity() {
		return angularVelocity;
	}

	public void setAngularMomentum(Vector3d angularMomentum) {
		//L = Iw >>> L: column vector; I: inertial tensor matrix; w: column vector
		Matrix3d av = new Matrix3d();
		av.mul(getOrientedMomentOfInertiaTensorInv(), VectorHelper.toColumnVectorMatrix(angularMomentum));
		angularVelocity = VectorHelper.fromColumnVectorMatrix(av);
	}

	protected Matrix3d getOrientedMomentOfInertiaTensor() {
		Matrix3d r = new Matrix3d();
		r.set(getOrientationRotation());
		
		Matrix3d i = new Matrix3d();
		i.mul(r, momentOfInertiaTensor);
		i.mulTransposeRight(i, r);
		return i;
	}
	protected Matrix3d getOrientedMomentOfInertiaTensorInv() {
		Matrix3d r = new Matrix3d();
		r.set(getOrientationRotation());
		
		Matrix3d i = new Matrix3d();
		i.mul(r, momentOfInertiaTensorInv);
		i.mulTransposeRight(i, r);
		return i;
	}

	public Vector3d getAngularMomentum() {
		Matrix3d am = new Matrix3d();
		am.mul(getOrientedMomentOfInertiaTensor(), VectorHelper.toColumnVectorMatrix(angularVelocity));
		return VectorHelper.fromColumnVectorMatrix(am);
	}

	/**
	 * Returns the kinetic and rotational energy.
	 */
//	public double energy() {
//		return (mass * (vx * vx + vy * vy + vz * vz) + angMass * (angxVel * angxVel + angyVel * angyVel + angzVel * angzVel)) / 2.0;
//	}

	/**
	 * Evolves this particle forward according to its kinematics. This method
	 * changes the particle's position and orientation.
	 * 
	 * @return this.
	 */
	public ClassicalParticle3D move(double dt) {
		return rotate(dt).translate(dt);
	}

	/**
	 * Evolves this particle forward according to its rotational kinematics.
	 * This method changes the particle's orientation.
	 * 
	 * @return this.
	 */
	public RigidBody3D rotate(double dt) {
		Vector3d angleDiff = new Vector3d(angularVelocity);
		angleDiff.scale(dt);
		orientationAngles.x = (normalizeAngle(orientationAngles.x + angleDiff.x));
		orientationAngles.y = (normalizeAngle(orientationAngles.y + angleDiff.y));
		orientationAngles.z = (normalizeAngle(orientationAngles.z + angleDiff.z));
		return this;
	}

	private double normalizeAngle(double angle) {
		if (angle >= TWO_PI) {
			angle -= TWO_PI;
		} else if (angle < 0.0) {
			angle += TWO_PI;
		}
		return angle;
	}
	
	/**
	 * Accelerates this particle. This method changes the particle's angular
	 * velocity. It is additive, that is
	 * <code>angularAccelerate(a1, dt).angularAccelerate(a2, dt)</code> is
	 * equivalent to <code>angularAccelerate(a1+a2, dt)</code>.
	 * 
	 * @return this.
	 */
	public RigidBody3D angularAccelerate(Vector3d angularAcceleration, double dt) {
		Vector3d av = new Vector3d(angularAcceleration);
		av.scale(dt);
		angularVelocity.add(av);
		return this;
	}

	/**
	 * Applies a torque to this particle. This method changes the particle's
	 * angular velocity. It is additive, that is
	 * <code>applyTorque(T1, dt).applyTorque(T2, dt)</code> is equivalent to
	 * <code>applyTorque(T1+T2, dt)</code>.
	 * 
	 * @return this.
	 */
	public RigidBody3D applyTorque(Vector3d torque, double dt) {
		//T = Ia
		Matrix3d aa = new Matrix3d();
		aa.mul(getOrientedMomentOfInertiaTensorInv(), VectorHelper.toColumnVectorMatrix(torque));
		return angularAccelerate(VectorHelper.fromColumnVectorMatrix(aa), dt);
	}

	/**
	 * Applies a force acting at a point away from the centre of mass. Any
	 * resultant torques are also applied. This method changes the particle's
	 * angular velocity.
	 * 
	 * @param x
	 *            x-coordinate from centre of mass.
	 * @param y
	 *            y-coordinate from centre of mass.
	 * @param z
	 *            z-coordinate from centre of mass.
	 * @return this.
	 */
	public RigidBody3D applyForce(PositionedVector appliedForce, double dt) {
		if(appliedForce.getVector().length()>0) {
			//APPLY TORQUE
			//T = r x F
			Vector3d position = new Vector3d(appliedForce.getPoint());
			applyTorque(VectorHelper.cross(position, appliedForce.getVector()), dt);
			
			//APPLY FORCE IN CENTER OF MASS
			//r.F/|r|^2
			//is this correct?
//			double k = position.dot(appliedForce.getVector()) / (position.length()*position.length());
//			Vector3d force = new Vector3d(appliedForce.getPoint());
//			force.scale(k);
//			applyForce(force, dt);
			
			//using this for now
			position.normalize();
			double k = position.dot(appliedForce.getVector());
			Vector3d force = new Vector3d(position);
			force.normalize();
			force.scale(k);
			applyForce(force, dt);
		}
		return this;
	}
}
