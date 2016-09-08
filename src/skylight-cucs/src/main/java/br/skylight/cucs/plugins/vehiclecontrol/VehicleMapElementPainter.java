package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;

import br.skylight.commons.MeasureType;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.infra.MathHelper;
import br.skylight.cucs.mapkit.painters.MapElementPainter;

public class VehicleMapElementPainter extends MapElementPainter<VehicleMapElement> {

	private Font labelFont = new Font("Arial", Font.PLAIN, 11);
	
	@Override
	protected Polygon paintElement(Graphics2D go, VehicleMapElement elem) {
		if(elem.getPosition().getLatitude()==0 && elem.getPosition().getLongitude()==0) return null;
		
		InertialStates inertialStates = elem.getVehicle().getLastReceivedMessage(MessageType.M101);
		if(inertialStates==null) {
			return null;
		}

		Graphics2D g = (Graphics2D) go.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//draw plane name/altitude
		if(elem.getVehicle().getVehicleID()!=null) {
		    g.setColor(Color.YELLOW);
		    GlyphVector gv = labelFont.createGlyphVector(g.getFontRenderContext(), elem.getVehicle().getLabel());
		    g.drawGlyphVector(gv, 9, 0);
		}
		
	    drawText(g, elem, "Alt: " + MeasureType.ALTITUDE.convertToTargetUnitStr(inertialStates.getAltitude(), true) + " ("+ inertialStates.getAltitudeType() +")", 0, false);
	    drawText(g, elem, "G.Speed: " + MeasureType.GROUND_SPEED.convertToTargetUnitStr(inertialStates.getGroundSpeed(), true), 15, false);
		
		//draw plane model
		//rotate graphics reference
		g.rotate(-MathHelper.HALF_PI+inertialStates.getPsi());
//		System.out.println("roll: "+inertialStates.getPhi() + "; pitch: " + inertialStates.getTheta() + "; yaw: "+inertialStates.getPsi());

		//draw model
		g.setPaint(Color.YELLOW);

		BasicStroke bs = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g.setStroke(bs);
		int length = (int)(9F*Math.cos(inertialStates.getTheta()*2F));//pitch
		int wing = (int)(9F*Math.cos(inertialStates.getPhi()*2F));//roll
		int back = (int)(3F*Math.cos(inertialStates.getPhi()*2F));//roll
		g.drawLine(-length, 0, length, 0);
		g.drawLine(3, -wing, 3, wing);
		g.drawLine(-8, -back, -8, back);

		if(elem.isSelected()) {
			g.setColor(Color.YELLOW.darker());
			g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.drawArc(-20, -20, 40, 40, 0, 360);
		}
		
		//selection mask
		Polygon p = new Polygon();
		p.addPoint(-8, -8);
		p.addPoint(-8, 8);
		p.addPoint(8, 8);
		p.addPoint(8, -8);
		return p;
	}

}
