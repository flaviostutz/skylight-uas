package br.skylight.flightsim.rigidbody;

import javax.vecmath.Vector3d;

import br.skylight.commons.infra.VectorHelper;

/**
 * The ClassicalParticle3D class provides an object for encapsulating classical
 * point particles that live in 3D.
 * Modified by Flávio Stutz 
 * @version 1.0
 * @author Silvere Martin-Michiellot
 * @author Mark Hale
  */
public class ClassicalParticle3D {
	/**
	 * Mass.
	 */
	protected double mass;

	/**
	 * Position coordinates.
	 */
	protected Vector3d position = new Vector3d();

	/**
	 * Velocity coordinates.
	 */
	protected Vector3d velocity = new Vector3d();

	/**
	 * Constructs a classical particle.
	 */
	public ClassicalParticle3D() {
	}

	public void setMass(double m) {
		mass = m;
	}

	public double getMass() {
		return mass;
	}

	public void setPosition(Vector3d position) {
		this.position = position;
	}

	public Vector3d getPosition() {
		return position;
	}
	
	public Vector3d getVelocity() {
		return velocity;
	}
	public void setVelocity(Vector3d velocity) {
		this.velocity = velocity;
	}
	
	public void setMomentum(Vector3d momentum) {
		Vector3d v = new Vector3d(momentum);
		v.scale(1.0/mass);
		velocity = v;
	}

	public Vector3d getMomentum() {
		Vector3d m = new Vector3d(velocity);
		m.scale(mass);
		return m;
	}

	/**
	 * Returns the kinetic energy.
	 */
	public double getKineticEnergy() {
		return mass * (velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z) / 2.0;
	}

	/**
	 * Evolves this particle forward according to its kinematics. This method
	 * changes the particle's position.
	 * 
	 * @return this.
	 */
	public ClassicalParticle3D move(double dt) {
		return translate(dt);
	}

	/**
	 * Evolves this particle forward according to its linear kinematics. This
	 * method changes the particle's position.
	 * 
	 * @return this.
	 */
	public ClassicalParticle3D translate(double dt) {
		Vector3d pdiff = new Vector3d(velocity);
		pdiff.scale(dt);
		position.add(pdiff);
		return this;
	}

	/**
	 * Accelerates this particle. This method changes the particle's velocity.
	 * It is additive, that is
	 * <code>accelerate(a1, dt).accelerate(a2, dt)</code> is equivalent to
	 * <code>accelerate(a1+a2, dt)</code>.
	 * 
	 * @return this.
	 */
	public ClassicalParticle3D accelerate(Vector3d acceleration, double dt) {
		Vector3d vdiff = new Vector3d(acceleration);
		vdiff.scale(dt);
		velocity.add(vdiff);
		return this;
	}

	/**
	 * Applies a force to this particle. This method changes the particle's
	 * velocity. It is additive, that is
	 * <code>applyForce(F1, dt).applyForce(F2, dt)</code> is equivalent to
	 * <code>applyForce(F1+F2, dt)</code>.
	 * 
	 * @return this.
	 */
	public ClassicalParticle3D applyForce(Vector3d force, double dt) {
		Vector3d a = new Vector3d(force);
		a.scale(1.0/mass);
		return accelerate(a, dt);
	}

	/**
	 * Evolves two particles under their mutual gravitational attraction. This
	 * method changes the velocity of both particles.
	 * 
	 * @return this.
	 */
	public ClassicalParticle3D gravitate(ClassicalParticle3D p, double dt) {
		final double dx = p.getPosition().x - position.x;
		final double dy = p.getPosition().y - position.y;
		final double dz = p.getPosition().z - position.z;
		final double rr = dx * dx + dy * dy + dz * dz;
		final double r = Math.sqrt(rr);
		final double g = p.mass / rr;
		final double pg = mass / rr;
		VectorHelper.subtract(velocity, 
								g * dx * dt/r, 
								g * dy * dt/r, 
								g * dz * dt/r);
		VectorHelper.subtract(p.getVelocity(), 
								pg * dx * dt/r, 
								pg * dy * dt/r, 
								pg * dz * dt/r);
		return this;
	}
	
}
