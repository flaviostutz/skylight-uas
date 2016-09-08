package br.skylight.cucs.plugins.missionplan.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import br.skylight.commons.MeasureType;
import br.skylight.commons.StringHelper;
import br.skylight.commons.dli.WaypointDef.WaypointState;
import br.skylight.commons.dli.enums.TurnType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.cucs.mapkit.painters.MapElementPainter;
import br.skylight.cucs.plugins.controlmap2d.PainterColors;
import br.skylight.cucs.plugins.loiterdirector.LoiterPainter;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class WaypointPainter extends MapElementPainter<WaypointMapElement> {

	private static Color stepNumberColor = new Color(0,0,0,255);
	
	private LoiterPainter loiterPainter = new LoiterPainter();
	private LoiterConfiguration loiterConfiguration = new LoiterConfiguration();
	
	@Override
	protected Polygon paintElement(Graphics2D go, WaypointMapElement element) {
		//draw loiter figure if applicable
		if(element.getWaypointDef().getLoiterWaypoint()!=null) {
			AVLoiterWaypoint lw = element.getWaypointDef().getLoiterWaypoint();
			loiterConfiguration.setLoiterBearing((float)lw.getLoiterBearing());
			loiterConfiguration.setLoiterDirection(lw.getLoiterDirection());
			loiterConfiguration.setLoiterLength(lw.getLoiterLength());
			loiterConfiguration.setLoiterRadius(lw.getLoiterRadius());
			loiterPainter.paintElement(go, element, loiterConfiguration);
			
		//show arrival radius
		} else if(element.isMouseOver() || element.isSelected()) {
			float arrivalRadius = 5;
			Color fill = PainterColors.FILL_COLOR_DYNAMIC_ARRIVAL;
			Color contour = PainterColors.CONTOUR_COLOR_DYNAMIC_ARRIVAL;
			if(element.getWaypointDef().getPositionWaypoint().getTurnType().equals(TurnType.FLYOVER)) {
				arrivalRadius = 10;
				fill = PainterColors.FILL_COLOR_FLYOVER_ARRIVAL;
				contour = PainterColors.CONTOUR_COLOR_FLYOVER_ARRIVAL;
			}
			float[] xyLength = CUCSViewHelper.metersToPixels(arrivalRadius, element.getPosition(), element.getMap());
			go.setPaint(fill);
			go.fillOval((int)-xyLength[0], (int)-xyLength[1], (int)(xyLength[0]*2F), (int)(xyLength[1]*2F));
			go.setPaint(contour);
			go.drawOval((int)-xyLength[0], (int)-xyLength[1], (int)(xyLength[0]*2F), (int)(xyLength[1]*2F));
		}
		
		//draw waypoint point
		Polygon polygon = new Polygon();
		polygon.addPoint(-5, 5);
		polygon.addPoint(5, 5);
		polygon.addPoint(5, -5);
		polygon.addPoint(-5, -5);
		Graphics2D g = (Graphics2D) go.create();
		g.rotate(Math.toRadians(45));
		g.setColor(PainterColors.getWaypointContourFillPathArrowColor(element)[1]);
		g.fill(polygon);
		g.setColor(PainterColors.getWaypointContourFillPathArrowColor(element)[0]);
		g.draw(polygon);
		g.rotate(Math.toRadians(-45));
		
		//draw infos
		drawAltitude(go, element, element.getWaypointDef().getPositionWaypoint().getWaypointAltitudeType());
		drawDefaultPositionInfo(go, element);
		drawText(go, element, "Speed: " + getFormattedSpeed(element.getWaypointDef().getPositionWaypoint()) + " ("+ element.getWaypointDef().getPositionWaypoint().getWaypointSpeedType().toString() +")", 29, true);
		if(element.getWaypointDef().getState().equals(WaypointState.FROM_WAYPOINT)) {
			drawText(go, element, "Reached at: " + StringHelper.formatTimestamp(element.getWaypointDef().getWaypointTime()), 40, true);
		} else if(element.getWaypointDef().getState().equals(WaypointState.TO_WAYPOINT) ||
				  element.getWaypointDef().getState().equals(WaypointState.NEXT_WAYPOINT)) {
			drawText(go, element, "Reach time: " + StringHelper.formatTimestamp(element.getWaypointDef().getWaypointTime()) + " ("+ StringHelper.formatElapsedTime(element.getWaypointDef().getWaypointTime()-(System.currentTimeMillis()/1000.0)) +")", 45, true);
		}

    	//draw waypoint number
	    FontRenderContext frc = g.getFontRenderContext();
		Font font = new Font(Font.DIALOG, Font.BOLD, 16);
	    GlyphVector gv = font.createGlyphVector(frc, element.getWaypointDef().getWaypointNumber()+(element.getWaypointDef().getLoiterWaypoint()!=null||element.getWaypointDef().getPayloadActionWaypoints().size()>0?"*":""));
		g.setColor(stepNumberColor);
	    g.drawGlyphVector(gv, 7, 5);

		Polygon mask = new Polygon();
		mask.addPoint(-10, 10);
		mask.addPoint(10, 10);
		mask.addPoint(10, -10);
		mask.addPoint(-10, -10);

		drawDefaultLabel(g, element);
		
		return mask;
	}

	public String getFormattedSpeed(AVPositionWaypoint pw) {
		if(pw.getWaypointSpeedType().equals(WaypointSpeedType.ARRIVAL_TIME)) {
			return (int)pw.getArrivalTime() + " s";
		} else if(pw.getWaypointSpeedType().equals(WaypointSpeedType.GROUND_SPEED)) {
			return MeasureType.GROUND_SPEED.convertToTargetUnitStr(pw.getWaypointToSpeed(), true);
		} else if(pw.getWaypointSpeedType().equals(WaypointSpeedType.INDICATED_AIRSPEED) ||
			pw.getWaypointSpeedType().equals(WaypointSpeedType.TRUE_AIRSPEED)) {
			return MeasureType.AIR_SPEED.convertToTargetUnitStr(pw.getWaypointToSpeed(), true);
		}
		return null;
	}
	
	
}
