package br.skylight.cucs.widgets.artificialhorizon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public abstract class ClickableElement extends MouseAdapter {
	
	private Color colorNormal = Color.ORANGE;
	private Color colorOver = Color.ORANGE.brighter();
	private Color colorDrag = Color.ORANGE.darker();

	protected enum State {NORMAL, OVER, DRAG}
	protected State currentState = State.NORMAL;
	
	protected Shape mask = null;
	protected Point2D clickPoint = new Point2D.Float();
	protected Component component;

	@Override
	public void mouseMoved(MouseEvent e) {
		if(mask!=null) {
			if(!mask.contains(e.getX(), e.getY())) {
				changeState(State.NORMAL);
			} else {
				if(currentState.equals(State.NORMAL)) {
					changeState(State.OVER);
				}
			}
		}
	}
	
	protected void changeState(State state) {
		currentState = state;
		updateCursor();
		component.repaint();
	}

	protected void updateCursor() {
		if(currentState.equals(State.NORMAL)) {
			component.setCursor(Cursor.getDefaultCursor());
		} else {
			component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(mask!=null) {
			clickPoint.setLocation(e.getX(), e.getY());
			if(mask.contains(clickPoint)) {
				changeState(State.DRAG);
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		clickPoint.setLocation(e.getX(), e.getY());
		if(mask!=null) {
			if(currentState.equals(State.DRAG)) {
				onDragFinished();
			}
			if(!mask.contains(clickPoint)) {
				changeState(State.NORMAL);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(!currentState.equals(State.NORMAL)) {
			onElementClicked(e);
		}
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		currentState = State.NORMAL;
	}

	public Color getCurrentColor() {
		if(currentState.equals(State.NORMAL)) {
			return colorNormal;
		} else if(currentState.equals(State.OVER)) {
			return colorOver;
		} else if(currentState.equals(State.DRAG)) {
			return colorDrag;
		} else {
			return Color.BLACK;
		}
	}
	
	public void setColorDrag(Color colorDrag) {
		this.colorDrag = colorDrag;
	}
	
	public void setColorNormal(Color colorNormal) {
		this.colorNormal = colorNormal;
	}
	
	public void setColorOver(Color colorOver) {
		this.colorOver = colorOver;
	}
	
	public void setColors(Color colorNormal, Color colorOver, Color colorDrag) {
		this.colorNormal = colorNormal;
		this.colorOver = colorOver;
		this.colorDrag = colorDrag;
	}

	public void install(Component component) {
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
		this.component = component;
	}
	
	public void setMask(Shape mask) {
		this.mask = mask;
	}
	
	public State getCurrentState() {
		return currentState;
	}
	
	public void setupToDefaultColors() {
		colorNormal = Color.ORANGE;
		colorOver = Color.ORANGE.brighter();
		colorDrag = Color.ORANGE.darker();
	}
	
	public abstract void onElementClicked(MouseEvent e);
	public abstract void onDragFinished();
	
}
