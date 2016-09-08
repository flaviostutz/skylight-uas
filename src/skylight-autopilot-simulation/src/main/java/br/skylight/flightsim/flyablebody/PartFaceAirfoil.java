package br.skylight.flightsim.flyablebody;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import br.skylight.flightsim.rigidbody.PositionedVector;

public class PartFaceAirfoil extends PartFaceRect {

	private double liftCoefficient;
	private Vector3d liftDir;
	private Point3d liftPos;
	
	public PartFaceAirfoil(BodyPart bodyPart, Point3d v1, Point3d v2, Point3d v3, Point3d v4, Vector3d liftDir, Point3d liftPos, double liftCoefficient) {
		super(bodyPart, v1, v2, v3, v4, DragCoefficients.AIRFOIL);
		this.liftCoefficient = liftCoefficient;
		this.liftDir = liftDir;
		this.liftPos = liftPos;
	}
	
	@Override
	public PositionedVector getSelfForce() {
		//calculate lift force
		Vector3d liftForce = new Vector3d(getBodyPart().rotateToMainBodyReference(liftDir));
		double f = calculateDynamics(liftCoefficient, getIAS());
		liftForce.scale(f);
		return new PositionedVector(liftForce, new Point3d(getBodyPart().toMainBodyReference(liftPos)));
	}

	public void setLiftCoefficient(double liftCoefficient) {
		this.liftCoefficient = liftCoefficient;
	}
	
}
