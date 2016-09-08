package br.skylight.cucs.widgets.artificialhorizon;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.swing.JFrame;
import javax.swing.JPanel;

import br.skylight.commons.MeasureType;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.ModeState;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.cucs.widgets.NumberInputDialog;

public class ArtificialHorizon extends JPanel {

	private static final long serialVersionUID = 1L;
	public enum Alignment {TOP, BOTTOM, LEFT, RIGHT}
	public enum HorizonControl {ALTITUDE, SPEED, HEADING, SPEED_TYPE, ALTITUDE_TYPE}
	
	private ImageObserver dummyObserver = new ImageObserver() {
		public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
			return false;
		}
	};

	//state variables
	private ArtificialHorizonValue pitch = new ArtificialHorizonValue(SI.RADIAN);
	private ArtificialHorizonValue roll = new ArtificialHorizonValue(SI.RADIAN);

	private Map<AltitudeType,ArtificialHorizonValue> altitudes = new HashMap<AltitudeType,ArtificialHorizonValue>();
	private AltitudeType altitudeType = AltitudeType.AGL;

	private Map<SpeedType,ArtificialHorizonValue> speeds = new HashMap<SpeedType,ArtificialHorizonValue>();
	private SpeedType speedType = SpeedType.GROUND_SPEED;
	
	private ArtificialHorizonValue heading = new ArtificialHorizonValue(SI.RADIAN);

	private BufferedImage img;  //  @jve:decl-index=0:
	private int lastSize;
	
	private Color targetColor = new Color(88,87,46);
	private Map<HorizonControl,ClickableElement> elements = new HashMap<HorizonControl,ClickableElement>();
	
	private boolean drawCirclePanel = false;
	private List<ArtificialHorizonListener> listeners = new ArrayList<ArtificialHorizonListener>();

	private float commandedHeading = Float.NaN;
	
	/**
	 * This is the default constructor
	 */
	public ArtificialHorizon() {
		super();
		initialize();
		
		//initialize maps
		for (AltitudeType at : AltitudeType.values()) {
			altitudes.put(at, new ArtificialHorizonValue(SI.METER));
		}
		for (SpeedType at : SpeedType.values()) {
			speeds.put(at, new ArtificialHorizonValue(SI.METERS_PER_SECOND));
		}
		
		//set default target display units
		roll.setDisplayUnit(NonSI.DEGREE_ANGLE);
		pitch.setDisplayUnit(NonSI.DEGREE_ANGLE);
		heading.setDisplayUnit(NonSI.DEGREE_ANGLE);
		setOpaque(false);
		
//		//tests
//		addMouseListener(new MouseAdapter() {
//			public void mouseClicked(MouseEvent e) {
//				getRoll().setCurrentValue((float)(Math.random()*Math.PI/3F));
//				getPitch().setCurrentValue((float)(Math.random()*Math.PI/3F));
//				getSpeed(SpeedType.GROUND_SPEED).setCurrentValue((float)(Math.random()*200));
//				getHeading().setCurrentValue((float)(Math.toRadians(Math.random()*360)));
//				getSpeed(SpeedType.GROUND_SPEED).setTargetValue(getSpeed(SpeedType.GROUND_SPEED).getCurrentValue()+(float)Math.random()*50);
//				getHeading().setTargetValue(getHeading().getCurrentValue()+(float)Math.random()*(float)Math.PI/4);
//				repaint();
//			}
//		});
		getAltitude(AltitudeType.AGL).setTargetValue(100);
		getSpeed(SpeedType.GROUND_SPEED).setTargetValue(20);
		
		//setup clickable/draggable elements
		elements.put(HorizonControl.ALTITUDE, new ArtificialHorizonElement() {
			@Override
			public float getCurrentValue() {
				return altitudes.get(altitudeType).getTargetValueOnDisplayUnit();
			}
			@Override
			public void onValueChanged(float value) {
				altitudes.get(altitudeType).setTargetValueFromDisplayUnit(value);
			}
			@Override
			public void onDragFinished() {
				for (ArtificialHorizonListener l : listeners) {
					l.onTargetAltitudeSet(altitudeType, altitudes.get(altitudeType).getTargetValue());
				}
			}
			@Override
			public Alignment getRulerAlignment() {
				return Alignment.TOP;
			}
			@Override
			public void onElementClicked(MouseEvent e) {
				if(e.getClickCount()>=2) {
					//remember that internal variables are always in SI units
					Double r = NumberInputDialog.showInputDialog(getThis(), "Enter target altitude ("+ altitudes.get(altitudeType).getDisplayUnit().toString() +"):", altitudes.get(altitudeType).getTargetValueOnDisplayUnit(), altitudes.get(altitudeType).convertToDisplayUnit(-1000), Double.MAX_VALUE, 1, 0, 4);
					if(r!=null) {
						altitudes.get(altitudeType).setTargetValueFromDisplayUnit(r.floatValue());
						for (ArtificialHorizonListener l : listeners) {
							l.onTargetAltitudeSet(altitudeType, r.floatValue());
						}
					}
				}
			}
		});
		elements.put(HorizonControl.SPEED, new ArtificialHorizonElement() {
			@Override
			public float getCurrentValue() {
				return speeds.get(speedType).getTargetValueOnDisplayUnit();
			}
			@Override
			public void onValueChanged(float value) {
				speeds.get(speedType).setTargetValueFromDisplayUnit(value);
			}
			@Override
			public void onDragFinished() {
				for (ArtificialHorizonListener l : listeners) {
					l.onTargetSpeedSet(speedType, speeds.get(speedType).getTargetValue());
				}
			}
			@Override
			public Alignment getRulerAlignment() {
				return Alignment.TOP;
			}
			@Override
			public void onElementClicked(MouseEvent e) {
				if(e.getClickCount()>=2) {
					//remember that internal variables are always in SI units
					Double r = NumberInputDialog.showInputDialog(getThis(), "Enter target "+ speedType +" ("+ speeds.get(speedType).getDisplayUnit().toString() +"):", speeds.get(speedType).getTargetValueOnDisplayUnit(), speeds.get(speedType).convertToDisplayUnit(-100), Double.MAX_VALUE, 1, 0, 4);
					if(r!=null) {
						speeds.get(speedType).setTargetValueFromDisplayUnit(r.floatValue());
						for (ArtificialHorizonListener l : listeners) {
							l.onTargetSpeedSet(speedType, r.floatValue());
						}
					}
				}
			}
		});
		elements.put(HorizonControl.HEADING, new ArtificialHorizonElement() {
			@Override
			public float getCurrentValue() {
				return heading.getTargetValueOnDisplayUnit();
			}
			@Override
			public void onValueChanged(float value) {
				heading.setTargetValueFromDisplayUnit((float)MeasureType.HEADING.convertToTargetUnit(MathHelper.normalizeAngle2(MeasureType.HEADING.convertToSourceUnit(value))));
			}
			@Override
			public void onDragFinished() {
				for (ArtificialHorizonListener l : listeners) {
					l.onTargetHeadingSet((float)MathHelper.normalizeAngle2(heading.getTargetValue()));
				}
			}
			@Override
			public Alignment getRulerAlignment() {
				return Alignment.RIGHT;
			}
			@Override
			public void onElementClicked(MouseEvent e) {
				if(e.getClickCount()>=2) {
					Double r = NumberInputDialog.showInputDialog(getThis(), "Enter target heading angle ("+ heading.getDisplayUnit().toString() +"):", heading.getTargetValueOnDisplayUnit(), heading.convertToDisplayUnit(0), heading.convertToDisplayUnit(360), Math.toRadians(1), 0, 4);
					if(r!=null) {
						heading.setTargetValueFromDisplayUnit(r.floatValue());
						for (ArtificialHorizonListener l : listeners) {
							l.onTargetHeadingSet(r.floatValue());
						}
					}
				}
			}
		});

		elements.put(HorizonControl.ALTITUDE_TYPE, new ClickableElement() {
			@Override
			public void onElementClicked(MouseEvent e) {
				int st = altitudeType.ordinal() + 1;
				if(st>=AltitudeType.values().length) {
					st = 0;
				}
				altitudeType = AltitudeType.values()[st];
			}
			@Override
			public void onDragFinished() {}
		});
		elements.get(HorizonControl.ALTITUDE_TYPE).setColorOver(Color.ORANGE);
		elements.get(HorizonControl.ALTITUDE_TYPE).setColorDrag(Color.ORANGE);
		elements.get(HorizonControl.ALTITUDE_TYPE).setColorNormal(new Color(1,1,1,0.5F));
		
		elements.put(HorizonControl.SPEED_TYPE, new ClickableElement() {
			@Override
			public void onElementClicked(MouseEvent e) {
				int st = speedType.ordinal() + 1;
				if(st>=SpeedType.values().length) {
					st = 0;
				}
				speedType = SpeedType.values()[st];
			}
			@Override
			public void onDragFinished() {}
		});
		elements.get(HorizonControl.SPEED_TYPE).setColorOver(Color.ORANGE);
		elements.get(HorizonControl.SPEED_TYPE).setColorDrag(Color.ORANGE);
		elements.get(HorizonControl.SPEED_TYPE).setColorNormal(new Color(1,1,1,0.5F));
		
		elements.get(HorizonControl.ALTITUDE).install(this);
		elements.get(HorizonControl.SPEED).install(this);
		elements.get(HorizonControl.HEADING).install(this);
		elements.get(HorizonControl.ALTITUDE_TYPE).install(this);
		elements.get(HorizonControl.SPEED_TYPE).install(this);
	}

	private ArtificialHorizon getThis() {
		return this;
	}
	
	@Override
	public void paint(Graphics go) {
		Graphics2D g = (Graphics2D)go.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//overall size scaling
		int size = Math.min(getWidth(), getHeight());

		//draw horizon (with pitch scale and heading)
//		s = System.currentTimeMillis();
		BufferedImage horizon = createHorizon(size);
		if(drawCirclePanel) {
			horizon = applyTopMask(horizon, size);
		}
		g.drawImage(horizon, 0, 0, dummyObserver);
//		System.out.println("HZ: "+(System.currentTimeMillis()-s));

		//draw altitude ruler
		drawAltitudeRuler(g, size);
		
		//draw heading ruler
		drawHeadingRuler(g, size);

		//draw speed ruler
		drawSpeedRuler(g, size);

		//draw roll scale
		g.translate((getWidth()-size)/2, (getHeight()-size)/2);
		drawRollScale(g, size);
		g.translate(-(getWidth()-size)/2, -(getHeight()-size)/2);

		super.paint(go);
	}

	private void drawAltitudeRuler(Graphics2D g, int size) {
		//draw ruler
		ArtificialHorizonValue hv = altitudes.get(altitudeType);
		float av = hv.getCurrentValueOnDisplayUnit();
		float range = hv.convertToDisplayUnit(250);
		int min = (int)(av - range);
		int max = (int)(av + range);
		int tick = MathHelper.nextMultiple(hv.convertToDisplayUnit(25), 5);
		int tickLength = size/20;
		int rulerLength = (int)(getHeight()*0.7);
		int rulerx = getWidth()-4;
		int rulery = (int)(getHeight()/2 - rulerLength/2);
		Rectangle2D bounds = drawRuler(g, rulerx, rulery, min, max, tick, tick*2, rulerLength, tickLength, size/25, Color.WHITE, Alignment.RIGHT, false, false);

		//draw box for current value
		float lineWidth = (float)size/280F;
		int pw = (int)(bounds.getWidth() + tickLength*2 - lineWidth) + 3;
		int ph = (int)(bounds.getHeight()*2);
		int x = (int)(getWidth()-pw);
		int y = (int)((getHeight()/2) - (ph/2));
		//draw
		Polygon b = createBoxShape(pw, ph, Alignment.LEFT);
		b.translate(x, y);
		g.setColor(Color.BLUE);
		g.fill(b);
		g.setColor(Color.BLUE.darker());
		g.setStroke(new BasicStroke(lineWidth));
		g.draw(b);

		//draw current value on box
		g.setColor(Color.WHITE);
		Font font = new Font(Font.DIALOG, Font.PLAIN, (int)(size/25F));
		TextLayout t = new TextLayout(""+Math.round(av), font, g.getFontRenderContext());
		t.draw(g, (int)(getWidth() - t.getBounds().getWidth()) - 4, getHeight()/2F + (float)bounds.getHeight()/2F);
		
		//draw ruler unit
		g.setColor(new Color(1,1,1,0.5F));
		int ty = (int)(getHeight()/2 + rulerLength/2 + bounds.getHeight()*1.7);
		g.drawString(hv.getDisplayUnit().toString(), getWidth() - pw, ty);

		//draw clickable altitude type
		ClickableElement ce = elements.get(HorizonControl.ALTITUDE_TYPE);
		g.setColor(ce.getCurrentColor());
		TextLayout tt = new TextLayout(altitudeType.toString(), font, g.getFontRenderContext());
		int ux = getWidth() - pw;
		int uy = (int)(ty + bounds.getHeight()*1.4);
		tt.draw(g, ux, uy);
//		ce.setMask(new Rectangle2D.Float(ux, uy-(float)tt.getBounds().getHeight(), (float)tt.getBounds().getWidth(), (float)tt.getBounds().getHeight()));
		ce.setMask(new Rectangle2D.Float(ux-5, uy-(float)tt.getBounds().getHeight()-5, (float)tt.getBounds().getWidth()+10F, (float)tt.getBounds().getHeight()+10F));
		
		//draw target setpoint
		if(hv.isTargetValueVisible() && !Float.isNaN(hv.getTargetValue()) && isEnabled()) {
			float tv = hv.getTargetValueOnDisplayUnit();
			int fl = (int)(ph*1.2);
			b = createTargetShape(fl, Alignment.RIGHT);
			float valueToPixelsRatio = (float)rulerLength/(max-min);
			int tsx = rulerx-pw;
			int tsy = (int)(rulery+valueToPixelsRatio*MathHelper.clamp(max-tv, 0, max-min))-(int)(fl/2)+1;
			b.translate(tsx, tsy);
			Color cc = elements.get(HorizonControl.ALTITUDE).getCurrentColor();
			g.setColor(cc);
			g.fill(b);
			g.setColor(cc.darker().darker());
			g.setStroke(new BasicStroke(lineWidth));
			g.draw(b);
			g.setColor(cc);
			ViewHelper.drawLeftCenterAligned(g, Math.round(tv)+"", font, tsx+fl/4, tsy+fl/2);

			//setup drag elements
			elements.get(HorizonControl.ALTITUDE).setMask(createMask(b, 0, (int)t.getBounds().getWidth(), 0, 0));
			((ArtificialHorizonElement)elements.get(HorizonControl.ALTITUDE)).setPixelToValueRatio((max-min)/(float)rulerLength);
		}
	}

	private void drawSpeedRuler(Graphics2D g, int size) {
		//draw ruler
		ArtificialHorizonValue hv = speeds.get(speedType);
		float av = hv.getCurrentValueOnDisplayUnit();
		float range = hv.convertToDisplayUnit(25);
		int min = (int)(av - range);
		int max = (int)(av + range);
		int tick = MathHelper.nextMultiple(hv.convertToDisplayUnit(2), 2);
		if(tick==0) tick = 1;
		int tickLength = size/20;
		int rulerLength = (int)(getHeight()*0.7);
		int rulerx = 4;
		int rulery = (int)(getHeight()/2 - rulerLength/2);
		Rectangle2D bounds = drawRuler(g, rulerx, rulery, min, max, tick, tick*5, rulerLength, tickLength, size/25, Color.WHITE, Alignment.LEFT, false, false);
		
		//draw box for current value
		float lineWidth = (float)size/280F;
		int pw = (int)(bounds.getWidth() + tickLength*2 - lineWidth) + 3;
		int ph = (int)(bounds.getHeight()*2);
		int x = 0;
		int y = (int)((getHeight()/2) - (ph/2));
		Polygon b = createBoxShape(pw, ph, Alignment.RIGHT);
		b.translate(x, y);
		g.setColor(Color.BLUE);
		g.fill(b);
		g.setColor(Color.BLUE.darker());
		g.setStroke(new BasicStroke(lineWidth));
		g.draw(b);
		
		//draw current value on box
		g.setColor(Color.WHITE);
		Font font = new Font(Font.DIALOG, Font.PLAIN, (int)(size/25F));
		TextLayout t = new TextLayout(""+(int)av, font, g.getFontRenderContext());
		t.draw(g, 4, getHeight()/2F + (float)bounds.getHeight()/2F);

		//draw ruler unit
		g.setColor(new Color(1,1,1,0.5F));
		int ty = (int)(getHeight()/2 + rulerLength/2 + bounds.getHeight()*1.7);
		ViewHelper.drawRightAbsAligned(g, hv.getDisplayUnit().toString(), font, pw + 4, ty);
		
		//draw clickable speed type
		ClickableElement ce = elements.get(HorizonControl.SPEED_TYPE);
		g.setColor(ce.getCurrentColor());
		Rectangle2D tt = 
		ViewHelper.drawRightAbsAligned(g, speedType.getName(), 			  font, pw + 4, (int)(ty + bounds.getHeight()*1.4));
		ce.setMask(tt);

		//draw target setpoint
		if(hv.isTargetValueVisible() && !Float.isNaN(hv.getTargetValue()) && isEnabled()) {
			int fl = (int)(ph*1.2);
			float tv = (float)hv.getTargetValueOnDisplayUnit();
			b = createTargetShape(fl, Alignment.LEFT);
			float valueToPixelsRatio = (float)rulerLength/(max-min);
			int tsx = pw - fl/4 + 4;
			int tsy = (int)(rulery+valueToPixelsRatio*MathHelper.clamp(max-tv, 0, max-min))-(int)(fl/2)+1;
			b.translate(tsx, tsy);
			Color cc = elements.get(HorizonControl.SPEED).getCurrentColor();
			g.setColor(cc);
			g.fill(b);
			g.setColor(cc.darker().darker());
			g.setStroke(new BasicStroke(lineWidth));
			g.draw(b);
			g.setColor(cc);
			ViewHelper.drawRightCenterAligned(g, Math.round(tv)+"", font, tsx-fl/4, tsy+fl/2);

			//setup drag elements
			elements.get(HorizonControl.SPEED).setMask(createMask(b, (int)t.getBounds().getWidth(), 0, 0, 0));
			((ArtificialHorizonElement)elements.get(HorizonControl.SPEED)).setPixelToValueRatio((max-min)/(float)rulerLength);
		}
	}
	
	private Shape createMask(Polygon b, int addLeft, int addRight, int addTop, int addBottom) {
		return new Rectangle2D.Double(b.getBounds().x-3-addLeft, b.getBounds().y-3-addTop, b.getBounds().getWidth()+6+addLeft+addRight, b.getBounds().getHeight()+6+addTop+addBottom);
	}

	private void drawHeadingRuler(Graphics2D g, int size) {
		//draw ruler
		float av = heading.getCurrentValueOnDisplayUnit();
		int range = 180;
		int min = (int)(av - range/2);
		int max = (int)(av + range/2);
		int tick = 10;
		int tickLength = size/25;
		int rulerLength = (int)(getWidth()*0.74);
		int rulerx = (int)((getWidth()-rulerLength)/2);
		int rulery = 4;
		
		Rectangle2D bounds = 
		drawRuler(g, rulerx, 						rulery, min, 				min + (range/2), 	tick, tick*3, rulerLength/2, size/25, size/25, Color.WHITE, Alignment.TOP, false, true);
		drawRuler(g, (int)(rulerx + rulerLength/2), rulery, max - (range/2), 	max, 			 	tick, tick*3, rulerLength/2, size/25, size/25, Color.WHITE, Alignment.TOP, false, true);
		
		//draw box for current value
		float lineWidth = (float)size/280F;
		int pw = (int)(size/13);
		int ph = (int)(bounds.getHeight() + tickLength*2 - lineWidth + 2);
		int x = (getWidth()/2) - (pw/2);
		int y = 0;
		Polygon b = createBoxShape(pw, ph, Alignment.BOTTOM);
		b.translate(x, y);
		g.setColor(Color.BLUE);
		g.fill(b);
		g.setColor(Color.BLUE.darker());
		g.setStroke(new BasicStroke(lineWidth));
		g.draw(b);
		b.translate(-x, -y);

		//draw current value on box
		g.setColor(Color.WHITE);
		Font font = new Font(Font.DIALOG, Font.PLAIN, (int)(size/25F));
		TextLayout t = new TextLayout(""+(int)Math.round(Math.toDegrees(MathHelper.normalizeAngle2(heading.getCurrentValue()))), font, g.getFontRenderContext());
		t.draw(g, (float)(getWidth()/2-(t.getBounds().getWidth()/2)), 5 + (int)t.getBounds().getHeight());
		
		//draw target setpoint
		if(heading.isTargetValueVisible() && !Float.isNaN(heading.getTargetValue()) && isEnabled()) {
			int fl = (int)(pw*1.2);
			float tv = (float)Math.toDegrees(MathHelper.normalizeAngle(heading.getTargetValue()));
			b = createTargetShape(fl, Alignment.TOP);
			float valueToPixelsRatio = (float)rulerLength/range;
			float diff = (float)Math.toDegrees(MathHelper.normalizeAngle2(Math.toRadians(tv-min)));
			int tsx = (int)MathHelper.clamp(1 + rulerx + valueToPixelsRatio*diff - fl/2, rulerx, rulerx+rulerLength);
			int tsy = ph - fl/4 + 4;
			b.translate(tsx, tsy);
			Color cc = elements.get(HorizonControl.HEADING).getCurrentColor();
			g.setColor(cc);
			g.fill(b);
			g.setColor(cc.darker().darker());
			g.setStroke(new BasicStroke(lineWidth));
			g.draw(b);
			g.setColor(cc);
			ViewHelper.drawCenterAbsAligned(g, Math.round(Math.toDegrees(MathHelper.normalizeAngle2(Math.toRadians(tv))))+"", font, tsx+fl/2, (int)(tsy*0.9));

			//setup drag elements
			elements.get(HorizonControl.HEADING).setMask(createMask(b, 0, 0, (int)t.getBounds().getHeight(), 0));
			((ArtificialHorizonElement)elements.get(HorizonControl.HEADING)).setPixelToValueRatio((max-min)/(float)rulerLength);
		}
		
	}
	
	private Polygon createTargetShape(int faceLength, Alignment alignment) {
		Polygon p = new Polygon();
		if(alignment.equals(Alignment.RIGHT)) {
			int w = faceLength/4;
			p.addPoint(0, 0);
			p.addPoint(w, 0);
			p.addPoint(w, w);
			p.addPoint(0, w*2);
			p.addPoint(w, w*3);
			p.addPoint(w, w*4);
			p.addPoint(0, w*4);
			
		} else if(alignment.equals(Alignment.LEFT)) {
			int w = faceLength/4;
			p.addPoint(w, 0);
			p.addPoint(w, w*4);
			p.addPoint(0, w*4);
			p.addPoint(0, w*3);
			p.addPoint(w, w*2);
			p.addPoint(0, w);
			p.addPoint(0, 0);

		} else if(alignment.equals(Alignment.TOP)) {
			int w = faceLength/4;
			p.addPoint(0, 0);
			p.addPoint(w, 0);
			p.addPoint(w*2, w);
			p.addPoint(w*3, 0);
			p.addPoint(w*4, 0);
			p.addPoint(w*4, w);
			p.addPoint(0, w);
		}
		return p;
	}

	private int normalize360(int v) {
		return (int)Math.round(Math.toDegrees(MathHelper.normalizeAngle2(Math.toRadians(v))));
	}

	private Rectangle2D drawRuler(Graphics2D g, int posx, int posy, 
										 int minValue, int maxValue, 
										 int showTicksMultipleOf, int showNumbersMultipleOf, 
										 int rulerLength, int tickLength, int fontSize,
										 Color color, Alignment alignment, boolean invertDirection, boolean normalizeScaleTo360) {
		g.setColor(color);
		g.setStroke(new BasicStroke((float)fontSize/21F));
		Font font = new Font(Font.DIALOG, Font.PLAIN, fontSize);
		g.setFont(font);

		//calculations
		Rectangle2D textAreaBounds;
		FontRenderContext frc = g.getFontRenderContext();
		if((""+minValue).length()>(""+maxValue).length()) {
			textAreaBounds = new TextLayout(""+minValue, font, frc).getBounds();
		} else {
			textAreaBounds = new TextLayout(""+maxValue, font, frc).getBounds();
		}

		int allCharsPixelsX = (int)textAreaBounds.getWidth() + 2;
		int charPixelsY = (int)textAreaBounds.getHeight();
		
		int rl = rulerLength;//pixels
		int tl = tickLength;//pixels
		int tlOneNumber = (int)((tl*2)+charPixelsY);
		int tlAllNumber = (int)((tl*2)+allCharsPixelsX);//minimum space for labels
		float valueToPixelsRatio = (float)rulerLength/(maxValue-minValue);
		
		//draw main ruler line
		if(alignment.equals(Alignment.BOTTOM)) {
			posy += tlOneNumber;
			g.translate(posx, posy);
			g.drawLine(1, 1, rl, 1);
		} else if(alignment.equals(Alignment.TOP)) {
			g.translate(posx, posy);
			g.drawLine(1, tlOneNumber, rl, tlOneNumber);
		} else if(alignment.equals(Alignment.LEFT)) {
			g.translate(posx, posy);
			g.drawLine(tlAllNumber, 1, tlAllNumber, rl);
		} else if(alignment.equals(Alignment.RIGHT)) {
			posx -= tlAllNumber;
			g.translate(posx, posy);
			g.drawLine(1, 1, 1, rl);
		}

		//draw ticks/numbers
		for (int i=(minValue-(minValue%showTicksMultipleOf)+showTicksMultipleOf); i<=maxValue;i+=showTicksMultipleOf) {
			int vi = normalizeScaleTo360?normalize360(i):i;
			int d = (int)((i-minValue)*valueToPixelsRatio);//tick distance in pixels
			boolean drawNumber = i%showNumbersMultipleOf==0;
			
			if(invertDirection) {
				d = rl - d;
			}
			
			if(alignment.equals(Alignment.BOTTOM)) {
				g.drawLine(d, 1,
						   d, drawNumber?tl*2:tl);
				if(drawNumber) {
					TextLayout t = new TextLayout(vi+"", font, frc);
					t.draw(g, (float)(d-(t.getBounds().getWidth()/2)), tl*2 + charPixelsY + 2);
				}
			} else if(alignment.equals(Alignment.TOP)) {
				g.drawLine(d, 1+charPixelsY + (drawNumber?2:tl),
						   d, tlOneNumber);
				if(drawNumber) {
					TextLayout t = new TextLayout(vi+"", font, frc);
					t.draw(g, (float)(d-(t.getBounds().getWidth()/2)), 1 + charPixelsY);
				}
			} else if(alignment.equals(Alignment.LEFT)) {
				g.drawLine(1+allCharsPixelsX + (drawNumber?2:tl), rl-d,
						   tlAllNumber,  		  				  rl-d);
				if(drawNumber) {
					g.drawString(vi+"", 1, rl-d-(charPixelsY/2)+charPixelsY);
				}
			} else if(alignment.equals(Alignment.RIGHT)) {
				g.drawLine(1, 						rl-d,
						   drawNumber?tl*2-2:tl, 	rl-d);
				if(drawNumber) {
					ViewHelper.drawRightAbsAligned(g, vi+"", font, tlAllNumber, rl-d-(charPixelsY/2)+charPixelsY);
				}
			}
		}
		
		//reset to initial translation
		g.translate(-posx, -posy);
		
		return textAreaBounds;
	}

	private void drawRollScale(Graphics2D g, int size) {
		//draw background
//		long s = System.currentTimeMillis();
//		g.setPaint(new GradientPaint(0F, 0F, new Color(10,10,10), size, size, new Color(50,50,50)));
//		g.fillRect(-(getWidth()-size)/2, -(getHeight()-size)/2, getWidth(), getHeight());
		
//		g.setPaint(new GradientPaint(0F, 0F, new Color(160,160,160), size, size, new Color(30,30,30)));
//		g.setStroke(new BasicStroke(size/80));
//		g.drawOval(size/20, size/21, size-(size/10), size-(size/10));
//		System.out.println("BG: "+(System.currentTimeMillis()-s));
		
		//draw roll pointer
//		s = System.currentTimeMillis();
		g.rotate(-roll.getCurrentValue(), size/2, size/2);
		if(roll.isCurrentValueOutsideMinMax()) {
			g.setColor(new Color(235,0,0));
		} else {
			g.setColor(Color.ORANGE);
		}
		g.setStroke(new BasicStroke(size/100, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine((int)(size/2.07), (int)((float)size/3.8), size/2, 		  (int)((float)size/4.2));
		g.drawLine(size/2, 			 (int)((float)size/4.2), 	(int)(size/1.93), (int)((float)size/3.8));
		g.rotate(roll.getCurrentValue(), size/2, size/2);
		
		//draw roll scale
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(size/150, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
		drawRollScaleLine(g, size, size/40, null, Math.abs(Math.toDegrees(roll.getTargetValue()))<4);
		g.rotate(-Math.toRadians(60), size/2, size/2);
		for(int i=60; i>=-60; i-=10) {
			setColorByProximity(i, (float)Math.toDegrees(roll.getCurrentValue()), 50, g);
			drawRollScaleLine(g, size, size/70, null, Math.abs(Math.toDegrees(roll.getTargetValue())-i)<4);
			g.rotate(Math.toRadians(10), size/2, size/2);
		}
		g.rotate(Math.toRadians(-70), size/2, size/2);

		//draw uav shape
		if(pitch.isCurrentValueOutsideMinMax()) {
			g.setColor(new Color(235,0,0));
		} else {
			g.setColor(Color.ORANGE);
		}
		g.fill(getUavShape(size));
		//		System.out.println("ROLL: "+(System.currentTimeMillis()-s));
	}

	private void drawRollScaleLine(Graphics2D g, int refSize, int height, Integer rollValue, boolean isTarget) {
		if(isTarget) {
			g.setColor(targetColor);
		}
		g.drawLine(refSize/2, (int)((float)refSize/5F), refSize/2, (int)((float)refSize/5F) + height);
//		if(rollValue!=null) {
//			String signal = "";
//			if(rollValue<0) {
//				signal = "-";
//			} else if(rollValue>0) {
//				signal = " ";
//			}
//			String str = rollValue + "";
//			if(rollValue<0) str = str.substring(1);
//			g.setFont(new Font(Font.DIALOG, Font.PLAIN, refSize/32));
//			g.drawString(signal, (int)(refSize/2.19F), (int)(refSize/9.5));
//			g.setFont(new Font(Font.DIALOG, Font.PLAIN, refSize/21));
//			g.drawString(str, (int)(refSize/2.11F + (rollValue==0?5:0)), refSize/9);
//		}
	}

	private Polygon createBoxShape(int width, int height, Alignment aligment) {
		Polygon p = new Polygon();
		if(aligment.equals(Alignment.RIGHT)) {
			p.addPoint(0, height);
			p.addPoint(width-(height/2), height);
			p.addPoint(width, height/2);
			p.addPoint(width-(height/2), 0);
			p.addPoint(0, 0);
		} else if(aligment.equals(Alignment.LEFT)) {
			p.addPoint(height/2, height);
			p.addPoint(width, height);
			p.addPoint(width, 0);
			p.addPoint(height/2, 0);
			p.addPoint(0, height/2);
		} else if(aligment.equals(Alignment.TOP)) {
			p.addPoint(0, height);
			p.addPoint(width, height);
			p.addPoint(width, height - (width/2));
			p.addPoint(width/2, 0);
			p.addPoint(0, height - (width/2));
		} else if(aligment.equals(Alignment.BOTTOM)) {
			p.addPoint(0, 0);
			p.addPoint(width, 0);
			p.addPoint(width, height - (width/2));
			p.addPoint(width/2, height);
			p.addPoint(0, height - (width/2));
		}
		return p;
	}
	
	private Shape getUavShape(int size) {
		Polygon p = new Polygon();
		int w = size/3;
		
		p.addPoint(-w/2, -1);
		p.addPoint(-w/9, -w/50);
		p.addPoint(0, -w/12);
		p.addPoint(w/9, -w/50);
		p.addPoint(w/2, -1);
		
		p.addPoint(w/2, w/150);
		p.addPoint(w/9, w/50);
		p.addPoint(0, w/27);
		p.addPoint(-w/9, w/50);
		p.addPoint(-w/2, w/150);
	
		p.translate((size/2)+(size/200), (size/2));
		return p;
	}

	private BufferedImage applyTopMask(BufferedImage image, int size) {
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		
		//Clear the image so all pixels have zero alpha
//		g2.setComposite(AlphaComposite.Clear);
//		g2.fillRect(0, 0, size, size);

		// Render our clip shape into the image.  Note that we enable
		// antialiasing to achieve the soft clipping effect.  Try
		// commenting out the line that enables antialiasing, and
		// you will see that you end up with the usual hard clipping.
		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fillOval(size/20, size/20, size-(size/10), size-(size/10));

		// Here's the trick... We use SrcAtop, which effectively uses the
		// alpha value as a coverage value for each pixel stored in the
		// destination.  For the areas outside our clip shape, the destination
		// alpha will be zero, so nothing is rendered in those areas.  For
		// the areas inside our clip shape, the destination alpha will be fully
		// opaque, so the full color is rendered.  At the edges, the original
		// antialiasing is carried over to give us the desired soft clipping
		// effect.
		g2.setComposite(AlphaComposite.SrcAtop);
		g2.drawImage(image, 0, 0, dummyObserver);
		return img;
	}

	private BufferedImage createHorizon(int size) {
		//force bufferedImage recreation
		if(lastSize!=(getWidth()+getHeight())) {
			img = null;
			lastSize = getWidth()+getHeight();
		}

		float pitchValue = pitch.getCurrentValueOnDisplayUnit();
		float targetPitchValue = pitch.getTargetValueOnDisplayUnit();
		float rollValue = roll.getCurrentValueOnDisplayUnit();
		float headingValue = heading.getCurrentValueOnDisplayUnit();
		
		//calculate the minimum size of the square that compounds the horizon. remember this square must fill the screen after rotated
		int minSize = (int)(Math.sqrt(Math.pow(getWidth(),2F) + Math.pow(getHeight(),2F))) * 2;
		int x0 = (getWidth()-minSize)/2;
		int y0 = (getHeight()-minSize)/2;

//		long s = System.currentTimeMillis();
		if(img==null) {
			img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);			
		}
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		System.out.println("CB: " + (System.currentTimeMillis()-s));
		
		//pitch
		//translate according to pitch (90 degrees is related to the entire screen height)
//		s = System.currentTimeMillis();
		g.translate(0, pitchToPixels(pitchValue, size));
//		System.out.println("PI: " + (System.currentTimeMillis()-s));
		//TODO add support for angles >90
		
		//roll
//		s = System.currentTimeMillis();
		g.rotate(-roll.getCurrentValue(), getWidth()/2, getHeight()/2 - pitchToPixels(pitchValue, size));
//		System.out.println("RT: " + (System.currentTimeMillis()-s));
		
		//draw sky
//		s = System.currentTimeMillis();
		g.setPaint(new GradientPaint(minSize/2, y0+(minSize/4), new Color(169,223,255), minSize/2, y0+((int)(minSize/2)), new Color(2,125,200)));
		g.fillRect(x0, y0, minSize, minSize/2);
//		System.out.println("KY: " + (System.currentTimeMillis()-s));

		//draw ground
//		s = System.currentTimeMillis();
		g.setPaint(new GradientPaint(minSize/2, y0+(minSize/2), new Color(107,69,49), minSize/2, y0+((int)(minSize/1.6)), new Color(51,37,27)));
		g.fillRect(x0, y0+(minSize/2), minSize, minSize/2);
//		System.out.println("GR: " + (System.currentTimeMillis()-s));

		//draw line separating sky/earth
		g.setStroke(new BasicStroke(size/200));
		g.setColor(Color.WHITE);
		g.drawLine(x0, y0+(minSize/2), x0+minSize, y0+(minSize/2));

		//draw heading scale
//		int ba = (int)MathHelper.normalizeAngle2(heading-50);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, size/28));
		g.setStroke(new BasicStroke(size/200));
		int bp = -headingToPixels(headingValue+5, size) + getWidth()/2;
		g.setColor(new Color(1,1,1,0.5F));
		for(int i=0; i<460;i+=45) {
			String cs = "";
			if(i%360==0) cs = " N";
			if(i%360==90) cs = " E";
			if(i%360==180) cs = " S";
			if(i%360==270) cs = " W";
			int xh = (size/20) + headingToPixels(i, size) + bp;
			g.drawLine(xh, (getHeight()/2)+(size/160), xh, (getHeight()/2)-(size/160));
			g.drawString((i==0||i==360?"  ":(i<100?" ":""))+(i==360?0:i)+cs, xh-(size/33), (getHeight()/2)-(size/80));
		}
		
		//draw target heading
		if(!Float.isNaN(commandedHeading)) {
			int xh = (size/20) + headingToPixels(commandedHeading, size) + bp;
			Polygon p = new Polygon();
			p.addPoint(0, -size/18);
			p.addPoint(size/36, 0);
			p.addPoint(-size/36, 0);
			p.translate(xh, (int)(getHeight()/2));
			g.setColor(Color.GREEN);
			g.fill(p);
			g.drawLine(xh, (size/2)+(size/160), xh, (size/2)-(size/100));
		}
		
		g.setStroke(new BasicStroke(size/200));
		
		//draw pitch scale
		//sky part (from bottom up)
//		s = System.currentTimeMillis();
		setColorByProximity(5, pitchValue, 27, g);
		drawPitchScaleLine(g, minSize, size, size/40, 5, false, targetPitchValue);
		drawPitchScaleLine(g, minSize, size, size/25, 10, true, targetPitchValue);
		
		setColorByProximity(15, pitchValue, 27, g);
		drawPitchScaleLine(g, minSize, size, size/40, 15, false, targetPitchValue);
		drawPitchScaleLine(g, minSize, size, size/15, 20, true, targetPitchValue);
		
		setColorByProximity(25, pitchValue, 27, g);
		drawPitchScaleLine(g, minSize, size, size/40, 25, false, targetPitchValue);
		drawPitchScaleLine(g, minSize, size, size/10, 30, true, targetPitchValue);
		
		//ground part (from top down)
		setColorByProximity(-5, pitchValue, 27, g);
		drawPitchScaleLine(g, minSize, size, size/40, -5, false, targetPitchValue);
		drawPitchScaleLine(g, minSize, size, size/25, -10, true, targetPitchValue);
		
		setColorByProximity(-15, pitchValue, 27, g);
		drawPitchScaleLine(g, minSize, size, size/40, -15, false, targetPitchValue);
		drawPitchScaleLine(g, minSize, size, size/15, -20, true, targetPitchValue);
		
		setColorByProximity(-25, pitchValue, 27, g);
		drawPitchScaleLine(g, minSize, size, size/40, -25, false, targetPitchValue);
		drawPitchScaleLine(g, minSize, size, size/10, -30, true, targetPitchValue);
//		System.out.println("PS: " + (System.currentTimeMillis()-s));
		
		return img;
	}

	public void setDrawCirclePanel(boolean drawCirclePanel) {
		this.drawCirclePanel = drawCirclePanel;
	}
	public boolean isDrawCirclePanel() {
		return drawCirclePanel;
	}
	
	private void setColorByProximity(int anyValue, float actualValue, int noProximityReference, Graphics2D g) {
		float opacity = 260 * (1-(Math.abs(actualValue-anyValue)/noProximityReference));
		opacity = MathHelper.clamp(opacity, 0, 255);
		g.setColor(new Color(255,255,255,(int)opacity));
	}

	private void drawPitchScaleLine(Graphics2D g, int minSize, int refSize, int width, int pitch, boolean drawNumbers, float targetPitch) {
		boolean isTarget = Math.abs(pitch-targetPitch)<2;
		int pp = pitchToPixels(pitch, refSize);
		if(isTarget) {
			g.setColor(targetColor);
		}
		int x1 = (getWidth()-width)/2;
		int x2 = (getWidth()+width)/2;
		g.drawLine(x1, getHeight()/2-pp, x2, getHeight()/2-pp);
		if(drawNumbers) {
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, refSize/25));
			g.drawString(pitch+"", x1-(int)(refSize/11), getHeight()/2 - pp + refSize/73);

			g.setFont(new Font(Font.DIALOG, Font.PLAIN, refSize/25));
			g.drawString(pitch+"", x2+(refSize/30), getHeight()/2 - pp + refSize/73);
		}
	}

	private int pitchToPixels(float pitch, int size) {
		return (int)(((float)size/110F)*pitch);
	}

	private int headingToPixels(float heading, int size) {
 		return (int)(((float)(size-(size/20))*((float)getWidth()/30000F))*heading);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 300);
		this.setLayout(new GridBagLayout());
	}

	public static void main(String[] args) throws Exception {
		JFrame f = new JFrame();
		final ArtificialHorizon h = new ArtificialHorizon();
		f.add(h);
		f.setSize(h.getSize());
		f.setVisible(true);
		ThreadWorker t = new ThreadWorker(20) {
			@Override
			public void onActivate() throws Exception {
				setName("ArtificialHorizon.t");
			}
			@Override
			public void step() {
				h.updateUI();
			}
		};
		t.activate();
	}

	public void addArtificialHorizonListener(ArtificialHorizonListener listener) {
		listeners.add(listener);
	}
	public void removeArtificialHorizonListener(ArtificialHorizonListener listener) {
		listeners.remove(listener);
	}
	
	public ArtificialHorizonValue getRoll() {
		return roll;
	}
	public ArtificialHorizonValue getPitch() {
		return pitch;
	}
	public ArtificialHorizonValue getHeading() {
		return heading;
	}
	public ArtificialHorizonValue getAltitude(AltitudeType type) {
		return altitudes.get(type);
	}
	public ArtificialHorizonValue getSpeed(SpeedType type) {
		return speeds.get(type);
	}

	public AltitudeType getSelectedAltitudeType() {
		return altitudeType;
	}
	
	public SpeedType getSelectedSpeedType() {
		return speedType;
	}
	
	public void setSelectedSpeedType(SpeedType speedType) {
		this.speedType = speedType;
	}
	
	public void setSelectedAltitudeType(AltitudeType altitudeType) {
		this.altitudeType = altitudeType;
	}
	
	public void setCommandedHeading(float commandedHeading) {
		this.commandedHeading = commandedHeading;
	}
	
	public ClickableElement getElement(HorizonControl horizonControl) {
		return elements.get(horizonControl);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
