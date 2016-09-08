package br.skylight.commons.j3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.jdesktop.j3d.loaders.collada.Collada14Loader;

import br.skylight.commons.infra.VectorHelper;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

public class Airplane3dViewer extends JPanel {

	private static final long serialVersionUID = 1L;

	private Canvas3D canvas3d;
	private BranchGroup branchGroup;
	private Transform3D rotation = new Transform3D(); // @jve:decl-index=0:
	private TransformGroup planeRotate = new TransformGroup(rotation);  //  @jve:decl-index=0:
	private Map<Object,Shape3D> lines = new HashMap<Object,Shape3D>();  //  @jve:decl-index=0:
	
	public Airplane3dViewer(Node modelNode) {
		planeRotate.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		planeRotate.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		loadModel(modelNode);
		initialize();
	}
	
	public Airplane3dViewer(URL modelURL) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		this(Airplane3dViewer.getNodeModelFromURL(modelURL));
	}
	
	public Airplane3dViewer() throws FileNotFoundException, IncorrectFormatException, ParsingErrorException  {
		this(Airplane3dViewer.class.getResource("/br/skylight/commons/j3d/models/Cessna001.obj"));
//		loadKmzModel(Airplane3dViewer.class.getResource("/br/skylight/commons/j3d/models/750mmSpeedo.kmz"));
//		loadDaeModel(Airplane3dViewer.class.getResource("/br/skylight/commons/j3d/models/Cessna001.dae"));
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(275, 167);
		this.setLayout(new BorderLayout());
		add(BorderLayout.CENTER, getCanvas3d());
	}

	private Canvas3D getCanvas3d() {
		if (canvas3d == null) {
			canvas3d = J3DHelper.getCanvas3D(getBranchGroup());
		}
		return canvas3d;
	}

	public BranchGroup getBranchGroup() {
		if (branchGroup == null) {
			branchGroup = new BranchGroup();

			branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			planeRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			branchGroup.addChild(planeRotate);
			addLights(branchGroup);
			
			Background backg = new Background();
		    backg.setColor(0.4f, 0.4f, 1.0f);
		    backg.setApplicationBounds(new BoundingSphere());
		    branchGroup.addChild(backg);			

			branchGroup.compile();
		}
		return branchGroup;
	}

	/**
	 * This creates some lights and adds them to the BranchGroup.
	 * 
	 * @param b
	 *            BranchGroup that the lights are added to.
	 */
	protected void addLights(BranchGroup b) {
		// Create a bounds for the lights
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		// Set up the ambient light
		Color3f ambientColour = new Color3f(1f, 1f, 1f);
		AmbientLight ambientLight = new AmbientLight(ambientColour);
		ambientLight.setInfluencingBounds(bounds);
		// Set up the directional light
		Color3f lightColour = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f lightDir = new Vector3f(1.0f, 1.0f, 1.0f);
		DirectionalLight light = new DirectionalLight(lightColour, lightDir);
		light.setInfluencingBounds(bounds);
		// Add the lights to the BranchGroup
		b.addChild(ambientLight);
		b.addChild(light);
	}

	public Shape3D updateVector(Object id, Point3d point, Vector3d vector, Color color, double scale) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		Shape3D s = lines.get(id);
		vector.scale(scale);
		if(s==null) {
			BranchGroup bg = new BranchGroup();
			bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			s = J3DHelper.createLine(new Point3f(), new Point3f(vector), color);
			s.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
			bg.addChild(s);
			getBranchGroup().addChild(bg);
			lines.put(id, s);
			
/*		    Text2D text = new Text2D(id, new Color3f(0.9f,1.0f, 1.0f), "Arial", 36, Font.BOLD);
		    Appearance app = text.getAppearance();
		    PolygonAttributes pa = app.getPolygonAttributes();
		    if (pa == null) {
		    	pa = new PolygonAttributes();
		    	app.setPolygonAttributes(pa);
		    }
		    pa.setCullFace(PolygonAttributes.CULL_NONE);

			Transform3D t = new Transform3D();
		    TransformGroup tg = new TransformGroup(t);
		    t.setTranslation(vector);
		    tg.addChild(text);
		    getBranchGroup().addChild(tg);
*/		}
		Point3f p1 = new Point3f(point);
		Point3f p2 = new Point3f(vector);
		p2.add(p1);
		s.setGeometry(J3DHelper.createLineGeometry(p1, p2));
		return s;
	}
	
	public void setOrientation(double roll, double pitch, double yaw) {
		setOrientation(VectorHelper.computeQuarternionFromEulerAngles(roll, pitch, yaw));
	}
	
	public void setOrientation(Quat4d orientation) {
		rotation.set(orientation);
		planeRotate.setTransform(rotation);
		updateUI();
	}

	public void loadDaeModel(URL modelURL) {
		Collada14Loader l = new Collada14Loader();
	    try {
			loadModel(l.load(modelURL).getSceneGroup());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void loadObjModel(URL modelURL) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		loadModel(getNodeModelFromURL(modelURL));
	}
	
	public static Node getNodeModelFromURL(URL modelURL) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		ObjectFile file = new ObjectFile(ObjectFile.RESIZE);
		Scene scene = file.load(modelURL);
		return scene.getSceneGroup();
	}
	
//	public void loadKmzModel(URL modelURL) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
//		ObjectFile file = new ObjectFile(ObjectFile.RESIZE);
//		Scene scene = null;
//		scene = file.load(modelURL);

//		Loader3DS l = new Loader3DS();
//		Scene scene = l.load(modelURL);
//		loadModel(scene.getSceneGroup());
		
//	    KmzLoader l = new KmzLoader();
//	    Scene s = l.load(modelURL);
//	    loadModel(s.getSceneGroup());
//	}

	protected void loadModel(Node modelNode) {
		planeRotate.addChild(modelNode);
		updateUI();
	}
	
//	planeRotate.addChild(J3DHelper.createFlyableRigidBodyGroup(p));

	
	public void setShowAxis(boolean showAxis) {
		if(showAxis) {
			try {
				updateVector("X", new Point3d(), new Vector3d(1,0,0), Color.RED.darker(), 0.5);
				updateVector("Y", new Point3d(), new Vector3d(0,1,0), Color.BLUE.darker(), 0.5);
				updateVector("Z", new Point3d(), new Vector3d(0,0,1), Color.YELLOW.darker(), 0.5);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			//TODO DELETE AXIS VECTORS
		}
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
