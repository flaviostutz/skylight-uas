package br.skylight.flightsim.flyablebody;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import br.skylight.commons.infra.VectorHelper;

public abstract class BodyPart {

	private FlyableRigidBody mainBody;
	private Point3d position;//in main body reference (with no rotation/translation)
	private Quat4d selfRotation = new Quat4d(0,0,0,1);
	private Point3d selfRotationPoint = new Point3d(0,0,0);

	private Vector3d controlRotationReference;
	private double controlValue;
	
	private Map<String, PartFaceRect> faces = new HashMap<String, PartFaceRect>();
	
	public BodyPart(FlyableRigidBody mainBody, Point3d position) {
		this.mainBody = mainBody;
		this.position = position;
	}

	public void setDragCoefficientForAllFaces(double dragCoefficient) {
		for(PartFaceRect p : faces.values()) {
			p.setDragCoefficient(dragCoefficient);
		}
	}

	public void setDragCoefficientForFace(String faceName, double dragCoefficient) {
		faces.get(faceName).setDragCoefficient(dragCoefficient);
	}

	public Quat4d getRotationToMainBodyReference() {
		//main body orientation transform
		Quat4d r = new Quat4d(mainBody.getOrientationRotation());
		
		//body part self rotation
		r.mul(selfRotation);
		
		//dynamic control rotation
		if(controlRotationReference!=null) {
			Quat4d cr = VectorHelper.calculateRotationAroundVector(controlRotationReference, controlValue);
			r.mul(cr);
		}
		return r;
	}

	public Tuple3d toMainBodyReference(Tuple3d partPoint) {
		Vector3d t = new Vector3d(getPosition());
		t.add(partPoint);
		t = VectorHelper.rotateVector(t, getRotationToMainBodyReference());
//TODO rotacionar ponto diversas vezes
		//self rotation
		Point3d rp = new Point3d(VectorHelper.rotateVector(new Vector3d(partPoint), selfRotation));
		rp.add(selfRotationPoint);
//		t.add(rp);
		
		return t;
	}

	public Point3d getPosition() {
		return position;
	}

	public void setSelfRotation(Quat4d selfRotation) {
		this.selfRotation = selfRotation;
	}
	public Quat4d getSelfRotation() {
		return selfRotation;
	}
	
	public Map<String, PartFaceRect> getFaces() {
		return faces;
	}
	
/*	public Vector3d getHeadDir() {
		return VectorHelper.rotateVector(new Vector3d(1,0,0), getRotationToMainBodyReference());
	}
	public Vector3d getRightSideDir() {
		return VectorHelper.rotateVector(new Vector3d(0,0,1), getRotationToMainBodyReference());
	}
	public Vector3d getNormalDir() {
		return VectorHelper.rotateVector(new Vector3d(0,1,0), getRotationToMainBodyReference());
	}
*/	public FlyableRigidBody getMainBody() {
		return mainBody;
	}
	
	public void setControlRotationReference(Vector3d controlRotationReference) {
		this.controlRotationReference = controlRotationReference;
	}
	public void setControlValue(double controlValue) {
		this.controlValue = controlValue;
	}
	
	public Point3d getSelfRotationPoint() {
		return selfRotationPoint;
	}
	public void setSelfRotationPoint(Point3d selfRotationPoint) {
		this.selfRotationPoint = selfRotationPoint;
	}

	public Vector3d rotateToMainBodyReference(Vector3d partVector) {
		return VectorHelper.rotateVector(partVector, getRotationToMainBodyReference());
	}

}
