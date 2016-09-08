package br.skylight.cucs.widgets;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jfree.ui.RefineryUtilities;

import br.skylight.commons.Coordinates;
import br.skylight.commons.MeasureType;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.AltitudeCommandType;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.EnginePartStatus;
import br.skylight.commons.dli.enums.HeadingCommandType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.VehicleOperatingStates;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.MeasureHelper;
import br.skylight.cucs.widgets.artificialhorizon.ArtificialHorizon;

public class CUCSViewHelper extends ViewHelper {

	private static final Logger logger = Logger.getLogger(CUCSViewHelper.class.getName());
	
	public static Polygon ARROW = new Polygon();
	static {
		ARROW.addPoint(-1, 0);
		ARROW.addPoint(-1, 12);
		ARROW.addPoint(-3, 12);
		ARROW.addPoint(0, 17);//head top
		ARROW.addPoint(3, 12);
		ARROW.addPoint(1, 12);
		ARROW.addPoint(1, 0);
	}
	
	public static NumberFormat af = NumberFormat.getNumberInstance();
	public static NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumIntegerDigits(2);
		nf.setMinimumIntegerDigits(2);

		af.setMaximumFractionDigits(2);
		af.setMinimumFractionDigits(0);
		af.setMaximumIntegerDigits(9);
		af.setMinimumIntegerDigits(1);
	}

