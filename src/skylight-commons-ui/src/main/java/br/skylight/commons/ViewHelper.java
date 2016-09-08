package br.skylight.commons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.treetable.TreeTableNode;

import br.skylight.commons.dli.enums.ModeState;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MathHelper;

public class ViewHelper {

	private static final Logger logger = Logger.getLogger(ViewHelper.class.getName());

/*	public static void prepareTelemetryChartAsMessageListener(final TelemetryChartFrame c) {
		c.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				CUCSGateways.getDLIGateway().removeMessageListener(c);
			}
		});
		CUCSGateways.getDLIGateway().addMessageListener(c);
	}
*/
	
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
		float lat = (float)Math.toDegrees(CoordinatesHelper.metersToLatitudeLength(meters, (float) Math.toRadians(refPosition.getLatitude())));
		float lng = (float)Math.toDegrees(CoordinatesHelper.metersToLongitudeLength(meters, (float) Math.toRadians(refPosition.getLatitude())));

		Point2D p1 = map.getTileFactory().geoToPixel(refPosition, map.getZoom());
		Point2D p2 = map.getTileFactory().geoToPixel(new GeoPosition(refPosition.getLatitude() + lat, refPosition.getLongitude() + lng), map.getZoom());

		return new float[] { (float) Math.abs(p2.getX() - p1.getX()), (float) Math.abs(p1.getY() - p2.getY()) };
	}

	public static void setupDefaultButton(JButton button) {
		button.setFont(new Font("Dialog", Font.PLAIN, 10));
		button.setMargin(getDefaultButtonMargin());
	}

	public static Insets getDefaultButtonMargin() {
		return new Insets(2, 4, 2, 4);
	}

	public static Insets getMinimalButtonMargin() {
		return new Insets(1, 1, 1, 1);
	}


