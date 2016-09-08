package br.skylight.cucs.plugins.controlmap2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import br.skylight.commons.dli.WaypointDef.WaypointState;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.plugins.missionplan.map.WaypointMapElement;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class PainterColors {

	public static final Color FILL_COLOR_GHOST_WAYPOINT = new Color(80, 80, 80, 170);
	public static final Color CONTOUR_COLOR_GHOST_WAYPOINT = new Color(220, 220, 220, 170);
	
	public static final Color FILL_COLOR_DYNAMIC_ARRIVAL = new Color(255, 255, 0, 60);
	public static final Color CONTOUR_COLOR_DYNAMIC_ARRIVAL = new Color(85, 85, 0, 80);
	
	public static final Color FILL_COLOR_FLYOVER_ARRIVAL = new Color(255, 255, 0, 60);
	public static final Color CONTOUR_COLOR_FLYOVER_ARRIVAL = new Color(85, 85, 0, 80);

	public static final Color lineRunwayColor = new Color(124, 166, 255, 255);
	public static final Color radiusContourColor = new Color(255,255,255,70);
	public static final Color radiusFillColor = new Color(255,255,255,20);
	public static final Color radiusDirectionColor = Color.DARK_GRAY;
	
    //styles
	private static final Color normalFillColor = new Color(255, 255, 0);
	private static final Color normalContourColor = new Color(176, 176, 0);
	private static final Color normalPathColor = new Color(255,200,0,50);
	public static final Color normalPathArrowColor = new Color(50,50,0,200);
	public static Stroke arrowStroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);

	private static final Color selectedFillColor = new Color(255, 0, 0);
	private static final Color selectedContourColor = new Color(0, 0, 0);
	private static final Color selectedPathColor = new Color(255,200,0,50);
	
	private static final Color fromWaypointFillColor = new Color(0, 255, 0);
	private static final Color fromWaypointContourColor = new Color(0, 111, 0);
	private static final Color fromWaypointPathColor = new Color(255,200,0,50);

	private static final Color toWaypointFillColor = new Color(255, 0, 0);
	private static final Color toWaypointContourColor = new Color(255, 255, 0);
	private static final Color toWaypointPathColor = new Color(255,255,0,180);
	private static final Color toWaypointPathArrowColor = new Color(20,20,0,255);
	
	private static final Color nextWaypointFillColor = new Color(255, 255, 0);
	private static final Color nextWaypointContourColor = new Color(176, 176, 0);
	private static final Color nextWaypointPathColor = new Color(255,200,0,50);

	private static final Color freeModeFillColor = new Color(0, 0, 230);
	private static final Color freeModeContourColor = new Color(0, 0, 80);
	
	private PainterColors() {
	}

	public static Color[] getWaypointContourFillPathArrowColor(WaypointMapElement elem) {
		Color[] result = new Color[4];
		
		if(elem.getWaypointDef().getState().equals(WaypointState.FROM_WAYPOINT)) {
			result[0] = fromWaypointContourColor;
			result[1] = fromWaypointFillColor;
			result[2] = fromWaypointPathColor;
			result[3] = normalPathColor;
		} else if(elem.getWaypointDef().getState().equals(WaypointState.TO_WAYPOINT)) {
			result[0] = toWaypointContourColor;
			result[1] = toWaypointFillColor;
			result[2] = toWaypointPathColor;
			result[3] = toWaypointPathColor;
		} else if(elem.getWaypointDef().getState().equals(WaypointState.NEXT_WAYPOINT)) {
			result[0] = nextWaypointContourColor;
			result[1] = nextWaypointFillColor;
			result[2] = nextWaypointPathColor;
			result[3] = normalPathColor;
		} else {
			result[0] = normalContourColor;
			result[1] = normalFillColor;
			result[2] = normalPathColor;
			result[3] = normalPathColor;
		}
		if(elem.isSelected()) {
			result[0] = selectedContourColor;
			result[1] = selectedFillColor;
		}
		if(elem.isMouseOver()) {
			result[1] = CUCSViewHelper.getBrighter(result[1], 0.3F);
		}
		return result;
	}
	
	public static Color[] getContourAndFillColor(MapElement element) {
		if(element.isSelected()) {
			return new Color[]{selectedContourColor, selectedFillColor};
		} else {
			return new Color[]{normalContourColor, normalFillColor};
		}
	}
}
