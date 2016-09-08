package br.skylight.flightsim;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.VectorHelper;
import br.skylight.flightsim.flyablebody.BodyPartAirfoil;
import br.skylight.flightsim.flyablebody.BodyPartCube;
import br.skylight.flightsim.flyablebody.BodyPartFlat;
import br.skylight.flightsim.flyablebody.DragCoefficients;
import br.skylight.flightsim.flyablebody.Environment;
import br.skylight.flightsim.flyablebody.FlyableRigidBody;
import br.skylight.flightsim.flyablebody.PartFaceAirfoil;
import br.skylight.flightsim.flyablebody.PartFaceRect;
import br.skylight.flightsim.flyablebody.PartFaceThrust;
import br.skylight.flightsim.rigidbody.PositionedVector;

public class BasicAirplane extends FlyableRigidBody {

	private static final long serialVersionUID = 1L;
	
	private double aileron = 0;
	private double rudder = 0;
	private double elevator = 0;
	private double throttle = 0;
	
	private BasicAirplaneInfo info;
	
	private BodyPartFlat aileronLeftPart;
	private BodyPartFlat aileronRightPart;
	private BodyPartFlat rudderPart;
	private BodyPartFlat elevatorPart;

	private PartFaceThrust thrustFace;
	private PartFaceAirfoil leftAirfoilFace;
	private PartFaceAirfoil rightAirfoilFace;
	
	public BasicAirplane(Environment environment) {
		super(environment);
		setBasicAirplaneInfo(new BasicAirplaneInfo());
	}
	