//	public static JSpinner createFloatSpinner() {
//		SpinnerNumberModel floatModel = new SpinnerNumberModel();
//		floatModel.setValue(0F);
//		floatModel.setStepSize(1F);
//		JSpinner result = new JSpinner(floatModel);
//		JSpinner.NumberEditor ne = ((JSpinner.NumberEditor) result.getEditor());
//		ne.getFormat().setMaximumFractionDigits(6);
//		ne.getFormat().setMinimumFractionDigits(0);
//		ne.getFormat().setMaximumIntegerDigits(99);
//		ne.getFormat().setMinimumIntegerDigits(1);
//		return result;
//	}

	public static Color getBrighter(Color color, float varBri) {
		return getHSBColorVariation(color, 0, 0, varBri);
	}

	public static Color getHSBColorVariation(Color baseColor, float varHue, float varSat, float varBri) {
		float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
		hsb[0] = hsb[0]+varHue;
		if(hsb[0]>1) {
			hsb[0] = 1-hsb[0];
		}
		hsb[1] = MathHelper.clamp(hsb[1]+varSat, 0, 1);
		hsb[2] = MathHelper.clamp(hsb[2]+varBri, 0, 1);
		return Color.getHSBColor(hsb[0],hsb[1],hsb[2]);
	}
	
	public static boolean showConfirmationDialog(Component component, String message) {
		return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(component, message, "Confirmation", JOptionPane.YES_NO_OPTION);
	}

	public static void showException(Exception e) {
		showException(null,e);
	}
	public static void showException(Component c, Exception e) {
		if (c != null) {
			JOptionPane.showMessageDialog(c, e.getClass().getName() + ": " + e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
		}
		logger.throwing(null, null, e);
		e.printStackTrace();
	}

	public static void centerWindow(Window window) {
		// Get the screen size
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();

		// Calculate the frame location
		int x = (screenSize.width - window.getWidth()) / 2;
		int y = (screenSize.height - window.getHeight()) / 2;

		// Set the new frame location
		window.setLocation(x, y);
	}

	public static TreeTableNode findTreeNode(TreeTableNode fromNode, Object userObject) {
		if(userObject.equals(fromNode.getUserObject())) {
			return fromNode;
			
		} else if(fromNode.getChildCount()>0) {
			for (int i=0; i<fromNode.getChildCount(); i++) {
				TreeTableNode tn = findTreeNode(fromNode.getChildAt(i), userObject);
				if(tn!=null) {
					return tn;
				}
			}
		}
		return null;
	}

	public static Rectangle2D drawRightAbsAligned(Graphics2D g, String text, Font font, int rightX, int y) {
		if(text!=null && text.length()>0) {
			g.setFont(font);
			TextLayout tt = new TextLayout(text, font, g.getFontRenderContext());
			tt.draw(g, rightX - (int)tt.getBounds().getWidth(), y);
			return new Rectangle2D.Float(rightX - (int)tt.getBounds().getWidth(), y-(float)tt.getBounds().getHeight(), 
										 (float)tt.getBounds().getWidth(), 		  (float)tt.getBounds().getHeight());
		}
		return null;
	}

	public static TextLayout drawLeftCenterAligned(Graphics2D g, String text, Font font, int x, int centerY) {
		if(text!=null && text.length()>0) {
			g.setFont(font);
			TextLayout tt = new TextLayout(text, font, g.getFontRenderContext());
			tt.draw(g, x, (int)(centerY+tt.getBounds().getHeight()/2));
			return tt;
		}
		return null;
	}

	public static void drawRightCenterAligned(Graphics2D g, String text, Font font, int rightX, int centerY) {
		if(text!=null && text.length()>0) {
			g.setFont(font);
			TextLayout tt = new TextLayout(text, font, g.getFontRenderContext());
			tt.draw(g, rightX - (int)tt.getBounds().getWidth(), (int)(centerY+tt.getBounds().getHeight()/2));
		}
	}

	public static void drawCenterAbsAligned(Graphics2D g, String text, Font font, int centerX, int y) {
		if(text!=null && text.length()>0) {
			g.setFont(font);
			TextLayout tt = new TextLayout(text, font, g.getFontRenderContext());
			tt.draw(g, centerX - (int)tt.getBounds().getWidth()/2, y);
		}
	}

	public static void setPrimaryFocus(JDialog dialog, final Component component) {
		dialog.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
				component.requestFocus();
			}
			public void componentResized(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
		});
	}

	public static void setupSpinnerNumber(JSpinner numberSpinner, double value, double minValue, double maxValue, double stepSize, int minFractionDigits, int maxFractionDigits) {
		numberSpinner.setModel(new SpinnerNumberModel(value, minValue, maxValue, stepSize));
		((NumberEditor)numberSpinner.getEditor()).getFormat().setMinimumIntegerDigits(1);
		((NumberEditor)numberSpinner.getEditor()).getFormat().setMaximumIntegerDigits(Integer.MAX_VALUE);
		((NumberEditor)numberSpinner.getEditor()).getFormat().setMinimumFractionDigits(minFractionDigits);
		((NumberEditor)numberSpinner.getEditor()).getFormat().setMaximumFractionDigits(maxFractionDigits);
	}

	public static File showFileSelectionDialog(Component parent, File initialDir, final String filterFileExtensions, final String filterDescription, boolean selectionForFileWrite) {
		JFileChooser fc = new JFileChooser(initialDir);
		fc.setFileFilter(new FileFilter() {
			public String getDescription() {
				return filterDescription;
			}
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith(filterFileExtensions.toLowerCase());
			}
		});
		// Show open dialog; this method does not return until the dialog is closed
		if(selectionForFileWrite) {
			fc.showSaveDialog(parent);
		} else {
			fc.showOpenDialog(parent);
		}
		File f = fc.getSelectedFile();
		if(selectionForFileWrite) {
			if(f!=null) {
				if(f.exists()) {
					if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "File already exists. Confirm overwrite?")) {
						return null;
					}
				} else {
					//append file extension at the end of file name if no other extension was defined
					if(!f.getName().toLowerCase().endsWith(filterFileExtensions.toLowerCase())) {
						if(!f.getName().contains("\\.")) {
							f = new File(f.getAbsolutePath() + filterFileExtensions);
						}
					}
				}
			}
		}
		return f;
	}

//	public static void setupSpinnerModel(JSpinner spinner, double value, double minValue, double maxValue, double stepSize) {
//		((SpinnerNumberModel)spinner.getModel()).setValue(value);
//		((SpinnerNumberModel)spinner.getModel()).setMinimum(minValue);
//		((SpinnerNumberModel)spinner.getModel()).setMaximum(maxValue);
//		((SpinnerNumberModel)spinner.getModel()).setStepSize(stepSize);
//	}

}
