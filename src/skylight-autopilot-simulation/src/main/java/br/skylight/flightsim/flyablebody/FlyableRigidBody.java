package br.skylight.flightsim.flyablebody;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import br.skylight.commons.Coordinates;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MeasureHelper;
import br.skylight.commons.infra.VectorHelper;
import br.skylight.flightsim.rigidbody.ClassicalParticle3D;
import br.skylight.flightsim.rigidbody.PositionedVector;
import br.skylight.flightsim.rigidbody.RigidBody3D;

public class FlyableRigidBody extends RigidBody3D {

	private static final long serialVersionUID = 1L;

	private Coordinates initialCoordinates = new Coordinates(15,45,0);
	private Coordinates coordinates = new Coordinates(15,45,0);
	
	private Map<String,BodyPart> parts = new HashMap<String,BodyPart>();
	private Environment environment;

	private double restitutionCoefficient = 0.6;
	
	private boolean flying = false;
	private boolean verifyGroundContact = false;
	
	public FlyableRigidBody(Environment environment) {
		this.environment = environment;
	}

	@Override
	public ClassicalParticle3D translate(double dt) {
		//apply forces from all faces
		for (BodyPart bp : parts.values()) {
			for (PartFaceRect face : bp.getFaces().values()) {
				applyForce(face.getDragForce(), dt);
				applyForce(face.getSelfForce(), dt);
			}
		}
		
		//gravity
		applyForce(getWeightForce(), dt);
		
		return super.translate(dt);
	}

	/**
	 * Evolve flight dynamics for a period of time considering active surfaces
	 * This method changes linear and angular positioning/velocity.
	 * @param dt
	 */
	@Override
	public ClassicalParticle3D move(double dt) {
		ClassicalParticle3D r = super.move(dt);
		verifyGroundContact();
		System.out.println("pos: "+ VectorHelper.str(getPosition()) +"; roll=" + VectorHelper.str(Math.toDegrees(getOrientationAngles().x)) + "; pitch=" + VectorHelper.str(Math.toDegrees(getOrientationAngles().z)) + "; head=" + VectorHelper.str(Math.toDegrees(getOrientationAngles().y)) + " vel: " + VectorHelper.str(getVelocity()));
		return r;
	}
	
	public Map<String, BodyPart> getParts() {
		return parts;
	}
	
	public Vector3d getLinearVelocity(Point3d point) {
		Vector3d vel = new Vector3d();
		vel.cross(getAngularVelocity(), new Vector3d(point));
		vel.add(getVelocity());
		return vel;
	}

	public Vector3d getTrueAirspeed(Point3d pos) {
		//airspeed due to rotational + translational movements
		Vector3d airspeed = new Vector3d(getLinearVelocity(pos));
		airspeed.scale(-1);
		//airspeed due to difference to wind movements
		airspeed.add(environment.getWindSpeed());
		return airspeed;
	}
	
	public Environment getEnvironment() {
		return environment;
	}

	public Coordinates getCoordinates() {
		double latitude = CoordinatesHelper.metersToLatitudeLength((float)getPosition().x, (float)initialCoordinates.getLatitudeRadians());
		double longitude = CoordinatesHelper.metersToLongitudeLength((float)getPosition().z, (float)initialCoordinates.getLatitudeRadians());
		float altitude = (float)getPosition().y;
		coordinates.setLatitude(latitude%(Math.PI/2));
		coordinates.setLongitude(longitude%Math.PI);
		coordinates.setAltitude(altitude);
		return coordinates;
	}
	
	public void setInitialCoordinates(double latitude, double longitude) {
		initialCoordinates.setLatitude(latitude);
		initialCoordinates.setLongitude(longitude);
	}

	public void setAltitude(float altitude) {
		getPosition().y = (MeasureHelper.feetToMeters(altitude));
	}
	public double getAltitude() {
		return getPosition().y;
	}
	
	public Vector3d getWeightForce() {
		Vector3d weightForce = new Vector3d(0,-getMass()*environment.getGravity(),0);
		return weightForce;
	}

