package br.skylight.flightsim;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;

import br.skylight.flightsim.flyablebody.BodyPart;
import br.skylight.flightsim.flyablebody.FlyableRigidBody;
import br.skylight.flightsim.flyablebody.J3DHelper;
import br.skylight.flightsim.flyablebody.PartFaceRect;

public class SceneHelper {

	public static Group createFlyableRigidBodyGroup(FlyableRigidBody body) {
		Group g = new Group();
		for (BodyPart part : body.getParts().values()) {
			Transform3D t3 = new Transform3D();
			t3.set(part.getRotationToMainBodyReference());
			TransformGroup tg = new TransformGroup(t3);
			for(PartFaceRect f : part.getFaces().values()) {
				Shape3D rect = J3DHelper.createRectangle(f.getV1InMainBodyReference(), f.getV2InMainBodyReference(), f.getV3InMainBodyReference(), f.getV4InMainBodyReference());
				tg.addChild(rect);
				Appearance a = new Appearance();
				
				TransparencyAttributes ta = new TransparencyAttributes();
				ta.setTransparency(0.5F);
				ta.setTransparencyMode(TransparencyAttributes.NICEST);
				a.setTransparencyAttributes(ta);

				ColoringAttributes ca = new ColoringAttributes();
				float grey = 1F - 1.5F/(float)f.getDragCoefficient();
				grey = Math.max(1, grey);
				ca.setColor(grey,grey,grey);
				a.setColoringAttributes(ca);
				rect.setAppearance(a);
			}
			g.addChild(tg);
		}
		return g;
	}
	
}
