package br.skylight.flightsim.flyablebody;

import java.awt.Color;
import java.awt.GraphicsConfiguration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import br.skylight.commons.infra.VectorHelper;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class J3DHelper {

	public static Canvas3D getCanvas3D(BranchGroup scene) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D c = new Canvas3D(config);
		
		// Create a simple scene and attach it to the virtual universe
		SimpleUniverse u = new SimpleUniverse(c);

		// set mouse orbit
		OrbitBehavior orbit = new OrbitBehavior(c);
		orbit.setReverseRotate(true);
		orbit.setReverseTranslate(true);
		orbit.setReverseZoom(true);
		orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		u.getViewingPlatform().setViewPlatformBehavior(orbit);
		
		Transform3D t = new Transform3D();
		t.rotY(Math.toRadians(-90));
		t.setTranslation(new Vector3d(-2.5,0,0));
		u.getViewingPlatform().getViewPlatformTransform().setTransform(t);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
//		u.getViewingPlatform().setNominalViewingTransform();
		
		u.addBranchGraph(scene);

		return c;
	}

	public static TriangleArray createTriangleGeometry(Point3d v1, Point3d v2, Point3d v3) {
		TriangleArray triangle = new TriangleArray(3, TriangleArray.COORDINATES);
		Point3d[] tp = {v1, v2, v3};
		triangle.setCoordinates(0, tp);
		return triangle;
	}
	public static QuadArray createRectangleGeometry(Point3d v1, Point3d v2, Point3d v3, Point3d v4) {
		QuadArray quad = new QuadArray(4, TriangleArray.COORDINATES);
		Point3d[] tp = {v1, v2, v3, v4};
		quad.setCoordinates(0, tp);
		return quad;
	}
	
	public static Shape3D createTriangle(Point3d v1, Point3d v2, Point3d v3) {
		Shape3D triangle = new Shape3D(createTriangleGeometry(v1, v2, v3));
		Appearance app = new Appearance();
		triangle.setAppearance(app);
		
		ColoringAttributes ca = new ColoringAttributes();
		app.setColoringAttributes(ca);
		
		PolygonAttributes pa = new PolygonAttributes();
		app.setPolygonAttributes(pa);
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		
		return triangle;
	}

	public static Shape3D createRectangle(Point3d v1, Point3d v2, Point3d v3, Point3d v4) {
		Shape3D rectangle = new Shape3D(createRectangleGeometry(v1, v2, v3, v4));
		Appearance app = new Appearance();
		rectangle.setAppearance(app);
		
		ColoringAttributes ca = new ColoringAttributes();
		app.setColoringAttributes(ca);
		
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		app.setPolygonAttributes(pa);
		
		return rectangle;
	}

	public static void main(String[] args) {
		//rotation tests
		Vector3d v = VectorHelper.rotateVector(new Vector3d(0,1,0), new Vector3d(1,0,0), Math.toRadians(90));
		if(v.z!=-1) {
			throw new AssertionError("invalid " + v);
		}

		v = VectorHelper.rotateVector(new Vector3d(1,0,0), new Vector3d(0,1,0), Math.toRadians(90));
		if(v.z!=1) {
			throw new AssertionError("invalid " + v);
		}

		v = VectorHelper.rotateVector(new Vector3d(0,1,0), new Vector3d(1,0,0), Math.toRadians(-90));
		if(v.z!=1) {
			throw new AssertionError("invalid " + v);
		}
		
//		center (1.0, 0.5, 0.5)
//		rotation (0.0, 0.7071067811865475, 0.0, 0.7071067811865476)
		v = VectorHelper.rotateVector(new Vector3d(1,0.5,0.5), new Vector3d(0,1,0), Math.toRadians(-90));
		if(v.z!=-1) {
			throw new AssertionError("invalid " + v);
		}
		
		v = new Vector3d(1,0,0);
		v = VectorHelper.rotateAirplaneVectorEuler(v, 0, Math.toRadians(45), Math.toRadians(90));
		if(v.z!=-0.7071067811865476 || v.y!=0.7071067811865475) {
			throw new AssertionError("invalid " + v);
		}
		
		v = new Vector3d(0,1,0);
		v = VectorHelper.rotateAirplaneVectorEuler(v, 0, 0, Math.toRadians(90));
		if(v.y!=1) {
			throw new AssertionError("invalid " + v);
		}

		v = new Vector3d(0,1,0);
		v = VectorHelper.rotateAirplaneVectorEuler(v, Math.toRadians(90), Math.toRadians(90), 0);
		if(v.z!=1) {
			throw new AssertionError("invalid " + v);
		}

		v = new Vector3d(1,0,0);
		v = VectorHelper.rotateAirplaneVectorEuler(v, Math.toRadians(30), Math.toRadians(10), Math.toRadians(90));
		if(v.y!=0.17364817766693033 || v.z!=-0.984807753012208) {
			throw new AssertionError("invalid " + v);
		}
		
	}

	public static LineArray createLineGeometry(Point3f p1, Point3f p2) {
		LineArray l = new LineArray(2, LineArray.COORDINATES);
		l.setCoordinates(0, new Point3f[]{p1, p2});
		return l;
	}
	
	public static Shape3D createLine(Point3f p1, Point3f p2, Color color) {
		
		//create geometry
		LineArray l = createLineGeometry(p1, p2);

		//set appearance
		Appearance appearance = new Appearance();
		LineAttributes lineAttr = new LineAttributes(); 
		ColoringAttributes colorAttr = new ColoringAttributes(); 
		appearance.setLineAttributes(lineAttr);
		appearance.setColoringAttributes(colorAttr);
		
		lineAttr.setLinePattern(LineAttributes.PATTERN_SOLID);
		lineAttr.setLineWidth(1);
		lineAttr.setLineAntialiasingEnable(true);
		colorAttr.setColor(new Color3f(color));
		
		//create shape3D
		Shape3D lineShape = new Shape3D(l, appearance);
		return lineShape;
	}

}
