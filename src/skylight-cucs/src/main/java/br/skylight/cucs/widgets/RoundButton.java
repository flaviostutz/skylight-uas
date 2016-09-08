package br.skylight.cucs.widgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import br.skylight.commons.ViewHelper;

public class RoundButton extends FeedbackButton {

	private static final long serialVersionUID = 1L;

	public enum RaiseType {
		RAISED, LOWERED
	}

	private Color colorSelected = new Color(255,220,61);  //  @jve:decl-index=0:
	private Color colorUnselected = Color.LIGHT_GRAY;  //  @jve:decl-index=0:

	private int roundness = 25;
	private int raiseLevel = 3;
	private boolean lowerButtonOnSelect = true;

	// calculated
	private Color border1Color;
	private Color border2Color;
	private Color bottomBackgroundColor;
	private Color upperBackground1Color;
	private Color upperBackground2Color;
	private RoundRectangle2D shape;
	
	private RaiseType raiseType = RaiseType.RAISED;  //  @jve:decl-index=0:

	public RoundButton() {
		setContentAreaFilled(false);
		setBackground(new Color(151, 182, 223));
	}

	@Override
	public void setBackground(Color bg) {
		this.colorSelected = bg;
		this.colorUnselected = bg;
		super.setBackground(bg);
	}
	
	private void calculateColors() {
		Color c = getCurrentColor();
		border1Color = Color.LIGHT_GRAY;
		border2Color = ViewHelper.getHSBColorVariation(c, 0, -0.67F, 0.26F);
		bottomBackgroundColor = c;
		upperBackground1Color = ViewHelper.getHSBColorVariation(c, 0, -0.19F, 0);
		upperBackground2Color = ViewHelper.getHSBColorVariation(c, 0, -0.09F, 0);
	}

	private RoundRectangle2D getShape() {
		if (shape == null || !shape.getBounds().equals(getBounds())) {
			shape = new RoundRectangle2D.Float(0, 0, getSize().width - raiseLevel, getSize().height - raiseLevel, roundness, roundness);
		}
		return shape;
	}

	// Paint the round background and label.
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		getShape();
		calculateColors();

		// paint background
		g2.setColor(getParent().getBackground());
		g2.fillRect(0, 0, (int) getSize().getWidth(), (int) getSize().getHeight());

		int d = 0;
		if (getRaiseType().equals(RaiseType.LOWERED)) {
			d = raiseLevel - 2;
		}

		// draw raise shadow
		if (raiseLevel > 2) {
			g2.setColor(Color.GRAY);
			g2.fillRoundRect((int) shape.getX() + raiseLevel, (int) shape.getY() + raiseLevel, (int) shape.getWidth(), (int) shape.getHeight(), roundness + 2, roundness + 2);
		}

		// draw upper background
		if (getModel().isArmed()) {
			g2.setPaint(new GradientPaint(getWidth() / 2, 0, ViewHelper.getBrighter(upperBackground1Color, -0.05F), getWidth() / 2, getHeight() / 2, ViewHelper.getBrighter(upperBackground1Color, -0.05F)));
		} else {
			g2.setPaint(new GradientPaint(getWidth() / 2, 0, upperBackground1Color, getWidth() / 2, getHeight() / 2, upperBackground2Color));
		}
		g2.fillRoundRect((int) shape.getX() + d, (int) shape.getY() + d, (int) shape.getWidth(), (int) shape.getHeight(), roundness, roundness);

		// draw bottom background
		if (getModel().isArmed()) {
			g2.setColor(ViewHelper.getBrighter(bottomBackgroundColor, -0.05F));
		} else {
			g2.setColor(bottomBackgroundColor);
		}
		g2.setClip(0, getHeight() / 2, getWidth(), getHeight());
		g2.fillRoundRect((int) shape.getX() + d, (int) shape.getY() + d, (int) shape.getWidth(), (int) shape.getHeight(), roundness, roundness);
		g2.setClip(null);

		// draw 3d shadow
		g2.setPaint(new GradientPaint(getWidth() / 2, getHeight(), new Color(55, 55, 55, 70), getWidth() / 2, getHeight() - 10, new Color(55, 55, 55, 0)));
		g2.fillRoundRect((int) shape.getX() + d, (int) shape.getY() + d, (int) shape.getWidth(), (int) shape.getHeight(), roundness, roundness);
		g2.setPaint(new GradientPaint(getWidth(), getHeight() / 2, new Color(55, 55, 55, 70), getWidth() - 10, getHeight() / 2, new Color(55, 55, 55, 0)));
		g2.fillRoundRect((int) shape.getX() + d, (int) shape.getY() + d, (int) shape.getWidth(), (int) shape.getHeight(), roundness, roundness);

		// This call will paint the label and the focus rectangle
		g.translate(d - 1, d - 1);
		super.paintComponent(g);
		g.translate(-d + 1, -d + 1);
	}

	// Paint the border of the button using a simple stroke.
	@Override
	protected void paintBorder(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		getShape();
		calculateColors();

		int d = 0;
		if (getRaiseType().equals(RaiseType.LOWERED)) {
			d = raiseLevel - 2;
		}

		// inner border
		g2.setStroke(new BasicStroke(1.5F));
		g2.setColor(border2Color);
		g2.drawRoundRect((int) shape.getX() + 1 + d, (int) shape.getY() + 1 + d, (int) shape.getWidth() - 2, (int) shape.getHeight() - 2, roundness - 2, roundness - 2);

		// outer border
		g2.setStroke(new BasicStroke(1));
		g2.setColor(border1Color);
		g2.drawRoundRect((int) shape.getX() + d, (int) shape.getY() + d, (int) shape.getWidth(), (int) shape.getHeight(), roundness, roundness);

	}

	@Override
	public boolean contains(int x, int y) {
		return getShape().contains(x, y);
	}

	public int getRoundness() {
		return roundness;
	}

	public void setRoundness(int roundness) {
		this.roundness = roundness;
	}

	public void setRaiseType(RaiseType raiseType) {
		this.raiseType = raiseType;
	}

	public void setRaiseLevel(int raiseLevel) {
		this.raiseLevel = Math.max(2, raiseLevel);
	}

	public int getRaiseLevel() {
		return raiseLevel;
	}
	
	public RaiseType getRaiseType() {
		if(lowerButtonOnSelect) {
			if(isSelected()) {
				return RaiseType.LOWERED;
			} else {
				return RaiseType.RAISED;
			}
		} else {
			return raiseType;
		}
	}
	
	public void setLowerButtonOnSelect(boolean lowerButtonOnSelect) {
		this.lowerButtonOnSelect = lowerButtonOnSelect;
	}
	
	public boolean isLowerButtonOnSelect() {
		return lowerButtonOnSelect;
	}

	protected Color getCurrentColor() {
		if(isSelected()) {
			return colorSelected;
		} else {
			return colorUnselected;
		}
	}
	
	public Color getColorUnselected() {
		return colorUnselected;
	}
	public void setColorUnselected(Color colorUnselected) {
		this.colorUnselected = colorUnselected;
	}
	public Color getColorSelected() {
		return colorSelected;
	}
	public void setColorSelected(Color colorSelected) {
		this.colorSelected = colorSelected;
	}

}
