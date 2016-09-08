package br.skylight.flightsim.flyablebody;

import javax.vecmath.Point3d;

public class BodyPartCube extends BodyPart {

	public static final String FACE_HEAD = "headDirFace";
	public static final String FACE_REAR = "rearDirFace";
	public static final String FACE_RIGHT_SIDE = "rightSideDirFace";
	public static final String FACE_LEFT_SIDE = "leftSideDirFace";
	public static final String FACE_TOP = "topDirFace";
	public static final String FACE_BOTTOM = "bottomDirFace";

	private double length;//x-axis
	private double width;//z-axis
	private double height;//y-axis

	public BodyPartCube(FlyableRigidBody mainBody, Point3d position, double width, double length, double height) {
		super(mainBody, position);
		createFaces(width, height, length, DragCoefficients.CUBE);
	}
	
	public void createFaces(double width, double height, double length, double dragCoefficient) {
		Point3d p1 = new Point3d(0,height,0);
		Point3d p2 = new Point3d(0,height,width);
		Point3d p3 = new Point3d(0,0,width);
		Point3d p4 = new Point3d(0,0,0);
		Point3d p5 = new Point3d(length,height,0);
		Point3d p6 = new Point3d(length,height,width);
		Point3d p7 = new Point3d(length,0,width);
		Point3d p8 = new Point3d(length,0,0);
		
		getFaces().put(FACE_HEAD, new PartFaceRect(this, p5,p6,p7,p8, dragCoefficient));
		getFaces().put(FACE_REAR, new PartFaceRect(this, p4,p3,p2,p1, dragCoefficient));

		getFaces().put(FACE_RIGHT_SIDE, new PartFaceRect(this, p3,p7,p6,p2, dragCoefficient));
		getFaces().put(FACE_LEFT_SIDE, new PartFaceRect(this, p1,p5,p8,p4, dragCoefficient));

		getFaces().put(FACE_TOP, new PartFaceRect(this, p2,p6,p5,p1, dragCoefficient));
		getFaces().put(FACE_BOTTOM, new PartFaceRect(this, p4,p8,p7,p3, dragCoefficient));

		this.width = width;
		this.height = height;
		this.length = length;
	}

	public double getWidth() {
		return width;
	}
	public double getLength() {
		return length;
	}
	public double getHeight() {
		return height;
	}
}