	public void setBasicAirplaneInfo(BasicAirplaneInfo basicAirplaneInfo) {
		this.info = basicAirplaneInfo;

		//BUILD AIRPLANE GEOMETRY
		getParts().clear();
		
		//body
		BodyPartCube body = new BodyPartCube(this, 
								new Point3d(info.getHorizontalStabilizatorLength(), 0, (info.getWingSpan()/2.0)-(info.getBodyWidth()/2.0)),
								info.getBodyWidth(), info.getBodyLength(), info.getBodyHeight());
		body.setDragCoefficientForAllFaces(DragCoefficients.LONG_CYLINDER);
		getParts().put("body", body);
		
		//wings
		BodyPartAirfoil leftWing = new BodyPartAirfoil(this, 
				new Point3d(info.getWingBodyPosition(), info.getBodyHeight(), 0), 
							info.getWingSpan()/2.0, info.getWingChordLength(), info.getWingHeight(),
							info.getWingLiftCoefficient());
		leftAirfoilFace = (PartFaceAirfoil)leftWing.getFaces().get(BodyPartCube.FACE_HEAD);
		leftWing.setSelfRotation(VectorHelper.calculateRotationAroundVector(new Vector3d(1,0,0), info.getWingDihedral()));
		leftWing.setSelfRotationPoint(new Point3d(info.getWingBodyPosition(),info.getBodyHeight(),info.getWingSpan()/2.0));
		getParts().put("leftWing", leftWing);
		
		BodyPartAirfoil rightWing = new BodyPartAirfoil(this, 
				new Point3d(info.getWingBodyPosition(), info.getBodyHeight(), info.getWingSpan()/2.0), 
							info.getWingSpan()/2.0, info.getWingChordLength(), info.getWingHeight(),
							info.getWingLiftCoefficient());
		rightAirfoilFace = (PartFaceAirfoil)rightWing.getFaces().get(BodyPartCube.FACE_HEAD);
		rightWing.setSelfRotation(VectorHelper.calculateRotationAroundVector(new Vector3d(1,0,0), -info.getWingDihedral()));
		rightWing.setSelfRotationPoint(new Point3d(info.getWingBodyPosition(),info.getBodyHeight(),info.getWingSpan()/2.0));
		getParts().put("rightWing", rightWing);

		//horizontal stabilizator
		BodyPartCube hstab = new BodyPartCube(this,
								new Point3d(0, info.getBodyHeight()/2.0, (info.getWingSpan()/2.0)-(info.getHorizontalStabilizatorWidth()/2.0)), 
								info.getHorizontalStabilizatorWidth(), info.getHorizontalStabilizatorLength(), info.getHorizontalStabilizatorHeight());
		hstab.setDragCoefficientForFace(BodyPartCube.FACE_HEAD, DragCoefficients.LONG_CYLINDER);
		hstab.setDragCoefficientForFace(BodyPartCube.FACE_REAR, DragCoefficients.LONG_CYLINDER);
		getParts().put("horizontalStabilizator", hstab);
		
		//vertical stabilizator
		BodyPartCube vstab = new BodyPartCube(this,
								new Point3d(0, info.getBodyHeight()/2.0, (info.getWingSpan()/2.0)-(info.getVerticalStabilizatorWidth()/2.0)), 
								info.getVerticalStabilizatorWidth(), info.getVerticalStabilizatorLength(), info.getVerticalStabilizatorHeight());
		vstab.setDragCoefficientForFace(BodyPartCube.FACE_HEAD, DragCoefficients.LONG_CYLINDER);
		vstab.setDragCoefficientForFace(BodyPartCube.FACE_REAR, DragCoefficients.LONG_CYLINDER);
		getParts().put("verticalStabilizator", vstab);
		
		//ailerons
		aileronLeftPart = new BodyPartFlat(this,
				new Point3d(leftWing.getPosition().x-info.getAileronLength(), leftWing.getPosition().y+(info.getWingHeight()/2.0), leftWing.getPosition().z),
				new Point3d(0,0,0),
				new Point3d(0,0,leftWing.getWidth()),
				new Point3d(info.getAileronLength(),0,leftWing.getWidth()),
				new Point3d(info.getAileronLength(),0,0));
		aileronLeftPart.setControlRotationReference(new Vector3d(0,0,1));
		getParts().put("aileronLeft", aileronLeftPart);

		aileronRightPart = new BodyPartFlat(this,
				new Point3d(rightWing.getPosition().x-info.getAileronLength(), rightWing.getPosition().y+(info.getWingHeight()/2.0), rightWing.getPosition().z),
				new Point3d(0,0,0),
				new Point3d(0,0,rightWing.getWidth()),
				new Point3d(info.getAileronLength(),0,rightWing.getWidth()),
				new Point3d(info.getAileronLength(),0,0));
		aileronRightPart.setControlRotationReference(new Vector3d(0,0,1));
		getParts().put("aileronRight", aileronRightPart);

		//rudder
		rudderPart = new BodyPartFlat(this,
				new Point3d(-info.getRudderLength(), info.getBodyHeight()/2.0, info.getWingSpan()/2.0),
				new Point3d(0,0,0),
				new Point3d(0,info.getVerticalStabilizatorHeight(),0),
				new Point3d(info.getRudderLength(),info.getVerticalStabilizatorHeight(),0),
				new Point3d(info.getRudderLength(),0,0));
		rudderPart.setControlRotationReference(new Vector3d(0,1,0));
		getParts().put("rudder", rudderPart);

		//elevator
		elevatorPart = new BodyPartFlat(this,
				new Point3d(-info.getElevatorLength(), (info.getBodyHeight()/2.0)+(info.getHorizontalStabilizatorHeight()/2.0), (info.getWingSpan()/2.0)-(info.getHorizontalStabilizatorWidth()/2.0)),
				new Point3d(0,0,0),
				new Point3d(0,0,info.getHorizontalStabilizatorWidth()),
				new Point3d(info.getElevatorLength(),0,info.getHorizontalStabilizatorWidth()),
				new Point3d(info.getElevatorLength(),0,0));
		elevatorPart.setControlRotationReference(new Vector3d(0,0,1));
		getParts().put("elevator", elevatorPart);

		//thrust (at head face of body)
		PartFaceRect pfh = body.getFaces().get(BodyPartCube.FACE_HEAD);
		thrustFace = new PartFaceThrust(body, pfh.getV1(), pfh.getV2(), pfh.getV3(), pfh.getV4());
		body.getFaces().put(BodyPartCube.FACE_HEAD, thrustFace);
		
		//put center of mass in geometry origin
		Point3d p = info.getCenterOfMass();
		p.negate();
		moveBodyPartsBy(p);

		setMass(info.getMass());
	}
	
	@Override
	public void setMass(double m) {
		super.setMass(m);
		updateMomentOfInertia();
	}

	public double getIAS() {
		return (leftAirfoilFace.getIAS() + rightAirfoilFace.getIAS())/2.0;
	}

