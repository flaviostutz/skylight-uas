package br.skylight.flightsim.flyablebody;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class BodyPartAirfoil extends BodyPartCube {

	public BodyPartAirfoil(FlyableRigidBody mainBody, Point3d position, double width, double length, double height, double liftCoefficient) {
		super(mainBody, position, width, length, height);
		Point3d liftPos = new Point3d(getLength()*(2.0/3.0),getHeight(),getWidth()/2.0);
		Vector3d liftDir = new Vector3d(0,1,0);
		setDragCoefficientForAllFaces(DragCoefficients.LONG_CYLINDER);
		
		PartFaceRect pf = getFaces().get(BodyPartCube.FACE_HEAD);
		PartFaceAirfoil airfoil = new PartFaceAirfoil(this, pf.getV1(), pf.getV2(), pf.getV3(), pf.getV4(), liftDir, liftPos, liftCoefficient);
		getFaces().put(BodyPartCube.FACE_HEAD, airfoil);
	}
	
}
