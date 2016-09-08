package br.skylight.flightsim.flyablebody;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import br.skylight.flightsim.rigidbody.PositionedVector;

public class PartFaceRect {

	private BodyPart bodyPart;
	
	private Point3d v1, v2, v3, v4;
	
	private Vector3d normal;//in bodypart reference
	private Point3d center;//in bodypart reference
	private double area;
	
	private PositionedVector sf = new PositionedVector(new Vector3d(), new Point3d());
	
	private double dragCoefficient;
	
	public PartFaceRect(BodyPart bodyPart, Point3d vp1, Point3d vp2, Point3d vp3, Point3d vp4, double dragCoefficient) {
		this.bodyPart = bodyPart;
		this.dragCoefficient = dragCoefficient;
		this.v1 = new Point3d(vp1);
		this.v2 = new Point3d(vp2);
		this.v3 = new Point3d(vp3);
		this.v4 = new Point3d(vp4);

		//calculate normal based on points (use avg between 2 triangles from rectangle)
		//normal first triangle
		Vector3d vv1 = new Vector3d(v2);
		vv1.sub(v1);
		Vector3d vv2 = new Vector3d(v4);
		vv2.sub(v1);
		Vector3d n1 = new Vector3d();
		n1.cross(vv1, vv2);
		
		//normal second triangle
		vv1 = new Vector3d(v2);
		vv1.sub(v3);
		vv2 = new Vector3d(v4);
		vv2.sub(v3);
		Vector3d n2 = new Vector3d();
		n2.cross(vv2, vv1);

		//total area
		this.area = n1.length()/2.0 + n2.length()/2.0;
		
		//plane normal dir
		Vector3d nt = new Vector3d();
		nt.add(n1, n2);
		nt.normalize();
		this.normal = nt;
		
		//geometric center
		//first diagonal
		Vector3d va = new Vector3d(v3.x-v1.x, v3.y-v1.y, v3.z-v1.z);
		va.scale(0.5);
		va.add(v1);
		
		//second diagonal
		Point3d vb = new Point3d(v4.x-v2.x, v4.y-v2.y, v4.z-v2.z);
		vb.scale(0.5);
		vb.add(v2);
		
		//average
		vb.add(va);
		vb.scale(0.5);
		this.center = vb;
	}

	/**
	 * Calculates the air drag force for this face in main body reference
	 * Airspeed is calculated considering main body translational and rotational state
	 * Return is in main body coordinates
	 */
	public PositionedVector getDragForce() {
		Vector3d dragForce = getNormalInMainBodyReference();
		double ias = getIAS();
		double f = -calculateDynamics(dragCoefficient, ias);
		dragForce.scale(f);
		return new PositionedVector(dragForce, getCenterInMainBodyReference());
	}

	//how much does the normal is facing airspeed?
	public double getIAS() {
		Vector3d airspeed = getAirspeed().getVector();
		Vector3d asdir = new Vector3d(airspeed);
		if(asdir.length()!=0) {
			asdir.normalize();
		}
		double ratio = asdir.dot(getNormalInMainBodyReference());
		//if not facing airspeed, return zero
		if(ratio>0) {
			return 0;
		} else {
			return airspeed.length() * ratio;
		}
	}
	
	public PositionedVector getAirspeed() {
		Point3d tcenter = getCenterInMainBodyReference();
		return new PositionedVector(getBodyPart().getMainBody().getTrueAirspeed(tcenter),tcenter);
	}
	
	public PositionedVector getSelfForce() {
		return sf;
	}

	protected double calculateDynamics(double coefficient, double speed) {
		return coefficient * bodyPart.getMainBody().getEnvironment().getAirDensity() * (Math.pow(speed, 2)/2.0) * (area);
	}
	
	public void setDragCoefficient(double dragCoefficient) {
		this.dragCoefficient = dragCoefficient;
	}
	
	public BodyPart getBodyPart() {
		return bodyPart;
	}

	public Point3d getCenter() {
		return center;
	}
	public Point3d getCenterInMainBodyReference() {
		return new Point3d(bodyPart.toMainBodyReference(center));
	}

	public Vector3d getNormal() {
		return normal;
	}
	public Vector3d getNormalInMainBodyReference() {
		return bodyPart.rotateToMainBodyReference(normal);
	}
	
	public Point3d getV1() {
		return v1;
	}
	public Point3d getV1InMainBodyReference() {
		return new Point3d(bodyPart.toMainBodyReference(v1));
	}
	public Point3d getV2() {
		return v2;
	}
	public Point3d getV2InMainBodyReference() {
		return new Point3d(bodyPart.toMainBodyReference(v2));
	}
	public Point3d getV3() {
		return v3;
	}
	public Point3d getV3InMainBodyReference() {
		return new Point3d(bodyPart.toMainBodyReference(v3));
	}
	public Point3d getV4() {
		return v4;
	}
	public Point3d getV4InMainBodyReference() {
		return new Point3d(bodyPart.toMainBodyReference(v4));
	}
	
	public double getDragCoefficient() {
		return dragCoefficient;
	}

}
