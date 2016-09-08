package br.skylight.cucs.widgets.artificialhorizon;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import br.skylight.cucs.widgets.artificialhorizon.ArtificialHorizon.Alignment;

public abstract class ArtificialHorizonElement extends ClickableElement {

	private float pixelToValueRatio = 0;
	private float dragStartValue;
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(currentState.equals(State.DRAG)) {
			int draggedPixelsOnRuler = 0;
			if(getRulerAlignment().equals(Alignment.TOP)) {
				draggedPixelsOnRuler = (int)(clickPoint.getY()-e.getY());
			} else if(getRulerAlignment().equals(Alignment.BOTTOM)) {
				draggedPixelsOnRuler = (int)(e.getY()-clickPoint.getY());
			} else if(getRulerAlignment().equals(Alignment.RIGHT)) {
				draggedPixelsOnRuler = (int)(e.getX()-clickPoint.getX());
			} else if(getRulerAlignment().equals(Alignment.LEFT)) {
				draggedPixelsOnRuler = (int)(clickPoint.getX()-e.getX());
			}
			
			onValueChanged(dragStartValue + pixelToValueRatio*(float)draggedPixelsOnRuler);
			component.repaint();
		}
	}

//	@Override
//	public void mouseWheelMoved(MouseWheelEvent e) {
//		System.out.println(currentState + " " + e);
//		if(currentState.equals(State.NORMAL)) {
//			float v = e.getWheelRotation();
//			if(getRulerAlignment().equals(Alignment.LEFT) || getRulerAlignment().equals(Alignment.BOTTOM)) {
//				v = -v;
//			}
//			onValueChanged(dragStartValue + pixelToValueRatio*v);
//			component.repaint();
//		}
//	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		if(currentState.equals(State.DRAG)) {
			dragStartValue = getCurrentValue();
		}
	}
	
	public void setPixelToValueRatio(float pixelToValueRatio) {
		this.pixelToValueRatio = pixelToValueRatio;
	}

	@Override
	protected void updateCursor() {
		if(currentState.equals(State.NORMAL)) {
			component.setCursor(Cursor.getDefaultCursor());
		} else {
			if(getRulerAlignment().equals(Alignment.TOP) || getRulerAlignment().equals(Alignment.BOTTOM)) {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
			} else if(getRulerAlignment().equals(Alignment.LEFT) || getRulerAlignment().equals(Alignment.RIGHT)) {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			} else {
				component.setCursor(Cursor.getDefaultCursor());
			}
		}
	}
	
	public abstract void onValueChanged(float value);
	public abstract float getCurrentValue();
	public abstract Alignment getRulerAlignment();

}