	/**
	 * Use calculation for a Rod of length L and mass m (considering wingspan same as head to tail span)
	 * I(center) = m*L*L
	 * Consider half mass because each axis considers only half mass (the other half has almost no moment of inertia)
	 */
	private void updateMomentOfInertia() {
		//TODO use tensors for this job to differentiate body span and tail span. This really needs to be improved!
		//moment of inertia of a sphere ( = 2/5 * MR^2
		Matrix3d mi = new Matrix3d();

		//using bar rotating on center model (I = m * L^2 / 2)
		double xaxis = getMass() * Math.pow(info.getWingSpan(), 2.0) / 12.0;
//		xaxis = Double.MAX_VALUE;
//		xaxis = 99;

		//using bar rotating on center model (I = m * L^2 / 2)
		double zaxis = getMass() * Math.pow(info.getBodyLength(), 2.0) / 12.0;
//		zaxis = Double.MAX_VALUE;
//		zaxis = 99;

		//using rectangle model
		double yaxis = getMass() * (Math.pow(info.getWingSpan(), 2.0) + Math.pow(info.getBodyLength(), 2.0)) / 12.0;
//		yaxis = Double.MAX_VALUE;
//		yaxis = 999;
		
		mi.m00 = (xaxis);
		mi.m11 = (yaxis);
		mi.m22 = (zaxis);
		setMomentOfInertiaTensor(mi);
	}

	public double getRoll() {
		return MathHelper.normalizeAngle(getOrientationAngles().x);
	}
	public void setRoll(double roll) {
		getOrientationAngles().x = (roll);
	}
	public double getRollVelocity() {
		return getAngularVelocity().x;
	}
	
	public double getPitch() {
		return MathHelper.normalizeAngle(getOrientationAngles().z);
	}
	public void setPitch(double pitch) {
		getOrientationAngles().z = (pitch);
	}
	public double getPitchVelocity() {
		return getAngularVelocity().z;
	}
	
	public double getHeading() {
		return MathHelper.normalizeAngle(-getOrientationAngles().y);
	}
	public void setHeading(double heading) {
		getOrientationAngles().y = (-heading);
	}
	public double getHeadingVelocity() {
		return -getAngularVelocity().y;
	}
	
	public void setAileron(double aileron) {
		this.aileron = aileron;
		double angle = (aileron/127.0) * (info.getAileronAngleRange());
		aileronLeftPart.setControlValue(angle);
		aileronRightPart.setControlValue(-angle);
	}
	public void setElevator(double elevator) {
		this.elevator = elevator;
		double angle = (elevator/127.0) * (info.getElevatorAngleRange());
		elevatorPart.setControlValue(-angle);
	}
	public void setRudder(double rudder) {
		this.rudder = rudder;
		double angle = (rudder/127.0) * (info.getRudderAngleRange());
		rudderPart.setControlValue(-angle);
	}
	public void setThrottle(double throttle) {
		thrustFace.setThrustValue(info.getEngineMinForce() + ((throttle/127F)*(info.getEngineMaxForce()-info.getEngineMinForce())));
		this.throttle = throttle;
	}
	public double getAileron() {
		return aileron;
	}
	public double getElevator() {
		return elevator;
	}
	public double getRudder() {
		return rudder;
	}
	public double getThrottle() {
		return throttle;
	}
	
	public static void main(String[] args) {
		Environment ev = new Environment();
		BasicAirplane b = new BasicAirplane(ev);
		b.moveBodyPartsBy(new Point3d(-1,-1,-1));
//		b.setVelocity(10, 0, 0);
		b.setOrientationAngles(new Vector3d(0, MathHelper.HALF_PI, 0));
		
		PositionedVector af = b.getParts().get(0).getFaces().get(BodyPartCube.FACE_HEAD).getDragForce();
		PositionedVector sf = b.getParts().get(0).getFaces().get(BodyPartCube.FACE_HEAD).getSelfForce();
		System.out.println("head  "+str(af) + "; self-force "+str(sf));

		af = b.getParts().get(0).getFaces().get(BodyPartCube.FACE_REAR).getDragForce();
		System.out.println("rear  "+str(af));

		af = b.getParts().get(0).getFaces().get(BodyPartCube.FACE_RIGHT_SIDE).getDragForce();
		System.out.println("right "+str(af));

		af = b.getParts().get(0).getFaces().get(BodyPartCube.FACE_LEFT_SIDE).getDragForce();
		System.out.println("left  "+str(af));

//		af = b.getParts().get(1).getFaces().get("face").getDragForce();
//		System.out.println("flat-yz  "+VectorHelper.str(af));
	}

}
