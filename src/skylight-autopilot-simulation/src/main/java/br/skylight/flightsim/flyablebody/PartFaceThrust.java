package br.skylight.flightsim.flyablebody;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import br.skylight.flightsim.rigidbody.PositionedVector;

public class PartFaceThrust extends PartFaceRect {

	private Point3d thrustPos = new Point3d();//in bodypart reference
	private Vector3d thrustDir = new Vector3d(1,0,0);//in bodypart reference
//	private Vector3d thrustDir = new Vector3d(0,1,0);//in bodypart reference
	private double thrustValue;
	
	public PartFaceThrust(BodyPart bodyPart, Point3d v1, Point3d v2, Point3d v3, Point3d v4) {
		super(bodyPart, v1, v2, v3, v4, DragCoefficients.SPHERE);
		thrustPos = getCenter();
	}

	@Override
	public PositionedVector getSelfForce() {
		Vector3d tv = new Vector3d(getBodyPart().rotateToMainBodyReference(thrustDir));
		tv.normalize();
		tv.scale(thrustValue);
		return new PositionedVector(tv, new Point3d(getBodyPart().toMainBodyReference(thrustPos)));
	}
	
	public void setThrustValue(double thrustValue) {
		this.thrustValue = thrustValue;
	}
	
	public void setThrustDir(Vector3d thrustDir) {
		this.thrustDir = thrustDir;
	}
	public void setThrustPos(Point3d thrustPos) {
		this.thrustPos = thrustPos;
	}
	
}
