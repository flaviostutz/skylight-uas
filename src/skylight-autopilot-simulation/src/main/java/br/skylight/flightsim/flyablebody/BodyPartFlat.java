package br.skylight.flightsim.flyablebody;

import javax.vecmath.Point3d;

public class BodyPartFlat extends BodyPart {

	public static final String FACE_TOP = "topDirFace";
	public static final String FACE_BOTTOM = "bottomDirFace";

	public BodyPartFlat(FlyableRigidBody mainBody, Point3d position, Point3d v1, Point3d v2, Point3d v3, Point3d v4) {
		super(mainBody, position);
		getFaces().put(FACE_TOP, new PartFaceRect(this, v1,v2,v3,v4, DragCoefficients.SQUARED_FLAT_PLATE));
		getFaces().put(FACE_BOTTOM, new PartFaceRect(this, v1,v4,v3,v2, DragCoefficients.SQUARED_FLAT_PLATE));
	}

}