	protected void verifyGroundContact() {
		if(verifyGroundContact) {
			//ground touching verification
			if(getAltitude()<=0) {
				if(!flying) {
					setAltitude(0);
					getVelocity().y = (0);
					getOrientationAngles().x = (0);
					getOrientationAngles().z = (0);
				} else {
					if(getVelocity().y<-0.6 || Math.abs(getOrientationAngles().x)>7 || Math.abs(getOrientationAngles().z)>7) {
						System.out.println("CRASHED!");
						setVelocity(new Vector3d());
						setAngularVelocity(new Vector3d());
						flying = false;
					} else {
						getPosition().y = (0);
						//bounce back (elastic colision with ground)
						getVelocity().y = (-restitutionCoefficient*getVelocity().y);
					}
				}
			}
			if(getPosition().y>0.5) {
				flying = true;
			}
		}
	}

	public Vector3d getHeadDir() {
		return VectorHelper.rotateVector(new Vector3d(1,0,0), getOrientationRotation());
	}
	public Vector3d getRightSideDir() {
		return VectorHelper.rotateVector(new Vector3d(0,0,1), getOrientationRotation());
	}
	public Vector3d getNormalDir() {
		return VectorHelper.rotateVector(new Vector3d(0,1,0), getOrientationRotation());
	}

	public Vector3d getGroundSpeed() {
		return getVelocity();
	}
	protected Vector3d getGroundSpeedDir() {
		Vector3d result = new Vector3d(getGroundSpeed());
		if(result.length()>0) {
			result.normalize();
			return result;
		} else {
			return null;
		}
	}
	
	public void moveBodyPartsBy(Tuple3d diff) {
		for (BodyPart bp : getParts().values()) {
			bp.getPosition().add(diff);
		}
	}

	public void setVerifyGroundContact(boolean verifyGroundContact) {
		this.verifyGroundContact = verifyGroundContact;
	}
	
	public static void main(String[] args) {
		Environment ev = new Environment();
		FlyableRigidBody f = new FlyableRigidBody(ev);
		
		BodyPartAirfoil p = new BodyPartAirfoil(f, new Point3d(10,0,0), 1, 1, 1, 1);
//		BodyPartCube p = new BodyPartCube(f, new Point3d(10,0,0), 1, 1, 1);
		f.getParts().put("1",p);
		
		BodyPartFlat p2 = new BodyPartFlat(f, new Point3d(), new Point3d(0,0,0), new Point3d(0,0,1), new Point3d(0,1,1), new Point3d(0,1,0));
		f.getParts().put("2",p2);
		
		f.moveBodyPartsBy(new Point3d(-0.5, -0.5, -0.5));

		f.setPosition(new Vector3d(100, 100, 100));
		f.setVelocity(new Vector3d(20, 0, 0));
//		f.setWindSpeed(new Vector3d(-10,0,0));
		f.setOrientationAngles(new Vector3d(0, 0, 0));
		
//		Vector3d v = new Vector3d(1,0,0);
//		System.out.println(VectorHelper.rotateVector(v, new Vector3d(0,0,1), Math.toRadians(90)));
		
		PositionedVector af = f.getParts().get(0).getFaces().get(BodyPartCube.FACE_HEAD).getDragForce();
		PositionedVector sf = f.getParts().get(0).getFaces().get(BodyPartCube.FACE_HEAD).getSelfForce();
		System.out.println("head  "+str(af) + "; self-force "+str(sf));

		af = f.getParts().get(0).getFaces().get(BodyPartCube.FACE_REAR).getDragForce();
		System.out.println("rear  "+str(af));

		af = f.getParts().get(0).getFaces().get(BodyPartCube.FACE_RIGHT_SIDE).getDragForce();
		System.out.println("right "+str(af));

		af = f.getParts().get(0).getFaces().get(BodyPartCube.FACE_LEFT_SIDE).getDragForce();
		System.out.println("left  "+str(af));

		af = f.getParts().get(1).getFaces().get("face").getDragForce();
		System.out.println("flat-yz  "+str(af));
	}

	public static String str(PositionedVector af) {
		return "point:" + VectorHelper.str(af.getPoint())+"; force:" + VectorHelper.str(af.getVector());
	}
	
}
