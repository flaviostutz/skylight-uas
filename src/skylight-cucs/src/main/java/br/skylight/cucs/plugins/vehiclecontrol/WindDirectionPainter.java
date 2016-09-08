package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.text.NumberFormat;

import br.skylight.commons.MeasureType;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.MeasureHelper;
import br.skylight.cucs.mapkit.painters.LayerPainter;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class WindDirectionPainter implements LayerPainter {

	private AirAndGroundRelativeStates airAndGroundRelativeStates;
	private NumberFormat nf;
	
	public WindDirectionPainter() {
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
	}
	
	public void setAirAndGroundRelativeStates(AirAndGroundRelativeStates airAndGroundRelativeStates) {
		this.airAndGroundRelativeStates = airAndGroundRelativeStates;
	}
	
	@Override
	public int getLayerNumber() {
		return 0;
	}

	@Override
	public void paint(Graphics2D g, Object obj, int i, int j) {
		Graphics2D g2 = (Graphics2D)g.create();
		if(airAndGroundRelativeStates!=null) {
			//draw wind indicator arrow
			g2.setStroke(new BasicStroke(3));
			g2.setColor(Color.YELLOW);
			double rotation = Math.PI + Math.atan2(airAndGroundRelativeStates.getVWind(), airAndGroundRelativeStates.getUWind());
			AffineTransform t = AffineTransform.getScaleInstance(1, 0.8);
    		Shape s = t.createTransformedShape(CUCSViewHelper.ARROW);
    		t = AffineTransform.getTranslateInstance(s.getBounds().getCenterX(), -s.getBounds().getCenterY());
    		s = t.createTransformedShape(s);
    		t = AffineTransform.getRotateInstance(rotation);
    		s = t.createTransformedShape(s);
    		t = AffineTransform.getTranslateInstance(34, 16);
    		s = t.createTransformedShape(s);
    		g2.fill(s);
    		
    		//draw wind speed value
    		g2.drawString(nf.format(MeasureType.AIR_SPEED.convertToTargetUnit(MeasureHelper.calculateMagnitude(airAndGroundRelativeStates.getUWind(),airAndGroundRelativeStates.getVWind()))) + " " + MeasureType.AIR_SPEED.getTargetUnit().toString(), 43, 19);
		}
	}

}