/*	public static void prepareTelemetryChartAsMessageListener(final TelemetryChartFrame c) {
		c.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				CUCSGateways.getDLIGateway().removeMessageListener(c);
			}
		});
		CUCSGateways.getDLIGateway().addMessageListener(c);
	}
*/
	
	public static class PidsComboItem {
		private byte id;

		private String label;

		private PIDConfiguration config;

		public PidsComboItem(byte id, String label, PIDConfiguration pidConfig) {
			super();
			this.id = id;
			this.label = label;
			this.config = pidConfig;
		}

		public byte getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}

		public PIDConfiguration getConfig() {
			return config;
		}
	}

	public static void openURLInOS(String url) {
		try {
			String osName = System.getProperty("os.name");

			if (osName.startsWith("Mac OS")) {
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });

			} else if (osName.startsWith("Windows")) {
				Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				int r = p.waitFor();
				if (r != 0) {
					throw new Exception("Process ended with return code " + r);
				}

			} else { // assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
						browser = browsers[count];
				if (browser == null) {
					throw new Exception("Could not find a web browser");
				} else {
					Process p = Runtime.getRuntime().exec(new String[] { browser, url });
					int r = p.waitFor();
					if (r != 0) {
						throw new Exception("Process ended with return code " + r);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns an array of number of pixels in x and y for a desired measurement
	 * in meters. result[0] - pixels in x for the length in meters result[1] -
	 * pixels in y for the length in meters
	 * 
	 * @param meters
	 * @param refPosition
	 * @param map
	 * @return
	 */
	public static float[] metersToPixels(float meters, GeoPosition refPosition, JXMapViewer map) {
		double latRad = CoordinatesHelper.metersToLatitudeLength(meters, Math.toRadians(refPosition.getLatitude()));
		double lngRad = CoordinatesHelper.metersToLongitudeLength(meters, Math.toRadians(refPosition.getLatitude()));

		Point2D p1 = map.getTileFactory().geoToPixel(refPosition, map.getZoom());
		Point2D p2 = map.getTileFactory().geoToPixel(new GeoPosition(refPosition.getLatitude() + Math.toDegrees(latRad), refPosition.getLongitude() + Math.toDegrees(lngRad)), map.getZoom());

		return new float[] { (float) (p2.getX() - p1.getX()), (float) (p1.getY() - p2.getY()) };
	}

	public static void setupDefaultButton(JButton button) {
		button.setMargin(ViewHelper.getDefaultButtonMargin());
	}

	public static String formatTime(long timeInSeconds) {
		long hou = timeInSeconds / (60 * 60);
		timeInSeconds -= hou * (60 * 60);
		long min = timeInSeconds / (60);
		timeInSeconds -= min * (60);
		long sec = timeInSeconds;
		String result = "";
		if (hou > 0) {
			result += nf.format(hou) + "h";
		}
		if (min > 0 || hou > 0) {
			result += nf.format(min) + "m";
		}
		result += nf.format(sec) + "s";
		return result;
	}

	public static JSpinner createFloatSpinner() {
		SpinnerNumberModel floatModel = new SpinnerNumberModel();
		floatModel.setValue(0F);
		floatModel.setStepSize(1F);
		JSpinner result = new JSpinner(floatModel);
		JSpinner.NumberEditor ne = ((JSpinner.NumberEditor) result.getEditor());
		ne.getFormat().setMaximumFractionDigits(6);
		ne.getFormat().setMinimumFractionDigits(0);
		ne.getFormat().setMaximumIntegerDigits(99);
		ne.getFormat().setMinimumIntegerDigits(1);
		return result;
	}

	public static Color getBrighter(Color c, float i) {
		float[] rgb = c.getRGBComponents((float[]) null);
		float dr = (1F - rgb[0]) * i;
		float dg = (1F - rgb[1]) * i;
		float db = (1F - rgb[2]) * i;
		dr = rgb[0] + dr;
		if (dr > 1)
			dr = 1;
		dg = rgb[1] + dg;
		if (dg > 1)
			dg = 1;
		db = rgb[2] + db;
		if (db > 1)
			db = 1;
		return new Color(dr, dg, db);
	}

	public static void setDefaultIcon(Window w) {
		w.setIconImage(Toolkit.getDefaultToolkit().getImage(CUCSViewHelper.class.getResource("/br/skylight/cucs/images/plane.gif")));
	}

	public static TelemetryChartFrame showMultiChart(String title, String xtitle, String ytitle, boolean legend, int maxItemsInSeries, MessageToChartConverter converter, String ... seriesName) {
		TelemetryChartFrame frame = TelemetryChartFrame.createMultiChart(title, xtitle, ytitle, legend, maxItemsInSeries, converter, seriesName);
		frame.setSize(620, 354);
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		return frame;
	}

	public static void updateArtificialHorizonValues(InertialStates m, ArtificialHorizon artificialHorizon) {
		artificialHorizon.getRoll().setCurrentValue(m.getPhi());
		artificialHorizon.getPitch().setCurrentValue(m.getTheta());
		artificialHorizon.getHeading().setCurrentValue((float)MathHelper.normalizeAngle2(CoordinatesHelper.calculateHeading(m.getUSpeed(), m.getVSpeed())));
		artificialHorizon.getSpeed(SpeedType.GROUND_SPEED).setCurrentValue(MeasureHelper.calculateMagnitude(m.getUSpeed(), m.getVSpeed()));
		artificialHorizon.getAltitude(m.getAltitudeType()).setCurrentValue(m.getAltitude());
	}

	public static void updateArtificialHorizonTargets(InertialStates m, ArtificialHorizon artificialHorizon) {
		artificialHorizon.getAltitude(m.getAltitudeType()).setTargetValue(m.getAltitude());
		artificialHorizon.getAltitude(m.getAltitudeType()).setTargetValueVisible(true);
		artificialHorizon.getSpeed(SpeedType.GROUND_SPEED).setTargetValue(MeasureHelper.calculateMagnitude(m.getUSpeed(), m.getVSpeed()));
		artificialHorizon.getSpeed(SpeedType.GROUND_SPEED).setTargetValueVisible(true);
		artificialHorizon.getHeading().setTargetValue(CoordinatesHelper.calculateHeading(m.getUSpeed(), m.getVSpeed()));
		artificialHorizon.getHeading().setTargetValueVisible(true);
	}

	public static void updateArtificialHorizonValues(AirAndGroundRelativeStates m, ArtificialHorizon artificialHorizon) {
		artificialHorizon.getAltitude(AltitudeType.AGL).setCurrentValue(m.getAltitude(AltitudeType.AGL));
		artificialHorizon.getAltitude(AltitudeType.BARO).setCurrentValue(m.getAltitude(AltitudeType.BARO));
		artificialHorizon.getAltitude(AltitudeType.PRESSURE).setCurrentValue(m.getAltitude(AltitudeType.PRESSURE));
		artificialHorizon.getAltitude(AltitudeType.WGS84).setCurrentValue(m.getAltitude(AltitudeType.WGS84));
		//ground speed is given by M101
		artificialHorizon.getSpeed(SpeedType.INDICATED_AIRSPEED).setCurrentValue(m.getIndicatedAirspeed());
		artificialHorizon.getSpeed(SpeedType.TRUE_AIRSPEED).setCurrentValue(m.getTrueAirspeed());
	}
	public static void updateArtificialHorizonTargets(AirAndGroundRelativeStates m, ArtificialHorizon artificialHorizon) {
		artificialHorizon.getAltitude(AltitudeType.AGL).setTargetValue(m.getAglAltitude());
		artificialHorizon.getAltitude(AltitudeType.AGL).setTargetValueVisible(true);
		artificialHorizon.getAltitude(AltitudeType.BARO).setTargetValue(m.getBarometricAltitude());
		artificialHorizon.getAltitude(AltitudeType.BARO).setTargetValueVisible(true);
		artificialHorizon.getAltitude(AltitudeType.PRESSURE).setTargetValue(m.getPressureAltitude());
		artificialHorizon.getAltitude(AltitudeType.PRESSURE).setTargetValueVisible(true);
		artificialHorizon.getAltitude(AltitudeType.WGS84).setTargetValue(m.getWgs84Altitude());
		artificialHorizon.getAltitude(AltitudeType.WGS84).setTargetValueVisible(true);
		artificialHorizon.getSpeed(SpeedType.INDICATED_AIRSPEED).setTargetValue(m.getIndicatedAirspeed());
		artificialHorizon.getSpeed(SpeedType.INDICATED_AIRSPEED).setTargetValueVisible(true);
		artificialHorizon.getSpeed(SpeedType.TRUE_AIRSPEED).setTargetValue(m.getTrueAirspeed());
		artificialHorizon.getSpeed(SpeedType.TRUE_AIRSPEED).setTargetValueVisible(true);
	}

	public static void updateArtificialHorizonTargets(VehicleOperatingStates m, ArtificialHorizon artificialHorizon) {
		//clear targets
		artificialHorizon.getHeading().setTargetValueVisible(false);
		for (SpeedType st : SpeedType.values()) {
			artificialHorizon.getSpeed(st).setTargetValueVisible(false);
		}
		for (AltitudeType at : AltitudeType.values()) {
			artificialHorizon.getAltitude(at).setTargetValueVisible(false);
		}
		//heading target
		if(m.getHeadingCommandType().equals(HeadingCommandType.COURSE) || m.getHeadingCommandType().equals(HeadingCommandType.HEADING_AND_COURSE)) {
			artificialHorizon.getHeading().setTargetValue(m.getCommandedCourse());
			artificialHorizon.getHeading().setTargetValueVisible(true);
		} else if(m.getHeadingCommandType().equals(HeadingCommandType.HEADING)) {
			artificialHorizon.getHeading().setTargetValue(m.getCommandedHeading());
			artificialHorizon.getHeading().setTargetValueVisible(true);
		} else if(m.getHeadingCommandType().equals(HeadingCommandType.ROLL)) {
			artificialHorizon.getRoll().setTargetValue(m.getCommandedRoll());
			artificialHorizon.getRoll().setTargetValueVisible(true);
		}
		
		//altitude target
		if(m.getAltitudeCommandType().equals(AltitudeCommandType.ALTITUDE)) {
			artificialHorizon.getAltitude(m.getAltitudeType()).setTargetValue(m.getCommandedAltitude());
			artificialHorizon.getAltitude(m.getAltitudeType()).setTargetValueVisible(true);
		}
		
		//speed target
		artificialHorizon.getSpeed(m.getSpeedType()).setTargetValue(m.getCommandedSpeed());
		artificialHorizon.getSpeed(m.getSpeedType()).setTargetValueVisible(true);
	}
	
	public static void updateDisplayUnits(ArtificialHorizon ah) {
		//setup measure types
		ah.getAltitude(AltitudeType.AGL).setDisplayUnit(MeasureType.ALTITUDE.getTargetUnit());
		ah.getAltitude(AltitudeType.BARO).setDisplayUnit(MeasureType.ALTITUDE.getTargetUnit());
		ah.getAltitude(AltitudeType.PRESSURE).setDisplayUnit(MeasureType.ALTITUDE.getTargetUnit());
		ah.getAltitude(AltitudeType.WGS84).setDisplayUnit(MeasureType.ALTITUDE.getTargetUnit());
		ah.getSpeed(SpeedType.GROUND_SPEED).setDisplayUnit(MeasureType.GROUND_SPEED.getTargetUnit());
		ah.getSpeed(SpeedType.INDICATED_AIRSPEED).setDisplayUnit(MeasureType.AIR_SPEED.getTargetUnit());
		ah.getSpeed(SpeedType.TRUE_AIRSPEED).setDisplayUnit(MeasureType.AIR_SPEED.getTargetUnit());
	}

	public static void copySetpointsFromArtificialHorizon(ArtificialHorizon ah, VehicleSteeringCommand mc) {
		if(ah.getAltitude(ah.getSelectedAltitudeType()).isTargetValueVisible()) {
			mc.setAltitudeCommandType(AltitudeCommandType.ALTITUDE);
			mc.setAltitudeType(ah.getSelectedAltitudeType());
			mc.setCommandedAltitude(ah.getAltitude(ah.getSelectedAltitudeType()).getTargetValue());
		} else {
			mc.setAltitudeCommandType(AltitudeCommandType.NO_VALID_ALTITUDE_COMMAND);
		}
		
		mc.setSpeedType(ah.getSelectedSpeedType());
		mc.setCommandedSpeed(ah.getSpeed(ah.getSelectedSpeedType()).getTargetValue());
		
		if(ah.getHeading().isTargetValueVisible()) {
			mc.setCommandedCourse(ah.getHeading().getTargetValue());
			mc.setHeadingCommandType(HeadingCommandType.COURSE);
		} else {
			mc.setHeadingCommandType(HeadingCommandType.NO_VALID_HEADING_COMMAND);
		}
	}

	public static void copySetpointsFromArtificialHorizon(ArtificialHorizon ah, LoiterConfiguration mc) {
		mc.setAltitudeType(ah.getSelectedAltitudeType());
		mc.setLoiterAltitude(ah.getAltitude(ah.getSelectedAltitudeType()).getTargetValue());
		
		mc.setSpeedType(ah.getSelectedSpeedType());
		mc.setLoiterSpeed(ah.getSpeed(ah.getSelectedSpeedType()).getTargetValue());
	}

	public static void setDefaultActionClick(JSpinner spinner, final JButton clickButtonOnAction) {
		((JSpinner.NumberEditor) spinner.getEditor()).getTextField().addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent ke) {
				if (ke.getKeyChar() == '\n') {
					clickButtonOnAction.doClick();
				}
			}
			public void keyReleased(KeyEvent arg0) {}
			public void keyPressed(KeyEvent arg0) {}
		});
	}

	public static void selectTypedTreeTableRow(JXTreeTable treeTable, Object obj) {
		//TODO IMPLEMENT
	}

	public static GeoPosition toGeoPosition(Coordinates point) {
		return new GeoPosition(point.getLatitude(), point.getLongitude());
	}

	public static void copyCoordinates(Coordinates targetCoordinates, GeoPosition sourcePosition) {
		targetCoordinates.setLatitude(sourcePosition.getLatitude());
		targetCoordinates.setLongitude(sourcePosition.getLongitude());
	}
	
	public static void setupButtonForEngineStatus(RoundButton button, EnginePartStatus es) {
		int status = es.ordinal();
		Color c = Color.DARK_GRAY;
		if(status==1 || status==7) {
			c = Color.RED;
		} else if(status==2 || status==6) {
			c = Color.YELLOW;
		} else if(status>=3 && status<=5) {
			c = Color.GREEN;
		}
		if(status>=1 && status <=3) {
			button.setToolTipText("Low");
		} else if(status>=5 && status <=7) {
			button.setToolTipText("High");
		} else if(status==4) {
			button.setToolTipText("Normal");
		} else if(status==0) {
			button.setToolTipText("No status");
		}
		button.setBackground(c);
	}

}
