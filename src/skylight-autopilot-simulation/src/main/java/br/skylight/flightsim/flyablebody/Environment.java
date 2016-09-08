package br.skylight.flightsim.flyablebody;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.vecmath.Vector3d;

import br.skylight.commons.infra.ThreadWorker;
import br.skylight.flightsim.rigidbody.RigidBody3D;

public class Environment extends ThreadWorker {

	private static final Random random = new Random();
	private static final int DUTY_FREQUENCY = 50;

	private double gravity = 9.8;//earth
	private double airDensity = 515.3788 * 0.0023769;//1 atm - kg/m3 = 515.3788 * slug/ft3

	private Vector3d windSpeed = new Vector3d();
	//value represents max random torque in dt for tubulence
	private int turbulenceLevel = 0;
//	private int turbulenceLevel = 70000;
	//max random millis for a turbulence to occurr
	private int turbulenceOccurence = 3000;
	
	private List<RigidBody3D> bodies = new ArrayList<RigidBody3D>();

	public Environment() {
		super(DUTY_FREQUENCY, 1000, 10000);
	}
	
	public void addRigidBody(RigidBody3D body) {
		bodies.add(body);
	}

	@Override
	public void step() throws Exception {
		double dt = (double)getLastWholeStepTime()/1000.0;
		//turbulence emulation
		if(turbulenceLevel>0) {
			//turbulence occurrence
			long turbulenceTime = random.nextInt(turbulenceOccurence);
			if((System.currentTimeMillis()%turbulenceOccurence)<turbulenceTime) {
				float tf = random.nextInt(turbulenceLevel) - (turbulenceLevel/2);
//				airplane.applyTorque(0, tf, 0, dt);
			}
		}

		//evolve body dynamics
		for (RigidBody3D b : bodies) {
			b.move(dt);
		}
	}

	public double getGravity() {
		return gravity;
	}
	public void setGravity(double gravity) {
		this.gravity = gravity;
	}

	public double getAirDensity() {
		return airDensity;
	}
	public void setAirDensity(double airDensity) {
		this.airDensity = airDensity;
	}
	
	public Vector3d getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(Vector3d windSpeed) {
		this.windSpeed = windSpeed;
	}

}
