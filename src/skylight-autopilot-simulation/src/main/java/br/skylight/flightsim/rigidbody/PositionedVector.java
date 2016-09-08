package br.skylight.flightsim.rigidbody;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class PositionedVector {

	private Vector3d vector;
	private Point3d point;
	
	public PositionedVector(Vector3d vector, Point3d point) {
		this.vector = vector;
		this.point = point;
	}
	
	public Point3d getPoint() {
		return point;
	}
	public void setPoint(Point3d point) {
		this.point = point;
	}
	public Vector3d getVector() {
		return vector;
	}
	public void setVector(Vector3d vector) {
		this.vector = vector;
	}
	
}
